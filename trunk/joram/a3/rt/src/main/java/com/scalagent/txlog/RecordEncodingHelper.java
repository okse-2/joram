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
package com.scalagent.txlog;

import java.nio.ByteBuffer;

import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.ByteBufferDecoder;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.EncodableFactoryRepository;
import fr.dyade.aaa.common.encoding.Encoder;

public class RecordEncodingHelper {
  
  public static final Logger logmon = Debug.getLogger(RecordEncodingHelper.class.getName());
  
  public static int getEncodedSize(Record record) throws Exception {
    // RecordType: 1
    // ObjectId classId: 4
    int encodedSize = 5 + record.getObjectId().getEncodedSize();
    
    if (record instanceof ValueRecord) {
      ValueRecord valueRecord = (ValueRecord) record;
      valueRecord.init();
      encodedSize += 4;
      encodedSize += valueRecord.getByteArraySize();
    }
    
    return encodedSize;
  }
  
  public static void encodeRecord(Record record, Encoder encoder, ByteBuffer buffer, TxLogFile file) throws Exception {
    encoder.encodeByte(record.getRecordType());
    Encodable objectId = record.getObjectId();
    encoder.encodeUnsignedInt(objectId.getEncodableClassId());
    objectId.encode(encoder);
    
    record.setFile(file);

    if (record instanceof ValueRecord) {
      ValueRecord valueRecord = (ValueRecord) record;

      // Needed when reading the log files after restart
      // Encode the encoded size of: classId + value
      encoder.encodeUnsignedInt(valueRecord.getByteArraySize());
      
      // The pointer need to directly reference the beginning of the
      // byte array
      valueRecord.setFilePointer(file.getCurrentFilePointer() + buffer.position());
      
      byte[] byteArray = valueRecord.getByteArray();
      if (byteArray == null) {
        Encodable value = valueRecord.getValue();
        encoder.encodeUnsignedInt(value.getEncodableClassId());
        value.encode(encoder);
      } else {
        buffer.put(byteArray);
      }
    }
  }
  
  public static Record decodeRecord(Decoder decoder, ByteBuffer buf, TxLogFile file) throws Exception {
    byte recordType = decoder.decodeByte();
    
    int objectIdClassId = decoder.decodeUnsignedInt();

    EncodableFactory objectIdFactory = EncodableFactoryRepository.getFactory(objectIdClassId);
    
    if (objectIdFactory == null) throw new Exception("Unknown encodable factory: " + objectIdClassId);
    
    Encodable objectId = objectIdFactory.createEncodable();
    objectId.decode(decoder);
    
    Record record;
    switch (recordType) {
    case Record.CREATE_RECORD:
      ValueRecord createRecord = ValueRecord.newCreateRecord(objectId, null, null);
      createRecord.setFile(file);
      decodeValueRecord(createRecord, decoder, buf, file);
      record = createRecord;
      break;
    case Record.SAVE_RECORD:
      ValueRecord saveRecord = ValueRecord.newSaveRecord(objectId, null, null);
      saveRecord.setFile(file);
      decodeValueRecord(saveRecord, decoder, buf, file);
      record = saveRecord;
      break;
    case Record.DELETE_RECORD:
      record = new DeleteRecord(objectId);
      record.setFile(file);
      break;
    default:
      throw new Exception("Unexpected record type: " + recordType);
    }

    record.setObjectId(objectId);
    
    return record;
  }
  
  private static void decodeValueRecord(ValueRecord valueRecord, Decoder decoder, ByteBuffer buf, TxLogFile file) throws Exception {
    // Don't load the encoded value because a delete may be following
    int byteArraySize = decoder.decodeUnsignedInt();
    valueRecord.setByteArraySize(byteArraySize);
    
    int currentPosition = buf.position();
    valueRecord.setFilePointer(currentPosition);
    
    // skip the byte array
    buf.position(buf.position() + byteArraySize);
  }
  
  public static Encodable decodeValue(Decoder decoder) throws Exception {
    int valueClassId = decoder.decodeUnsignedInt();
    EncodableFactory valueFactory = EncodableFactoryRepository.getFactory(valueClassId);
    if (valueFactory == null) throw new Exception("EncodableFactory not found: " + valueClassId);
    Encodable value = valueFactory.createEncodable();
    value.decode(decoder);
    return value;
  }
  
  public static byte[] resolveEncodedValue(ValueRecord record) throws Exception {
    byte[] encodedValue = record.getByteArray();
    if (encodedValue == null) {
      record.getFile().loadEncodedValue(record, true);
      encodedValue = record.getByteArray();
    }
    return encodedValue;
  }
  
  public static Encodable resolveValue(ValueRecord record) throws Exception {
    Encodable value = record.getValue();
    if (value != null) return value;
    byte[] encodedValue = resolveEncodedValue(record);
    return decodeValue(encodedValue);
  }
  
  public static Encodable decodeValue(byte[] encodedValue) throws Exception {
    ByteBuffer buf = ByteBuffer.wrap(encodedValue);
    Decoder decoder = new ByteBufferDecoder(buf);
    return decodeValue(decoder);
  }

}
