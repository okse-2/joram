package org.objectweb.joram.mom.messages;

import org.objectweb.joram.mom.util.JoramHelper;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;

public class MessageBodyTxId extends MessageTxId {
  
  public MessageBodyTxId() {
    super();
  }

  public MessageBodyTxId(AgentId ownerId, long order) {
    super(ownerId, order);
  }
  
  public int getClassId() {
    return JoramHelper.MESSAGEBODYTXID_CLASS_ID;
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer(18);
    buf.append('M').append(getOwnerId().toString()).append('_');
    buf.append(getOrder());
    buf.append('B');
    return buf.toString();
  }
  
  public static class MessageBodyTxIdFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new MessageBodyTxId();
    }
    
  }

}
