//   The contents of this file are subject to the Mozilla Public License
//   Version 1.1 (the "License"); you may not use this file except in
//   compliance with the License. You may obtain a copy of the License at
//   http://www.mozilla.org/MPL/
//
//   Software distributed under the License is distributed on an "AS IS"
//   basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//   License for the specific language governing rights and limitations
//   under the License.
//
//   The Original Code is RabbitMQ.
//
//   The Initial Developers of the Original Code are LShift Ltd., and
//   Cohesive Financial Technologies LLC.
//
//   Portions created by LShift Ltd. and Cohesive Financial
//   Technologies LLC. are Copyright (C) 2007 LShift Ltd. and Cohesive
//   Financial Technologies LLC.; All Rights Reserved.
//
//   Contributor(s): ______________________________________.
//

package org.objectweb.joram.mom.amqp.marshalling;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An object providing access to a LongString.
 * This might be implemeted to read directly from connection
 * socket, depending on the size of the content to be read -
 * long strings may contain up to 4Gb of content.
 * @author david
 */
public interface LongString
{
    public static final long MAX_LENGTH = 0xffffffffL;
    
    /**
     * Get the length of the content of the long string in bytes
     * @return the length in bytes >= 0 <= MAX_LENGTH
     */
    public long length();
    
    /**
     * Get the content stream.
     * Repeated calls to this function return the same stream,
     * which may not support rewind.
     * @return An input stream the reads the content
     * @throws IOException 
     */
    public DataInputStream getStream()
        throws IOException;

    /**
     * Get the content as a byte array.
     * Repeated calls to this function return the same array.
     * This function will fail if getContentLength() > Integer.MAX_VALUE
     * throwing an IllegalStateException.
     * @return the content as an array
     * @throws IOException
     */
    public byte [] getBytes();
    

}
