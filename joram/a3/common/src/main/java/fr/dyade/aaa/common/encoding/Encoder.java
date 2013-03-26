package fr.dyade.aaa.common.encoding;

public interface Encoder {

  void encodeBoolean(boolean bool) throws Exception;
  
  void encodeByte(byte b) throws Exception;
  
  void encodeSignedShort(short s) throws Exception;
  
  void encodeUnsignedShort(short s) throws Exception;
  
  void encodeSignedInt(int i) throws Exception;
  
  void encodeUnsignedInt(int i) throws Exception;
  
  void encodeSignedLong(long l) throws Exception;
  
  void encodeUnsignedLong(long l) throws Exception;
  
  void encodeNullableString(String str);
  
  void encodeString(String str) throws Exception;
  
  void encodeNullableByteArray(byte[] tab) throws Exception;
  
  void encodeByteArray(byte[] tab) throws Exception;
  
  void encodeNullableByteArray(byte[] tab, int offset, int length) throws Exception;
    
  void encodeByteArray(byte[] tab, int offset, int length) throws Exception;
  
}
