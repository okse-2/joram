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
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.glassfish.jersey.client.ClientConfig;

import framework.TestCase;

/**
 * Test: REST admin command with authentication
 * create queues, users and connections factories
 */
public class AdminREST extends TestCase {

  public static void main(String[] args) {
    new AdminREST().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);

      ClientConfig config = new ClientConfig();
      Client client = ClientBuilder.newClient(config);
      WebTarget target = client.target(Helper.getBaseAdminURI());

      URI uri = target.path("queue").path("myQueue").getUri();

      String encodedUserPassword = DatatypeConverter.printBase64Binary("admin:admin".getBytes());

      // Create a queue
      Response response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
      assertEquals("admin create queue (myQueue)", 201, response.getStatus());


      //create queue
      uri = target.path("queue").path("myQueue1").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON).post( 
          Entity.entity(
              "{\n" +
                  "    \"name\": \"myqueue\",\n" +
                  "    \"type\": \"0\",\n" +
                  "     \"description\": \"Queue test\"\n" +
                  "}\n", MediaType.APPLICATION_JSON));
      assertEquals("admin create queue (myQueue1)", 201, response.getStatus());

      //create tcp CF
      uri = target.path("tcp").path("create").queryParam("jndi-name", "myCF").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
      assertEquals("admin create tcp CF (myCF)", 201, response.getStatus());

      //create local CF
      uri = target.path("local").path("create").queryParam("jndi-name", "myCF1").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
      assertEquals("admin create local CF (myCF1)", 201, response.getStatus());

      //create user
      uri = target.path("user").path("myUser").queryParam("password", "pass").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
      assertEquals("admin create user (myUser)", 201, response.getStatus());

      //create user
      uri = target.path("user").path("myUser1").queryParam("password", "pass").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON).post( 
          Entity.entity(
              "{\n" +
                  "    \"name\": \"user1\",\n" +
                  "    \"type\": \"0\",\n" +
                  "     \"description\": \"User test\"\n" +
                  "}\n", MediaType.APPLICATION_JSON));
      assertEquals("admin create user (myUser1)", 201, response.getStatus());

      //delete user
      uri = target.path("user").path("myUser1").queryParam("password", "pass").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("admin delete user (myUser1)", 200, response.getStatus());

      //delete queue
      uri = target.path("queue").path("myQueue").getUri();
      response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).delete();
      assertEquals("admin delete queue (myQueue)", 200, response.getStatus());

    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest(); 
    }
  }
}
