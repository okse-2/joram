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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 *
 */
public class SimpleIdentity extends Identity {

  private static final long serialVersionUID = 1L;
  
  private String user;
  private String passwd;
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#getUserName()
   */
  public String getUserName() {
    return user;
  }
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#setUserName(java.lang.String)
   */
  public void setUserName(String userName) {
    this.user = userName;
  }
  
  /**
   * @return password
   */
  public Object getCredential() {
    return passwd;
  }
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#setIdentity(java.lang.String, java.lang.String)
   */
  public void setIdentity(String user, String passwd) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "setIdentity(" + user + ", ****)");
    this.user = user;
    this.passwd = passwd;
  }
  
  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#check(org.objectweb.joram.shared.security.Identity)
   */
  public boolean check(Identity identity) throws Exception  {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "SimpleIdentity.check(" + identity + ')');
    
    if (! (identity instanceof SimpleIdentity)) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "check : SimpleIdentity is not an instance of " + identity);
      throw new Exception("check : SimpleIdentity is not an instance of " + identity);
    }
    
    if (! getUserName().equals(identity.getUserName())) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, 
            "Invalid user [" + classnames[getClassId()] +':' + getUserName() + 
            "] wait [" + classnames[getClassId()] +':' + identity.getUserName() + "]");
      throw new Exception(
          "Invalid user [" + classnames[getClassId()] +':' + getUserName() + 
          "] wait [" + classnames[getClassId()] +':' + identity.getUserName() + "]");
    }
    if (! getCredential().equals(identity.getCredential())) {
      if (getCredential() instanceof String) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "Invalid password for user [" + identity.getUserName() + "]");
        throw new Exception("Invalid password for user [" + identity.getUserName() + "]");
      } else {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, 
              "Invalid authentication class for user [" + identity.getUserName() + 
              "] wait Identity class : " + classnames[getClassId()]);
        throw new Exception(
            "Invalid authentication class for user [" + identity.getUserName() + 
            "] wait Identity class : " + classnames[getClassId()]);
      }
    }
    return true;
  }
  
  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("SimpleIdentity (");
    buff.append("user=");
    buff.append(user);
    buff.append(",password=****");
    //buff.append(passwd);
    buff.append(')');
    return buff.toString();
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#getClassId()
   */
  protected int getClassId() {
    return SIMPLE_IDENTITY;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.stream.Streamable#readFrom(java.io.InputStream)
   */
  public void readFrom(InputStream is) throws IOException {
    user = StreamUtil.readStringFrom(is);
    passwd = StreamUtil.readStringFrom(is);
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "readFrom: user = " + user + ", passwd = ****");
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.stream.Streamable#writeTo(java.io.OutputStream)
   */
  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(user, os);
    StreamUtil.writeTo(passwd, os);
  }
}
