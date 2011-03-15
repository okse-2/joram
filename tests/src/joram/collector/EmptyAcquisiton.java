/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 */
package joram.collector;

import java.util.Properties;

import org.objectweb.joram.mom.dest.AcquisitionDaemon;
import org.objectweb.joram.mom.dest.ReliableTransmitter;
import org.objectweb.joram.shared.messages.Message;

public class EmptyAcquisiton implements AcquisitionDaemon {
	private ReliableTransmitter transmitter = null;
	
	/* (non-Javadoc)
	 * @see org.objectweb.joram.mom.dest.AcquisitionDaemon#start(java.util.Properties, org.objectweb.joram.mom.dest.ReliableTransmitter)
	 */
	public void start(Properties properties, ReliableTransmitter transmitter) {
		this.transmitter = transmitter;
		Message message = new Message();
		message.setProperty("collector.status", "start");
		message.setText("EmptyAcquisiton.start");
		transmitter.transmit(message, message.id);
	}

	/* (non-Javadoc)
	 * @see org.objectweb.joram.mom.dest.AcquisitionDaemon#stop()
	 */
	public void stop() {
		Message message = new Message();
		message.setProperty("collector.status", "stop");
		message.setText("EmptyAcquisiton.stop");
		if (transmitter != null)
			transmitter.transmit(message, message.id);
	}

}
