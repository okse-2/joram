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

import java.io.FileInputStream;
import java.net.URI;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.SslConfigurator;

import framework.TestCase;

public class ClientRESTSSL extends TestCase {

  public static void main(String[] args) {
    new ClientRESTSSL().run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);

      javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
          new javax.net.ssl.HostnameVerifier(){
            public boolean verify(String hostname,
                javax.net.ssl.SSLSession sslSession) {
              //System.out.println("== verify " + hostname);
              if (hostname.equals("localhost")) {
                return true;
              }
              return false;
            }
          });

      SslConfigurator sslConfig = SslConfigurator.newInstance()
          .trustStoreFile("./jssecacerts")
          .keyStoreFile("./joram_ks")
          .keyPassword("jorampass");
      SSLContext sslContext = sslConfig.createSSLContext();
      Client client = ClientBuilder.newBuilder().sslContext(sslContext).build();

      WebTarget target = client.target("https://localhost:8443/joram/");

      // lookup the destination
      Builder builder = target.path("jndi").path("queue").request();

      Response response = builder.accept(MediaType.TEXT_PLAIN).get();
      assertEquals("jndi-queue", 201, response.getStatus());

      URI uriCreateProd = response.getLink("create-producer").getUri();
      URI uriCreateCons = response.getLink("create-consumer").getUri();

      // Create the producer
      response = client.target(uriCreateProd).queryParam("user", "anonymous").queryParam("password", "anonymous").request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-producer", 201, response.getStatus());

      URI uriCloseProd = response.getLink("close-context").getUri();
      URI uriSendNextMsg = response.getLink("send-next-message").getUri();

      // Send next message
      String message = "Test message.";
      response = client.target(uriSendNextMsg).request().
          accept(MediaType.TEXT_PLAIN).post(Entity.entity(message, MediaType.TEXT_PLAIN));
      assertEquals("send-next-message", 200, response.getStatus());

      // Create the consumer
      response = client.target(uriCreateCons).request().accept(MediaType.TEXT_PLAIN).head();
      assertEquals("create-consumer", 201, response.getStatus());

      URI uriCloseCons = response.getLink("close-context").getUri();

      // receive the message
      URI uriReceive = response.getLink("receive-next-message").getUri();
      builder = client.target(uriReceive).request().accept(MediaType.TEXT_PLAIN);
      response = builder.get();
      String msg = response.readEntity(String.class);
      assertEquals("receive-next-message", 200, response.getStatus());
      assertNotNull("receive-message", msg);
      assertEquals("receive-message", message, msg);

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

  public static KeyStore loadStore(String trustStoreFile, String password) throws Exception {
    KeyStore store = KeyStore.getInstance("JKS");
    store.load(new FileInputStream(trustStoreFile), password.toCharArray());
    return store;
  }

}
