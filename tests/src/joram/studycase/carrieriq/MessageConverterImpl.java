package joram.carrieriq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.Serializable;
import java.net.InetAddress;

/**
 * Converts objects to ObjectMessages and populates some standard
 * information required by the MessageBus.  Also converts back.
 *
 * Properties set by MessageBus
 * <ul>
 * <li> <b>producer.hostname</b> the hostname or ip address of the sending machine
 * </ul>
 */
public class MessageConverterImpl {
    protected static String hostname = "(unknown)";

    static
    {
        InetAddress localMachine = null;
        try {
            localMachine = InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        } catch( Exception e ) {
            try {
                // fallback to IP address
                hostname = localMachine.getHostAddress();
            } catch( Exception f ) {
                System.out.println("Unable to determine hostname of local machine." + f);
            }
        }
    }

    public Message toMessage(Object object, Session session )
            throws JMSException, MessageConversionException
    {
        if( ! ( object instanceof Serializable) )
            throw new MessageConversionException("Error trying to send non-serializable object of type " + object.getClass().getName() );

        ObjectMessage message = session.createObjectMessage();

        message.setObject( (Serializable) object );
        message.setStringProperty("producer_hostname", hostname );

        return message;
    }

    public Object fromMessage( Message message )
            throws JMSException, MessageConversionException
    {
        if( ! ( message instanceof ObjectMessage) )
            throw new MessageConversionException("Received message not of type ObjectMessage. Received: " + message.getClass().getName());

        return ((ObjectMessage)message).getObject();
    }
}
