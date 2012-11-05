/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package perfs;

import javax.jms.*;

/**
 * Implements the <code>javax.jms.MessageListener</code> interface.
 */
public class TopicMsgListener implements MessageListener
{
  private int max;
  private java.io.FileWriter writer;
  private int overall = 50;
  private int counter = 0;
  private double travelT = 0;

  public TopicMsgListener(int max) throws Exception
  {
    this.max = max;
    writer = new java.io.FileWriter("PerfsFile");
  }

  public void onMessage(Message msg)
  {
    counter++;
    try {
      travelT = travelT
                + System.currentTimeMillis()
                - msg.getLongProperty("time");

      if (counter == 50) {
        counter = 0;
        System.out.println("Overall counter: " + overall);
        writer.write("" + overall + "  Mean travel time (ms):  "
                     + (travelT / 50) + "    \n");

        //msg.acknowledge();

        travelT = 0;
        overall = overall + 50;

        if (overall == max + 50) {
          writer.close();
          System.out.println("Writer closed");
          synchronized(this) {
            this.notify();
          }
        }
      }
    }
    catch (Exception exc) {}
  }
}
