/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
package fr.dyade.aaa.util;

/**
 * This class describes a Key for Operation object with a relative path, key for
 * other object is the name of the object (String).
 */
public class OperationKey {
  /**
   * Creates a new key for the specified object.
   * 
   * @param dirName relative path of the object.
   * @param name    name of the object.
   * @return A new key for the specified object.
   */
  public static Object newKey(String dirName, String name) {
    if (dirName == null) return name;

    return new OperationKey(dirName, name);
  }

  private String dirName;
  private String name;

  private OperationKey(String dirName,
                       String name) {
    this.dirName = dirName;
    this.name = name;
  }

  /**
   * Returns a hash code value for the object.
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    // Should compute a specific one.
    return dirName.hashCode();
  }

  /**
   * Indicates whether some other object is "equal to" this one. 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof OperationKey) {
      OperationKey opk = (OperationKey)obj;
      if (opk.name.length() != name.length()) return false;
      if (opk.dirName.length() != dirName.length()) return false;
      if (!opk.dirName.equals(dirName)) return false;            
      return opk.name.equals(name);
    }
    return false;
  }
}
