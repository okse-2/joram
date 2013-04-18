package org.objectweb.joram.mom.proxies;

import org.objectweb.joram.mom.util.JoramHelper;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.common.encoding.Decoder;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.encoding.EncodableFactory;
import fr.dyade.aaa.common.encoding.Encoder;

public class ClientContextTxId implements Encodable {
  
  private AgentId ownerId;
  
  private int clientId;

  public ClientContextTxId() {
    super();
  }

  public ClientContextTxId(AgentId ownerId, int clientId) {
    super();
    this.ownerId = ownerId;
    this.clientId = clientId;
  }

  public AgentId getOwnerId() {
    return ownerId;
  }

  public int getClientId() {
    return clientId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + clientId;
    result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
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
    ClientContextTxId other = (ClientContextTxId) obj;
    if (clientId != other.clientId)
      return false;
    if (ownerId == null) {
      if (other.ownerId != null)
        return false;
    } else if (!ownerId.equals(other.ownerId))
      return false;
    return true;
  }

  public int getClassId() {
    return JoramHelper.CLIENTCONTEXTTXID_CLASS_ID;
  }

  public int getEncodedSize() throws Exception {
    return ownerId.getEncodedSize() + 4;
  }

  public void encode(Encoder encoder) throws Exception {
    ownerId.encode(encoder);
    encoder.encodeUnsignedInt(clientId);
  }

  public void decode(Decoder decoder) throws Exception {
    ownerId = new AgentId((short) 0, (short) 0, 0);
    ownerId.decode(decoder);
    clientId = decoder.decodeUnsignedInt();
  }
  
  public String toString() {
    StringBuffer buf = new StringBuffer(19);
    buf.append("CC").append(ownerId.toString()).append('_');
    buf.append(clientId);
    return buf.toString();
  }
  
  public static ClientContextTxId fromString(String s) throws Exception {
    int sepIndex = s.indexOf('_');
    if (sepIndex < 0) throw new Exception("Malformed ClientContextTxId: " + s);
    int id = Integer.parseInt(s.substring(sepIndex + 1));
    AgentId ownerId = AgentId.fromString(s.substring(2, sepIndex));
    return new ClientContextTxId(ownerId, id);
  }
  
  public static class ClientContextTxIdEncodableFactory implements EncodableFactory {

    public Encodable createEncodable() {
      return new ClientContextTxId();
    }
    
  }

}
