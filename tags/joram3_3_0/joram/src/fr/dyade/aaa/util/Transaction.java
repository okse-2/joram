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

public interface Transaction {
  public static final String RCS_VERSION="@(#)$Id: Transaction.java,v 1.11 2002-12-11 11:27:01 maistrfr Exp $"; 

  static final String separator = "_";

  void init(String path) throws IOException;

  void begin() throws IOException;

  File getDir();
  String[] getList(String prefix);

  void save(Serializable obj, String name) throws IOException;
  Object load(String name) throws IOException, ClassNotFoundException;
  void delete(String name);

  void save(Serializable obj, String dirName, String name) throws IOException;
  Object load(String dirName, String name) throws IOException, ClassNotFoundException;
  void delete(String dirName, String name);

  void commit() throws IOException;
  void rollback() throws IOException;

  void release() throws IOException;

  void stop();
}