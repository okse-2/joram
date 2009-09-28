package joram.carrieriq;

/**
 * Exception used to force a rollback from the MDB
 */
public class ForceRollbackRuntimeException extends MessageBusRuntimeException 
{
    public ForceRollbackRuntimeException() {
    }

    public ForceRollbackRuntimeException(String msg) {
        super(msg);
    }

    public ForceRollbackRuntimeException(Throwable e) {
        super(e);
    }

    public ForceRollbackRuntimeException(String msg, Throwable e) {
        super(msg, e);
    }

}
