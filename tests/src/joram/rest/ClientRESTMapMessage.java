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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import framework.TestCase;

/**
 * Test:
 * - create jms queue (if not exist)
 * - send receive a MapMessage
 *
 */
public class ClientRESTMapMessage extends TestCase {

  public static void main(String[] args) {
    new ClientRESTMapMessage().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);

      ClientConfig config = new ClientConfig();
      Client client = ClientBuilder.newClient(config);
      WebTarget target = client.target(Helper.getBaseJmsURI());

      URI uriCreateProd = target.path("jms").path("queue").path("myQueue1").path("create-producer")
          .queryParam("name", "prod1").getUri();
      URI uriCreateCons = target.path("jms").path("queue").path("myQueue1").path("create-consumer")
          .queryParam("name", "cons1")
          .getUri();

      // Create the producer
      Response response = client.target(uriCreateProd).request().accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON).head();
      assertEquals("create-producer jms (myQueue1)", 201, response.getStatus());

      URI uriCloseProd = response.getLink("close-context").getUri();
      URI uriSendNextMsg = response.getLink("send-next-message").getUri();

      HashMap<String, Object> maps = new HashMap<>();
      maps.put("type", "MapMessage");

      String value1 = "my test message";
      HashMap mapMessage = new HashMap();
      mapMessage.put("key1", value1);
      mapMessage.put("key2", 2);
      maps.put("body", mapMessage);

      HashMap<String, Object> props = new HashMap<>();
      props.put("p1", new String[]{"value1", String.class.getName()});
      props.put("p2", new String[]{"2", Integer.class.getName()});
      maps.put("properties", props);
      //maps.put("header", props);

      Gson gson = new GsonBuilder().create();
      String json = gson.toJson(maps);
      //System.out.println("send json = " + json);

      // Send next message
      response = client.target(uriSendNextMsg).request().accept(MediaType.TEXT_PLAIN).post( 
          Entity.entity(json, MediaType.APPLICATION_JSON));
      assertEquals("send-next-message", 200, response.getStatus());

      // Create the consumer
      response = client.target(uriCreateCons).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-consumer jms (myQueue1)", 201, response.getStatus());

      URI uriCloseCons = response.getLink("close-context").getUri();

      // receive the message
      URI uriReceive = response.getLink("receive-next-message").getUri();
      Builder builder = client.target(uriReceive).request().accept(MediaType.APPLICATION_JSON);
      response = builder.get();
      json = response.readEntity(String.class);
      assertEquals("receive-next-message", 200, response.getStatus());
      assertNotNull("receive-next-message json", json);

      HashMap<String, Object> msg = gson.fromJson(json, HashMap.class);
      assertNotNull("receive-next-message msg", msg);
      String type = (String) msg.get("type");
      assertEquals("receive-next-message type", "MapMessage", type);
      Map jmsBody = (Map) msg.get("body");
      // System.out.println("*** message = " + jmsBody);
      assertEquals("receive-next-message key1", value1, jmsBody.get("key1"));
      Map jmsProperties = (Map) msg.get("properties");
      // System.out.println("*** jmsProperties " + jmsProperties);
      assertNotNull("receive-next-message jmsProperties", jmsProperties);
      Map jmsHeader = (Map) msg.get("header");
      // System.out.println("*** header " + jmsHeader);
      assertNotNull("receive-next-message jmsHeader", jmsHeader);

      // close the producer
      response = client.target(uriCloseProd).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-producer", 200, response.getStatus());

      // close the consumer
      response = client.target(uriCloseCons).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-consumer", 200, response.getStatus());
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      killAgentServer((short)0);
      endTest(); 
    }
  }
}