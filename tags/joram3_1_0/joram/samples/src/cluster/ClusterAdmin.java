/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package cluster;

import fr.dyade.aaa.joram.admin.*;

import javax.jms.*;
import javax.naming.*;

public class ClusterAdmin
{
  static Context ictx;

  public static void main(String[] args) throws Exception
  {
    System.out.println();
    System.out.println("Cluster administration...");

    Admin admin0 = new Admin("localhost", 16010, "root", "root", 60);
    Admin admin1 = new Admin("localhost", 16011, "root", "root", 60);
    Admin admin2 = new Admin("localhost", 16012, "root", "root", 60);

    User publisher00 = admin0.createUser("publisher00", "publisher00");
    User subscriber10 = admin1.createUser("subscriber10", "subscriber10");
    User subscriber20 = admin2.createUser("subscriber20", "subscriber20"); 
    User subscriber21 = admin2.createUser("subscriber21", "subscriber21");

    ConnectionFactory cf0 = admin0.createConnectionFactory();
    ConnectionFactory cf1 = admin1.createConnectionFactory();
    ConnectionFactory cf2 = admin2.createConnectionFactory();

    Topic t0 = admin0.createTopic("topic0");
    Topic t1 = admin1.createTopic("topic1");
    Topic t2 = admin2.createTopic("topic2");

    admin0.setFreeReading("topic0");
    admin1.setFreeReading("topic1");
    admin2.setFreeReading("topic2");
    admin0.setFreeWriting("topic0");
    admin1.setFreeWriting("topic1");
    admin2.setFreeWriting("topic2");

    Cluster cluster = new Cluster("cluster");
    cluster.addTopic((fr.dyade.aaa.joram.Topic) t0);
    cluster.addTopic((fr.dyade.aaa.joram.Topic) t1);
    cluster.addTopic((fr.dyade.aaa.joram.Topic) t2);

    admin0.createCluster(cluster);
    admin1.createCluster(cluster);
    admin2.createCluster(cluster);

    ictx = new InitialContext();
    ictx.rebind("cf0", cf0);
    ictx.rebind("cf1", cf1);
    ictx.rebind("cf2", cf2);
    ictx.rebind("top0", t0);
    ictx.rebind("top1", t1);
    ictx.rebind("top2", t2);
    ictx.close();

    admin0.close();
    admin1.close();
    admin2.close();
    System.out.println("Admins closed.");
  }
}
