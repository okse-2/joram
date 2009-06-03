/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2008 ScalAgent Distributed Technologies
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
package org.objectweb.joram.shared.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.util.Hashtable;

import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.joram.shared.stream.Streamable;
import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.Debug;

/**
 * Abstract class needed to describe all identities.
 */
public abstract class Identity implements Externalizable, Streamable {
  
  public static Logger logger = Debug.getLogger(Identity.class.getName());
  
  /**
   * Get the user name.
   * @return username.
   */
  public abstract String getUserName();
  
  /**
   * set user name.
   * @param userName
   */
  public abstract void setUserName(String userName);
  
  /**
   * get password or subject in jaas mode.
   * @return password or subject.
   */
  public abstract Object getCredential();
   
  /**
   * set the identity.
   * @param user
   * @param passwd
   * @throws Exception
   */
  public abstract void setIdentity(String user, String passwd) throws Exception;
  
  /**
   * check the identity.
   * @return true if ok
   * @throws Exception
   */
  public abstract boolean check(Identity identity) throws Exception;
  
  /** separate identity class name and root name. */
  private static final String SEPARATE_CHAR = ":";
  
  /**
   * @param rootName 
   * @return identity class name.
   */
  public static String getRootIdentityClass(String rootName) {
    String identityClassName = null;
    
    int index = rootName.indexOf(SEPARATE_CHAR);
    if (index > 0) {
      identityClassName = rootName.substring(0, index);
    } else {
      // default identity
      identityClassName = SimpleIdentity.class.getName();
    }
    
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
                 "getRootIdentityClass: identityClassName = " + identityClassName);
    
    return identityClassName;
  }
  
  /**
   * 
   * @param rootName
   * @return the rootName without Identity class name.
   */
  public static String getRootName(String rootName) {
    String root = rootName;
    int index = rootName.indexOf(SEPARATE_CHAR);
    if (index > 0) {
      root = root.substring(index+1, rootName.length()); 
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,
          "getRootName: rootName = " + root);  
    return root;
  }

  /**
   * Constructs an <code>Identity</code>.
   */
  public Identity() {}

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Interface needed for soap serialization
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  /**
   *
   * @exception IOException
   */
  public Hashtable soapCode() throws IOException {
    Hashtable h = new Hashtable();
    h.put("classname", getClass().getName());

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writeTo(baos);
    baos.flush();
    h.put("bytecontent", baos.toByteArray());
    baos.close();

    return h;
  }

  /**
   *
   * @exception ClassNotFound
   * @exception InstantiationException
   * @exception IllegalAccessException
   * @exception IOException
   */
  public static Object soapDecode(Hashtable h) throws Exception {
    Identity identity = null;
    ByteArrayInputStream bais = null;

    try {
      String classname = (String) h.get("classname");
      identity = (Identity) Class.forName(classname).newInstance();
      byte[] content = (byte[]) h.get("bytecontent");
      bais = new ByteArrayInputStream(content);
      identity.readFrom(bais);
    } finally {
      bais.close();
    }

    return identity;
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Externalizable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  public final void writeExternal(ObjectOutput out) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "Identity.writeExternal");
    
    writeTo((OutputStream) out);
  }

  public final void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "Identity.readExternal");
    
    readFrom((InputStream) in);
  }

  /** ***** ***** ***** ***** ***** ***** ***** *****
   * Streamable interface
   * ***** ***** ***** ***** ***** ***** ***** ***** */

  static public void write(Identity identity,
                           OutputStream os) throws IOException {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG,  "Identity.write: " + identity);
   
    if (identity == null) {
      StreamUtil.writeTo((String) null, os);
      return;
    } else if (identity instanceof SimpleIdentity) {
      StreamUtil.writeTo("", os);
    } else {
      StreamUtil.writeTo(identity.getClass().getName(), os);
    }
    identity.writeTo(os);
  }

  static public Identity read(InputStream is) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
    Identity identity = null;

    String classname = StreamUtil.readStringFrom(is);
    if (classname != null) {
      if (classname.length() == 0) classname = SimpleIdentity.class.getName();
      identity = (Identity) Class.forName(classname).newInstance();
      identity.readFrom(is);
    }

    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "Identity.read: " + identity);

    return identity;
  }
}
