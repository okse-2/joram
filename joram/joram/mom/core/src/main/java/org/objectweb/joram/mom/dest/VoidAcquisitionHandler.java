/**
 * Copyright (C) 2010 ScalAgent Distributed Technologies
 * All rights reserved.
 */
package org.objectweb.joram.mom.dest;

import java.util.Properties;

/**
 * An acquisition handler that does nothing and can be used, for instance to
 * configure an alias acquisition destination.
 */
public class VoidAcquisitionHandler implements AcquisitionHandler
{
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.joram.mom.dest.AcquisitionHandler#retrieve(org.objectweb
	 * .joram.mom.dest.ReliableTransmitter)
	 */
	@Override
	public void retrieve(ReliableTransmitter transmitter) throws Exception
	{
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.objectweb.joram.mom.dest.AcquisitionHandler#setProperties(java.util
	 * .Properties)
	 */
	@Override
	public void setProperties(Properties properties)
	{
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.objectweb.joram.mom.dest.AcquisitionHandler#close()
	 */
	@Override
	public void close()
	{
		// Nothing to do
	}
}
