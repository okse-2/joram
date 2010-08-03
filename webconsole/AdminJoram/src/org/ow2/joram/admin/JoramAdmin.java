package org.ow2.joram.admin;

public interface JoramAdmin {

	public boolean connect(String login, String password);

	public void start(DestinationListener listener);

	public void stop();

	public void disconnect();

}