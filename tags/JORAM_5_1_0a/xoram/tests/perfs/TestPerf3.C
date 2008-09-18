/*
 * XORAM: Open Reliable Asynchronous Messaging
 * Copyright (C) 2007 ScalAgent Distributed Technologies
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
 * Initial developer(s):  ScalAgent Distributed Technologies
 * Contributor(s):
 */
#include <unistd.h>
#include <stdlib.h>

#include "Xoram.H"
#include "Message.H"
#include "Destination.H"
#include "Types.H"
#include "XoramAdmin.H"
#include "AbstractAdminMessage.H"
#include "BaseTestCase.H"
int main(int argc, char *argv[]) {

    boolean prod = TRUE;
    boolean cons = TRUE;
   
 
  try {
    BaseTestCase::startTest(argv);
    // create Admin and connect
    XoramAdmin* admin = new XoramAdmin();
    admin->connect("root", "root", 60);

    // create destination
   
   
    Topic* topic = admin->createTopic("topic");
    printf("topic->getUID() = %s, topic->getName() = %s\n",topic->getUID(), topic->getName());
    // set right
    admin->setFreeReading(topic);
    admin->setFreeWriting(topic);

    // create "anonymous" user
    admin->createUser("anonymous", "anonymous");

    admin->disconnect();
       
    if (argc > 1) {
      if (strcmp(argv[1],"consumer") == 0) {
        cons = TRUE;
        prod = FALSE;
      }
      if (strcmp(argv[1],"producer") == 0) {
        prod = TRUE;
        cons = FALSE;
      }
    }else{
      printf("error : argument not specified : consumer or producer\n");
      BaseTestCase::error(new Exception("argument not specified"));
      BaseTestCase::endTest();
      exit(1);
    }





    ConnectionFactory* cf = new TCPConnectionFactory("localhost", 16010);
    Connection* cnx = cf->createConnection("anonymous", "anonymous");
    cnx->start();
    Session* sess1 = cnx->createSession();
    MessageProducer* prod1 = sess1->createProducer(topic);
    MessageConsumer* cons1 = sess1->createConsumer(topic);

    int nbMessage = 200;
    int nbRound = 3;
    double total = 0L;
    
    char* name =  "prop_name";
    char* value = "my property";
    char* keyInt =  "prop_int";
    char* time =  "time";
    printf("value = %s\n", value);
   
    Message* msg1 = NULL;
    Message* msg = NULL;   

    printf("prod = %x, cons = %x\n",prod,cons);
    long travel=0;
    if (prod == TRUE) 
      sleep(2); // sleep producer during consumer attach to topic

    byte* content = new byte[1024];
	  for (int k = 0; k< 1024; k++)
	    content[k] = (byte) (k & 0xFF);
    for(int j=0; j < nbRound; j++) {
       struct timeval tp;
       gettimeofday(&tp,NULL); 
       double start  = (double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3) ;
     
      for (int i = 0; i < nbMessage; i++) {
	if (prod == TRUE) {
	  msg1 = sess1->createMessage();
	  msg1->setStringProperty(name, value);
	  msg1->setIntProperty(keyInt, i);

	  msg1->body=content;

	  gettimeofday(&tp,NULL); 
	  msg1->setDoubleProperty(time,(double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3) );
	  prod1->send(msg1);
	  if (i % 100 == 1)
	    printf("##### Message sent on topic: %s, prop = %s, i = %i\n", msg1->getMessageID(), msg1->getStringProperty(name), msg1->getIntProperty(keyInt));
	  delete msg1;
	}

	if (cons == TRUE) {
	  msg = cons1->receive();
	  gettimeofday(&tp,NULL); 
	  travel +=(long)( (double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3) -  msg->getDoubleProperty(time));
	 	  
	  if (i % 100 == 1)
	    printf("##### Message received: %s, prop = %s, i = %i\n", msg->getMessageID(), msg->getStringProperty(name), msg->getIntProperty(keyInt));
          delete msg;
	}
      }
      gettimeofday(&tp,NULL); 
      double end =(double(tp.tv_sec)*1e+3 + double(tp.tv_usec)*1e-3) ;

      total += (end - start);
     
    }
    if (cons == TRUE) {
      printf("consumer : %ld msg/s\n",(long)((1000*nbMessage*nbRound)/total));
      printf("mean travel : %ld ms\n",(long)(travel/(nbMessage*nbRound)));
      char *temp=new char[300];
       char *temp2=new char[256];
       strcpy(temp,"consumer : ");
       sprintf(temp2,"%ld",(long)((1000*nbMessage*nbRound)/total));
       strcat(temp,temp2);
       strcat(temp," msg/s\n mean travel : ");
       sprintf(temp2,"%ld",(long)(travel/(nbMessage*nbRound)));
       strcat(temp,temp2);
       strcat(temp," ms");
       BaseTestCase::writeIntoFile(temp);

    }else{
      printf("producer : %ld msg/s\n",(long)((1000*nbMessage*nbRound)/total));
       char *temp=new char[300];
       char *temp2=new char[256];
       strcpy(temp,"producer : ");
       sprintf(temp2,"%ld",(long)((1000*nbMessage*nbRound)/total));
       strcat(temp,temp2);
       strcat(temp," msg/s");
       BaseTestCase::writeIntoFile(temp);
    }
    
    sess1->close();
    cnx->close();

    //delete sess1;
    //delete cnx;
    delete topic;
    delete cf;

  } catch (Exception exc) {
    printf("##### exception - %s", exc.getMessage());
    BaseTestCase::error(&exc);
  } catch (...) {
    printf("##### exception\n");
    BaseTestCase::error(new Exception(" catch ..., unknown exception "));
  }
  printf("##### bye\n");
  if(cons == TRUE) 
    BaseTestCase::endTest();
}













