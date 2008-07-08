package org.objectweb.joram.mom.util;

import org.objectweb.joram.mom.dest.QueueImpl;
import org.objectweb.joram.mom.notifications.ClientMessages;
import org.objectweb.joram.shared.messages.Message;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.agent.AgentId;
import fr.dyade.aaa.agent.Channel;
import fr.dyade.aaa.util.Debug;

/**
 * The <code>DMQManager</code> is made to stock the dead messages before sending
 * them to the dead message queue, only if such a queue is defined.
 */
public class DMQManager {

  /**
   * If the message expired before delivery.
   **/
  public static final short EXPIRED = 0;

  /**
   * If the target destination of the message did not accept the sender as a
   * WRITER.
   **/
  public static final short NOT_WRITEABLE = 1;

  /**
   * If the number of delivery attempts of the message overtook the threshold.
   **/
  public static final short UNDELIVERABLE = 2;

  /**
   * If the message has been deleted by an admin request.
   */
  public static final short ADMIN_DELETED = 3;

  /**
   * If the target destination of the message could not be found.
   */
  public static final short DELETED_DEST = 4;

  /**
   * If the queue has reached its max number of messages.
   */
  public static final short QUEUE_FULL = 5;

  /**
   * If an unexpected error happened during delivery.
   */
  public static final short UNEXPECTED_ERROR = 6;

  private ClientMessages deadMessages = null;
  
  private AgentId destDmqId = null;
  
  private AgentId senderId = null;
  
  public static Logger logger = Debug.getLogger(DMQManager.class.getName());

  /**
   * Creates a DMQManager. The <code>specificDmq</code> is used in priority. If
   * <code>null</code>, destination DMQ is used if it exists, else default DMQ
   * is used. If none exists, dead messages will be lost.
   * 
   * @param specificDmq
   *          Identifier of the dead message queue to use in priority.
   * @param currentDestDmq
   *          The DMQ of the destination
   * @param senderId
   *          The id of the destination. This is used to avoid sending to
   *          itself.
   */
  public DMQManager(AgentId specificDmq, AgentId currentDestDmq, AgentId senderId) {
    if (specificDmq != null) {
      // Sending the dead messages to the provided DMQ
      destDmqId = specificDmq;
    } else if (currentDestDmq != null) {
      // Sending the dead messages to the destination's DMQ
      destDmqId = currentDestDmq;
    } else {
      // Sending the dead messages to the server's default DMQ
      destDmqId = QueueImpl.getDefaultDMQId();
    }
    this.senderId = senderId;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this.getClass().getName() + " created, destDmqId: " + destDmqId);
  }

  /**
   * Creates a DMQManager. Destination DMQ is used if it exists, else default
   * DMQ is used. If none exists, dead messages will be lost
   * 
   * @param currentDestDmq
   *          The DMQ of the destination
   * @param senderId
   *          The id of the destination. This is used to avoid sending to
   *          itself.
   */
  public DMQManager(AgentId currentDestDmq, AgentId senderId) {
    this(null, currentDestDmq, senderId);
  }

  /**
   * Stocks a dead message waiting to be sent to the DMQ. If no DMQ was found at
   * creation time, the message is lost.
   * 
   * @param mess
   *          The message to stock
   * @param reason
   *          The reason explaining why the message has to be send to the DMQ.
   *          It can be one of the following: <code>EXPIRED</code>,
   *          <code>NOT_WRITEABLE</code>, <code>UNDELIVERABLE</code>,
   *          <code>ADMIN_DELETED</code>, <code>DELETED_DEST</code>,
   *          <code>QUEUE_FULL</code> or <code>UNEXPECTED_ERROR</code>.
   */
  public void addDeadMessage(Message mess, short reason) {

    if (destDmqId != null) {
      
      switch (reason) {
      case EXPIRED:
        mess.setProperty("JMS_JORAM_EXPIRED", Boolean.TRUE);
        mess.setProperty("JMS_JORAM_EXPIRATIONDATE", new Long(mess.expiration));
        break;
      case NOT_WRITEABLE:
        mess.setProperty("JMS_JORAM_NOTWRITABLE", Boolean.TRUE);
        break;
      case UNDELIVERABLE:
        mess.setProperty("JMS_JORAM_UNDELIVERABLE", Boolean.TRUE);
        break;
      case ADMIN_DELETED:
        mess.setProperty("JMS_JORAM_ADMINDELETED", Boolean.TRUE);
        break;
      case DELETED_DEST:
        mess.setProperty("JMS_JORAM_DELETEDDEST", Boolean.TRUE);
        break;
      case QUEUE_FULL:
        mess.setProperty("JMS_JORAM_QUEUEFULL", Boolean.TRUE);
        break;
      case UNEXPECTED_ERROR:
        mess.setProperty("JMS_JORAM_UNEXPECTEDERROR", Boolean.TRUE);
        break;
      default:
        break;
      }

      if (deadMessages == null) {
        deadMessages = new ClientMessages();
      }
      mess.expiration = 0;
      deadMessages.addMessage(mess);
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, this.getClass().getName() + ", addDeadMessage for dmq: " + destDmqId
          + ". Msg: " + mess);
  }

  /**
   * Sends previously stocked messages to the appropriate DMQ.
   */
  public void sendToDMQ() {
    if (deadMessages != null) {
      deadMessages.setExpiration(0);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, this.getClass().getName() + ", sendToDMQ " + destDmqId);
      if (destDmqId != null && !destDmqId.equals(senderId)) {
        Channel.sendTo(destDmqId, deadMessages);
      } else {
        // Else it means that the dead message queue is
        // the queue itself: drop the messages.
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, this.getClass().getName() + ", can't send to itself, messages dropped");
      }
    }
  }
}