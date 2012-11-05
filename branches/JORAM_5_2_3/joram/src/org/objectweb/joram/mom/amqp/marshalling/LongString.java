package org.objectweb.joram.mom.amqp.marshalling;

import java.io.IOException;
import java.io.InputStream;

public interface LongString {

  public static final long MAX_LENGTH = 0xffffffffL;

  public long length();

  public InputStream getStream() throws IOException;

  public byte[] getBytes();

}
