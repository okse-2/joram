/**
 * (C) 2013 ScalAgent Distributed Technologies
 * All rights reserved
 */
package org.objectweb.joram.mom.util;

public class InMemoryMessageTableFactory extends MessageTableFactory {

  @Override
  public MessageTable createMessageTable(String tableId) {
    return new InMemoryMessageTable();
  }

}
