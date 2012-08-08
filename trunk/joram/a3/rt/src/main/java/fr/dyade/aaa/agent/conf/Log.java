package fr.dyade.aaa.agent.conf;

import org.objectweb.util.monolog.api.Logger;

public class Log {

  public final static Logger logger;

  static {
    logger = fr.dyade.aaa.agent.Debug.getLogger("fr.dyade.aaa.agent.conf");
  }

}
