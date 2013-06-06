/*
 * Copyright (C) 2001 - 2007 ScalAgent Distributed Technologies 
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.common.encoding;

/**
 * Encodable object representing a pair of strings.
 */
public class StringPair implements Encodable {
  
  private EncodedString s1;
  
  private EncodedString s2;
  
  public StringPair() {}

  public StringPair(EncodedString s1, EncodedString s2) {
    super();
    this.s1 = s1;
    this.s2 = s2;
  }

  public int getEncodableClassId() {
    return EncodableFactoryRepository.STRING_PAIR_CLASS_ID;
  }

  public int getEncodedSize() {
    return s1.getEncodedSize() + s2.getEncodedSize();
  }

  public void encode(Encoder encoder) throws Exception {
    s1.encode(encoder);
    s2.encode(encoder);
  }

  public void decode(Decoder decoder) throws Exception {
    s1 = new EncodedString();
    s1.decode(decoder);
    s2 = new EncodedString();
    s2.decode(decoder);
  }
  
  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(s1.getString().length() + 1
        + s2.getString().length());
    buf.append(s1);
    buf.append('-');
    buf.append(s2);
    return buf.toString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((s1 == null) ? 0 : s1.hashCode());
    result = prime * result + ((s2 == null) ? 0 : s2.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StringPair other = (StringPair) obj;
    if (s1 == null) {
      if (other.s1 != null)
        return false;
    } else if (!s1.equals(other.s1))
      return false;
    if (s2 == null) {
      if (other.s2 != null)
        return false;
    } else if (!s2.equals(other.s2))
      return false;
    return true;
  }

  public static class Factory implements EncodableFactory {

    public Encodable createEncodable() {
      return new StringPair();
    }
    
  }

}
