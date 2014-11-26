/*
 * Copyright (C) 2005 - 2012 ScalAgent Distributed Technologies
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA.
 *
 * Initial developer(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.agent;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class implements a ServerDesc hashtable, which uses sid as keys.
 */
public class ServersHT {
  /** The hash table data. */
  private transient ServerDescEntry table[];
  /** The total number of entries in the hash table. */
  private transient int count;
							 
  /** The default initial capacity for the hashtable: 13. */
  private static final int initialCapacity = 13;
  /** The default load factor for the hashtable: 0.75f. */
  private static final float loadFactor = 0.75f;
  /** The table is rehashed each time its size exceeds this threshold. */
  private int threshold;

  /**
   * Constructs a new, empty hashtable with the default initial 
   * capacity and load factor.
   */
  public ServersHT() {
    table = new ServerDescEntry[initialCapacity];
    threshold = (int)(initialCapacity * loadFactor);
  }

  /**
   * Returns the number of entries in this hashtable.
   *
   * @return  the number of entries in this hashtable.
   */
  public synchronized int size() {
    return count;
  }

  /**
   * Returns an enumeration of the keys (server id.) in this hashtable.
   *
   * @return  an enumeration of the keys in this hashtable.
   * @see     Enumeration
   */
  public synchronized Enumeration<Short> keys() {
    return new Enumerator(KEYS);
  }

  /**
   * Returns an enumeration of the server descriptors in this hashtable.
   * Use the Enumeration methods on the returned object to fetch the elements
   * sequentially.
   *
   * @return  an enumeration of the values in this hashtable.
   * @see     java.util.Enumeration
   */
  public synchronized Enumeration<ServerDesc> elements() {
    return new Enumerator(VALUES);
  }

  /**
   * Returns the descriptor of the corresponding server.
   *
   * @param   sid   The server unique identification.
   * @return  the descriptor of the corresponding server.
   */
  public synchronized ServerDesc get(short sid) {
    ServerDescEntry tab[] = table;
    int index = (sid & 0x7FFF) % tab.length;
    for (ServerDescEntry e = tab[index] ; e != null ; e = e.next) {
      if (e.desc.sid == sid) return e.desc;
    }
    return null;
  }

  /**
   * Increases the capacity of and internally reorganizes this 
   * hashtable, in order to accommodate and access its entries more 
   * efficiently.  This method is called automatically when the 
   * number of keys in the hashtable exceeds this hashtable's capacity 
   * and load factor. 
   */
  protected void rehash() {
    int oldCapacity = table.length;
    ServerDescEntry oldMap[] = table;

    int newCapacity = oldCapacity * 2 + 1;
    ServerDescEntry newMap[] = new ServerDescEntry[newCapacity];

    threshold = (int)(newCapacity * loadFactor);
    table = newMap;

    for (int i = oldCapacity ; i-- > 0 ;) {
      for (ServerDescEntry old = oldMap[i] ; old != null ; ) {
        ServerDescEntry e = old;
        old = old.next;

        int index = (e.desc.sid & 0x7FFFFFFF) % newCapacity;
        e.next = newMap[index];
        newMap[index] = e;
      }
    }
  }

  /**
   * Maps the specified <code>desc</code>  in this hashtable.
   * The descriptor can be retrieved by calling the <code>get</code>
   * method with a key that is equal to the server id. 
   *
   * @param      desc   the descriptor.
   * @return     the previous value of the descriptor, or <code>null</code>
   *             if it did not have one.
   * @exception  NullPointerException  if the descriptor is <code>null</code>.
   */
  public synchronized ServerDesc put(ServerDesc desc) {
    // Make sure the value is not null
    if (desc == null) throw new NullPointerException();

    // Makes sure the key is not already in the hashtable.
    ServerDescEntry tab[] = table;
    int index = (desc.sid & 0x7FFF) % tab.length;
    for (ServerDescEntry e = tab[index] ; e != null ; e = e.next) {
      if (e.desc.sid == desc.sid) {
        ServerDesc old = e.desc;
        e.desc = desc;
        return old;
      }
    }

    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();

      tab = table;
      index = (desc.sid & 0x7FFF) % tab.length;
    } 

    // Creates the new entry.
    ServerDescEntry e = new ServerDescEntry(desc, tab[index]);
    tab[index] = e;
    count++;
    return null;
  }

  /**
   * Removes the descriptor from this hashtable.
   * This method does nothing if the key is not in the hashtable.
   *
   * @param   sid   the id of server that needs to be removed.
   * @return  the descriptor of the server or <code>null</code> if
   *          it is not defined.
   */
  public synchronized ServerDesc remove(short sid) {
    ServerDescEntry tab[] = table;
    int index = (sid & 0x7FFF) % tab.length;
    for (ServerDescEntry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
      if (e.desc.sid == sid) {
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        ServerDesc oldDesc = e.desc;
        e.desc = null;
        return oldDesc;
      }
    }
    return null;
  }

  /**
   * Clears this hashtable so that it contains no descriptors. 
   */
  public synchronized void clear() {
    ServerDescEntry tab[] = table;
    for (int index = tab.length; --index >= 0; )
      tab[index] = null;
    count = 0;
  }

  /**
   * Returns a string representation of this <tt>Hashtable</tt> object.
   *
   * @return  a string representation of this hashtable.
   */
  public synchronized String toString() {
//    int max = size() - 1;
    StringBuffer buf = new StringBuffer();

    buf.append("(").append(super.toString());
// 	for (int i = 0; i <= max; i++) {
// 	    Map.Entry e = (Map.Entry) (it.next());
//             Object key = e.getKey();
//             Object value = e.getValue();
//             buf.append((key   == this ? "(this Map)" : key) + "=" + 
//                        (value == this ? "(this Map)" : value));

// 	    if (i < max)
// 		buf.append(", ");
// 	}
    buf.append(")");
    return buf.toString();
  }

  /**
   * Hashtable collision list.
   */
  private static final class ServerDescEntry {
    ServerDesc desc;
    ServerDescEntry next;

    protected ServerDescEntry(ServerDesc desc, ServerDescEntry next) {
      this.desc = desc;
      this.next = next;
    }

    public String toString() {
      return desc.toString();
    }
  }

  // Types of Enumerations/Iterations
  private static final int KEYS = 0;
  private static final int VALUES = 1;

  /**
   * A hashtable enumerator class.  This class implements both the
   * Enumeration and Iterator interfaces, but individual instances
   * can be created with the Iterator methods disabled.  This is necessary
   * to avoid unintentionally increasing the capabilities granted a user
   * by passing an Enumeration.
   */
  private class Enumerator implements Enumeration {
    ServerDescEntry[] table = ServersHT.this.table;
    int index = table.length;
    ServerDescEntry entry = null;
    int type;

    Enumerator(int type) {
      this.type = type;
    }

    public boolean hasMoreElements() {
      ServerDescEntry e = entry;
      int i = index;
      ServerDescEntry t[] = table;
      /* Use locals for faster loop iteration */
      while (e == null && i > 0) { 
        e = t[--i];
      }
      entry = e;
      index = i;
      return e != null;
    }

    public Object nextElement() {
      ServerDescEntry et = entry;
      int i = index;
      ServerDescEntry t[] = table;
      /* Use locals for faster loop iteration */
      while (et == null && i > 0) { 
        et = t[--i];
      }
      entry = et;
      index = i;
      if (et != null) {
        ServerDescEntry e = entry;
        entry = e.next;
        return (type == KEYS)?((Object) new Short(e.desc.sid)):((Object) e.desc);
      }
      throw new NoSuchElementException("ServersHT Enumerator");
    }
  }

}
