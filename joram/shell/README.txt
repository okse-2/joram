#############################################
###             JORAM Shell              ####
#############################################
By: Scalagent D.T.

I\ Installation

1) Required bundles
  * Apache Felix bundles
    - Remote Shell
  * Apache Felix Gogo bundles:
    - Gogo Runtime
    - Gogo Shell : simple TUI
    - Gogo Commands: provides useful commands
  * JORAM Shell bundles:
    - Shell A3 : A3 commands
    - Shell MOM : MOM commands
    - Shell JNDI : JNDI commands
    
2) Connect to Joram Shell
The Remote Shell bundle allows you to connect to Joram via Telnet. Make sure you have Telnet available.
Command: telnet <host> <port>
RQ: - osgi.shell.telnet.ip : IP address of the server's interface to which telnet connections are authorized.
                             For instance, there are two interfaces:
                             - loopback with IP address 127.0.0.1
                             - eth0 with address 192.168.1.42 linked to the company network
                             If the JORAM server has to be only accessible from the machine on which it runs, set the property to 127.0.0.1. Else, it must be accessible from other computer (such as administration computer), use the 129.168.1.42 address.
                             Take care though, for there is no authentification, make sure only authorized stations have access to the JORAM server.
    - osgi.shell.telnet.port : Port used for the connection

II\ Using JORAM Shell Commands

1) A3 commands
  - joram:a3:close :
  Stops Joram and exit OSGi.
  
  - joram:a3:engineLoad
  Displays the average engine load for the last minute.
  
  - joram:a3:garbageRatio
  Displays the garbage ratio of the transactionnal persistence system.
  
  - joram:a3:info
  Displays info about the engine.
  
  - joram:a3:restartServer
  Stops and restarts the server.
  
  - joram:a3:startServer
  Starts the server, reinitialiazing the configuration.
  
  - joram:a3:stopServer
  Stops the server and resets the configuration.

2) MOM commands
  - joram:mom:create
  Creates a new user or a destination
  
  - joram:mom:delete
  Deletes a user or a destination
  
  - joram:mom:deleteMsg
  Not yet implemented.
  
  - joram:mom:help
  Shows help about a MOM command.
  
  - joram:mom:info
  Gives useful info about topic/queue/subscription.
  
  - joram:mom:list
  Lists users/destinations/queues/topics/subscriptions.
  
  - joram:mom:lsMsg
  Displays messages from a queue or a subscription. A range of indexes can be selected for displaying.
  
  - joram:mom:ping
  Checks whether a JoramAdminTopic exists. This is a temporary implementation.
  
  - joram:mom:queueLoad
  Displays the load of a queue.
  
  - joram:mom:receiveMsg
  Not yet implemented.
  
  - joram:mom:sendMsg
  Not yet implemented.
  
  - joram:mom:subscriptionLoad
  Displays the load of a subscription.

3) JNDI commands
  - joram:jndi:createSubcontext
  - joram:jndi:destroySubcontext
  - joram:jndi:getNamingContext
  - joram:jndi:getStrOwnerId
  - joram:jndi:lookup
  - joram:jndi:setStrOwnerId
  - joram:jndi:unbind

