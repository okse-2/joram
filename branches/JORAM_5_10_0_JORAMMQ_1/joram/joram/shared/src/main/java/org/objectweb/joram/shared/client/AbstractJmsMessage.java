/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 - 2013 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.client;

import java.io.Externalizable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.IOException;

import fr.dyade.aaa.common.Debug;
import fr.dyade.aaa.common.encoding.Encodable;
import fr.dyade.aaa.common.stream.StreamUtil;
import fr.dyade.aaa.common.stream.Streamable;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * An <code>AbstractJmsMessage</code> is a message exchanged between a
 * Joram client and its proxy.
 */
public abstract class AbstractJmsMessage implements Externalizable, Streamable, Encodable {
  public static Logger logger = Debug.getLogger(AbstractJmsMessage.class.getName());

  protected final static int NULL_CLASS_ID = -1;

  protected final static int CNX_CONNECT_REQUEST = 0;
  protected final static int CNX_CONNECT_REPLY = 1;
  protected final static int CNX_START_REQUEST = 2;
  protected final static int CNX_STOP_REQUEST = 3;
  protected final static int CNX_CLOSE_REQUEST = 4;
  protected final static int CNX_CLOSE_REPLY = 5;
  protected final static int PRODUCER_MESSAGES = 6;
  protected final static int CONSUMER_RECEIVE_REQUEST = 7;
  protected final static int CONSUMER_MESSAGES = 8;
  protected final static int CONSUMER_SUB_REQUEST = 9;
  protected final static int CONSUMER_UNSUB_REQUEST = 10;
  protected final static int CONSUMER_ACK_REQUEST = 11;
  protected final static int CONSUMER_DENY_REQUEST = 12;
  protected final static int SESS_ACK_REQUEST = 13;
  protected final static int SESS_DENY_REQUEST = 14;
  protected final static int MOM_EXCEPTION_REPLY = 15;
  protected final static int SERVER_REPLY = 16;

  protected final static int ACTIVATE_CONSUMER_REQUEST = 17;
  protected final static int COMMIT_REQUEST = 18;
  protected final static int CONSUMER_CLOSE_SUB_REQUEST = 19;
  protected final static int CONSUMER_SET_LIST_REQUEST = 20;
  protected final static int CONSUMER_UNSET_LIST_REQUEST = 21;
  protected final static int GET_ADMIN_TOPIC_REPLY = 22;
  protected final static int GET_ADMIN_TOPIC_REQUEST = 23;
  protected final static int JMS_REQUEST_GROUP = 24;
  protected final static int PING_REQUEST = 25;
  protected final static int QBROWSE_REPLY = 26;
  protected final static int QBROWSE_REQUEST = 27;
  protected final static int SESS_CREATE_DEST_REPLY = 28;
  protected final static int SESS_CREATE_DEST_REQUEST = 29;
  protected final static int XXX_SESS_CREATE_TTREQUEST = 30;
  protected final static int TEMP_DEST_DELETE_REQUEST = 31;
  protected final static int XA_CNX_COMMIT = 32;
  protected final static int XA_CNX_PREPARE = 33;
  protected final static int XA_CNX_RECOVER_REPLY = 34;
  protected final static int XA_CNX_RECOVER_REQUEST = 35;
  protected final static int XA_CNX_ROLLBACK = 36;
  protected final static int ADD_CLIENTID_REQUEST = 37;
  protected final static int ADD_CLIENTID_REPLY = 38;

  protected int classid;

  protected static final String[] classnames = {
    CnxConnectRequest.class.getName(),
    CnxConnectReply.class.getName(),
    CnxStartRequest.class.getName(),
    CnxStopRequest.class.getName(),
    CnxCloseRequest.class.getName(),
    CnxCloseReply.class.getName(),
    ProducerMessages.class.getName(),
    ConsumerReceiveRequest.class.getName(),
    ConsumerMessages.class.getName(),
    ConsumerSubRequest.class.getName(),
    ConsumerUnsubRequest.class.getName(),
    ConsumerAckRequest.class.getName(),
    ConsumerDenyRequest.class.getName(),
    SessAckRequest.class.getName(),
    SessDenyRequest.class.getName(),
    MomExceptionReply.class.getName(),
    ServerReply.class.getName(),

    ActivateConsumerRequest.class.getName(),
    CommitRequest.class.getName(),
    ConsumerCloseSubRequest.class.getName(),
    ConsumerSetListRequest.class.getName(),
    ConsumerUnsetListRequest.class.getName(),
    GetAdminTopicReply.class.getName(),
    GetAdminTopicRequest.class.getName(),
    JmsRequestGroup.class.getName(),
    PingRequest.class.getName(),
    QBrowseReply.class.getName(),
    QBrowseRequest.class.getName(),
    SessCreateDestReply.class.getName(),
    SessCreateDestRequest.class.getName(),
    null,
    TempDestDeleteRequest.class.getName(),
    XACnxCommit.class.getName(),
    XACnxPrepare.class.getName(),
    XACnxRecoverReply.class.getName(),
    XACnxRecoverRequest.class.getName(),
    XACnxRollback.class.getName(),
    AddClientIDRequest.class.getName(),
    AddClientIDReply.class.getName()
  };

  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractJmsMessage</code>.
   */
  public AbstractJmsMessage() {
    classid = getClassId();
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Externalizable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  public final void writeExternal(ObjectOutput out) throws IOException {
    writeTo((OutputStream) out);
  }

  public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    readFrom((InputStream) in);
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  static public void write(AbstractJmsMessage msg,
                           OutputStream os) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "AbstractJmsMessage.write: " + msg);

    if (msg == null) {
      StreamUtil.writeTo(NULL_CLASS_ID, os);
    } else {
      StreamUtil.writeTo(msg.getClassId(), os);
      msg.writeTo(os);
    }
  }

  static public AbstractJmsMessage read(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    int classid = -1;
    AbstractJmsMessage msg = null;

    classid = StreamUtil.readIntFrom(is);
    if (classid != NULL_CLASS_ID) {
      switch (classid) {
      case PRODUCER_MESSAGES:
        msg = new ProducerMessages();
        break;
      case CONSUMER_MESSAGES:
        msg = new ConsumerMessages();
        break;
      case CONSUMER_ACK_REQUEST:
        msg = new ConsumerAckRequest();
        break;
      case CONSUMER_RECEIVE_REQUEST:
        msg = new ConsumerReceiveRequest();
        break;
      case SERVER_REPLY:
        msg = new ServerReply();
        break;
      case CONSUMER_SET_LIST_REQUEST:
        msg = new ConsumerSetListRequest();
        break;
      default:
        msg = (AbstractJmsMessage) Class.forName(classnames[classid]).newInstance();
      }
      
      msg.readFrom(is);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractJmsMessage.read: " + msg);

    return msg;
  }
}
