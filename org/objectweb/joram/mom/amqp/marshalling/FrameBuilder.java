package org.objectweb.joram.mom.amqp.marshalling;

import java.io.IOException;

public interface FrameBuilder {
  
  public Frame toFrame(int channelNumber) throws IOException;

}
