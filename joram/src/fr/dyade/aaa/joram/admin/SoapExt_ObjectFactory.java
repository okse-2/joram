/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package fr.dyade.aaa.joram.admin;

import fr.dyade.aaa.joram.ConnectionFactory;
import fr.dyade.aaa.joram.FactoryParameters;
import fr.dyade.aaa.joram.soap.SoapConnectionFactory;
import fr.dyade.aaa.joram.soap.QueueSoapConnectionFactory;
import fr.dyade.aaa.joram.soap.TopicSoapConnectionFactory;

import javax.naming.*;


/**
 * The <code>SoapExt_ObjectFactory</code> class extends the
 * <code>ObjectFactory</code> class for re-constructing SOAP connection
 * factories.
 */
public class SoapExt_ObjectFactory extends ObjectFactory
{
  String soap_cf_ClassName = "fr.dyade.aaa.joram.soap.SoapConnectionFactory";
  String soap_qcf_ClassName =
    "fr.dyade.aaa.joram.soap.QueueSoapConnectionFactory";
  String soap_tcf_ClassName =
    "fr.dyade.aaa.joram.soap.TopicSoapConnectionFactory";


  /** Returns an instance of an object given its reference. */
  public Object getObjectInstance(Object obj, Name name, Context ctx,
                                  java.util.Hashtable env) throws Exception
  {
    Object instance = super.getObjectInstance(obj, name, ctx, env);
    
    if (instance != null)
      return instance;

    Reference ref = (Reference) obj;

    if (ref.getClassName().equals(soap_cf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      ConnectionFactory cnxFact =
        new SoapConnectionFactory(host, (new Integer(port)).intValue(),
                                  (new Integer(soapCnxTimeout)).intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(soap_qcf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      ConnectionFactory cnxFact =
        new QueueSoapConnectionFactory(host, (new Integer(port)).intValue(),
                                       (new Integer(soapCnxTimeout))
                                         .intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else if (ref.getClassName().equals(soap_tcf_ClassName)) {
      String host = (String) ref.get("cFactory.host").getContent();
      String port = (String) ref.get("cFactory.port").getContent();
      String cnxTimer = (String) ref.get("cFactory.cnxT").getContent();
      String txTimer = (String) ref.get("cFactory.txT").getContent();
      String soapCnxTimeout =
        (String) ref.get("cFactory.soapCnxT").getContent();
      ConnectionFactory cnxFact =
        new TopicSoapConnectionFactory(host, (new Integer(port)).intValue(),
                                       (new Integer(soapCnxTimeout))
                                         .intValue());
      FactoryParameters params = cnxFact.getParameters();
      params.connectingTimer = (new Integer(cnxTimer)).intValue();
      params.txPendingTimer = (new Integer(txTimer)).intValue();
      return cnxFact;
    }
    else
      return null;
  }
}
