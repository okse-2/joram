/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
