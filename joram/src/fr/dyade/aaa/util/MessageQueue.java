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
 * MsgQItemList
 */
class MsgQItemList extends DLList {
    SerializAAA item;

    public MsgQItemList(SerializAAA item)  {
	super();
	this.item = item;
    }

    public MsgQItemList(SerializAAA item, DLList list) {
	super(list);
	this.item = item;
    }
}

/**
 * A First-In-First-Out (FIFO) persistent storage of objects.
 * @version	v1.2, 17 Jan 1997
 * @author	Freyssinet Andr*
 * @see		Channel
 * @see		Engine
 */
public class MessageQueue {
    private static final boolean DEBUG = true;
    private String name;
    private int first, last;
    private DLList list;
  
    public void Restore() throws Exception {
	FileInputStream is;
	DataInputStream dis;

	// Load message queue status from persistent way.
	try {
	    dis = new DataInputStream(new FileInputStream(name));

	    first = dis.readInt();
	    last = dis.readInt();
	} catch (FileNotFoundException exc) {
	    // It's the first time, Create it.
	    FileOutputStream os = new FileOutputStream(name);
	    DataOutputStream dos = new DataOutputStream(os);

	    dos.writeInt(first);
	    dos.writeInt(last);

	    dos.flush();
	    os.close();
 	}

	list = new DLList();

	// Then load all items.
	
	for (int i=first; i++<last;) {
	    try {
		is = new FileInputStream(name + "_" + i);
		dis = new DataInputStream(is);

		// Get the real class.
		int n = dis.readInt();
		char tmp[] = new char[n];
		for (int j=0; j<n; j++)
		    tmp[j] = dis.readChar();
		String cn = new String(tmp);
		// Create an instance and restore it.
		SerializAAA item = (SerializAAA) Class.forName(cn).newInstance();
		item.Decode(dis);
		new MsgQItemList(item, list);
	    } catch (Exception exc) {
		if (DEBUG) System.err.println("MessageQueue.Restore[" + Thread.currentThread() + "]: " + exc);
		throw exc;
	    }
	}
    }
    
    public MessageQueue(String name) throws Exception {
	this.name = name;
	Restore();
    }
  
    public synchronized void Push(SerializAAA item) throws IOException {
	FileOutputStream os;
	DataOutputStream dos;

	// Save new item in message queue.
	os = new FileOutputStream(name + "_" + (++last));
	dos = new DataOutputStream(os);

	String cn = item.getClass().getName();
	dos.writeInt(cn.length());
	dos.writeChars(cn);
	item.Encode(dos);
	
	dos.flush();
	os.close();

	// Update message queue status (first, last).
	// TODO: Keep a RandomAccessFile open.

	os = new FileOutputStream(name);
	dos = new DataOutputStream(os);

	dos.writeInt(first);
	dos.writeInt(last);

	dos.flush();
	os.close();

	new MsgQItemList(item, list);
	notify();
    }
    
    public synchronized SerializAAA Get() {
	while (list.next == list) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	return ((MsgQItemList) list.next).item;
    }

    public synchronized SerializAAA Get(boolean nowait) {
	if (nowait)
	    if (list.next == list)
		return null;
	    else
		return ((MsgQItemList) list.next).item;
	else
	    return Get();
    }
    
    public synchronized void Pop() throws IOException, EmptyQueue {
	FileOutputStream os;
	DataOutputStream dos;

	if (list.next != list) {
	    	    
	    first++;

            File file = new File(name + "_" + first);

	    // Update message queue status (first, last).
	    // TODO: Keep a RandomAccessFile open.

	    os = new FileOutputStream(name);
	    dos = new DataOutputStream(os);

	    dos.writeInt(first);
	    dos.writeInt(last);

	    dos.flush();
	    os.close();

	    // Delete unused item.
	    
	    file.delete();
	
	    list.next.delete();
	} else {
	    if (DEBUG) System.err.println("MessageQueue.Pop[" + Thread.currentThread() + "EmptyQueue");
	    throw new EmptyQueue();
	}
    }
}
