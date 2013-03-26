package fr.dyade.aaa.common.encoding;

public interface Encodable {
  
  int getClassId();
  
  int getEncodeSize();
  
  void encode(Encoder encoder) throws Exception;

  void decode(Decoder decoder) throws Exception;

}
