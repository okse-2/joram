package fr.dyade.aaa.task;

import fr.dyade.aaa.task.* ;
import fr.dyade.aaa.agent.* ;
import java.util.* ;


/**
 * This class enables a <code>Client</code> agent to program the Scheduler
 * with a scheduling event date described by a cron like string 
 * The configurator proceeds to the programming of the scheduler
 * when the agent is deployed
 * assuming that there is only one default scheduler in the agent server.
 * <br>
 * Should be in <code>SchedulingCondition</code>.
 *
 * @author	Lacourte Serge
 * @version	v1.0
 *
 * @see		Scheduler
 * @see		CronEvent
 * @see		Condition
 */
public class Client extends Agent {

  /** RCS version number of this file: $Revision: 1.1 $ */
  public static final String RCS_VERSION="@(#)$Id: Client.java,v 1.1 2002-03-06 16:52:20 joram Exp $"; 

  /** CronEvent for scheduling */
  private CronEvent crone ;

  /** the condition the scheduler is supposed to send */
  private String schedulerCondition ;

  /** cron like string representing the event scheduling date */
  private String frequency ;


  /**
    * Creates an agent to be configured.
    *
    * @param to		target agent server
    * @param name	symbolic name of this agent
    */
  public Client(short to, String name, String condition, String frequency) {
    super(to, name) ;
    setSchedulerCondition(condition) ;
    setFrequency(frequency) ;
  }    

  /**
   * Constructor invoked by the configurator at deployment stage.
   */
  public Client(short to, String name) {
    super(to, name) ;
  }

  /**
   * Initializes the Client.
   * Creates a <code>CronEvent</cod> and send it to the default scheduler
   *
   * @param firstTime	<code>true</code> when agent server starts a new Client
   */
  protected void initialize(boolean firstTime) throws Exception {
    super.initialize(firstTime);
    if (firstTime) {     
      crone = new CronEvent(schedulerCondition, this.frequency);   
      sendTo(Scheduler.getDefault(), crone);
    }
  }


  /**
   * Property accessor.
   * 
   * @param condition	condition the scheduler must send
   */
  public void setSchedulerCondition(String schedulerCondition) {
    this.schedulerCondition = schedulerCondition ;
  }


  /**
   * Property accessor.
   * 
   * @param frequency	cron like string to determine the next scheduled date
   */
  public void setFrequency(String frequency) {
    this.frequency = frequency ;
  }


}
