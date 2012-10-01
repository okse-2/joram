package org.ow2.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public interface LongString extends Serializable {

  public static final long MAX_LENGTH = 0xffffffffL;

  public long length();

  public InputStream getStream() throws IOException;

  public byte[] getBytes();

}
