package jmx.remote.jms;

import java.awt.Component;

import javax.swing.JOptionPane;

public class ShowMessageInformations {
	
	public ShowMessageInformations(Component parentComponent,Object message,String titleOfMessage,int messageType){
		
		JOptionPane.showMessageDialog(parentComponent,message,titleOfMessage,messageType);
	}
}
