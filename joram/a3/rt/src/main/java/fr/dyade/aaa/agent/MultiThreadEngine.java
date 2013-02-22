/*
 * Copyright (C) 2001 - 2012 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 */
package fr.dyade.aaa.agent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

// JORAM_PERF_BRANCH: new engine
public class MultiThreadEngine implements Engine, MultiThreadEngineMBean {
  
  public static final int DEFAULT_ENGINE_WORKER_NUMBER = 2;
  
  public static final int DEFAULT_REACT_NUMBER_BEFORE_COMMIT = 0;
  
  //public static final int DEFAULT_QIN_THRESHOLD = 10000;
  
  private Logger logmon;
  
  private String name;
  
  protected boolean started;
  
  /** This table is used to maintain a list of agents already in memory
   * using the AgentId as primary key.
   */
  private Hashtable<AgentId, AgentContext> agents;
  
  /** Virtual time counter use in FIFO swap-in/swap-out mechanisms. */
  private AtomicLong now;
  
  private ExecutorService executorService;
  
  /** Vector containing id's of all fixed agents. */
  private Vector<AgentId> fixedAgentIdList;
  
  /** Logical timestamp information for messages in "local" domain. */
  private int stamp;

  /** Buffer used to optimize */
  private byte[] stampBuf;

  /** True if the timestamp is modified since last save. */
  private boolean modified;
  
  private List<AgentContext> toValidate;
  
  public MultiThreadEngine() throws Exception {
    name = "Engine#" + AgentServer.getServerId();

    // Get the logging monitor from current server MonologLoggerFactory
    logmon = Debug.getLogger(Debug.A3Engine + ".#" + AgentServer.getServerId());

    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " created [" + getClass().getName() + "].");
    
    started = false;
    
    int workerNumber = AgentServer.getInteger("EngineWorkerNumber", DEFAULT_ENGINE_WORKER_NUMBER).intValue();
    executorService = Executors.newFixedThreadPool(workerNumber, new EngineThreadFactory());
    
    toValidate = new ArrayList<AgentContext>();
    
    now = new AtomicLong(0);
    
    modified = false;
    restore();
    if (modified) save();
  }
  
  public String getName() {
    return name;
  }
  
  /**
   * Tests if the engine is alive.
   *
   * @return  true if this <code>MessageConsumer</code> is alive; false
   *    otherwise.
   */
  public boolean isStarted() {
    return started;
  }
  
  public EngineWorker getCurrentWorker() {
    Thread currentThread = Thread.currentThread();
    if (currentThread instanceof EngineThread) {
      EngineThread engineThread = (EngineThread) currentThread;
      return engineThread.getEngineWorker();
    }
    return null;
  }
  
  public boolean isEngineThread() {
    Thread currentThread = Thread.currentThread();
    if (currentThread instanceof EngineThread) {
      return true;
    }
    return false;
  }
  
  public void init() throws Exception {
    // Before any agent may be used, the environment, including the hash table,
    // must be initialized.
    agents = new Hashtable<AgentId, AgentContext>();
    try {
      // Creates or initializes AgentFactory, then loads and initializes
      // all fixed agents.
      fixedAgentIdList = (Vector<AgentId>) AgentServer.getTransaction().load(getName() + ".fixed");
      if (fixedAgentIdList == null) {
        // It's the first launching of this engine, in other case there is
        // at least the factory in fixedAgentIdList.
        fixedAgentIdList = new Vector<AgentId>();
        // Creates factory
        AgentFactory factory = new AgentFactory(AgentId.factoryId);
        AgentContext agentContext = getAgentContextAndCreate(AgentId.factoryId);
        agentContext.setStatus(AgentContext.CREATED);
        agentContext.getWorker().setAgent(factory);
        createAgent(AgentId.factoryId, factory);
        factory.save();
        logmon.log(BasicLevel.INFO, getName() + ", factory created");
      }

      // loads all fixed agents
      for (int i=0; i<fixedAgentIdList.size(); ) {
        try {
          if (logmon.isLoggable(BasicLevel.DEBUG))
            logmon.log(BasicLevel.DEBUG,
                       getName() + ", loads fixed agent" + fixedAgentIdList.elementAt(i));
          AgentId fixedAgentId = (AgentId) fixedAgentIdList.elementAt(i);
          AgentContext agentContext = getAgentContextAndCreate(fixedAgentId);
          if (agentContext == null && agentContext.getWorker().getAgent() == null) {
            Agent fixedAgent = load(fixedAgentId, null);
            agentContext.getWorker().setAgent(fixedAgent);
            agentContext.setStatus(AgentContext.CREATED);
          }
          i += 1;
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     getName() + ", can't restore fixed agent" +  fixedAgentIdList.elementAt(i), exc);
          fixedAgentIdList.removeElementAt(i);
        }
      }
    } catch (IOException exc) {
      logmon.log(BasicLevel.ERROR, getName() + ", can't initialize", exc);
      throw exc;
    }
    
    //averageLoadTask = new EngineAverageLoadTask(AgentServer.getTimer());

    logmon.log(BasicLevel.DEBUG, getName() + ", initialized");
  }

  /**
   * Creates and initializes an agent.
   *
   * @param agent agent object to create
   *
   * @exception Exception
   *  unspecialized exception
   */
  public final void createAgent(AgentId id, Agent agent) throws Exception {
    EngineWorker worker = getCurrentWorker();
    agent.id = id;
    agent.deployed = true;
    agent.agentInitialize(true);
    createAgent(agent, worker);
  }

  public void resetAverageLoad() {
    // TODO Auto-generated method stub
    
  }

  /**
   * Deletes an agent.
   *
   * @param agent agent to delete
   */
  public void deleteAgent(AgentId from) throws Exception {
    EngineWorker worker = getCurrentWorker();
    Agent ag;
    Agent old = worker.agent;
    try {
      AgentContext agentContext = agents.get(from);
      if (agentContext == null) {
        ag = load(from, worker);
      } else {
        ag = agentContext.getWorker().getAgent();
        if (ag == null) {
          ag = load(from, worker);
        }
      }
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", delete Agent" + ag.id + " [" + ag.name + "]");
      AgentServer.getTransaction().delete(ag.id.toString());
    } catch (UnknownAgentException exc) {
      logmon.log(BasicLevel.ERROR,
                 getName() +
                 ", can't delete unknown Agent" + from);
      throw new Exception("Can't delete unknown Agent" + from);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 getName() + ", can't delete Agent" + from, exc);
      throw new Exception("Can't delete Agent" + from);
    }
    if (ag.isFixed())
      removeFixedAgentId(ag.id);
    agents.remove(ag.getId());
    try {
      // Set current agent running in order to allow from field fixed
      // for sendTo during agentFinalize (We assume that only Engine
      // use this method).
      worker.agent = ag;
      ag.agentFinalize(true);
    } catch (Exception exc) {
      logmon.log(BasicLevel.ERROR,
                 "Agent" + ag.id + " [" + ag.name + "] error during agentFinalize", exc);
    } finally {
      worker.agent = old;
    }
  }

  /**
   * Creates and initializes an agent.
   *
   * @param agent agent object to create
   *
   * @exception Exception
   *  unspecialized exception
   */
  private void createAgent(Agent agent, EngineWorker worker) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + ", creates: " + agent);

    if (agent.isFixed()) {
      // Subscribe the agent in pre-loading list.
      addFixedAgentId(agent.getId());
    }
    if (agent.logmon == null)
      agent.logmon = Debug.getLogger(Agent.class.getName());
    agent.save();

    // Memorize the agent creation and ...
    now.incrementAndGet();

    AgentContext agentContext;
    synchronized (agents) {
      agentContext = getAgentContext(agent.getId());
      // TODO: Should check that the context is not null
      agentContext.getWorker().setAgent(agent);
      // TODO: Should check that the status is SEED (otherwise this is an error)
      agentContext.setStatus(AgentContext.CREATED);
    }
    execute(agentContext);
  }
  
  /**
   * Removes an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id  the <code>AgentId</code> of no more used fixed agent.
   */
  private void removeFixedAgentId(AgentId id) throws IOException {
    fixedAgentIdList.removeElement(id);
    AgentServer.getTransaction().save(fixedAgentIdList, getName() + ".fixed");
  }

  /**
   * Adds an <code>AgentId</code> in the <code>fixedAgentIdList</code>
   * <code>Vector</code>.
   *
   * @param id  the <code>AgentId</code> of new fixed agent.
   */
  private void addFixedAgentId(AgentId id) throws IOException {   
    fixedAgentIdList.addElement(id);
    AgentServer.getTransaction().save(fixedAgentIdList, getName() + ".fixed");
  }
  
  /**
   *  The <code>load</code> method return the <code>Agent</code> object
   * designed by the <code>AgentId</code> parameter. If the <code>Agent</code>
   * object is not already present in the server memory, it is loaded from
   * the storage.
   *
   *  Be careful, if the save method can be overloaded to optimize the save
   * process, the load procedure used by engine is always load.
   *
   * @param id    The agent identification.
   * @return      The corresponding agent.
   *
   * @exception IOException
   *  If an I/O error occurs.
   * @exception ClassNotFoundException
   *  Should never happen (the agent has already been loaded in deploy).
   * @exception UnknownAgentException
   *  There is no corresponding agent on secondary storage.
   * @exception Exception
   *  when executing class specific initialization
   */
  private Agent load(AgentId id, EngineWorker worker) throws IOException, ClassNotFoundException, Exception {
    long last = now.incrementAndGet();
    Agent ag = Agent.load(id);
    if (ag == null) {
      throw new UnknownAgentException();
    } else {
      if (worker != null) {
        worker.setAgent(ag);
      }
    }
    reload(ag, worker);
    ag.last = last;
    return ag;
  }
  
  /**
   * The <code>reload</code> method return the <code>Agent</code> object
   * loaded from the storage.
   *
   * @param id    The agent identification.
   * @return      The corresponding agent.
   *
   * @exception IOException
   *  when accessing the stored image
   * @exception ClassNotFoundException
   *  if the stored image class may not be found
   * @exception Exception
   *  unspecialized exception
   */
  private void reload(Agent ag, EngineWorker worker)
  throws IOException, ClassNotFoundException, Exception {
      Agent old;
      if (worker != null) {
        old = worker.agent;
      } else {
        old = null;
      }
      try {
        // Set current agent running in order to allow from field fixed
        // for sendTo during agentInitialize (We assume that only Engine
        // use this method).
        if (worker != null) {
          worker.agent = ag;
        }
        ag.agentInitialize(false);
      } catch (Throwable exc) {
        // AF: May be we have to delete the agent or not to allow
        // reaction on it.
        logmon.log(BasicLevel.ERROR,
                   getName() + "Can't initialize Agent" + ag.id + " [" + ag.name + "]",
                   exc);
        throw new Exception(getName() + "Can't initialize Agent" + ag.id);
      } finally {
        if (worker != null) {
          worker.agent = old;
        }
      }
      if (ag.logmon == null)
        ag.logmon = Debug.getLogger(fr.dyade.aaa.agent.Debug.A3Agent +
                                    ".#" + AgentServer.getServerId());
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + "Agent" + ag.id + " [" + ag.name + "] loaded");
  }
  
  public final void push(AgentId to, Notification not) {
    EngineWorker w = getCurrentWorker();
    if (w != null) {
      w.push(to, not);
    } else {
      Channel.channel.directSendTo(AgentId.localId, to, not);
    }
  }
  
  public final void push(AgentId from, AgentId to, Notification not) {
    EngineWorker w = getCurrentWorker();
    if (w != null) {
      w.push(from, to, not);
    } else {
      Channel.channel.directSendTo(from, to, not);
    }
  }
  
  /**
   *  Returns a string representation of the specified agent. If the agent
   * is not present it is loaded in memory, be careful it is not initialized
   * (agentInitialize) nor cached in agents vector.
   *
   * @param id  The agent's unique identification.
   * @return  A string representation of specified agent.
   */
  public String dumpAgent(AgentId id) throws IOException, ClassNotFoundException {
    AgentContext ctx = getAgentContext(id);
    Agent ag;
    if (ctx == null) {
      ag = Agent.load(id);
      if (ag == null) {
        return id.toString() + " unknown";
      }
    } else {
      ag = ctx.getWorker().getAgent();
    }
    return ag.toString();
  }
  
  class EngineWorker implements Runnable {
    
    private AgentId agentId;
    
    /**
     * Queue of messages to be delivered to local agents.
     */ 
    private ConcurrentLinkedMessageQueue qin;
    
    /**
     * The agent owned by this worker.
     */ 
    private Agent agent;

    private List<Message> reactMessageList;
    
    private boolean running;
    
    private Message currentMessage;
    
    private List<Message> mq;
    
    private boolean persistentPush;
    
    private boolean beginTransaction;
    
    EngineWorker(AgentId agentId) {
      this.agentId = agentId;
      qin = new ConcurrentLinkedMessageQueue(agentId.toString());
      mq = new ArrayList<Message>();
      persistentPush = false;
      beginTransaction = false;
      reactMessageList = new ArrayList<Message>();
    }
     
    public ConcurrentLinkedMessageQueue getQin() {
      return qin;
    }
    
    public void execute() {
      if (!running) {
        running = true;
        executorService.execute(this);
      }
    }

    public Agent getAgent() {
      return agent;
    }

    public void setAgent(Agent agent) {
      this.agent = agent;
    }

    public AgentId getAgentId() {
      return agentId;
    }

    public void run() {
      Thread currentThread = Thread.currentThread();
      EngineThread engineThread = (EngineThread) currentThread;
      engineThread.setEngineWorker(this);
      try {
        main_loop:
          while (true) {
            Message msg;
            
            // Get a notification, then execute the right reaction.
            msg = qin.pop();
            if (msg == null) {
              if (reactMessageList.size() > 0) {
                // Commit all changes then test again
                commit();
              }
              synchronized (qin) {
                msg = qin.pop();
                if (msg == null) {
                  running = false;
                  return;
                }
              }
            }
            currentMessage = msg;
            if ((msg.from == null) || (msg.to == null) || (msg.not == null)) {
              // The notification is malformed.
              logmon.log(BasicLevel.ERROR,
                         AgentServer.getName() + ": Bad message [" +
                         msg.from + ", " + msg.to + ", " + msg.not + ']');
              // .. then deletes it ..
              msg.delete();
              // .. and frees it.
              msg.free();

              continue;
            }

            if ((msg.not.expiration <= 0L) ||
                (msg.not.expiration >= System.currentTimeMillis())) {
              // The message is valid, try to load the destination agent
              try {
                if (agent == null) {
                  agent = load(msg.to, this);
                }
                // Else teh agent is already loaded as there is no agent garbage
              } catch (UnknownAgentException exc) {
                //  The destination agent don't exists, send an error
                // notification to sending agent.
                logmon.log(BasicLevel.ERROR,
                           getName() + ": Unknown agent, " + msg.to + ".react(" +
                           msg.from + ", " + msg.not + ")");
                //agent = null;
                push(AgentId.localId, msg.from, new UnknownAgent(msg.to, msg.not));
              } catch (Exception exc) {
                //  Can't load agent then send an error notification
                // to sending agent.
                logmon.log(BasicLevel.ERROR,
                           getName() + ": Can't load agent, " + msg.to + ".react(" +
                           msg.from + ", " + msg.not + ")",
                           exc);
                //agent = null;
                // Stop the AgentServer
                AgentServer.stop(false);
                break main_loop;
              }
            } else {
              if (msg.not.deadNotificationAgentId != null) {
                if (logmon.isLoggable(BasicLevel.DEBUG)) {
                  logmon.log(BasicLevel.DEBUG,
                             getName() + ": forward expired notification " +
                             msg.from + ", " + msg.not + " to " +
                             msg.not.deadNotificationAgentId);
                }
                ExpiredNot expiredNot = new ExpiredNot(msg.not, msg.from, msg.to);
                push(AgentId.localId, msg.not.deadNotificationAgentId, expiredNot);
              } else {
                if (logmon.isLoggable(BasicLevel.DEBUG)) {
                  logmon.log(BasicLevel.DEBUG,
                             getName() + ": removes expired notification " +
                             msg.from + ", " + msg.not);
                }
              }
            }

            if (agent != null) {
              if (logmon.isLoggable(BasicLevel.DEBUG))
                logmon.log(BasicLevel.DEBUG,
                           getName() + ": " + agent + ".react(" + msg.from + ", " + msg.getStamp() + ", " + msg.not + ")");
             
              try {
                agent.react(msg.from, msg.not);
                agent.reactNb += 1;
              } catch (Exception exc) {
                logmon.log(BasicLevel.ERROR,
                           getName() + ": Uncaught exception during react, " + agent + ".react(" + msg.from + ", " + msg.not + ")",
                           exc);
                // Stop the AgentServer
                AgentServer.stop(false);
                break main_loop;
              }
            }
                 
            boolean updatedAgent = agent.isUpdated();
            if (msg.not.persistent == true || updatedAgent || persistentPush) {
              beginTransaction = true;
            }
            reactMessageList.add(currentMessage);
            if (reactMessageList.size() > DEFAULT_REACT_NUMBER_BEFORE_COMMIT) {
              commit();
            }
            
            //else if (currentMessage.not.priority == 9) {
            //  commit();
            //}
            currentMessage = null;
            /*
            if (reactMessageList.size() > DEFAULT_QIN_THRESHOLD / 2) {
              synchronized (qin) {
                qin.notifyAll();
              }
            }
            */
          }
      } catch (Throwable exc) {
        //  There is an unrecoverable exception during the transaction
        // we must exit from server.
        logmon.log(BasicLevel.FATAL, getName() + ": Transaction problem", exc);
        //canStop = false;
        // Stop the AgentServer
        AgentServer.stop(false);
        terminate();
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + " stopped.");
      }
    }
    
    protected void onTimeOut() throws Exception {}
    
    final void push(AgentId to, Notification not) {
      AgentId from = agent.getId();
      push(from, to, not);
    }
    
    /**
     * Push a new message in temporary queue until the end of current reaction.
     * As this method is only  called by engine's thread it does not need to be
     * synchronized.
     */
    final void push(AgentId from,
                    AgentId to,
                    Notification not) {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG,
                   getName() + ", push(" + from + ", " + to + ", " + not + ")");
      if ((to == null) || to.isNullId())
        return;
      if (not.persistent) {
        persistentPush = true;
      }
      mq.add(Message.alloc(from, to, not));
    }
    
    /**
     * Commit the agent reaction in case of right termination:<ul>
     * <li>suppress the processed notification from message queue,
     * then deletes it ;
     * <li>push all new notifications in qin and qout, and saves them ;
     * <li>saves the agent state ;
     * <li>then commit the transaction to validate all changes.
     * </ul>
     */
    private void commit() throws Exception {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ": commit()");
      if (agent != null) agent.save();
      // JORAM_PERF_BRANCH:
      if (beginTransaction) {
        AgentServer.getTransaction().begin();
        for (Message msg : reactMessageList) {
          // Deletes the message
          msg.delete();
          // .. and frees it.
          msg.free();
        }
        // Post all notifications temporary kept in mq to the right consumers,
        // then saves changes.
        dispatch();
        // Saves the agent state then commit the transaction.
        AgentServer.getTransaction().commit(false);
        // The transaction has committed, then validate all messages.
        Channel.validate();
        AgentServer.getTransaction().release();
      } else {
        for (Message msg : reactMessageList) {
          msg.delete();
          msg.free();
        }
        // dispatch
        for (Message sentMsg : mq) {
          if (sentMsg.from == null) sentMsg.from = AgentId.localId;
          MessageConsumer cons = AgentServer.getConsumer(sentMsg.to.getTo());
          cons.postAndValidate(sentMsg);
        }
        mq.clear();
      }
      reactMessageList.clear();
      persistentPush = false;
      beginTransaction = false;
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ": committed");
    }
    
    /**
     * Dispatch messages between the <a href="MessageConsumer.html">
     * <code>MessageConsumer</code></a>: <a href="Engine.html">
     * <code>Engine</code></a> component and <a href="Network.html">
     * <code>Network</code></a> components.<p>
     * Handle persistent information in respect with engine transaction.
     * <p><hr>
     * Be careful, this method must only be used during a transaction in
     * order to ensure the mutual exclusion.
     *
     * @exception IOException error when accessing the local persistent
     *        storage.
     */
    private void dispatch() throws Exception {
      for (Message sentMsg : mq) {
        if (sentMsg.from == null) sentMsg.from = AgentId.localId;
        Channel.post(sentMsg);
      }
      mq.clear();
      Channel.save();
    }
    
    private void terminate() {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + ", ends");
      Agent[] ag = new Agent[agents.size()];
      int i = 0;
      for (Enumeration<AgentContext> e = agents.elements() ; e.hasMoreElements() ;) {
        ag[i++] = e.nextElement().getWorker().getAgent();
      }
      for (i--; i>=0; i--) {
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG,
                     "Agent" + ag[i].id + " [" + ag[i].name + "] garbaged");
        agents.remove(ag[i].id);
        try {
          // Set current agent running in order to allow from field fixed
          // for sendTo during agentFinalize (We assume that only Engine
          // use this method).
          agent = ag[i];
          ag[i].agentFinalize(false);
        } catch (Exception exc) {
          logmon.log(BasicLevel.ERROR,
                     "Agent" + ag[i].id + " [" + ag[i].name + "] error during agentFinalize", exc);
        }
        
        //finally {
        //  agent = null;
        //}
        ag[i] = null;
      }
    }

    @Override
    public String toString() {
      return "EngineWorker [agentId=" + agentId + ", qin=" + qin + ", agent="
          + agent + ", reactContextList=" + reactMessageList + ", running="
          + running + ", currentReact=" + currentMessage + ", mq=" + mq
          + ", persistentPush=" + persistentPush + ", beginTransaction="
          + beginTransaction + "]";
    }
    
  }
  
  // MBean methods

  /**
   * Returns the number of agents actually loaded in memory.
   *
   * @return  the maximum number of agents actually loaded in memory
   */
  public int getNbAgents() {
    return agents.size();
  }

  public boolean isRunning() {
    return isStarted();
  }

  /**
   * Returns the number of agent's reaction since last boot.
   *
   * @return  the number of agent's reaction since last boot
   */
  public long getNbReactions() {
    return now.get();
  }

  public int getNbMessages() {
    return stamp;
  }

  public int getNbWaitingMessages() {
    int res = 0;
    for (Enumeration<AgentContext> e = agents.elements() ; e.hasMoreElements() ;) {
      res += e.nextElement().getWorker().getQin().size();
    }
    return res;
  }

  /**
   * Returns the number of fixed agents.
   *
   * @return  the number of fixed agents
   */
  public int getNbFixedAgents() {
    return fixedAgentIdList.size();
  }

  /**
   *  Returns a string representation of the specified agent.
   *
   * @param id  The string representation of the agent's unique identification.
   * @return  A string representation of the specified agent.
   * @see   Engine#dumpAgent(AgentId)
   */
  public String dumpAgent(String id) throws Exception {
    return dumpAgent(AgentId.fromString(id));
  }

  /**
   * Returns true if the agent profiling is on.
   * 
   * @see fr.dyade.aaa.agent.EngineMBean#isAgentProfiling()
   */
  public boolean isAgentProfiling() {
    return false;
  }

  public void setAgentProfiling(boolean agentProfiling) {
    
  }

  public long getReactTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void resetReactTime() {
    // TODO Auto-generated method stub
    
  }

  public long getCommitTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  public void resetCommitTime() {
    // TODO Auto-generated method stub
    
  }

  public void resetTimer() {
    // TODO Auto-generated method stub
    
  }

  public float getAverageLoad1() {
    // TODO Auto-generated method stub
    return 0;
  }

  public float getAverageLoad5() {
    // TODO Auto-generated method stub
    return 0;
  }

  public float getAverageLoad15() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  public int getNbMaxAgents() {
    // TODO Auto-generated method stub
    return -1;
  }

  public void setNbMaxAgents(int NbMaxAgents) {
    // TODO Auto-generated method stub
    
  }
  
  // !!!
  // Consumer methods
  //!!!

  public String getDomainName() {
    return "engine";
  }

  /**
   * Insert a message in the <code>MessageQueue</code>.
   * This method is used during initialization to restore the component
   * state from persistent storage.
   *
   * @param msg   the message
   */
  public void insert(Message msg) {
    // No need to synchronize
    checkAgentCreate(msg);
    AgentContext ctx = getAgentContextAndCreate(msg.to);
    ctx.getWorker().getQin().insert(msg);
  }

  /**
   * Saves logical clock information to persistent storage.
   */
  public void save() throws IOException {
    if (modified) {
      stampBuf[0] = (byte)((stamp >>> 24) & 0xFF);
      stampBuf[1] = (byte)((stamp >>> 16) & 0xFF);
      stampBuf[2] = (byte)((stamp >>>  8) & 0xFF);
      stampBuf[3] = (byte)(stamp & 0xFF);
      AgentServer.getTransaction().saveByteArray(stampBuf, getName());
      modified = false;
    }
  }

  /**
   * Restores logical clock information from persistent storage.
   */
  public void restore() throws Exception {
    stampBuf = AgentServer.getTransaction().loadByteArray(getName());
    if (stampBuf == null) {
      stamp = 0;
      stampBuf = new byte[4];
      modified = true;
    } else {
      stamp = ((stampBuf[0] & 0xFF) << 24) +
      ((stampBuf[1] & 0xFF) << 16) +
      ((stampBuf[2] & 0xFF) <<  8) +
      (stampBuf[3] & 0xFF);
      modified = false;
    }
  }
  
  private AgentContext getAgentContext(AgentId id) {
    AgentContext ctx = agents.get(id);
    if (ctx == null) throw new RuntimeException("Unknown agent context: " + id);
    return ctx;
  }
  
  private AgentContext getAgentContextAndCreate(AgentId id) {
    AgentContext ctx = agents.get(id);
    if (ctx == null) {
      ctx = new AgentContext(id, new EngineWorker(id));
      ctx.setStatus(AgentContext.CREATED);
      agents.put(id, ctx);
    }
    return ctx;
  }
  /*
  private void checkFullQin(ConcurrentLinkedMessageQueue qin) {
    synchronized (qin) {
      while (qin.size() > DEFAULT_QIN_THRESHOLD) {
        try {
          qin.wait();
        } catch (InterruptedException e) {}
      }
    }
  }
*/
  /**
   *  Adds a message in "ready to deliver" list. This method allocates a
   * new time stamp to the message ; be Careful, changing the stamp imply
   * the filename change too.
   */
  public void post(Message msg) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + "post: " + msg);
    // No need to synchronize
    checkAgentCreate(msg);
    stamp(msg);
    msg.save();
    AgentContext ctx = getAgentContext(msg.to);
    ConcurrentLinkedMessageQueue qin = ctx.getWorker().getQin();
    //checkFullQin(qin);
    qin.push(msg);
    toValidate.add(ctx);
  }
  
  private void checkAgentCreate(Message msg) {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + "checkAgentCreate: " + msg);
    if (msg.to.stamp == AgentId.FactoryIdStamp) {
      if (msg.not instanceof AgentCreateRequest) {
        // create the agent context
        AgentCreateRequest acr =(AgentCreateRequest) msg.not;
        AgentContext ctx = new AgentContext(acr.getDeploy(), 
            new EngineWorker(acr.getDeploy()));
        ctx.setStatus(AgentContext.SEED);
        // TODO: should check if the agent context is already created (should be an error)
        agents.put(acr.getDeploy(), ctx);
        if (logmon.isLoggable(BasicLevel.DEBUG))
          logmon.log(BasicLevel.DEBUG, getName() + "create seed: " + ctx);
      } else if (msg.not instanceof AgentDeleteRequest) {
        // TODO
      }
    }
  }

  // JORAM_PERF_BRANCH
  public void postAndValidate(Message msg) throws Exception {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + "postAndValidate: " + msg);
    // No need to synchronize
    checkAgentCreate(msg);
    stamp(msg);
    msg.save();
    AgentContext ctx = getAgentContext(msg.to);
    ConcurrentLinkedMessageQueue qin = ctx.getWorker().getQin();
    synchronized (qin) {
      //checkFullQin(qin);
      qin.pushAndValidate(msg);
      execute(ctx);
    }
  }
  
  private void execute(AgentContext ctx) {
    if (logmon.isLoggable(BasicLevel.INFO))
      logmon.log(BasicLevel.INFO, getName() + " execute: " + ctx.getWorker().getAgentId());
    if (ctx.getStatus() == AgentContext.CREATED) {
      ctx.getWorker().execute();
    } else {
      if (logmon.isLoggable(BasicLevel.DEBUG))
        logmon.log(BasicLevel.DEBUG, getName() + " agent not created: " + ctx);
      // Else: means that the agent has not been created yet
      // do nothing
    }
  }
  
  private void stamp(Message msg) {
    if (msg.isPersistent())
      // If the message is transient there is no need to save the stamp counter.
      modified = true;
    msg.source = AgentServer.getServerId();
    msg.dest = AgentServer.getServerId();
    msg.stamp = ++stamp;
  }

  /**
   * Validates all messages pushed in queue during transaction session.
   */
  public void validate() {
    if (logmon.isLoggable(BasicLevel.DEBUG))
      logmon.log(BasicLevel.DEBUG, getName() + " validate");
    for (AgentContext ctx : toValidate) {
      ConcurrentLinkedMessageQueue qin = ctx.getWorker().getQin();
      synchronized (qin) {
        qin.validate();
        execute(ctx);
      }
    }
    toValidate.clear();
  }

  /**
   * This operation always throws an IllegalStateException.
   */
  public void delete() throws IllegalStateException {
    throw new IllegalStateException();
  }

  /**
   * Get this engine's <code>MessageQueue</code> qin.
   *
   * @return this <code>Engine</code>'s queue.
   *
  public MessageQueue getQueue() {
    return qin;
  }*/
  
  static class AgentContext {
    public static final int NO_STATUS = 0;
    public static final int SEED = 1;
    public static final int CREATED = 2;
    public static final int DELETED = 3;
    
    private EngineWorker worker;
    private int status;
    
    public AgentContext(AgentId agentId, EngineWorker worker) {
      super();
      this.worker = worker;
      status = NO_STATUS;
    }

    public EngineWorker getWorker() {
      return worker;
    }

    public int getStatus() {
      return status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    @Override
    public String toString() {
      return "AgentContext [worker=" + worker + ", status=" + status + "]";
    }
    
  }
  
  class EngineThreadFactory implements ThreadFactory {
    
    private int counter;

    public Thread newThread(Runnable runnable) {
      Thread t = new EngineThread(runnable, counter++);
      t.setPriority(Thread.MAX_PRIORITY);
      return t;
    }
    
  }
  
  class EngineThread extends Thread {
    
    private EngineWorker engineWorker;

    EngineThread(Runnable runnable, int id) {
      super(AgentServer.getThreadGroup(), runnable, "MultiThreadEngine#" + id);
    }

    public void setEngineWorker(EngineWorker engineWorker) {
      this.engineWorker = engineWorker;
    }

    public EngineWorker getEngineWorker() {
      return engineWorker;
    }
    
  }

  public void start() throws Exception {

  }

  public void stop() {
    
  }

}
