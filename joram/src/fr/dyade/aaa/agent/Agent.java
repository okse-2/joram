/*
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.agent;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import fr.dyade.aaa.util.*;

/**
 * The <code>Agent</code> class represents the basic component in our model.
 * <i>agents</i> are "reactive" objects which behave according to 
 * "event -> reaction"model: an event embodies a significant state change
 * which one or many agents may react to.<p>
 * Class <code>Agent</code> defines the generic interface and the common
 * behavior for all agents; every agent is an object of a class deriving
 * from class Agent. Agents are the elementary programming and execution
 * entities; they only communicate using notifications through the message
 * bus, and are controlled by the execution engine.<p>
 * The reactive behavior is implemented by function member React, which
 * defines the reaction of the agent when receiving a notification; this
 * function member is called by the execution engine.<p>
 * Agents are persistent objects, and the Agent class realizes a
 * "swap-in/swap-out" mechanism which allows loading (or finding) in main
 * memory the agents to activate, and unloading the agents idle since a while.
 * <p><hr>
 * Agents must be created in two steps: 
 * <ul>
 * <li>locally creating the object in memory (via constructor),
 * <li>configure it (for example via get/set methods),
 * <li>the deploy it .
 * </ul>
 * <p>
 * The following code would then create a simple agent and deploy it:
 * <p><blockquote><pre>
 *     Agent ag = new Agent();
 *     ag.deploy();
 * </pre></blockquote>
 * <p>
 *
 * @author  André Freyssinet
 * 
 * @see Notification
 * @see Engine
 * @see Channel
 */
public abstract class Agent implements Serializable {
  public static final String RCS_VERSION="@(#)$Id: Agent.java,v 1.3 2000-10-05 15:15:19 tachkeni Exp $"; 

  /** This table is used to maintain a list of agents already in memory
   * using the AgentId as primary key.
   */
  static Hashtable agents;
  /** Virtual time counter use in FIFO swap-in/swap-out mechanisms. */
  static long now = 0;
  /** Maximum number of memory loaded agents. */
  static int NbMaxAgents;
  /** Number of agents pinned in memory. */
  static int nbFixedAgents;

  /**
   * Before any agent may be used, the environment, including the hash table,
   * must be initialized with its static init method.
   *
   * @see Server#init
   *
   * @exception Exception	unspecialized exception
   */
  static void init() throws Exception {
    agents = new Hashtable();

    // Initialize 
    if ((Server.isTransient(Server.getServerId())) ||
	(Server.transaction instanceof NullTransaction)) {
      // in order to avoid swap-out.
      NbMaxAgents = Integer.MAX_VALUE;
    } else {
      NbMaxAgents = Integer.getInteger("NbMaxAgents", 100).intValue();
    }

    // Creates or initializes AgentFactory, then loads and initializes
    //  all fixed agents.
    nbFixedAgents = 0;
    AgentFactory factory = null;
    try {
      factory = (AgentFactory) Agent.load(AgentId.factoryId);
      // Initializes factory
      factory.initialize(false);

      // initializes the configured services
      initServices(Server.services, false);

      // loads all fixed agents
      for (Enumeration e = factory.getFixedAgentIdList() ;
	   e.hasMoreElements() ;) {
	AgentId id = (AgentId) e.nextElement();
	try {
	  Agent ag = Agent.load(id);
	} catch (UnknownAgentException exc) {
	  // Should never happen
	  factory.removeFixedAgentId(id);
	}
      }
      factory.save();
    } catch (UnknownAgentException e) {
      // Creates factory and all "system" agents...
      factory = new AgentFactory();
      agents.put(factory.getId(), factory);
      factory.initialize(true);
      AgentAdmin admin = new AgentAdmin();
      factory.createAgent(admin);

      if (Server.isTransient(Server.getServerId())) {
	TransientServer transientServer = new TransientServer();
	factory.createAgent(transientServer);
      } else if (Server.networkServers[Server.getServerId()].transientPort != -1) {
	TransientManager transientManager = new TransientManager();
	factory.createAgent(transientManager);
      }

      // creates the configured services
      initServices(Server.services, true);

      // creates a DebugProxy agent when A3DEBUG_PROXY variable is true
      if (System.getProperty("A3DEBUG_PROXY", "false").equals("true")) {
	// creates the object indirectly so that no dependency against the
	// ip package is created
	try {
	  Class debugProxyClass = Class.forName("fr.dyade.aaa.ip.DebugProxy");
	  Agent debugProxy = (Agent) debugProxyClass.newInstance();
	  factory.createAgent(debugProxy);
	} catch (Exception exc) {
	  Debug.trace("cannot create DebugProxy agent", exc);
	}
      }
      factory.save();
    }
  }

  /**
   * Initializes services described as <code>ServiceDesc</code> objects.
   * Calls the <code>init</code> static function of the service class with
   * the service parameters.
   *
   * @param services		the list of services
   * @param firstTime		true when agent server is starting anew
   *
   * @exception Exception
   *	unspecialized exception
   */
  static void initServices(ServiceDesc services[], boolean firstTime) throws Exception {
    if (services == null)
      return;
    Class ptypes[] = new Class[2];
    ptypes[0] = Class.forName("java.lang.String");
    ptypes[1] = Boolean.TYPE;
    Object args[] = new Object[2];
    args[1] = new Boolean(firstTime);
    for (int i = 0; i < services.length; i ++) {
      try {
	Class srvClass = Class.forName(services[i].className);
	Method srvInit = srvClass.getMethod("init", ptypes);
	args[0] = services[i].parameters;
	srvInit.invoke(null, args);
      } catch (Exception exc) {
	Debug.trace("cannot create service " + services[i].className, exc);
      }
    }
  }

  /**
   *  The <code>garbage</code> method should be called regularly , to swap out
   * from memory all the agents which have not been accessed for a time.
   */
  static void garbage() {
    long deadline = now - NbMaxAgents;
    for (Enumeration e = agents.elements() ; e.hasMoreElements() ;) {
      Agent ag = (Agent) e.nextElement();
      if ((ag.last <= deadline) && (!ag.fixed)) {
	if (Debug.garbageAgent)
	  Debug.trace("Agent garbage " + ag, false);
	agents.remove(ag.id);
      }
    }
  }

  /**
   *  the <code>last</code> variable contains the virtual time of the
   * last access. It is used by swap-out policy.
   *
   * @see garbage
   */
  transient long last;

  /**
   *  The <code>load</code> method return the <code>Agent</code> object
   * designed by the <code>AgentId</code> parameter. If the <code>Agent</code>
   * object is not already present in the server memory, it is loaded from
   * the storage.
   *
   *  Be carefull, if the save method can be overloaded to optimize the save
   * processus, the load procedure used by engine is always Agent.load.
   *
   * @param	id		The agent identification.
   * @return			The corresponding agent.
   *
   * @exception	IOException
   *	If an I/O error occurs.
   * @exception	ClassNotFoundException
   *	Should never happen (the agent has already been loaded in deploy).
   * @exception	UnknownAgentException
   *	There is no correponding agent on secondary storage.
   * @exception Exception
   *	when executing class specific initialization
   */
  static Agent load(AgentId id)
    throws IOException, ClassNotFoundException, Exception {
    now += 1;
    Agent ag = (Agent) agents.get(id);
    if (ag == null) {
      if (agents.size() > (NbMaxAgents + nbFixedAgents))
	garbage();
      
      if ((ag = (Agent) Server.transaction.load(id.toString())) != null) {
	agents.put(ag.id, ag);
	if (Debug.loadAgent)
	  Debug.trace("Agent load " + ag, false);
	// initializes agent
	ag.initialize(false);
      } else {
	if (Debug.loadAgent)
	  Debug.trace("Agent can't load " + id, false);
	throw new UnknownAgentException();
      }
    }

    ag.last = now;
    return ag;
  }

  /**
   * The <code>reload</code> method return the <code>Agent</code> object
   * loaded from the storage.
   *
   * @param	id		The agent identification.
   * @return			The corresponding agent.
   *
   * @exception IOException
   *	when accessing the stored image
   * @exception ClassNotFoundException
   *	if the stored image class may not be found
   * @exception Exception
   *	unspecialized exception
   */
  static Agent reload(AgentId id)
    throws IOException, ClassNotFoundException, Exception {
    Agent ag = (Agent) Server.transaction.load(id.toString());
    if (ag  != null) {
      agents.put(ag.id, ag);
      if (Debug.loadAgent)
	Debug.trace("Agent reload " + ag, false);
      ag.initialize(false);	// initializes agent
    } else {
      if (Debug.loadAgent)
	Debug.trace("Agent can't load " + id, false);
      throw new UnknownAgentException();
    }
    ag.last = now;
    return ag;
  }

  void save() throws IOException {
    Server.transaction.save(this, id.toString());
    if (Debug.saveAgent)
	  Debug.trace("Agent save " + this, false);
  }

  /* ***** ***** ***** ***** ***** ***** ***** ***** */
  //  Declares all fields transient in order to avoid useless
  // description of each during serialization.

  /**
   * Global unique identifier of the agent. Each agent is identified by a
   * unique identifier allowing the agent to be found. The identifiers format
   * is detailed in <a href="AgentId.html">AgentId</a> class.
   */
  transient AgentId id;
  /** Symbolic name of the agent */
  public transient String name;
  /**
   * Some agents must be loaded at any time, this can be enforced by this
   * member variable. If <code>true</code> agent is pinned in memory.
   */
  protected transient boolean fixed;
 
  private static String nullName = "";

  /**
   * If <code>true</code> the agent notifies its listeners when a
   * monitoring event occurs.
   */
  transient boolean monitored = false;

  /**
   * This transient table contains all the EventNot notifications 
   * sent by the agent during a reaction. This table is cleared
   * between each reaction.
   */
  private transient Hashtable eventNots;

  /**
   * This table contains the listeners for each monitoring
   * event type. The key is a <code>String</code> that describes the event
   * type. The value is a <code>RoleMultiple</code>.
   */
  private transient Hashtable mListeners;

  private void writeObject(java.io.ObjectOutputStream out)
    throws IOException {
      out.writeShort(id.from);
      out.writeShort(id.to);
      out.writeInt(id.stamp);
      out.writeUTF(name);
      out.writeBoolean(fixed);
      if(Server.MONITOR_AGENT) {
	  out.writeBoolean(monitored);
	  if(monitored) out.writeObject(mListeners);
      }      
  }

  private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
      id = new AgentId(in.readShort(), in.readShort(), in.readInt());
      if ((name = in.readUTF()).equals(nullName))
	name = nullName;
      fixed = in.readBoolean();
      if(Server.MONITOR_AGENT) {
	  monitored = in.readBoolean();
	  if(monitored) mListeners = (Hashtable)in.readObject();
      }
  }

  /**
   * Allocates a new Agent object. The resulting object <b>is not an agent</b>;
   * before it can react to a notification you must deploy it. This constructor
   * has the same effect as <code>Agent(Server.serverId, null, false)</code>.
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   * @see #deploy()
   */
  public Agent() {
    this(null, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(Server.serverId, null, fixed)</code>.
   * 
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, String, boolean)
   */
  public Agent(boolean fixed) {
    this(null, fixed);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(Server.serverId, name, false)</code>.
   * 
   * @param name  symbolic name
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(String name) {
    this(name, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(Server.serverId, name, fixed)</code>.
   * 
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(String name, boolean fixed) {
    this(Server.serverId, name, fixed);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, null, false)</code>.
   *
   * @param to	  Identication of target agent server
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to) {
    this(to, null, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, name, false)</code>.
   *
   * @param to	  Identication of target agent server
   * @param name  symbolic name
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to, String name) {
    this(to, name, false);
  }

  /**
   * Allocates a new Agent object. This constructor has the same effect
   * as <code>Agent(to, null, fixed)</code>.
   *
   * @param to	  Identication of target agent server
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see Agent#Agent(short, java.lang.String, boolean)
   */
  public Agent(short to, boolean fixed) {
    this(to, null, fixed);
  }

  /**
   * Allocates a new Agent object. The resulting object <b>is not an agent</b>;
   * before it can react to a notification you must deploy it.
   *
   * @param to	  Identication of target agent server
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   *
   * @see #deploy()
   */
  public Agent(short to, String name, boolean fixed) {
    id = new AgentId(to);
    if (name == null)
      this.name = nullName;
    else
      this.name = name;
    this.fixed = fixed;
  }

  // Methods used for promotion of Agents.
//   protected void setFields(Hashtable values) throws Exception {
//     Class c = getClass();
//     for (Enumeration e = values.keys() ; e.hasMoreElements() ;) {
//       String name = (String) e.nextElement();
//       Field field = c.getField(name);
//       field.set(this, values.get(name));
//     }
//   }

//   protected Hashtable getFields() throws Exception {
//     Class c = getClass();
//     Field[] fields = c.getFields();
//     Hashtable values = new Hashtable(fields.length);

//     for (int i=0; i<fields.length; i++) {
//       String n;
//       Object v;

//       try {
// 	n = fields[i].getName();
// 	v = fields[i].get(this);
//       } catch (IllegalAccessException exc) {
// 	continue;
//       }
//       values.put(n, v);
//     }
//     return values;
//   }

//   protected void promote(String cname, AgentId reply) throws Exception {
//     Class c = Class.forName(cname);
//     Agent ag = (Agent) c.newInstance();
//     ag.setFields(getFields());
//     ag.deploy(reply);
//   }

//   protected void promote(short to, String cname, AgentId reply) throws Exception {
//     Class c = Class.forName(cname);

//     Class[] constrType = new Class[1];
//     constrType[0] = Short.class;

//     Constructor constr = c.getDeclaredConstructor(constrType);

//     Object[]constrPar = new Object[3];
//     constrPar[0] = new Short(to);

//     Agent ag = (Agent) constr.newInstance(constrPar);
//     ag.setFields(getFields());
//     ag.deploy(reply);
//   }

  /**
   * Constructor used to build "system" agents like <code>AgentFactory</code>.
   * System agents are created from the <code>agent</code> package. This
   * constructor takes the agent id as a parameter instead of building it.
   *
   * @param name  symbolic name
   * @param fixed if <code>true</code> agent is pinned in memory
   * @param stamp well known stamp
   */
  Agent(String name, boolean fixed, AgentId id) {
    this.id = id;
    if (name == null)
      this.name = nullName;
    else
      this.name = name;
    this.fixed = fixed;
  }

  /**
   * Constructor used to build Well Known Services agents.
   * <p>
   * System agents are created from the <code>agent</code> package.
   * WKS agents are similar to system agents, except that they may be
   * defined in separate packages, and they do not necessarily exist on all
   * agent servers. Their creation is controlled from the configuration file
   * of the agent server.<p>
   * This constructor takes the agent id as a parameter instead of building it.
   * Since the constructor has been made public, the consistency of agent ids
   * allocation must be enforced. This is done by the constructor checking
   * that the id stamp is comprised in the <code>AgentId.MinWKSIdStamp</code>
   * - <code>AgentId.MaxWKSIdStamp</code> interval.
   *
   * @param name	symbolic name
   * @param fixed	if <code>true</code> agent is pinned in memory
   * @param stamp	well known stamp
   */
  public Agent(String name, boolean fixed, int stamp) {
    if (stamp < AgentId.MinWKSIdStamp ||
	stamp > AgentId.MaxWKSIdStamp)
      throw new IllegalArgumentException(
	"well known service stamp out of range: " + stamp);

    this.id = new AgentId(Server.serverId, Server.serverId, stamp);
    if (name == null)
      this.name = nullName;
    else
      this.name = name;
    this.fixed = fixed;
  }

  /**
   * Determines if the currently <code>Agent</code> has already been deployed.
   */
  private boolean deployed = false;

  /**
   * Returns if the currently <code>Agent</code> has already been deployed.
   */
  public boolean isDeployed() {
    return deployed;
  }

  /**
   * Deploys a new <i>agent</i>.
   * It works by sending a notification to a special agent, of class Factory,
   * running on the target agent server. The notification asks for a remote
   * creation of the agent. This solution presents the advantage of reusing
   * the standard communication mechanisms of the agent machine.<p>
   * The whole process involves then the following steps:
   * <ul>
   * <li>serializing the object state,
   * <li>building an <code>AgentCreateRequest</code> notification with the
   *     resulting bytes stream,
   * <li>sending it to the target Factory agent.
   * </ul>
   * In reaction, the factory agent builds the agent in the target server
   * from the serialized image, and saves it into operational storage.
   *
   * @exception IOException
   *	unspecialized exception
   */
  public final void deploy() throws IOException {
    deploy(null);
  }

  /**
   * Deploys a new <i>agent</i>.
   * It works as <a href="#deploy()">deploy()</a> method above; after the
   * agent creation, the Factory agent sends an <code>AgentCreateReply</code>
   * notification.
   *
   * @param reply	agent to reply to
   * @exception IOException
   *	unspecialized exception
   */
  public final void deploy(AgentId reply) throws IOException {
    if (deployed)
      throw new IOException("Already exist");
    //  If we use sendTo agent's method the from field is the agent id, and
    // on reception the from node (from.to) can be false (see NetServerIn).
    Channel.channel.sendTo(new AgentId(id.to, id.to, AgentId.FactoryIdStamp),
			     new AgentCreateRequest(this, reply));
    deployed = true;
  }

  /**
   * Returns a string representation of this agent, including the agent's
   * class, name, global identication, and fixed property.
   *
   * @return	A string representation of this agent. 
   */
  public String toString() {
    return "(" + getClass().getName() + ", " +
                 name + ", " +
                 id.toString() + ", " +
		 fixed + ")";
  }

  /**
   * Returns the global unique identifier of the agent. Each agent is
   * identified by a unique identifier allowing the agent to be found.
   * The identifiers format is detailed in <a href="AgentId.html">AgentId</a>
   * class.
   *
   * @return the global unique identifier of the agent.
   */
  public final AgentId getId() {
    return id;
  }

  /** 
   * Tests if the agent is pinned in memory.
   *
   * @return true if this agent is a pinned in memory; false otherwise.
   * @see fixed
   */
  public final  boolean isFixed() {
    return fixed;
  }

  /**
   * Gives this agent an opportunity to initialize after having been deployed,
   * and each time it is loaded into memory.
   * <p>
   * This function is first called by the factory agent, just after it deploys
   * the agent.
   * <p>
   * This function is used by agents with a <code>fixed</code> field set to
   * <code>true</code> to initialize their transient variables, as it is called
   * each time the agent server is restarted.
   * <p>
   * This function is not declared <code>final</code> so that derived classes
   * may change their reload policy.
   *
   * @param firstTime		true when first called by the factory
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected void initialize(boolean firstTime) throws Exception {
    if (Debug.agentInit)
      Debug.trace("initialize agent " + id, false);
  }

  /**
   * This method sends a notification to the agent which id is given in
   * parameter.
   *
   * @param to   the unique id. of destination <code>Agent</code>.
   * @param not  the notification to send.
   */
  protected final void
  sendTo(AgentId to, Notification not) {
    Channel.channel.sendTo(id, to, not);
  }

  /**
   * This method sends a notification to the agent which id is wrapped
   * in the specified role. It also fires a monitoring event.
   *
   * @param role  the destination <code>Role</code>.
   * @param not   the notification to send.
   */
  protected final void sendTo(Role role, Notification not) {
      if(role == null) return;
      if(Server.MONITOR_AGENT) {
	  if(monitored) {
	      notifyOutputListeners(role.getName(),not);
	  }
      }
      sendTo(role.getListener(),not);
  }
 
  /**
   * Sends a notification to all the agents registered in a role.
   * Also fires a monitoring event.
   *
   * @param role  the destination <code>MultiplRole</code>.
   * @param not   the notification to send.
   */
  protected final void
  sendTo(RoleMultiple role, Notification not) {
    if (role == null)
      return;
    if(Server.MONITOR_AGENT) {
      if(monitored) {
	notifyOutputListeners(role.getName(),not);
      }
    }
    Enumeration to = role.getListeners();
    if (to == null)
      return;
    while (to.hasMoreElements())
      Channel.channel.sendTo(id, (AgentId) to.nextElement(), not);
  }

  /**
   * Permits this agent to destroy it. If necessary (fixed agent by
   * example), its method should be overloaded to work properly.
   */
  protected void delete() {
    sendTo(new AgentId(id.to, id.to, AgentId.FactoryIdStamp),
	   new AgentDeleteRequest());
  }

  protected void setField(SetField not) throws Exception {
    // It seems it's not necessary to protect "id" and "fixed" fields
    // but I can't explain why!!!
    //  if ((not.name.equals("id")) || (not.name.equals("fixed")))
    //     throw new IllegalAccessException("Can't change Field:" + not.name);
 
    // test if a specific setAttribute method exist    
    String MethName;
    byte tmp[] = not.name.getBytes();
    if ((tmp[0] >= 'a') && (tmp[0] <= 'z')){
      // we must set to upcase the first letter of the attribute name
      // the sepecific method convention name is "setAtt", att is the 
      // name of the attribute
      tmp[0] = (byte) (tmp[0] - ('a'-'A'));
      
      MethName=new String ("set"+new String(tmp));
    } else { 
      // make the method name
      MethName = new String ("set"+not.name);
    }
    
    // Search the agent classe for calling the specific method
    Class AgClass = ((Object) this).getClass();
    
    // Search the parameter class of the specific method
    // the classe of the attribute to be set must be an object.
    // the specific method always have one parameter
    Class  Param_class[] = new Class[1];
    Object Param_value[] = new Object[1];
    Param_class[0] = not.value.getClass();
    Param_value[0] = not.value;
		   
    try {    
      // get the method for invoke it
      Method SetMeth = AgClass.getMethod(MethName, Param_class);
      // the result of the method is always void
      SetMeth.invoke(this, Param_value);
    } catch(NoSuchMethodException e){
      // the getmethod failled, we try to use generic setfield
      Field field = getClass().getField(not.name);
      field.set(this, not.value);
    } catch (SecurityException e){
      // the getmethod failled, we try to use generic setfield
      Field field = getClass().getField(not.name);
      field.set(this, not.value);
    }
  }

  /**
   * Method to react to <code>DupRequest</code> notifications. It deploys
   * a copy of current agent on <code>not.to</code> agent server
   * @param not	   notification to react to
   * @param reply  agent to sending creation acknowledge
   *
   * @exception Exception
   *	unspecialized exception
   */
  protected AgentId dup(DupRequest not, AgentId reply) throws Exception {
    AgentId oldId = id;
    AgentId newId = new AgentId(not.to);

    try {
      id = newId;
      deployed = false;
      deploy(reply);
      id = oldId;
    } catch (IOException exc) {
      id = oldId;
      throw exc;
    }

    return newId;
  }
 
  /**
   * Defines the reaction of the agent when receiving a notification. This
   * member function implements the common reactive behavior of an agent, it
   * is called by the execution engine (see <a href="Engine.html">Engine</a>
   * class).
   * <ul>
   * <li>Configuration/Reconfiguration: <code>SetField</code>,
   * <code>DupRequest</code>, <code>DeleteNot</code>
   * <li>Monitoring: <code>SubscribeNot</code>, <code>GetStatusNot</code>, 
   * <li>Error: <code>UnknownAgent</code>, <code>UnknownNotification</code>,
   * <code>ExceptionNotification</code>
   * </ul><p>
   * If there is no corresponding reaction, the agent send an
   * <code>UnknownNotification</code> notification to the sender.
   *
   * @param from	agent sending notification
   * @param not		notification to react to
   *
   * @exception Exception
   *	unspecialized exception
   */
  public void react(AgentId from, Notification not) throws Exception {
    if (not instanceof SetField) {
      try {
	setField((SetField) not);
	if (((SetField) not).reply!= null ) 
	  // we must send a reply to the agent identified by 
	  // reply
	  sendTo(((SetField) not).reply, new SetFieldAck());

      } catch (Exception exc) {
	sendTo(from, new ExceptionNotification(getId(), not, exc));
      }
    } else if (not instanceof DupRequest) {
      AgentId newId = dup((DupRequest) not, null);
      sendTo(from, new DupReply(newId));
    } else if (not instanceof DeleteNot) {
        delete();
        if (((DeleteNot) not).reply!= null ) 
	  // we must send a reply to the agent identified by 
	  // reply
	  sendTo(((DeleteNot) not).reply, new DeleteAck());
    } else if (not instanceof SubscribeNot) {	
	doReact(from,(SubscribeNot)not);
    } else if (not instanceof GetStatusNot) {
	doReact(from,(GetStatusNot)not);
    } else if (not instanceof UnknownAgent) {
      if (Server.MONITOR_AGENT) {
	doReact((UnknownAgent)not);
      }
      if (Debug.agentError)
 	Debug.trace("Agent: " + id + ".react(" + not + ")", false);
    } else if (not instanceof UnknownNotification) {
      if (Debug.agentError)
 	Debug.trace("Agent: " + id + ".react(" + not + ")", false);
    } else if (not instanceof ExceptionNotification) {
      if (Debug.agentError)
 	Debug.trace("Agent: " + id + ".react(" + not + ")", false);
    } else {
      sendTo(from, new UnknownNotification(id, not));
    }
  }

  /**
   *  Static method used for debug and monitoring. It returns an
   * enumeration of all agents loaded.
   */
  static AgentId[] getLoadedAgentIdlist() {
    AgentId list[] = new AgentId[agents.size()];
    int i = 0;
    for (Enumeration e = agents.elements(); e.hasMoreElements() ;)
      list[i++] = ((Agent) e.nextElement()).id;
    return list;
  }

  static String dumpAgent(AgentId id)
    throws IOException, ClassNotFoundException, Exception {
    Agent ag = (Agent) agents.get(id);
    if (ag == null) {
      ag = (Agent) Server.transaction.load(id.toString());
      if (ag == null) return "Agent" + id + " unknown";
      return "Agent" + id + " on disk = " + ag;
    }
    return "Agent" + id + " in memory = " + ag;
  } 

  /**
   * Fires an input event by sending an <code>InputEvent</code>
   * to the listeners that subscribed to the event type. This method is
   * called by the <code>Engine</code>.
   * @param not The received notification.
   * @see TransactionEngine#run
   * @see TransientEngine#run
   */
  void notifyInputListeners(Notification not) {
    String notifTypeName = not.getClass().getName();
    String key = getInputEventKey(notifTypeName);
    RoleMultiple listeners = getListeners(key);
    if(listeners != null) {
      Enumeration enum = listeners.getListeners();
      while(enum.hasMoreElements()) {
	sendEvent((AgentId)enum.nextElement(),new InputReport(not));
      }
    }
  }

  private void sendEvent(AgentId id,MonitoringReport report) {
    if(eventNots == null) eventNots = new Hashtable(10);
    // try to get the EventNot if there is already one.
    EventNot not = (EventNot)eventNots.get(id);
    if(not == null) {
      not = new EventNot(name);
      // Send the notification now in order to have a causal order.
      // Notice that this notification is not monitored.
      sendTo(id,not);
      // Keep the notification if we have more events to send
      // to the listener.
      eventNots.put(id,not);      
    }
    not.events.addElement(report);
  }

  /**
   * Fires an output event by sending an <code>OutputEvent</code>
   * to the listeners that subscribed to the event type. This method is
   * called in the sendTo method.
   * @param role the role name.
   * @param not the sent notification.
   */
  private void notifyOutputListeners(String role,Notification not) {
    String notifTypeName = not.getClass().getName();
    String key = getOutputEventKey(role,notifTypeName);
    RoleMultiple listeners = getListeners(key);
    if(listeners != null) {
      Enumeration enum = listeners.getListeners();
      while(enum.hasMoreElements()) {
	sendEvent((AgentId)enum.nextElement(),new OutputReport(role,not));
      }
    }
  }

  /**
   * Fires a status event by sending a <code>StatusEvent</code>
   * to the listeners that subscribed to the event type. This method is
   * called in the specific agent code. It does nothing if the property
   * <code>monitored</code> is set to false.
   * @param status the name of the status attribute.
   * @param report a monitoring report associated with the event.
   */
  protected final void notifyStatusListeners(String status, Serializable report) {
    if(Server.MONITOR_AGENT) {
      if(!monitored) return;
      String key = getStatusEventKey(status);
      RoleMultiple listeners = getListeners(key);
      if(listeners != null) {
	Enumeration enum = listeners.getListeners();
	while(enum.hasMoreElements()) {
	  sendEvent((AgentId)enum.nextElement(),new StatusReport(status,report));
	}
      }
    }
  }

  /**
   * Clears the EventNot table. This method is called by the 
   * <code>Engine</code> at the end of each reaction.
   * @see TransactionEngine#run
   * @see TransientEngine#run
   */
  void onReactionEnd() {
    if(eventNots != null) eventNots.clear();
  }

  /**
   * Creates a key for the <code>mListeners</code> table.
   * @param notifType the type of the received notification.
   * @return the key as a <code>String</code>.
   */
  private String getInputEventKey(String notifType) {
    return "input/" + notifType;
  }

  /**
   * Creates a key for the <code>mListeners</code> table.
   * @param role the role name.
   * @param notifType the type of the sent notification.
   * @return the key as a <code>String</code>.
   */ 
  private String getOutputEventKey(String role,String notifType) {
    return "output/" + role + "/" + notifType;
  }

  /**
   * Creates a key for the <code>mListeners</code> table.
   * @param status the name of the status attribute.
   * @return the key as a <code>String</code>.
   */  
  private String getStatusEventKey(String status) {
    return "status/" + status;
  }  
 
  /**
   * Returns the property <code>monitored</code>.
   * @return the property <code>monitored</code>.
   */
  public boolean isMonitored() {
    return monitored;
  }

  /**
   * Sets the property <code>monitored</code>.
   * If <code>true</code>, the agent notifies its 
   * monitoring listeners when a monitoring event occurs.
   * @param monitored indicates whether the agent works in monitoring mode
   * or not.
   */
  public void setMonitored(boolean monitored) {
    this.monitored = monitored;
  }

  /**
   * Dispatch the <code>SubscribeNot</code> notifications. 
   */
  private void doReact(AgentId from,SubscribeNot not) {
    if(Server.MONITOR_AGENT) {
      if(not instanceof InputSubscribeNot) {        
	doReact(from,(InputSubscribeNot)not);     
      }
      else if(not instanceof OutputSubscribeNot) {
	doReact(from,(OutputSubscribeNot)not);      
      }
      else if(not instanceof StatusSubscribeNot) {
        doReact(from,(StatusSubscribeNot)not);      
      }
      else if(not.action == SubscribeNot.REMOVE) {
        removeForAllEvents(from);
      }
    }
    else {
      Exception exc = new IllegalStateException("The agent server " + Server.getServerId() + 
						" is not monitored (Server.MONITOR_AGENT=false).");
      sendTo(from,new ExceptionNotification(getId(),not,exc));
    }
  }    

  private void doReact(AgentId from,InputSubscribeNot not) {    
    String key = getInputEventKey(not.notifType);
    subscribe(not.action,key,from);
  }

  private void doReact(AgentId from,OutputSubscribeNot not) {
    String key = getOutputEventKey(not.roleName,not.notifType);
    subscribe(not.action,key,from);
  }

  private void doReact(AgentId from,StatusSubscribeNot not) {
    String key = getStatusEventKey(not.statusName);
    subscribe(not.action,key,from);

    // send a first event in order to initialize the status value
    // for the new monitoring listener.
    Serializable status = getStatus(not.statusName);
    
    // Send a status event to the listener.
    sendEvent(from,new StatusReport(not.statusName,status));
  }

  private void doReact(AgentId from,GetStatusNot not) {
    sendTo(from,new StatusNot(new StatusReport(not.statusName,
					       getStatus(not.statusName))));
  }

  private Serializable getStatus(String statusName) {
    // look for the method getXxx()
    String methName;
    byte tmp[] = statusName.getBytes();
    if ((tmp[0] >= 'a') && (tmp[0] <= 'z')){
      // upcase the first letter of the attribute name.
      tmp[0] = (byte) (tmp[0] - ('a'-'A'));      
      methName=new String ("get"+new String(tmp));
    } 
    else { 
      methName = new String ("get"+statusName);
    }
    
    Class agClass = getClass();
    try {    
      Method setMeth = agClass.getMethod(methName,new Class[0]);
      // Get the status value that must be Serializable.
      Serializable report = (Serializable)setMeth.invoke(this,new Object[0]);
      return report;
    } 
    catch(Exception exc){
      // do nothing as the status value cannot be reached.
      Debug.trace("Agent " + name + ": getStatus(" + statusName + ") raised " + exc,false);
    }
    return null;
  }
    
  private void subscribe(int action,String key,AgentId id) {
      switch(action) {
      case SubscribeNot.ADD:
	  Debug.trace("Agent " + name + ": " + " add listener for " + key,false);
	  addListener(key,id);
	  break;
      case SubscribeNot.REMOVE:
	  Debug.trace("Agent " + name + ": " + " remove listener for " + key,false);
	  removeListener(key,id);
	  break;
      }
  }


  /**
   * Encapsulates the access to the table mListeners in order to check
   * whether it is null or not.
   * @param key describes the event type.
   * @return the listeners in a <code>RoleMultiple</code>.
   */
  private RoleMultiple getListeners(String key) {
    if(mListeners == null) return null;
    return (RoleMultiple)mListeners.get(key);
  }	    

  /**
   * Adds the specified agent as a listener of the specified event type.
   * @param key describes the event type.
   * @param id the listener id.
   */
  private void addListener(String key,AgentId id) {
    if(mListeners == null) {
	mListeners = new Hashtable(10);
	// The agent becomes monitored as soon as there is one 
	// registered listener.
	monitored = true;
    }
    RoleMultiple listeners = (RoleMultiple)mListeners.get(key);
    if(listeners == null) {
      listeners = new RoleMultiple();
      mListeners.put(key,listeners);
    }
    if(!listeners.contains(id))
	listeners.addListener(id);
  }

  /**
   * Removes the specified agent as a listener of the specified event type.
   * @param key describes the event type.
   * @param id the listener id.
   */
  private void removeListener(String key,AgentId id) {
    if(mListeners == null) return;
    RoleMultiple listeners = (RoleMultiple)mListeners.get(key);
    if(listeners != null) {
      listeners.removeListener(id);
      // If the RoleMultiple is empty, remove it from the table.
      if(listeners.getListeners() == null) mListeners.remove(key);
      if(mListeners.isEmpty()) {
	monitored = false;
	mListeners = null;
      }
    }
  }

  /**
   * Removes the specified agent for all the event type.
   * @param id the listener id.
   */
  private void removeForAllEvents(AgentId id) {
    if(mListeners == null) return;
    Enumeration listeners = mListeners.elements();
    Enumeration keys = mListeners.keys();
    while(listeners.hasMoreElements()) {
      Object key = keys.nextElement();
      RoleMultiple rm = (RoleMultiple)listeners.nextElement();
      rm.removeListener(id);
      if(!rm.getListeners().hasMoreElements()) 
	  mListeners.remove(key);
    }
    if(mListeners.isEmpty()) {
      monitored = false;
      mListeners = null;
    }
  }
  
  /**
   * Checks that the notification responsible for the exception
   * was an EventNot. If it was, then the AgentId must be
   * removed from the listener list.
   */
  private void doReact(UnknownAgent not) {
    if(not.not instanceof EventNot) {
      // This means that a listener has disappeared.
      // We have to unsubscribe it.
      removeForAllEvents(not.agent);
    }
  }
}
