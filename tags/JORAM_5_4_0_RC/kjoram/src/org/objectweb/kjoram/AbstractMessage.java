/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.kjoram;

import java.io.IOException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * An <code>AbstractMessage</code> is a message exchanged between a
 * kjoram client and its proxy.
 */
public abstract class AbstractMessage implements Streamable {
  public static Logger logger = Debug.getLogger(AbstractMessage.class.getName());
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
//   protected final static int COMMIT_REQUEST = 18;
  protected final static int CONSUMER_CLOSE_SUB_REQUEST = 19;
//   protected final static int CONSUMER_SET_LIST_REQUEST = 20;
//   protected final static int CONSUMER_UNSET_LIST_REQUEST = 21;
   protected final static int GET_ADMIN_TOPIC_REPLY = 22;
   protected final static int GET_ADMIN_TOPIC_REQUEST = 23;
//   protected final static int JMS_REQUEST_GROUP = 24;
//   protected final static int PING_REQUEST = 25;
//   protected final static int QBROWSE_REPLY = 26;
//   protected final static int QBROWSE_REQUEST = 27;
   protected final static int SESS_CREATE_TDREPLY = 28;
//   protected final static int SESS_CREATE_TQREQUEST = 29;
   protected final static int SESS_CREATE_TTREQUEST = 30;
   protected final static int TEMP_DEST_DELETE_REQUEST = 31;
//   protected final static int XA_CNX_COMMIT = 32;
//   protected final static int XA_CNX_PREPARE = 33;
//   protected final static int XA_CNX_RECOVER_REPLY = 34;
//   protected final static int XA_CNX_RECOVER_REQUEST = 35;
//   protected final static int XA_CNX_ROLLBACK = 36;

  protected int classid;

  protected static final String[] classnames = {
    "org.objectweb.kjoram.CnxConnectRequest",
    "org.objectweb.kjoram.CnxConnectReply",
    "org.objectweb.kjoram.CnxStartRequest",
    "org.objectweb.kjoram.CnxStopRequest",
    "org.objectweb.kjoram.CnxCloseRequest",
    "org.objectweb.kjoram.CnxCloseReply",
    "org.objectweb.kjoram.ProducerMessages",
    "org.objectweb.kjoram.ConsumerReceiveRequest",
    "org.objectweb.kjoram.ConsumerMessages",
    "org.objectweb.kjoram.ConsumerSubRequest",
    "org.objectweb.kjoram.ConsumerUnsubRequest",
    "org.objectweb.kjoram.ConsumerAckRequest",
    "org.objectweb.kjoram.ConsumerDenyRequest",
    "org.objectweb.kjoram.SessAckRequest",
    "org.objectweb.kjoram.SessDenyRequest",
    "org.objectweb.kjoram.MomExceptionReply",
    "org.objectweb.kjoram.ServerReply",

    "org.objectweb.kjoram.ActivateConsumerRequest",
    "org.objectweb.kjoram.CommitRequest",
    "org.objectweb.kjoram.ConsumerCloseSubRequest",
    "org.objectweb.kjoram.ConsumerSetListRequest",
    "org.objectweb.kjoram.ConsumerUnsetListRequest",
    "org.objectweb.kjoram.GetAdminTopicReply",
    "org.objectweb.kjoram.GetAdminTopicRequest",
    "org.objectweb.kjoram.JmsRequestGroup",
    "org.objectweb.kjoram.PingRequest",
    "org.objectweb.kjoram.QBrowseReply",
    "org.objectweb.kjoram.QBrowseRequest",
    "org.objectweb.kjoram.SessCreateTDReply",
    "org.objectweb.kjoram.SessCreateTQRequest",
    "org.objectweb.kjoram.SessCreateTTRequest",
    "org.objectweb.kjoram.TempDestDeleteRequest",
    "org.objectweb.kjoram.XACnxCommit",
    "org.objectweb.kjoram.XACnxPrepare",
    "org.objectweb.kjoram.XACnxRecoverReply",
    "org.objectweb.kjoram.XACnxRecoverRequest",
    "org.objectweb.kjoram.XACnxRollback"
  };

  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractJmsMessage</code>.
   */
  public AbstractMessage() {
    classid = getClassId();
  }

  public final String toString() {
    StringBuffer strbuf = new StringBuffer();
    strbuf.append(classnames[getClassId()]);
    toString(strbuf);
    return strbuf.toString();
  }

  public abstract void toString(StringBuffer strbuf);

  // ==================================================
  // Streamable interface
  // ==================================================

  static public void write(AbstractMessage msg,
                           OutputXStream os) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "AbstractAdminMessage write: classid = " + msg.getClassId() + 
          ", " + classnames[msg.getClassId()]);
    if (msg == null) {
      os.writeInt(NULL_CLASS_ID);
    } else {
      os.writeInt(msg.getClassId());
      msg.writeTo(os);
    }
  }

  static public AbstractMessage read(InputXStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    int classid = -1;
    AbstractMessage msg = null;

    classid = is.readInt();
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "AbstractAdminMessage read: classid = " + classid + ", " + classnames[classid]);
    if (classid != NULL_CLASS_ID) {
      msg = (AbstractMessage) Class.forName(classnames[classid]).newInstance();
      msg.readFrom(is);
    }
    return msg;
  }
}
