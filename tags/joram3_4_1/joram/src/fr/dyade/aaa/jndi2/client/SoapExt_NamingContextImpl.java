/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
package fr.dyade.aaa.jndi2.client;

import fr.dyade.aaa.joram.admin.AdministeredObject;

import org.apache.soap.Constants;
import org.apache.soap.Fault;
import org.apache.soap.SOAPException;
import org.apache.soap.encoding.soapenc.BeanSerializer;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;


/**
 * The <code>SoapExt_NamingContextImpl</code> class is an extended
 * <code>NamingContextImpl</code> calling a JNDI SOAP service's methods
 * rather than using a TCP connection for interacting with the JNDI server.
 */
public class SoapExt_NamingContextImpl extends NamingContextImpl
{
  /** SOAP service's URL. */
  private URL serviceUrl;
  /** Call object used for binding requests. */
  private Call bindCall = null;
  /** Call object used for rebinding requests. */
  private Call rebindCall = null;
  /** Call object used for lookup requests. */
  private Call lookupCall = null;
  /** Call object used for unbinding requests. */
  private Call unbindCall = null;


  /**
   * Constructs a <code>SoapExt_NamingContextImpl</code>, deploys and
   * initializes the JNDI SOAP service.
   *
   * @param soapHost  Host hosting the SOAP service.
   * @param soapPort  SOAP service's port.
   * @param jndiHost  Host hosting the JNDI server.
   * @param jndiPort  JNDI server's port.
   *
   * @exception NamingException  If the SOAP service could not be initialized.
   */
  public SoapExt_NamingContextImpl(String soapHost, int soapPort,
                                   String jndiHost, int jndiPort)
         throws NamingException
  {
    super();

    // Building the service URL:
    try {
      serviceUrl = new URL("http://" + soapHost + ":" + soapPort
                           + "/soap/servlet/rpcrouter");
    }
    catch (MalformedURLException exc) {}

    // Deploying and starting the service:
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "Starting the SOAP service on host "
                                         + soapHost
                                         + " listening on port "
                                         + soapPort);

    try {
      ServiceManagerClient smc = new ServiceManagerClient(serviceUrl);
      smc.deploy(getDeploymentDescriptor());
    }
    catch (Exception exc) {
      NamingException nEx =
        new NamingException("Could not deploy the SOAP service");
      nEx.setRootCause(exc);
      throw nEx;
    }

    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SOAP service deployed.");

    // Initializing the service:
    Call initCall = new Call();
    initCall.setTargetObjectURI("urn:JndiService");
    initCall.setMethodName("init");

    Vector params = new Vector();
    params.add(new Parameter("jndiHost", String.class, jndiHost, null));
    params.add(new Parameter("jndiPort", Integer.class,
                             new Integer(jndiPort), null));
    initCall.setParams(params);

    try {
      Response resp = initCall.invoke(serviceUrl,"");
    }
    catch (Exception exc) {}

    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SOAP service initialized.");
  } 


  /**
   * Binds an object.
   *
   * @exception NamingException  If the binding fails or if the object could
   *              not be coded for the SOAP protocol.
   */
  public void bind(String name, Object obj) throws NamingException
  {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SoapExt_NamingContextImpl.bind("
                                         + name + ',' + obj + ')');

    // Building the binding call if needed:
    if (bindCall == null) {
      bindCall = new Call();
      bindCall.setTargetObjectURI("urn:JndiService");
      bindCall.setMethodName("bind");
      bindCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    }

    // Coding the object:
    Vector codedObj;
    if (obj instanceof AdministeredObject)
      codedObj = ((AdministeredObject) obj).code();
    else
      throw new NamingException("Non codable object: "
                                + obj.getClass().getName());

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));
    params.add(new Parameter("vec", Vector.class, codedObj, null));

    bindCall.setParams(params);

    try {
      Response resp = bindCall.invoke(serviceUrl,"");

      // Check the response.
      if (resp.generatedFault ()) {
        throw new NamingException("The SOAP service failed to process"
                                  + " the call: "
                                  + resp.getFault().getFaultString());
      }
    }
    catch (SOAPException exc) {
      throw new NamingException("The SOAP call failed: " + exc.getMessage());
    }
  }

  /**
   * Rebinds an object.
   *
   * @exception NamingException  If the binding fails or if the object could
   *              not be coded for the SOAP protocol.
   */
  public void rebind(String name, Object obj) throws NamingException
  {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SoapExt_NamingContextImpl.rebind("
                                         + name + ',' + obj + ')');

    // Building the rebinding call if needed:
    if (rebindCall == null) {
      rebindCall = new Call();
      rebindCall.setTargetObjectURI("urn:JndiService");
      rebindCall.setMethodName("rebind");
      rebindCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    }

    // Coding the object:
    Vector codedObj;
    if (obj instanceof AdministeredObject)
      codedObj = ((AdministeredObject) obj).code();
    else
      throw new NamingException("Non codable object: "
                                + obj.getClass().getName());

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));
    params.add(new Parameter("vec", Vector.class, codedObj, null));

    rebindCall.setParams(params);

    try {
      Response resp = rebindCall.invoke(serviceUrl,"");

      // Check the response.
      if (resp.generatedFault ()) {
        throw new NamingException("The SOAP service failed to process"
                                  + " the call: "
                                  + resp.getFault().getFaultString());
      }
    }
    catch (SOAPException exc) {
      throw new NamingException("The SOAP call failed: " + exc.getMessage());
    }
  }

  /**
   * Retrieves an object.
   *
   * @exception NamingException  If the lookup fails or if the object could
   *              not be decoded.
   */
  public Object lookup(String name) throws NamingException
  {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SoapExt_NamingContextImpl.lookup("
                                         + name + ')');

    // Building the lookup call if needed:
    if (lookupCall == null) {
      lookupCall = new Call();
      lookupCall.setTargetObjectURI("urn:JndiService");
      lookupCall.setMethodName("lookup");
      lookupCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    }

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));

    lookupCall.setParams(params);

    Response resp = null;
    try {
      resp = lookupCall.invoke(serviceUrl,"");

      // Check the response.
      if (resp.generatedFault ()) {
        throw new NamingException("The SOAP service failed to process"
                                  + " the call: "
                                  + resp.getFault().getFaultString());
      }
    }
    catch (SOAPException exc) {
      throw new NamingException("The SOAP call failed: " + exc.getMessage());
    }

    Vector codedObj = (Vector) resp.getReturnValue().getValue();
    return AdministeredObject.decode(codedObj);
  }

  /**
   * Unbinds an object.
   *
   * @exception NamingException  If the unbind fails.
   */
  public void unbind(String name) throws NamingException
  {
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG, "SoapExt_NamingContextImpl.unbind("
                                         + name + ')');
    
    // Building the unbind call if needed:
    if (unbindCall == null) {
      unbindCall = new Call();
      unbindCall.setTargetObjectURI("urn:JndiService");
      unbindCall.setMethodName("unbind");
      unbindCall.setEncodingStyleURI(Constants.NS_URI_SOAP_ENC);
    }

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));

    unbindCall.setParams(params);

    Response resp = null;
    try {
      resp = unbindCall.invoke(serviceUrl,"");

      // Check the response.
      if (resp.generatedFault ()) {
        throw new NamingException("The SOAP service failed to process"
                                  + " the call: "
                                  + resp.getFault().getFaultString());
      }
    }
    catch (SOAPException exc) {
      throw new NamingException("The SOAP call failed: " + exc.getMessage());
    }
  }

  /**
   * Method not implemented.
   *
   * @exception NamingException  Systematically.
   */
  public NamingEnumeration list(String name) throws NamingException
  {
    throw new NamingException("Method not implemented.");
  }

  /**
   * Method not implemented.
   *
   * @exception NamingException  Systematically.
   */
  public NamingEnumeration listBindings(String name) throws NamingException
  {
    throw new NamingException("Method not implemented.");
  }

  /**
   * Method not implemented.
   *
   * @exception NamingException  Systematically.
   */
  public Context createSubcontext(String name) throws NamingException
  {
    throw new NamingException("Method not implemented.");
  }

  /**
   * Method not implemented.
   *
   * @exception NamingException  Systematically.
   */
  public void destroySubcontext(String name) throws NamingException
  {
    throw new NamingException("Method not implemented.");
  }


  /**
   * Returns a <code>DeploymentDescriptor</code> describing the JNDI SOAP
   * service.
   */
  private DeploymentDescriptor getDeploymentDescriptor()
  {
    DeploymentDescriptor dd = new DeploymentDescriptor();
    
    dd.setID("urn:JndiService");

    dd.setProviderType(DeploymentDescriptor.PROVIDER_JAVA);
    dd.setProviderClass("fr.dyade.aaa.jndi2.server.JndiSoapService");
    dd.setScope(DeploymentDescriptor.SCOPE_APPLICATION);
    
    String[] methods = {"init", "bind", "rebind", "lookup", "unbind"};
    dd.setMethods(methods);

    String[] listener = {"org.apache.soap.server.DOMFaultListener"};
    dd.setFaultListener(listener);

    return dd;
  }
}
