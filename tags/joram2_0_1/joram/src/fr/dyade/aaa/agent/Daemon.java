package fr.dyade.aaa.agent;

/**
 * The Daemon class represents a basic active component in an agent server.
 * It provides usefull code to start and safely stop inner Thread.
 * <p>
 * main loop of daemon:
 * <p><hr>
 * <blockquote><pre>
 *  try {
 *    while (isRunning) {
 *	canStop = true;
 *
 *	// Get a notification, then execute the right reaction.
 *	try {
 *	  // Get a request
 *	  ...
 *	} catch (InterruptedException exc) {
 *	  continue;
 *	}
 *	
 *	canStop = false;
 *
 *	// executes the request
 *	...
 *    }
 *  } finally {
 *    isRunning = false;
 *    thread = null;
 *    // Close any ressources no longer needed, eventually stop the
 *    // enclosing component.
 *    shutdown();
 *  }
 * </pre></blockquote>
 */
abstract class Daemon implements Runnable {
  /**
   * Boolean variable used to stop the daemon properly. The dameon tests
   * this variable between each request, and stops if it is false.
   * @see start
   * @see stop
   */
  protected volatile boolean isRunning;
  /**
   * Boolean variable used to stop the daemon properly. If this
   * variable is true then the daemon is waiting and it can interupted,
   * else it handles a request and it will exit after (it tests the
   * <code>{@link #isRunning isRunning}</code> variable between
   * each reaction)
   */
  protected volatile boolean canStop;
  /** The active component of this daemon. */ 
  Thread thread = null;
  /** The <code>daemon</code>'s name. */ 
  private String name;

  /**
   * Returns this <code>daemon</code>'s name.
   *
   * @return this <code>daemon</code>'s name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Returns a string representation of this daemon.
   *
   * @return	A string representation of this daemon. 
   */
  public String toString() {
    StringBuffer strbuf = new StringBuffer();

    strbuf.append(getName())
      .append(" [").append(isRunning).append("/")
      .append(canStop).append("]");

    if (thread != null) strbuf.append(" -> ").append(thread.isAlive());

    return strbuf.toString();
  }

  protected Daemon(String name) {
    this.name = name;

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " created.", false);

    isRunning = false;
    canStop = false;
    thread = null;
  }

  public void start() {
    if (isRunning) return;

    thread = new Thread(this, getName());
    thread.setDaemon(false);

    isRunning = true;
    canStop = true;
    thread.start();

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " started.", false);
  }

  abstract void shutdown();

  public void stop() {
    isRunning = false;

    if (Debug.debug && Debug.A3Server)
      Debug.trace(getName() + " stopped.", false);

    if (thread == null)
      // The session is idle.
      return;

    if (canStop) {
      if (thread.isAlive()) thread.interrupt();
      shutdown();
    }

    thread = null;
  }
}
