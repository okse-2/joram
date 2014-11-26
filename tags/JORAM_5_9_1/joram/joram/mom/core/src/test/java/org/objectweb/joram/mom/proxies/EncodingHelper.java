/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.mom.proxies;

import java.nio.ByteBuffer;

import junit.framework.Assert;
import fr.dyade.aaa.common.encoding.ByteBufferDecoder;
import fr.dyade.aaa.common.encoding.ByteBufferEncoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;

public class EncodingHelper {
  
  public static void init() throws Exception {
    // Need to load the class ConnectionManager
    // in order to register the encodable factories
    Class.forName(ConnectionManager.class.getName());
  }
  
  public static byte[] encode(Encodable encodable) throws Exception {
    ByteBuffer byteBuffer = ByteBuffer.allocate(encodable.getEncodedSize());
    ByteBufferEncoder encoder = new ByteBufferEncoder(byteBuffer);
    encodable.encode(encoder);
    
    // Check the encoded size
    Assert.assertEquals(encodable.getEncodedSize(), byteBuffer.position());
    
    return byteBuffer.array();
  }
  
  public static Encodable decode(int classId, byte[] bytes) throws Exception {
    EncodableFactory factory = EncodableFactoryRepository.getFactory(classId);
    Encodable encodable = factory.createEncodable();
    
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
    ByteBufferDecoder decoder = new ByteBufferDecoder(byteBuffer);
    encodable.decode(decoder);
    
    return encodable;
  }

}
