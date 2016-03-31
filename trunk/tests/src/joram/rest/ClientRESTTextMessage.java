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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;

import framework.TestCase;

/**
 * Test: 
 * - lockup queue
 * - create producer
 * - send Text message
 * - send Text message next
 * - create consumer
 * - receive Text message
 * - receive Text message next
 * - close 
 */
public class ClientRESTTextMessage extends TestCase {

  public static void main(String[] args) {
    new ClientRESTTextMessage().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);
      
      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);
      
      ClientConfig config = new ClientConfig();
      Client client = ClientBuilder.newClient(config);
      WebTarget target = client.target(Helper.getBaseJmsURI());

      Builder builder = target.path("jndi").path("queue").request();
      Response response = builder.accept(MediaType.TEXT_PLAIN).get();
      assertEquals("jndi-queue", 201, response.getStatus());

      URI uriCreateCons = response.getLink("create-consumer").getUri();

      response = client.target(response.getLink("create-producer").getUri()).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-producer", 201, response.getStatus());

      URI uriCloseProd = response.getLink("close-context").getUri();

      String message = "my test message";
      String messageNext = "my test message next";
      
      response = client.target(response.getLink("send-message").getUri()).request().
          accept(MediaType.TEXT_PLAIN).post(Entity.entity(message, MediaType.TEXT_PLAIN));
      assertEquals("send-message", 200, response.getStatus());

      response = client.target(response.getLink("send-next-message").getUri()).request().
          accept(MediaType.TEXT_PLAIN).post(Entity.entity(messageNext, MediaType.TEXT_PLAIN));
      assertEquals("send-next-message", 200, response.getStatus());

      response = client.target(uriCreateCons).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-consumer", 201, response.getStatus());

      URI uriCloseCons = response.getLink("close-context").getUri();

      URI uriConsume = response.getLink("receive-message").getUri();
      builder = client.target(uriConsume).request().accept(MediaType.TEXT_PLAIN);
      response = builder.get();
      String msg = response.readEntity(String.class);
      assertEquals(200, response.getStatus());
      assertNotNull("receive-message", msg);
      assertEquals("receive-message", message, msg);

      uriConsume = response.getLink("receive-next-message").getUri();
      builder = client.target(uriConsume).request().accept(MediaType.TEXT_PLAIN);
      response = builder.get();
      msg = response.readEntity(String.class);
      assertEquals(200, response.getStatus());
      assertNotNull("receive-next-message", msg);
      assertEquals("receive-next-message", messageNext, msg);

      response = client.target(uriCloseProd).request().accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("close-producer", 200, response.getStatus());

      response = client.target(uriCloseCons).request().accept(MediaType.TEXT_PLAIN).delete();
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
