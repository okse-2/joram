/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2006 ScalAgent Distributed Technologies
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Hashtable;

import org.objectweb.joram.shared.stream.Streamable;
import org.objectweb.joram.shared.stream.StreamUtil;

import fr.dyade.aaa.util.Debug;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

/**
 * An <code>AbstractJmsMessage</code> is a message exchanged between a
 * Joram client and its proxy.
 */
public abstract class AbstractJmsMessage implements Externalizable, Streamable {
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
  protected final static int SESS_CREATE_TDREPLY = 28;
  protected final static int SESS_CREATE_TQREQUEST = 29;
  protected final static int SESS_CREATE_TTREQUEST = 30;
  protected final static int TEMP_DEST_DELETE_REQUEST = 31;
  protected final static int XA_CNX_COMMIT = 32;
  protected final static int XA_CNX_PREPARE = 33;
  protected final static int XA_CNX_RECOVER_REPLY = 34;
  protected final static int XA_CNX_RECOVER_REQUEST = 35;
  protected final static int XA_CNX_ROLLBACK = 36;

  protected int classid;

  protected static final String[] classnames = {
    "org.objectweb.joram.shared.client.CnxConnectRequest",
    "org.objectweb.joram.shared.client.CnxConnectReply",
    "org.objectweb.joram.shared.client.CnxStartRequest",
    "org.objectweb.joram.shared.client.CnxStopRequest",
    "org.objectweb.joram.shared.client.CnxCloseRequest",
    "org.objectweb.joram.shared.client.CnxCloseReply",
    "org.objectweb.joram.shared.client.ProducerMessages",
    "org.objectweb.joram.shared.client.ConsumerReceiveRequest",
    "org.objectweb.joram.shared.client.ConsumerMessages",
    "org.objectweb.joram.shared.client.ConsumerSubRequest",
    "org.objectweb.joram.shared.client.ConsumerUnsubRequest",
    "org.objectweb.joram.shared.client.ConsumerAckRequest",
    "org.objectweb.joram.shared.client.ConsumerDenyRequest",
    "org.objectweb.joram.shared.client.SessAckRequest",
    "org.objectweb.joram.shared.client.SessDenyRequest",
    "org.objectweb.joram.shared.client.MomExceptionReply",
    "org.objectweb.joram.shared.client.ServerReply",

    "org.objectweb.joram.shared.client.ActivateConsumerRequest",
    "org.objectweb.joram.shared.client.CommitRequest",
    "org.objectweb.joram.shared.client.ConsumerCloseSubRequest",
    "org.objectweb.joram.shared.client.ConsumerSetListRequest",
    "org.objectweb.joram.shared.client.ConsumerUnsetListRequest",
    "org.objectweb.joram.shared.client.GetAdminTopicReply",
    "org.objectweb.joram.shared.client.GetAdminTopicRequest",
    "org.objectweb.joram.shared.client.JmsRequestGroup",
    "org.objectweb.joram.shared.client.PingRequest",
    "org.objectweb.joram.shared.client.QBrowseReply",
    "org.objectweb.joram.shared.client.QBrowseRequest",
    "org.objectweb.joram.shared.client.SessCreateTDReply",
    "org.objectweb.joram.shared.client.SessCreateTQRequest",
    "org.objectweb.joram.shared.client.SessCreateTTRequest",
    "org.objectweb.joram.shared.client.TempDestDeleteRequest",
    "org.objectweb.joram.shared.client.XACnxCommit",
    "org.objectweb.joram.shared.client.XACnxPrepare",
    "org.objectweb.joram.shared.client.XACnxRecoverReply",
    "org.objectweb.joram.shared.client.XACnxRecoverRequest",
    "org.objectweb.joram.shared.client.XACnxRollback"
  };

  protected abstract int getClassId();

  /**
   * Constructs an <code>AbstractJmsMessage</code>.
   */
  public AbstractJmsMessage() {
    classid = getClassId();
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Interface needed for soap serialization
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  /**
   *
   * @exception	IOException
   */
  public Hashtable soapCode() throws IOException {
    Hashtable h = new Hashtable();
    h.put("classname", getClass().getName());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writeTo(baos);
    baos.flush();
    h.put("bytecontent", baos.toByteArray());
    baos.close();

    return h;
  }

  /**
   *
   * @exception	ClassNotFound
   * @exception	InstantiationException
   * @exception	IllegalAccessException
   * @exception	IOException
   */
  public static Object soapDecode(Hashtable h) throws Exception {
    AbstractJmsMessage msg = null;
    ByteArrayInputStream bais = null;

    String classname = (String) h.get("classname");
    msg = (AbstractJmsMessage) Class.forName(classname).newInstance();
    byte[] content = (byte[]) h.get("bytecontent");
    try {
      bais = new ByteArrayInputStream(content);
      msg.readFrom(bais);
    } finally {
      if (bais != null) bais.close();
    }

    return msg;
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
      msg = (AbstractJmsMessage) Class.forName(classnames[classid]).newInstance();
      msg.readFrom(is);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "AbstractJmsMessage.read: " + msg);

    return msg;
  }
}
