/*
 * JORAM: Open Reliable Asynchronous Messaging
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

/**
 * The <code>AdminModule</code> class allows to set an administrator
 * connection to a given JORAM server, and provides administration and
 * monitoring methods at a server/platform level.
 */
public class AdminModule {
  /** The connection used to link the administrator and the platform. */
  public static Connection cnx;
  /** The requestor for sending the synchronous requests. */
  public static AdminRequestor requestor;
  
  public static class AdminRequestor {
    Connection cnx;
    Session sess;
    Topic topic;
    MessageProducer producer;
    TemporaryTopic tmpTopic;
    MessageConsumer consumer;
    
    public AdminRequestor(Connection cnx) throws JoramException {
       this.cnx = cnx;
      this.sess = cnx.createSession(false, Session.AUTO_ACKNOWLEDGE);
      topic = sess.createTopic("#AdminTopic");
      producer = sess.createProducer(topic);
      tmpTopic = sess.createTemporaryTopic();
      consumer = sess.createConsumer(tmpTopic);
    }

    public Message request(AdminRequest request, long timeout) 
    throws JoramException {
      AdminMessage requestMsg = new AdminMessage();
      try {
        requestMsg.setAdminMessage(request);
      } catch (Exception e) {
        throw new JoramException(e.getMessage());
      }
      requestMsg.setReplyTo(tmpTopic);
      producer.send(requestMsg);
      String correlationId = requestMsg.getMessageID();
      while (true) {
        Message reply = consumer.receive(timeout);
        if (reply == null) {
          throw new JoramException("Interrupted request");
        } else {
          if (correlationId.equals(reply.getCorrelationID())) {
            return reply;
          } else {
            continue;
          }
        }
      }
    }

    void close() throws JoramException {
      consumer.close();
      producer.close();
      tmpTopic.delete();
      sess.close();
    }
  }

  /**
   * Opens a connection dedicated to administering with the Joram server
   * which parameters are wrapped by a given
   * <code>TopicConnectionFactory</code>.
   *
   * @param cnxFact  The TopicConnectionFactory to use for connecting.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   *
   */
  public static void connect(TcpConnectionFactory cnxFact, String name, String password) 
  throws JoramException {
    if (cnx != null)
      throw new ConnectException("Admin connection not established.");

    cnx = cnxFact.createConnection(name, password);
    requestor = new AdminRequestor(cnx);
    cnx.start();
  }

  /**
   * Opens a TCP connection with the Joram server running on a given host and
   * listening to a given port.
   *
   * @param host  The name or IP address of the host the server is running on.
   * @param port  The number of the port the server is listening to.
   * @param name  Administrator's name.
   * @param password  Administrator's password.
   * @param cnxTimer  Timer in seconds during which connecting to the server
   *          is attempted.
   * @param reliableClass  Reliable class name.
   */
  public static void connect(
      String hostName,
      int port,
      String name,
      String password,
      int cnxTimer,
      String reliableClass) throws JoramException  {
    TcpConnectionFactory cnxFact = new TcpConnectionFactory(hostName, port);
    connect(cnxFact, name, password);
  }

  public static void connect(
      String hostName,
      int port,
      String name,
      String password,
      int cnxTimer) throws JoramException {
    connect(hostName, port, name, password, cnxTimer, null);
  }

  public static void connect(String name, String password, int cnxTimer) throws JoramException {
    connect("localhost", 16010, name, password, cnxTimer);
  }

  public static void disconnect() throws JoramException {
    requestor.close();
    cnx.close();
  }

  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception ConnectException  If the connection to the platform fails.
   * @exception AdminException  If the platform's reply is invalid, or if
   *              the request failed.
   */
  public static AdminReply doRequest(AdminRequest request, long timeout) throws JoramException {
    if (cnx == null)
      throw new ConnectException("Admin connection not established.");

    if (timeout < 1)
      timeout = 120000;

    AdminMessage replyMsg = (AdminMessage) requestor.request(request, timeout);
    AdminReply reply;
    try {
      reply = (AdminReply) replyMsg.getAdminMessage();
    } catch (Exception e) {
      //e.printStackTrace();
      throw new JoramException("AdminException:: " + e.getMessage());
    }

    if (! reply.succeeded())
      throw new JoramException("AdminException:: " + reply.getInfo());
      
    return reply;
  }
  
  /**
   * Method actually sending an <code>AdminRequest</code> instance to
   * the platform and getting an <code>AdminReply</code> instance.
   *
   * @exception JoramException  If the connection to the platform fails,
   *                            If the platform's reply is invalid, or if
   *                            the request failed.
   */
  public static AdminReply doRequest(AdminRequest request) throws JoramException {
    return doRequest(request, 120000);
  }
  
}
