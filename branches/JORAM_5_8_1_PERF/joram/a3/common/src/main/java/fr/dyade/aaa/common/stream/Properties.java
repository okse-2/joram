/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2009 ScalAgent Distributed Technologies
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
 * Contributor(s): 
 */
package fr.dyade.aaa.common.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.Encoder;


/**
 *  This class implements a set of properties, which maps keys to values.
 * Only string object can be used as a key, all primitives type can be used
 * as a value. <p>
 */
public class Properties implements Serializable, Cloneable, Encodable {
  /** The total number of entries in the hash table. */
  private transient int count;
  /** The hash table data. */
  private transient Entry table[];

  /**
   * The table is rehashed when its size exceeds this threshold.  (The
   * value of this field is (int)(capacity * loadFactor).)
   */
  private transient int threshold;
							 
  /** The load factor for the hashtable. */
  private transient float loadFactor;

  /**
   * The number of times this Properties has been structurally modified
   * Structural modifications are those that change the number of entries in
   * the Properties or otherwise modify its internal structure (e.g.,
   * rehash).  This field is used to make iterators on Collection-views of
   * the Properties fail-fast.  (See ConcurrentModificationException).
   */
  private transient int modCount = 0;

  /**
   * Constructs a new, empty hashtable with the specified initial 
   * capacity and the specified load factor.
   *
   * @param      initialCapacity   the initial capacity of the hashtable.
   * @param      loadFactor        the load factor of the hashtable.
   * @exception  IllegalArgumentException  if the initial capacity is less
   *             than zero, or if the load factor is nonpositive.
   */
  public Properties(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
      throw new IllegalArgumentException("Illegal Capacity: "+
                                         initialCapacity);
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
      throw new IllegalArgumentException("Illegal Load: "+loadFactor);

    if (initialCapacity==0)
      initialCapacity = 1;
    this.loadFactor = loadFactor;
    table = new Entry[initialCapacity];
    threshold = (int)(initialCapacity * loadFactor);
  }

  /**
   * Constructs a new, empty hashtable with the specified initial capacity
   * and default load factor, which is <tt>0.75</tt>.
   *
   * @param     initialCapacity   the initial capacity of the hashtable.
   * @exception IllegalArgumentException if the initial capacity is less
   *              than zero.
   */
  public Properties(int initialCapacity) {
    this(initialCapacity, 0.75f);
  }

  /**
   * Constructs a new, empty hashtable with a default initial capacity (11)
   * and load factor, which is <tt>0.75</tt>. 
   */
  public Properties() {
    this(11, 0.75f);
  }

  /**
   * Returns the number of keys in this hashtable.
   *
   * @return  the number of keys in this hashtable.
   */
  public synchronized int size() {
    return count;
  }

  /**
   * Tests if this hashtable maps no keys to values.
   *
   * @return  <code>true</code> if this hashtable maps no keys to values;
   *          <code>false</code> otherwise.
   */
  public synchronized boolean isEmpty() {
    return count == 0;
  }

  /**
   * Returns an enumeration of the keys in this hashtable.
   *
   * @return  an enumeration of the keys in this hashtable.
   * @see     Enumeration
   * @see     #elements()
   */
  public synchronized Enumeration keys() {
    return getEnumeration(KEYS);
  }

  /**
   * Returns an enumeration of the values in this hashtable.
   * Use the Enumeration methods on the returned object to fetch the elements
   * sequentially.
   *
   * @return  an enumeration of the values in this hashtable.
   * @see     java.util.Enumeration
   * @see	Map
   */
  public synchronized Enumeration elements() {
    return getEnumeration(VALUES);
  }

  /**
   * Tests if the specified object is a key in this hashtable.
   * 
   * @param   key   possible key.
   * @return  <code>true</code> if and only if the specified object 
   *          is a key in this hashtable, as determined by the 
   *          <tt>equals</tt> method; <code>false</code> otherwise.
   * @throws  NullPointerException  if the key is <code>null</code>.
   */
  public synchronized boolean containsKey(String key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index] ; e != null ; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the value to which the specified key is mapped in this hashtable.
   *
   * @param   key   a key in the hashtable.
   * @return  the value to which the key is mapped in this hashtable;
   *          <code>null</code> if the key is not mapped to any value in
   *          this hashtable.
   * @throws  NullPointerException  if the key is <code>null</code>.
   */
  public synchronized Object get(String key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index] ; e != null ; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        return e.value;
      }
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
    Entry oldMap[] = table;

    int newCapacity = oldCapacity * 2 + 1;
    Entry newMap[] = new Entry[newCapacity];

    modCount++;
    threshold = (int)(newCapacity * loadFactor);
    table = newMap;

    for (int i = oldCapacity ; i-- > 0 ;) {
      for (Entry old = oldMap[i] ; old != null ; ) {
        Entry e = old;
        old = old.next;

        int index = (e.hash & 0x7FFFFFFF) % newCapacity;
        e.next = newMap[index];
        newMap[index] = e;
      }
    }
  }

  /**
   * Calls the method put.
   * <p>
   * Provided to Enforce the use of primitive type for values.
   * The value returned is the result of the call to put.
   * 
   * @param key     the key to be placed into this property object.
   * @param value   the value corresponding to key.
   * @return        the previous value of the specified key in this property object, or null if it did not have one.
   */
  public Object setProperty(String key, Object value) throws ClassCastException {
    if ((value instanceof Number) || (value instanceof String)) {
      return put(key, value);
    }
    throw new ClassCastException("Bad property value: " + value.getClass());
  }
  
  /**
   * Maps the specified <code>key</code> to the specified <code>value</code>
   * in this hashtable. Neither the key nor the value can be <code>null</code>.
   * <p>
   * The value can be retrieved by calling the <code>get</code> method with a
   * key that is equal to the original key. 
   * 
   * Be careful only primitive type can be used as value, in the other case an
   * exception will be thrown at serialization.
   *
   * @param      key     the hashtable key.
   * @param      value   the value.
   * @return     the previous value of the specified key in this hashtable,
   *             or <code>null</code> if it did not have one.
   * @exception  NullPointerException  if the key or value is
   *               <code>null</code>.
   * @see     Object#equals(Object)
   */
  public synchronized Object put(String key, Object value) {
    // Make sure the value is not null
    if (value == null) throw new NullPointerException();


    // Makes sure the key is not already in the hashtable.
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index] ; e != null ; e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        Object old = e.value;
        e.value = value;
        return old;
      }
    }

    modCount++;
    if (count >= threshold) {
      // Rehash the table if the threshold is exceeded
      rehash();

      tab = table;
      index = (hash & 0x7FFFFFFF) % tab.length;
    } 

    // Creates the new entry.
    Entry e = new Entry(hash, key, value, tab[index]);
    tab[index] = e;
    count++;
    return null;
  }

  /**
   * Removes the key (and its corresponding value) from this 
   * hashtable. This method does nothing if the key is not in the hashtable.
   *
   * @param   key   the key that needs to be removed.
   * @return  the value to which the key had been mapped in this hashtable,
   *          or <code>null</code> if the key did not have a mapping.
   * @throws  NullPointerException  if the key is <code>null</code>.
   */
  public synchronized Object remove(String key) {
    Entry tab[] = table;
    int hash = key.hashCode();
    int index = (hash & 0x7FFFFFFF) % tab.length;
    for (Entry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
      if ((e.hash == hash) && e.key.equals(key)) {
        modCount++;
        if (prev != null) {
          prev.next = e.next;
        } else {
          tab[index] = e.next;
        }
        count--;
        Object oldValue = e.value;
        e.value = null;
        return oldValue;
      }
    }
    return null;
  }

  /**
   * Clears this hashtable so that it contains no keys. 
   */
  public synchronized void clear() {
    Entry tab[] = table;
    modCount++;
    for (int index = tab.length; --index >= 0; )
      tab[index] = null;
    count = 0;
  }

  /**
   * Creates a shallow copy of this hashtable. All the structure of the 
   * hashtable itself is copied, but the keys and values are not cloned. 
   * This is a relatively expensive operation.
   *
   * @return  a clone of the hashtable.
   */
  public synchronized Object clone() {
    try { 
      Properties t = (Properties) super.clone();
      t.table = new Entry[table.length];
      for (int i = table.length ; i-- > 0 ; ) {
        t.table[i] = (table[i] != null) 
          ? (Entry)table[i].clone() : null;
      }
      t.modCount = 0;
      return t;
    } catch (CloneNotSupportedException e) { 
      // this shouldn't happen, since we are Cloneable
      throw new InternalError();
    }
  }

  /**
   * Returns a string representation of this <tt>Properties</tt> object 
   * in the form of a set of entries, enclosed in braces and separated 
   * by the ASCII characters "<tt>,&nbsp;</tt>" (comma and space). Each 
   * entry is rendered as the key, an equals sign <tt>=</tt>, and the 
   * associated element, where the <tt>toString</tt> method is used to 
   * convert the key and element to strings. <p>Overrides to 
   * <tt>toString</tt> method of <tt>Object</tt>.
   *
   * @return  a string representation of this hashtable.
   */
  public synchronized String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("(").append(super.toString());
    buf.append("}");
    return buf.toString();
  }


  private Enumeration getEnumeration(int type) {
    if (count == 0)  return emptyEnumerator;

    return new Enumerator(type, false);
  }

  // Comparison and hashing

  /**
   * Returns the hash code value for this Map as per the definition in the
   * Map interface.
   *
   * @see Map#hashCode()
   * @since 1.2
   */
  public synchronized int hashCode() {
    /*
     * This code detects the recursion caused by computing the hash code
     * of a self-referential hash table and prevents the stack overflow
     * that would otherwise result.  This allows certain 1.1-era
     * applets with self-referential hash tables to work.  This code
     * abuses the loadFactor field to do double-duty as a hashCode
     * in progress flag, so as not to worsen the space performance.
     * A negative load factor indicates that hash code computation is
     * in progress.
     */
    int h = 0;
    if (count == 0 || loadFactor < 0)
      return h;  // Returns zero

    loadFactor = -loadFactor;  // Mark hashCode computation in progress
    Entry tab[] = table;
    for (int i = 0; i < tab.length; i++)
      for (Entry e = tab[i]; e != null; e = e.next)
        h += e.key.hashCode() ^ e.value.hashCode();
    loadFactor = -loadFactor;  // Mark hashCode computation complete

    return h;
  }

//   /**
//    * Save the state of the Properties to a stream (i.e., serialize it).
//    *
//    * @serialData The <i>capacity</i> of the Properties (the length of the
//    *		   bucket array) is emitted (int), followed  by the
//    *		   <i>size</i> of the Properties (the number of key-value
//    *		   mappings), followed by the key (Object) and value (Object)
//    *		   for each key-value mapping represented by the Properties
//    *		   The key-value mappings are emitted in no particular order.
//    */
//   private synchronized void writeObject(java.io.ObjectOutputStream s)
//     throws IOException
//     {
//       // Write out the length, threshold, loadfactor
//       s.defaultWriteObject();

//       // Write out length, count of elements and then the key/value objects
//       s.writeInt(table.length);
//       s.writeInt(count);
//       for (int index = table.length-1; index >= 0; index--) {
//         Entry entry = table[index];

//         while (entry != null) {
//           s.writeObject(entry.key);
//           s.writeObject(entry.value);
//           entry = entry.next;
//         }
//       }
//     }

//   /**
//    * Reconstitute the Properties from a stream (i.e., deserialize it).
//    */
//   private void readObject(java.io.ObjectInputStream s)
//     throws IOException, ClassNotFoundException
//     {
//       // Read in the length, threshold, and loadfactor
//       s.defaultReadObject();

//       // Read the original length of the array and number of elements
//       int origlength = s.readInt();
//       int elements = s.readInt();

//       // Compute new size with a bit of room 5% to grow but
//       // No larger than the original size.  Make the length
//       // odd if it's large enough, this helps distribute the entries.
//       // Guard against the length ending up zero, that's not valid.
//       int length = (int)(elements * loadFactor) + (elements / 20) + 3;
//       if (length > elements && (length & 1) == 0)
//         length--;
//       if (origlength > 0 && length > origlength)
//         length = origlength;

//       table = new Entry[length];
//       count = 0;

//       // Read the number of elements and then all the key/value objects
//       for (; elements > 0; elements--) {
//         String key = (String) s.readObject();
//         Object value = s.readObject();
//         put(key, value);  // synch could be eliminated for performance
//       }
//     }


  /**
   * Properties collision list.
   */
  private static class Entry {
    int hash;
    String key;
    Object value;
    Entry next;

    protected Entry(int hash, String key, Object value, Entry next) {
      this.hash = hash;
      this.key = key;
      this.value = value;
      this.next = next;
    }

    protected Object clone() {
      return new Entry(hash, key, value,
                       (next==null ? null : (Entry)next.clone()));
    }

    // Map.Entry Ops 

    public String getKey() {
      return key;
    }

    public Object getValue() {
      return value;
    }

    public Object setValue(Object value) {
      if (value == null)
        throw new NullPointerException();

      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Entry))
        return false;
      Entry e = (Entry) o;

      return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
        (value==null ? e.getValue()==null : value.equals(e.getValue()));
    }

    public int hashCode() {
      return hash ^ (value==null ? 0 : value.hashCode());
    }

    public String toString() {
      return key + "=" + value.toString();
    }
  }

  // Types of Enumerations/Iterations
  private static final int KEYS = 0;
  private static final int VALUES = 1;
  private static final int ENTRIES = 2;

  /**
   * A hashtable enumerator class.  This class implements both the
   * Enumeration and Iterator interfaces, but individual instances
   * can be created with the Iterator methods disabled.  This is necessary
   * to avoid unintentionally increasing the capabilities granted a user
   * by passing an Enumeration.
   */
  private class Enumerator implements Enumeration {
    Entry[] table = Properties.this.table;
    int index = table.length;
    Entry entry = null;
    Entry lastReturned = null;
    int type;

    /**
     * Indicates whether this Enumerator is serving as an Iterator
     * or an Enumeration.  (true -> Iterator).
     */
    boolean iterator;

    /**
     * The modCount value that the iterator believes that the backing
     * List should have.  If this expectation is violated, the iterator
     * has detected concurrent modification.
     */
    protected int expectedModCount = modCount;

    Enumerator(int type, boolean iterator) {
      this.type = type;
      this.iterator = iterator;
    }

    public boolean hasMoreElements() {
      Entry e = entry;
      int i = index;
      Entry t[] = table;
      /* Use locals for faster loop iteration */
      while (e == null && i > 0) { 
        e = t[--i];
      }
      entry = e;
      index = i;
      return e != null;
    }

    public Object nextElement() {
      Entry et = entry;
      int i = index;
      Entry t[] = table;
      /* Use locals for faster loop iteration */
      while (et == null && i > 0) { 
        et = t[--i];
      }
      entry = et;
      index = i;
      if (et != null) {
        Entry e = lastReturned = entry;
        entry = e.next;
        return type == KEYS ? e.key : (type == VALUES ? e.value : e);
      }
      throw new NoSuchElementException("Properties Enumerator");
    }

    // Iterator methods
    public boolean hasNext() {
      return hasMoreElements();
    }

    public Object next() {
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();
      return nextElement();
    }

    public void remove() {
      if (!iterator)
        throw new UnsupportedOperationException();
      if (lastReturned == null)
        throw new IllegalStateException("Properties Enumerator");
      if (modCount != expectedModCount)
        throw new ConcurrentModificationException();

      synchronized(Properties.this) {
        Entry[] tab = Properties.this.table;
        int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;

        for (Entry e = tab[index], prev = null; e != null;
             prev = e, e = e.next) {
          if (e == lastReturned) {
            modCount++;
            expectedModCount++;
            if (prev == null)
              tab[index] = e.next;
            else
              prev.next = e.next;
            count--;
            lastReturned = null;
            return;
          }
        }
        throw new ConcurrentModificationException();
      }
    }
  }
   
  private static EmptyEnumerator emptyEnumerator = new EmptyEnumerator();

  /**
   * A hashtable enumerator class for empty hash tables, specializes
   * the general Enumerator
   */
  private static class EmptyEnumerator implements Enumeration {

    EmptyEnumerator() {
    }

    public boolean hasMoreElements() {
      return false;
    }

    public Object nextElement() {
      throw new NoSuchElementException("Properties Enumerator");
    }
  }

  public void copyInto(Map h) {
    if (count > 0) {
      for (int index = table.length-1; index >= 0; index--) {
        Entry entry = table[index];

        while (entry != null) {
          h.put(entry.key, entry.value);
          entry = entry.next;
        }
      }
    }
  }

  /* ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** */

  /**
   *  The object implements the writeTo method to write its contents to
   * the output stream.
   *
   * @param os the stream to write the object to
   */
  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(count, os);
    for (int index = table.length-1; index >= 0; index--) {
      Entry entry = table[index];
      
      while (entry != null) {
        StreamUtil.writeTo(entry.key, os);
        StreamUtil.writeObjectTo(entry.value, os);
        entry = entry.next;
      }
    }
  }
  
  // JORAM_PERF_BRANCH
  public int getEncodedSize() throws IOException {
    int size = 0;
    size += 4;
    for (int index = table.length-1; index >= 0; index--) {
      Entry entry = table[index];
      
      while (entry != null) {
        size += 4 + entry.key.length();
        size += StreamUtil.getEncodedSize(entry.value);
        entry = entry.next;
      }
    }
    
    return size;
  }
  
  //JORAM_PERF_BRANCH
  public int getClassId() {
    return Encodable.PROPERTIES_CLASS_ID;
  }

  //JORAM_PERF_BRANCH
  public void encode(Encoder encoder) throws Exception {
    encoder.encodeUnsignedInt(count);
    for (int index = table.length-1; index >= 0; index--) {
      Entry entry = table[index];
      
      while (entry != null) {
        encoder.encodeString(entry.key);
        StreamUtil.writeObjectTo(entry.value, encoder);
        entry = entry.next;
      }
    }
  }

  //JORAM_PERF_BRANCH
  public void decode(Decoder decoder) throws Exception {
    int count = decoder.decodeUnsignedInt();
    if (count == -1) return;
    
    String key;
    Object value;
    for (int i=0; i<count; i++) {
      key = decoder.decodeString();
      value = StreamUtil.readObjectFrom(decoder);
      put(key, value);
    }
  }

  /**
   *  The object implements the readFrom method to restore its contents from
   * the input stream.
   *
   * @param is the stream to read data from in order to restore the object
   */
  public static Properties readFrom(InputStream is) throws IOException {
    int count = StreamUtil.readIntFrom(is);
    if (count == -1) return null;

    Properties p = new Properties(((4*count)/3) +1);

    String key;
    Object value;
    for (int i=0; i<count; i++) {
      key = StreamUtil.readStringFrom(is);
      value = StreamUtil.readObjectFrom(is);
      p.put(key, value);
    }

    return p;
  }
  
  // JORAM_PERF_BRANCH
  public static Properties readFrom(Decoder decoder) throws Exception {
    int count = decoder.decodeUnsignedInt();
    if (count == -1) return null;

    Properties p = new Properties(((4*count)/3) +1);

    String key;
    Object value;
    for (int i=0; i<count; i++) {
      key = decoder.decodeString();
      value = StreamUtil.readObjectFrom(decoder);
      p.put(key, value);
    }

    return p;
  }
  
  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Serializable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  private void writeObject(ObjectOutputStream out) throws IOException {
    writeTo(out);
  }

  /**
   * @throws ClassNotFoundException  
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readFrom(in);
  }

}
