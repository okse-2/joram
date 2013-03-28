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

public class StringPair implements Encodable {
  
  private EncodedString s1;
  
  private EncodedString s2;

  public StringPair(EncodedString s1, EncodedString s2) {
    super();
    this.s1 = s1;
    this.s2 = s2;
  }

  public int getClassId() {
    return STRING_PAIR_CLASS_ID;
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

}
