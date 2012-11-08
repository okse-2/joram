/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - 2008 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 Dyade
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
 * Contributor(s): Nicolas Tachker (ScalAgent DT)
 */
package fr.dyade.aaa.jndi2.soap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.soap.Constants;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.Call;
import org.apache.soap.rpc.Parameter;
import org.apache.soap.rpc.Response;
import org.apache.soap.server.DeploymentDescriptor;
import org.apache.soap.server.ServiceManagerClient;
import org.objectweb.util.monolog.api.BasicLevel;

import fr.dyade.aaa.jndi2.client.NamingContextImpl;
import fr.dyade.aaa.jndi2.client.Trace;


/**
 * The <code>SoapExt_NamingContextImpl</code> class is an extended
 * <code>NamingContextImpl</code> calling a JNDI SOAP service's methods
 * rather than using a TCP connection for interacting with the JNDI server.
 */
public class SoapExt_NamingContextImpl extends NamingContextImpl {
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
                                   String jndiHost, int jndiPort) throws NamingException {
    super();

    // Building the service URL:
    try {
      serviceUrl = new URL("http://" + soapHost + ":" + soapPort + "/soap/servlet/rpcrouter");
    } catch (MalformedURLException exc) {
      // Should never happen
    }

    // Deploying and starting the service:
    if (Trace.logger.isLoggable(BasicLevel.DEBUG))
      Trace.logger.log(BasicLevel.DEBUG,
                       "Starting the SOAP service on host "
                       + soapHost
                       + " listening on port "
                       + soapPort);
    try {
      ServiceManagerClient smc = new ServiceManagerClient(serviceUrl);
      smc.deploy(getDeploymentDescriptor());
    } catch (Exception exc) {
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
    } catch (Exception exc) {}

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
    Hashtable codedObj = SoapObjectHelper.soapCode(obj);

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));
    params.add(new Parameter("map", Hashtable.class, codedObj, null));

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
    Hashtable codedObj = SoapObjectHelper.soapCode(obj);

    Vector params = new Vector();
    params.add(new Parameter("name", String.class, name, null));
    params.add(new Parameter("map", Hashtable.class, codedObj, null));

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

    Map codedObj = (Map) resp.getReturnValue().getValue();
    return SoapObjectHelper.soapDecode((Hashtable) codedObj);
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
    dd.setProviderClass("fr.dyade.aaa.jndi2.soap.JndiSoapService");
    dd.setScope(DeploymentDescriptor.SCOPE_APPLICATION);
    
    String[] methods = {"init", "bind", "rebind", "lookup", "unbind"};
    dd.setMethods(methods);

    String[] listener = {"org.apache.soap.server.DOMFaultListener"};
    dd.setFaultListener(listener);

    return dd;
  }
}
