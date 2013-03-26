package fr.dyade.aaa.common.encoding;

public interface Decoder {

  short decodeSignedShort() throws Exception;
  
  short decodeUnsignedShort() throws Exception;
  
  int decodeSignedInt() throws Exception;
  
  int decodeUnsignedInt() throws Exception;
  
  long decodeUnsignedLong() throws Exception;
  
  long decodeSignedLong() throws Exception;
  
  String decodeNullableString() throws Exception;
  
  String decodeString() throws Exception;
  
  String decodeString(int length) throws Exception;
  
  byte decodeByte() throws Exception;
  
  byte[] decodeNullableByteArray() throws Exception;
  
  byte[] decodeByteArray() throws Exception;
  
  byte[] decodeByteArray(int length) throws Exception;
  
  boolean isNull() throws Exception;
  
  boolean decodeBoolean() throws Exception;
  
}
