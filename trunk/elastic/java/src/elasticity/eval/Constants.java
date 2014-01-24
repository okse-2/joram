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

package elasticity.eval;

class Constants {
	/** 
	 * Size of the produced messages (in bytes).
	 */
	public static final int MSG_SIZE = 1000;
	
	/**
	 * Maximum number a worker can consume per WORKER_PERIOD. 
	 */
	public static final int WORKER_MAX = 100;
	
	/**
	 * Period of time between two worker rounds (in ms).
	 */
	public static final int WORKER_PERIOD = 1000;
	
	/**
	 * Number of producers.
	 */
	public static final int NB_OF_PRODUCERS = 2;
	
	/**
	 * Period of time between two producer rounds (in ms).
	 */
	public static final int PRODUCER_PERIOD = 100;
}
