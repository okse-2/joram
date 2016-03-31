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

public class ClientRESTSelector extends TestCase {

  public static void main(String[] args) {
    new ClientRESTSelector().run();
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
          .queryParam("selector", "(index > 10) OR (index < 4) AND (p1='value1')")
          .getUri();
      URI uriCreateConsInvalidSelector = target.path("jms").path("queue").path("myQueue1").path("create-consumer")
          .queryParam("name", "cons2")
          .queryParam("selector", "(index > 10) OR (index < 4) AND (p1='value2')")
          .getUri();

      // Create the producer
      Response response = client.target(uriCreateProd).request().accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON).head();
      assertEquals("create-producer jms (myQueue1)", 201, response.getStatus());

      URI uriCloseProd = response.getLink("close-context").getUri();
      URI uriSendNextMsg = response.getLink("send-next-message").getUri();

      String body = "my test message.";
      HashMap<String, Object> maps = new HashMap<>();
      maps.put("type", "TextMessage");
      maps.put("body", body);
      HashMap<String, Object> props = new HashMap<>();
      props.put("p1", "value1");
      props.put("p2", new String[]{"12", Integer.class.getName()});
      props.put("p3", true);
      props.put("p4", new String[]{"1", Byte.class.getName()});
      props.put("p5", new String[]{"true", Boolean.class.getName()});
      props.put("p5", new String[]{"789", Long.class.getName()});
      props.put("index", 3);
      maps.put("properties", props);
      //maps.put("header", props);

      Gson gson = new GsonBuilder().create();
      String json = gson.toJson(maps);

      // Send next message 1
      response = client.target(uriSendNextMsg).request().accept(MediaType.TEXT_PLAIN).post( 
          Entity.entity(json, MediaType.APPLICATION_JSON));
      assertEquals("send-next-message 1", 200, response.getStatus());
      uriSendNextMsg = response.getLink("send-next-message").getUri();
      // Send next message 2
      response = client.target(uriSendNextMsg).request().accept(MediaType.TEXT_PLAIN).post( 
          Entity.entity(json, MediaType.APPLICATION_JSON));
      assertEquals("send-next-message 2", 200, response.getStatus());

      // Create the consumer
      response = client.target(uriCreateCons).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-consumer jms (myQueue1)", 201, response.getStatus());
      URI uriCloseCons = response.getLink("close-context").getUri();

      // receive the message
      URI uriReceive = response.getLink("receive-next-message").getUri();
      Builder builder = client.target(uriReceive).request().accept(MediaType.TEXT_PLAIN);
      response = builder.get();
      String msg = response.readEntity(String.class);
      assertEquals("receive-next-message", 200, response.getStatus());
      assertNotNull("receive-next-message msg", msg);
      assertEquals("receive-next-message", body, msg);
      
      // Create the consumer invalid selector
      response = client.target(uriCreateConsInvalidSelector).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-consumer jms (myQueue1) invalid selector", 201, response.getStatus());
      URI uriCloseConsInvalid = response.getLink("close-context").getUri();

      // receive the message
      uriReceive = response.getLink("receive-next-message").getUri();
      builder = client.target(uriReceive).queryParam("timeout", "0").request().accept(MediaType.TEXT_PLAIN);
      response = builder.get();
      msg = response.readEntity(String.class);
      assertEquals("receive-next-message", 200, response.getStatus());
      assertTrue("receive-next-message msg", (msg == null || msg.isEmpty()));
      
      // close the producer
      response = client.target(uriCloseProd).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-producer", 200, response.getStatus());

      // close the consumer
      response = client.target(uriCloseCons).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-consumer", 200, response.getStatus());
      
      // close the consumer invalid
      response = client.target(uriCloseConsInvalid).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-consumer", 200, response.getStatus());
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}
