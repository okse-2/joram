/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2016 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent D.T.
 * Contributor(s):
 */
package joram.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import framework.TestCase;

public class JmxREST extends TestCase {

  public static void main(String[] args) {
    new JmxREST().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);

      ClientConfig config = new ClientConfig();
      Client client = ClientBuilder.newClient(config);
      WebTarget target = client.target(Helper.getBaseJmsURI());

      String encodedUserPassword = DatatypeConverter.printBase64Binary("admin:admin".getBytes());

      Gson gson = new GsonBuilder().create();
      URI uri = target.path("jmx").getUri();
      Response response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.APPLICATION_JSON).get();
      String json = response.readEntity(String.class);
      //System.out.println("json = " + json);
      assertEquals("get all MBeans", 200, response.getStatus());
      HashMap<String, String> mbeans = gson.fromJson(json, HashMap.class);
      assertFalse("get all MBeans", mbeans.isEmpty());

      uri = target.path("jmx").path("domains").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.APPLICATION_JSON).get();
      json = response.readEntity(String.class);
      //System.out.println("json = " + json);
      ArrayList<String> domains = gson.fromJson(json, ArrayList.class);
      assertEquals("get domains", 200, response.getStatus());
      assertFalse("get domains", domains.isEmpty());

      uri = response.getLink(domains.get(domains.indexOf("Joram#0"))).getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.APPLICATION_JSON).get();
      json = response.readEntity(String.class);
      //System.out.println("json = " + json);
      HashMap<String, String> objectNames = gson.fromJson(json, HashMap.class);
      assertEquals("get objectNames for the Joram#0 domain", 200, response.getStatus());
      assertNotNull("get objectNames for the Joram#0 domain", objectNames);

      uri = response.getLink(objectNames.get("Joram#0:name=JoramAdminTopic,type=Destination")).getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.APPLICATION_JSON).get();
      json = response.readEntity(String.class);
      //System.out.println("json = " + json);
      HashMap<String, String> attributes = gson.fromJson(json, HashMap.class);
      assertEquals("get attributes (Joram#0:name=JoramAdminTopic,type=Destination)", 200, response.getStatus());
      assertFalse("get attributes (Joram#0:name=JoramAdminTopic,type=Destination)", attributes.isEmpty());

      uri = response.getLink("CreationDate").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
      assertEquals("get attribute CreationDate", 200, response.getStatus());
      assertNotNull("get attribute CreationDate", response.readEntity(String.class));

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      killAgentServer((short)0);
      endTest(); 
    }
  }
}

