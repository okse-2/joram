package com.scalagent.engine.client;


/**
 * Interface to represent the messages contained in resource bundle:
 * 	/home/florian/dev/Java/econnectware-webclient2/src/com/tagsys/econnectware/console/gwt/shared/client/SharedMessages.properties'.
 */
public interface SharedMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "Home".
   * 
   * @return translated "Home"
   */
  @DefaultMessage("Home")
  @Key("header.home")
  String header_home();

  /**
   * Translated "Logout".
   * 
   * @return translated "Logout"
   */
  @DefaultMessage("Logout")
  @Key("header.logout")
  String header_logout();

  /**
   * Translated "Welcome".
   * 
   * @return translated "Welcome"
   */
  @DefaultMessage("Welcome")
  @Key("header.welcome")
  String header_welcome();
  
  /**
   * Translated "True".
   * 
   * @return translated "True"
   */
  @DefaultMessage("True")
  @Key("main.true")
  String main_true();
	
  
  /**
   * Translated "False".
   * 
   * @return translated "False"
   */
  @DefaultMessage("False")
  @Key("main.false")
  String main_false();
	
	
	

  /**
   * Translated "en".
   * 
   * @return translated "en"
   */
  @DefaultMessage("en")
  @Key("locale")
  String locale();

  /**
   * Translated "The browser met issue while communicating with the server. This is likely because the server is no longer accessible.<BR/>Here are the main explanations of this state:".
   * 
   * @return translated "The browser met issue while communicating with the server. This is likely because the server is no longer accessible.<BR/>Here are the main explanations of this state:"
   */
  @DefaultMessage("The browser met issue while communicating with the server. This is likely because the server is no longer accessible.<BR/>Here are the main explanations of this state:")
  @Key("systemMessageWidget.introduction")
  String systemMessageWidget_introduction();

  /**
   * Translated "Here is the exception associated to this issue: ".
   * 
   * @return translated "Here is the exception associated to this issue: "
   */
  @DefaultMessage("Here is the exception associated to this issue: ")
  @Key("systemMessageWidget.introduction.exception")
  String systemMessageWidget_introduction_exception();

  /**
   * Translated "Your network connection has been shut down.".
   * 
   * @return translated "Your network connection has been shut down."
   */
  @DefaultMessage("Your network connection has been shut down.")
  @Key("systemMessageWidget.introduction.explanation.one")
  String systemMessageWidget_introduction_explanation_one();

  /**
   * Translated "The server has been restarted for maintenance.".
   * 
   * @return translated "The server has been restarted for maintenance."
   */
  @DefaultMessage("The server has been restarted for maintenance.")
  @Key("systemMessageWidget.introduction.explanation.second")
  String systemMessageWidget_introduction_explanation_second();

  /**
   * Translated "Issue while communicating with the server".
   * 
   * @return translated "Issue while communicating with the server"
   */
  @DefaultMessage("Issue while communicating with the server")
  @Key("systemMessageWidget.title")
  String systemMessageWidget_title();
}
