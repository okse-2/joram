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
package org.objectweb.joram.shared.security.jaas;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.acl.Group;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.naming.Context;
import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.objectweb.jonas.security.auth.JPrincipal;
import org.objectweb.jonas.security.auth.JSigned;
import org.objectweb.jonas.security.auth.callback.NoInputCallbackHandler;
import org.objectweb.joram.shared.security.Identity;
import org.objectweb.joram.shared.stream.StreamUtil;
import org.objectweb.util.monolog.api.BasicLevel;

/**
 * JAAS identity class used to authenticate through JOnAS.
 */
public class JonasIdentity extends Identity {
  /** Define serialVersionUID for interoperability. */
  private static final long serialVersionUID = 1L;

  /**
   * Name used in the JOnAS JAAS config file.
   */
  private static final String JAAS_ENTRY_NAME = "ask_remote";
  private static final String KEYSTORE_FILE = "joram.security.jaas.keystoreFile";
  private static final String KEYSTORE_PASS = "joram.security.jaas.keystorePass";
  private static final String KEYSTORE_ALIAS = "joram.security.jaas.alias";
  private static final String UNTESTED_SIGNATURE = "joram.security.jaas.untestedSignature";
  private static final String UNSORT_ROLES = "joram.security.jaas.unsortRoles";

  private String principal;
  private Subject subject = null;
  private LoginContext loginContext;
  private PublicKey publickey = null;

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#setIdentity(java.lang.String, java.lang.String)
   */
  public void setIdentity(String user, String passwd) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JonasIdentity.setIdentity(" + user + ", ****)");

    principal = user;
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, 
          "JonasIdentity.setIdentity principal = " + principal);

    try {
      String jaasEntryName = System.getProperty("joram.security.jaas.entryName", JAAS_ENTRY_NAME);
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "setIdentity: jaasEntryName = "  + jaasEntryName);
      loginContext = new LoginContext(jaasEntryName, new NoInputCallbackHandler(user, passwd));
    } catch (LoginException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION setIdentity::",e);
      throw new Exception(e.getMessage());
    }

    // Negotiate a login via this LoginContext
    try {
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, 
            "JonasIdentity.setIdentity factory initial = " + 
            System.getProperty(Context.INITIAL_CONTEXT_FACTORY)); 
      loginContext.login();     
      subject = loginContext.getSubject();
      if (subject == null) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "No subject for the user " + principal);
        throw new Exception("No subject for the user " + principal);
      }

    } catch (AccountExpiredException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Account expired for the user " + principal, e);
      throw new Exception("Account expired for the user " + principal, e);
    } catch (CredentialExpiredException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Credential expired for the user " + principal, e);
      throw new Exception("Credential expired for the user " + principal, e);
    } catch (FailedLoginException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Failed Login for the user " + principal, e);
      throw new Exception("Failed Login for the user " + principal, e);
    } catch (LoginException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "Login exception for the user " + principal, e);
      throw new Exception("Login exception for the user " + principal, e);
    }
  }

  /**
   * @return
   */
  public Subject getSubject() {
    return subject; 
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#getCredential()
   */
  public Object getCredential() {
    return serializeSubject(subject);
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#getUserName()
   */
  public String getUserName() {
    return principal;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#setUserName(java.lang.String)
   */
  public void setUserName(String userName) {
    this.principal = userName;
  }

  private String getPrincipal() {
    // Retrieve the principal (members of the Group.class)    
    Set principals = subject.getPrincipals(Principal.class);
    Iterator iterator = principals.iterator();
    while (iterator.hasNext()) {
      Principal principal = (Principal) iterator.next();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "getPrincipal class = " + principal.getClass().getName());
      // Signed group (empty group that contains a signature)?
      if (principal instanceof JPrincipal) {
        if (logger.isLoggable(BasicLevel.DEBUG))
          logger.log(BasicLevel.DEBUG, "getPrincipal name = " + ((JPrincipal)principal).getName());
        return principal.getName();
      }
    }
    return null;
  }

  /**
   * @return
   */
  private Object[] getRoles() {
    ArrayList principalRoles = new ArrayList();
    // Retrieve all roles of the user (Roles are members of the Group.class)
    Set principals = subject.getPrincipals(Group.class);
    Iterator iterator = principals.iterator();
    while (iterator.hasNext()) {
      Group group = (Group) iterator.next();
      // Signed group (empty group that contains a signature)?
      if (group instanceof JSigned) {
        continue;
      }
      Enumeration e = group.members();
      while (e.hasMoreElements()) {
        Principal p = (Principal) e.nextElement();
        principalRoles.add(p.getName());
      }
    }
    return principalRoles.toArray();
  }


  /**
   * @return
   */
  private  byte[] getSignature() {
    // Retrieve signature (members of the Group.class)
    Set principals = subject.getPrincipals(Group.class);
    Iterator iterator = principals.iterator();
    while (iterator.hasNext()) {
      Group group = (Group) iterator.next();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "getSignature group = " + group.getClass().getName());
      // Signed group (empty group that contains a signature)?
      if (group instanceof JSigned) {
        return ((JSigned) group).getSignature();
      }
    }
    return null;
  }

  /**
   * @throws Exception
   */
  private synchronized void initPublicKey() throws Exception {
    // Keystore file
    String keystoreFile = System.getProperty(KEYSTORE_FILE);
    if (keystoreFile == null) {
      throw new IllegalStateException("The '" + KEYSTORE_FILE + "' attribute was not found but this attribute is mandatory");
    }

    // Keystore pass
    String keystorePass = System.getProperty(KEYSTORE_PASS);
    if (keystorePass == null) {
      throw new IllegalStateException("The '" + KEYSTORE_PASS + "' attribute was not found but this attribute is mandatory");
    }

    // Alias
    String alias = System.getProperty(KEYSTORE_ALIAS);
    if (alias == null) {
      throw new IllegalStateException("The '" + KEYSTORE_ALIAS + "' attribute was not found but this attribute is mandatory");
    }


    // Check that the file exists
    File f = new File(keystoreFile);
    if (!f.exists()) {
      throw new IllegalStateException("The keystore file named '" + f + "' was not found.");
    }

    // Gets the keystore instance
    KeyStore keyStore = null;
    try {
      keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    } catch (KeyStoreException e) {
      throw new IllegalStateException("Error while getting a keystore ':" + e.getMessage());
    }

    // Load the keystore file
    try {
      keyStore.load(new BufferedInputStream(new FileInputStream(f)), keystorePass.toCharArray());
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Error while loading the keystore file '" + f + "'." + e.getMessage());
    } catch (CertificateException e) {
      throw new IllegalStateException("Error while loading the keystore file '" + f + "'." + e.getMessage());
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("Error while loading the keystore file '" + f + "'." + e.getMessage());
    } catch (IOException e) {
      throw new IllegalStateException("Error while loading the keystore file '" + f + "'." + e.getMessage());
    }

    // Get certificate
    Certificate cert;
    try {
      cert = keyStore.getCertificate(alias);
    } catch (KeyStoreException e) {
      throw new IllegalStateException("Error while getting the alias '" + alias + "' in the keystore file '" + keystoreFile + "':" + e.getMessage());
    }

    // set the public key
    publickey = cert.getPublicKey();
  }

  /**
   * @return
   * @throws Exception
   */
  private PublicKey getPublicKey() throws Exception {
    if (publickey == null)
      initPublicKey();
    return publickey;
  }

  private boolean validate() throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "validate(" + subject + ')');

    if (Boolean.getBoolean(UNTESTED_SIGNATURE)) {
      if (logger.isLoggable(BasicLevel.WARN)) {
        logger.log(BasicLevel.WARN, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.log(BasicLevel.WARN, "!!!!!!!!!!!! untested signature.");
        logger.log(BasicLevel.WARN, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
      }
      return true;
    }

    // Get public key
    PublicKey publickey = getPublicKey();

    return validate(publickey, subject);
  }

  private boolean validate(PublicKey publickey, Subject subject) throws Exception {

    // Build signature with data to validate (principal name + roles)
    Signature signature = null;
    try {
      signature = Signature.getInstance("SHA1withDSA");
    } catch (NoSuchAlgorithmException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: validate", e);
      throw new Exception("Error while getting the algorithm 'SHA1withDSA' :" + e.getMessage());
    }

    try {
      signature.initVerify(publickey);
    } catch (InvalidKeyException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: validate", e);
      throw new Exception("Cannot initialize the signature with the given public key:" + e.getMessage());
    }

    // Add principal name
    String principal = null;
    try {
      //signature.update(principal.getBytes());
      principal = getPrincipal();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "validate principal = " + principal);

      if (principal == null) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: validate principal == null");
        throw new Exception("Cannot add the bytes for the principal name '" + principal + "'");      
      }

      signature.update(principal.getBytes());

    } catch (SignatureException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: validate", e);
      throw new Exception("Cannot add the bytes for the principal name '" + principal + "' :" + e.getMessage());
    }

    // Add roles
    Object[] roles = getRoles();
    if (!Boolean.getBoolean(UNSORT_ROLES)) {
      // Sort roles before adding it to the signature to preserve the order
      Arrays.sort(roles);
    }
    for (int r = 0; r < roles.length; r++) {
      try {
        signature.update(((String)roles[r]).getBytes());
      } catch (SignatureException e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: validate", e);
        throw new Exception("Cannot add the bytes for the role '" + roles[r] + "' : " + e.getMessage());
      }
    }

    // Check signature
    boolean trusted = false;
    try {
      trusted = signature.verify(getSignature());
    } catch (SignatureException e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: validate", e);
      throw new Exception("The signature found in the identity '" + this + "' is invalid:" + e.getMessage());
    }

    // Invalid signature !
    if (!trusted) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "validate trusted = false");
      throw new Exception("The signature for the identity '" + this + "' has been altered by an unknown source.");
    }
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "validate trusted = " + trusted);

    return trusted;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.security.Identity#check(org.objectweb.joram.shared.security.Identity)
   */
  public boolean check(Identity identity) throws Exception {
    if (logger.isLoggable(BasicLevel.DEBUG))
      logger.log(BasicLevel.DEBUG, "JonasIdentity.check(" + identity + ')');

    if (! (identity instanceof JonasIdentity)) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "check : JonasIdentity is not an instance of " + identity);
      throw new Exception("check : JonasIdentity is not an instance of " + identity);
    }

    return validate();
  }

  /**
   * remove unserialized object.
   * @param subject
   * @return
   */
  private Subject serializeSubject(final Subject subject) {
    Subject subjectSer = null;
    try {
      PipedOutputStream pos = new PipedOutputStream();
      PipedInputStream pis = new PipedInputStream(pos);
      final ObjectOutputStream oos = new ObjectOutputStream(pos);
      new Thread() {
        public void run() {
          try {
            oos.writeObject(subject);
            oos.flush();
          } catch (IOException e) {
            if (logger.isLoggable(BasicLevel.ERROR))
              logger.log(BasicLevel.ERROR, "EXCEPTION:: serializeSubject Thread", e);
          }
        }
      }.start();
      ObjectInputStream ois = new ObjectInputStream(pis);
      subjectSer = (Subject) ois.readObject();
      oos.close();
      ois.close();
      if (logger.isLoggable(BasicLevel.DEBUG))
        logger.log(BasicLevel.DEBUG, "serializeSubject subjectSer = " + subjectSer);
    } catch (Exception e) {
      if (logger.isLoggable(BasicLevel.ERROR))
        logger.log(BasicLevel.ERROR, "EXCEPTION:: serializeSubject", e);
    }
    return subjectSer;
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.stream.Streamable#readFrom(java.io.InputStream)
   */
  public void readFrom(InputStream is) throws IOException {
    principal = StreamUtil.readStringFrom(is);
    ByteArrayInputStream bais = null;
    ObjectInputStream ois = null;
    try {
      byte[] subByte = StreamUtil.readByteArrayFrom(is);
      bais = new ByteArrayInputStream(subByte);
      ois = new ObjectInputStream(bais);
      try {
        subject = (Subject) ois.readObject();
      } catch (ClassNotFoundException e) {
        if (logger.isLoggable(BasicLevel.ERROR))
          logger.log(BasicLevel.ERROR, "EXCEPTION:: readFrom", e);
        throw new IOException(e.getMessage());
      }
    } finally {
      try {
        ois.close();
      } catch (IOException exc) {}
      try {
        bais.close();
      } catch (IOException exc) {}
    }
  }

  public String toString() {
    StringBuffer buff = new StringBuffer();
    buff.append("JonasIdentity (");
    buff.append("principal=");
    buff.append(principal);
    buff.append(",subject=");
    buff.append(subject);
    buff.append(')');
    return buff.toString();
  }

  /* (non-Javadoc)
   * @see org.objectweb.joram.shared.stream.Streamable#writeTo(java.io.OutputStream)
   */
  public void writeTo(OutputStream os) throws IOException {
    StreamUtil.writeTo(principal, os);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    try {
      oos.writeObject(subject);
      oos.flush();
      StreamUtil.writeTo(baos.toByteArray(), os);
      oos.close();
      baos.close();
    } finally {
      try {
        oos.close();
      } catch (IOException exc) {}
      try {
        baos.close();
      } catch (IOException exc) {}
    }
  }
}
