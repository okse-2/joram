/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C)  2003 ScalAgent Distributed Technologies
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
 * Initial developer(s):ScalAgent D.T.
 * Contributor(s): 
 */

package a3.base;

import java.io.*;

import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import java.security.SecureRandom;

public class genks {
  public static void main(String args[]) throws Exception  {
   // Creates the keystore.

    // Each peer has an identity that must be locally (but not globally)
    // unique. This identity and its associated public and private keys
    // are stored in a keystore and protected by a password. Each
    // peer also has a name that must be globally unique.
    String identity = "a3server";
    String password = "changeit";
    String name = "com.scalagent.a3server";
    // Create keystore. We must run an external process to create the
    // keystore, because the security APIs don't expose enough
    // functionality to do this inline. I haven't tested this widely enough
    // to know how portable this code is, but it works on everything I
    // tried it on.
    String keystorename = ".keystore";
    File fileKeyStore = new File(keystorename);
    if (fileKeyStore.exists() == false) {
      System.out.println("Creating keystore...");

      String [] cmd = new String []
        {
          System.getProperty("java.home") + File.separator + "bin" + File.separator + "keytool",
          "-genkey",
          "-alias", identity,
          "-keyalg", "RSA",
          "-keysize", "1024",
          "-dname", "CN=" + name,
          "-keystore", keystorename,
          "-keypass", password,
          "-storetype", "JKS",
          "-storepass", password
        };
      Process process = Runtime.getRuntime().exec(cmd);
      process.waitFor();
      if (process.exitValue() != 0)
        System.exit(-1);
    }
  }
}
