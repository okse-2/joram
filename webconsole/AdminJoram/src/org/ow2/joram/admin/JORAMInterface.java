package org.ow2.joram.admin;


public class JORAMInterface {

	SysoListener listener;
	
	public JORAMInterface(String login, String password) throws Exception {
		
		listener = new SysoListener();
		boolean connected = false;

		JoramAdmin admin = new JoramAdminImpl();
	    connected = admin.connect(login, password);
	    
	    if(!connected) throw new Exception("Erreur de login/password");
	    
	    admin.start(listener);
		
	
	}
	
	public SysoListener getListener() {
		return listener;
	}
	
}
