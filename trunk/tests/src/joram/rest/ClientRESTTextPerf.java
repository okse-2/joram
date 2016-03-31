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
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.DatatypeConverter;

import org.glassfish.jersey.client.ClientConfig;

import framework.TestCase;

public class ClientRESTTextPerf extends TestCase {

  ClientConfig config = new ClientConfig();
  Client client = ClientBuilder.newClient(config);
  WebTarget target = client.target(Helper.getBaseJmsURI());
  private final AtomicLong counter = new AtomicLong(1);
  private final AtomicLong failure = new AtomicLong(1);
  boolean DEBUG = false;
  int NB_THREAD = 4;
  int NB_MSG = 100;
  int MODULO = 10;
  
  long duration = 0;
  long nbMsg = 0;
  int nbRun = 0;

  public static void main(final String[] args) {
    ClientRESTTextPerf clientPerf = new ClientRESTTextPerf();
    if (args != null && args.length > 1) {
      clientPerf.NB_THREAD = Integer.parseInt(args[0]);
      clientPerf.NB_MSG = Integer.parseInt(args[1]);
      if (args.length > 2)
        clientPerf.MODULO = Integer.parseInt(args[2]);
      else
        clientPerf.MODULO = clientPerf.NB_MSG;
      if (args.length == 4)
        clientPerf.DEBUG = Boolean.parseBoolean(args[3]);
    }
    clientPerf.run();
  }

  public void run() {
    try {
      startAgentServer((short)0);

      // wait REST bundle
      Helper.waitConnection(Helper.getBaseJmsURI(), 10);

      System.out.println("start TEST: \n\tnb thread " + getNbThread() + ", send/receive " + getNbMessage() + " messages/thread");
      writeIntoFile("nb thread " + getNbThread() + ", send/receive " + getNbMessage() + " messages/thread");
      
      ArrayList<Thread> threads = new ArrayList<>();      
      for (int i = 0; i < getNbThread(); i++) {
        Thread thread = new Thread(new Runnable() {
          @Override
          public void run() {
            long i = counter.getAndIncrement();
            try {
              //Thread.sleep(50);
              createQueue("queue"+i);
              test1(getNbMessage(), getModulo(), "queue"+i, "prod"+i, "cons"+i, "clientID-"+i);
              deleteQueue("queue"+i);
            } catch (Exception e) {
              System.out.println("==== nb failure = " + failure.getAndIncrement() + ", queue" + i + ", prod" + i + " / cons" + i);
              System.out.println("Error: " + e.getMessage());
            }
          }
        });
        thread.start();
        threads.add(thread);
      }
      
      for (Thread t : threads) {
        t.join();//100000);
      }
      
      System.out.println("\tresult: " + nbMsg + " messages in " + getStringDuration(duration/nbRun));
      writeIntoFile("result: " + nbMsg + " messages in " + getStringDuration(duration/nbRun));
      System.out.println("====");
      
    } catch (Throwable exc) {
      exc.printStackTrace();
      error(exc);
    } finally {
      stopAgentServer((short)0);
      endTest(); 
    }
  }

  private int getNbThread() {
    return NB_THREAD;
  }
  
  private int getNbMessage() {
    return NB_MSG;
  }
  
  private int getModulo() {
    return MODULO;
  }
  
  private String getStringDuration(long duration) {
    StringBuffer buff = new StringBuffer();
    long ddays = duration / 86400000;
    if (ddays > 0) {
      duration -= ddays * 86400000;
      buff.append(ddays+"D ");
    }
    long dhours = duration / 3600000;
    if (dhours > 0) {
      duration -= dhours * 3600000;
      buff.append(dhours+"H ");
    }
    long dminutes = duration / 60000;
    if (dminutes > 0) {
      duration -= dminutes * 60000;
      buff.append(dminutes+"M ");
    }
    long dsecs = duration / 1000;
    if (dsecs > 0) {
      duration -= dsecs * 1000;
      buff.append(dsecs+"S ");
    }
    if (duration > 0) {
      buff.append(duration+"ms");
    }
    return buff.toString();
  }
  
  private void setPerfs(String prodName, String consName, int nbMsg, long duration) {
    if (DEBUG) {
      if (prodName != null || consName != null)
        System.out.println(prodName + "/" + consName + ": " + nbMsg + " messages in " + duration + "ms");
      else
        System.out.println(nbMsg + " messages in " + duration + "ms");
    }
    this.nbRun++;
    this.nbMsg += nbMsg;
    this.duration += duration;
  }
  
  public void createQueue(String queueName) throws Exception {
    URI uri = target.path("admin").path("queue").path(queueName).getUri();
    String encodedUserPassword = DatatypeConverter.printBase64Binary("admin:admin".getBytes());
    // Create a queue
    Response response = client.target(uri).queryParam("bind", true).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).get();
    if (response.getStatus() != Response.Status.CREATED.getStatusCode())
      throw new Exception("admin createQueue " + response.getStatus());
  }
  
  public void deleteQueue(String queueName) throws Exception {
    URI uri = target.path("admin").path("queue").path(queueName).getUri();
    String encodedUserPassword = DatatypeConverter.printBase64Binary("admin:admin".getBytes());
    // Create a queue
    Response response = client.target(uri).request().header("Authorization", encodedUserPassword).accept(MediaType.TEXT_PLAIN).delete();
    if (response.getStatus() != Response.Status.OK.getStatusCode())
      throw new Exception("admin deleteQueue " + response.getStatus());
  }
  
  public void test1(int nbMsg, int modulo, String queueName, String prodName, String consName, String clientId) throws Exception {
    Builder builder = target.path("jndi").path(queueName).request();
    Response response = builder.accept(MediaType.TEXT_PLAIN).get();
    if (response.getStatus() != Response.Status.CREATED.getStatusCode())
      throw new Exception("lookup \"" + queueName + "\" = " + response.getStatus());
   
    // URI to create prod/cons
    URI uriCreateCons = response.getLink("create-consumer").getUri();
    URI uriCreateProd = response.getLink("create-producer").getUri();
    // create consumer
    response = createConsumer(uriCreateCons, consName, clientId);
    // URI to close consumer and consume next message
    URI uriCloseCons = response.getLink("close-context").getUri();
    URI uriConsume = response.getLink("receive-next-message").getUri();
    // create producer
    response = createProducer(uriCreateProd, prodName, clientId);
    // URI to close producer and send next message
    URI uriCloseProd = response.getLink("close-context").getUri();
    URI uriSendNext = response.getLink("send-next-message").getUri();
    
    if (DEBUG) {
      System.out.println("uriSend = " + uriSendNext);
      System.out.println("uriConsume = " + uriConsume);
    }
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < nbMsg; i++) {
      uriSendNext = send(uriSendNext, i, modulo);
      uriConsume = consume(uriConsume, i, modulo);
    }
    
    setPerfs(prodName, consName, nbMsg, System.currentTimeMillis() - start);

    response = client.target(uriCloseProd).request().accept(MediaType.TEXT_PLAIN).delete();
//    System.out.println("== close-producer = " + response.getStatus());

    response = client.target(uriCloseCons).request().accept(MediaType.TEXT_PLAIN).delete();
//    System.out.println("== close-consumer = " + response.getStatus());
    //System.out.println("close");
  }

  public void test2(int nbMsg, int modulo, String queueName, String prodName, String consName, String clientId) throws Exception {
    Builder builder = target.path("jndi").path(queueName).request();
    Response response = builder.accept(MediaType.TEXT_PLAIN).get();
    if (response.getStatus() != Response.Status.CREATED.getStatusCode())
      throw new Exception("lookup \"" + queueName + "\" = " + response.getStatus());
   
    // URI to create prod/cons
    URI uriCreateCons = response.getLink("create-consumer").getUri();
    URI uriCreateProd = response.getLink("create-producer").getUri();
    // create consumer
    response = createConsumer(uriCreateCons, consName, clientId);
    // URI to close consumer and consume next message
    URI uriCloseCons = response.getLink("close-context").getUri();
    URI uriConsume = response.getLink("receive-next-message").getUri();
    // create producer
    response = createProducer(uriCreateProd, prodName, clientId);
    // URI to close producer and send next message
    URI uriCloseProd = response.getLink("close-context").getUri();
    URI uriSendNext = response.getLink("send-next-message").getUri();
    
    if (DEBUG) {
      System.out.println("uriSend = " + uriSendNext);
      System.out.println("uriConsume = " + uriConsume);
    }
    
    //send
    long start = System.currentTimeMillis();
    for (int i = 0; i < nbMsg; i++) {
      uriSendNext = send(uriSendNext, i, modulo);
    }
    long deltaProd = System.currentTimeMillis() - start;
    //consume
    start = System.currentTimeMillis();
    for (int i = 0; i < nbMsg; i++) {
      uriConsume = consume(uriConsume, i, modulo);
    }
    long deltaCons = System.currentTimeMillis() - start;
    System.out.println(prodName + ": " + nbMsg + " messages in " + deltaProd + "ms" + ", "
        + consName + ": " + nbMsg + " messages in " + deltaCons + "ms");
    
    response = client.target(uriCloseProd).request().accept(MediaType.TEXT_PLAIN).delete();
//    System.out.println("== close-producer = " + response.getStatus());

    response = client.target(uriCloseCons).request().accept(MediaType.TEXT_PLAIN).delete();
//    System.out.println("== close-consumer = " + response.getStatus());
    //System.out.println("close");
  }

  private Response createConsumer(URI uriCreateCons, String name, String clientId) throws Exception {
    WebTarget target = client.target(uriCreateCons);
    if (clientId != null)
      target = target.queryParam("client-id", clientId);
    if (name != null)
      target = target.queryParam("name", name);
    Response response = target.request().accept(MediaType.TEXT_PLAIN).head();
    if (response.getStatus() != Response.Status.CREATED.getStatusCode())
      throw new Exception("createConsumer = " + response.getStatus() + ", target = " + target);
    return response;
  }
  
  private Response createProducer(URI uriCreateProd, String name, String clientId) throws Exception {
    WebTarget target = client.target(uriCreateProd);
    if (clientId != null)
      target = target.queryParam("client-id", clientId);
    if (name != null)
      target = target.queryParam("name", name);
    Response response = target.request().accept(MediaType.TEXT_PLAIN).head();
    if (response.getStatus() != Response.Status.CREATED.getStatusCode())
      throw new Exception("createProducer = " + response.getStatus() + ", " + target);
    return response;
  }
  
  private URI send(URI uriSendNext, int count, int modulo) throws Exception {
    Response response = client.target(uriSendNext).request()
        .accept(MediaType.TEXT_PLAIN)
        .post(Entity.entity("mon message de test next " + count, MediaType.TEXT_PLAIN));
    if (response.getStatus() != Response.Status.OK.getStatusCode())
      throw new Exception("send-next-message = " + response.getStatus());
    else
      if (DEBUG && count%modulo == 0)
        System.out.println(uriSendNext + ", send "+ count);
    return response.getLink("send-next-message").getUri();
  }
  
  private URI consume(URI uriConsume, int count, int modulo) throws Exception {
    Response response = client.target(uriConsume).request().accept(MediaType.TEXT_PLAIN).get();
    String msg = response.readEntity(String.class);
    if (response.getStatus() == Response.Status.OK.getStatusCode() && msg != null) {
      if (DEBUG &&count%modulo == 0)
        System.out.println(uriConsume + ", receive msg = " + msg);
    } else {
      System.out.println("ERROR consume msg = " + msg);
      System.out.println("== receive-next-message = " + response);
      throw new Exception("receive-next-message = " + response + ", msg = " + msg);
    }
    return response.getLink("receive-next-message").getUri();
  }
}
