package joram.carrieriq;

public class MessageBusRuntimeException extends RuntimeException {
    public MessageBusRuntimeException() {   
    }

    public MessageBusRuntimeException(String s) {
        super(s);
    }

    public MessageBusRuntimeException(Throwable t) {
        super(t);
    }

    public MessageBusRuntimeException(String s, Throwable t) {
        super(s, t);
    }
}
