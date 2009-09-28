package joram.carrieriq;

/**
 * Implement this interface to consume SqaDataObject messages from
 * the MesssageBus.
 *
 * The user of this class should not need to worry about:
 * - which Destination this object comes from ??? BUG ???
 * - transactions or pooling or threading
 * - persistence or durability
 *
 * If you find that you do need to worry about some of these things,
 * let's talk about them so we can revise the MessageBus API.
 */
public interface ObjectListener {
    public void onObject( Object o );
}
