package fr.dyade.aaa.agent;

import org.objectweb.util.monolog.api.BasicLevel;
import org.objectweb.util.monolog.api.Logger;

import fr.dyade.aaa.util.TimerTask;

/**
 * Class used to schedule a wake up on a specific agent. A notification is sent
 * to activate the agent.
 */
public class WakeUpTask extends TimerTask {

  private AgentId destId;
  private Logger logger;
  private Class wakeUpNot;

  /**
   * Creates a new WakeUpTask.
   * 
   * @param id
   *          the id of the agent to wake up.
   * @param wakeUpNotClass
   *          the notification which will be sent to the agent
   */
  public WakeUpTask(AgentId id, Class wakeUpNotClass) {
    destId = id;
    wakeUpNot = wakeUpNotClass;
    logger = Debug.getLogger(getClass().getName());
  }

  public void run() {
    try {
      Channel.sendTo(destId, (Notification) wakeUpNot.newInstance());
    } catch (Exception exc) {
      logger.log(BasicLevel.ERROR, "--- " + this, exc);
    }
  }

  /**
   * Schedules the wake up task for execution after the given period.
   * 
   * @param period Delay in ms before waking up.
   */
  public void schedule(long period) {
    // Don't schedule on HA slaves.
    if (AgentServer.isHAServer() && !AgentServer.isMasterHAServer())
      return;

    if (period != -1) {
      try {
        timer = AgentServer.getTimer();
        timer.schedule(this, period);
      } catch (Exception exc) {
        if (logger.isLoggable(BasicLevel.WARN))
          logger.log(BasicLevel.WARN, "--- " + this, exc);
      }
    }
  }
}
