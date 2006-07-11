/*
 * Created on 27 avr. 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.objectweb.joram.shared.client;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author feliot
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CommitRequest extends AbstractJmsRequest {
  
  /**
   * List of ProducerMessages
   */
  private Vector producerMessages;
  
  /**
   * List of SessAckRequest
   */
  private Vector ackRequests;
  
  /**
   * Indicates whether the produced messages
   * are asynchronously send or not
   * (without or with an acknowledgement).
   */
  private boolean asyncSend = false;
  
  public CommitRequest() {
    
  }
  
  public void addProducerMessages(ProducerMessages pm) {
    if (producerMessages == null) producerMessages = new Vector();
    producerMessages.addElement(pm);
  }
  
  public void addAckRequest(SessAckRequest sar) {
    if (ackRequests == null) ackRequests = new Vector();
    ackRequests.addElement(sar);
  }
  
  public Enumeration getProducerMessages() {
    if (producerMessages != null) {
      return producerMessages.elements();
    } else {
      return null;
    }
  }
  
  public Enumeration getAckRequests() {
    if (ackRequests != null) {
      return ackRequests.elements();
    } else {
      return null;
    }
  }
  
  public void setAsyncSend(boolean b) {
    asyncSend = b;
  }
  
  public final boolean getAsyncSend() {
    return asyncSend;
  }
  
  /**
   * Transforms this request into a hashtable of primitive values that can
   * be vehiculated through the SOAP protocol.
   */
  public Hashtable soapCode() {
    Hashtable h = super.soapCode();
    
    // Coding and adding the producerMessages into a array:
    int size = 0;
    if (producerMessages != null)
      size = producerMessages.size();
    if (size > 0) {
      Hashtable [] arrayMsg = new Hashtable[size];
      for (int i = 0; i<size; i++) {
        ProducerMessages msg = (ProducerMessages) producerMessages.elementAt(0);
        producerMessages.removeElementAt(0);
        arrayMsg[i] = msg.soapCode();
      }
      h.put("producerMessages",arrayMsg);
    }
    
    //  Coding and adding the ackRequests into a array:
    size = 0;
    if (ackRequests != null)
      size = ackRequests.size();
    if (size > 0) {
      Hashtable [] arrayMsg = new Hashtable[size];
      for (int i = 0; i<size; i++) {
        SessAckRequest msg = (SessAckRequest) ackRequests.elementAt(0);
        ackRequests.removeElementAt(0);
        arrayMsg[i] = msg.soapCode();
      }
      h.put("ackRequests",arrayMsg);
    }
    
    return h;
  }
  
  /** 
   * Transforms a hastable of primitive values into a
   * <code>CommitRequest</code> request.
   */
  public static Object soapDecode(Hashtable h) {
    CommitRequest req = new CommitRequest();
    req.setRequestId(((Integer) h.get("requestId")).intValue());
    req.setTarget((String) h.get("target"));
    
    Object [] arrayMsg = (Object []) h.get("producerMessages");
    if (arrayMsg != null) {
      for (int i = 0; i<arrayMsg.length; i++)
        req.addProducerMessages(
            (ProducerMessages)ProducerMessages.soapDecode(
                (Hashtable) arrayMsg[i]));
    }
    
    arrayMsg = (Object []) h.get("ackRequests");
    if (arrayMsg != null) {
      for (int i = 0; i<arrayMsg.length; i++)
        req.addAckRequest(
            (SessAckRequest)SessAckRequest.soapDecode(
                (Hashtable) arrayMsg[i]));
    }
    
    return req;
  }

  public String toString() {
    return '(' + super.toString() +
      ",producerMessages=" + producerMessages + 
      ",ackRequests=" + ackRequests + 
      ",asyncSend=" + asyncSend + ')';
  }

}
