/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.util;

import java.io.*;

/**
 *  Serializability of a class is enabled by the class implementing this interface.
 * Serialization is used for persistence and for communication. Each class must implement
 * its own external encoding.
 * @version	v1.1, 14 Jan 97
 * @author	Freyssinet Andr*
 * @see		Agent,
 * @see		AgentId,
 * @see		Notification
 */
public interface SerializAAA {
    /**
     * Serialize the object in the corresponding stream.
     * @param stream    The output stream. By example, it must be support by a
     * 			FileOutputStream for persistence or a ByteOutputStream for
     * 			communication.
     */
    public void Encode(DataOutputStream s) throws IOException;
    /**
     * Unserialize an object from the stream.
     * @param stream    The input stream that contains the object serialisation. It
     * 			must be support by a FileInputStream for persistence or a
     * 			ByteInputStream for communication.
     */
    public void Decode(DataInputStream s) throws IOException;
}
