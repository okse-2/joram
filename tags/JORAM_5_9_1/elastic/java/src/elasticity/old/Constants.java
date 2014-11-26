/*
 *  JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2013 - 2014 ScalAgent Distributed Technologies
 * Copyright (C) 2013 - 2014 Université Joseph Fourier
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
 * Initial developer(s): Université Joseph Fourier
 * Contributor(s): ScalAgent Distributed Technologies
 */

package elasticity.old;

public class Constants {
	
	static final int QUEUE_PERIOD = 1000;
	
	static final int MSG_SIZE = 1000;
	static final int TIME_UNIT = 1000;
	
	// Round sending/receiving
	static final int NB_OF_ROUNDS = 1000;
	static final int MSG_PER_ROUND = 1000;
	
	// Regulated sending/Receiving
	static final int MSG_LOAD = 500; // PER TIME_UNIT
	static final int REG_ROUNDS = 1000;
	
}