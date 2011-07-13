package jmx.remote.jms;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
	import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
public class FenetreMenu extends JFrame implements ActionListener{
	
			private JPanel pan = new JPanel();
	        private JButton bouton = new JButton("Get Class Name");
	        private JButton bouton2 = new JButton("Get Attributes");
	        private JButton bouton3 = new JButton("Get Operations");
	        private JLabel label = new JLabel();
	        
	        public FenetreMenu(){
	                
	                this.setTitle("Menu Principale");
	                this.setSize(500, 500);
	                this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	                this.setLocationRelativeTo(null);
	              //On définit le layout à utiliser sur le contentPane
	                this.setLayout(new BorderLayout());
	                pan.add(bouton);
	                pan.add(bouton2);
	                pan.add(bouton3);
	                label.setText("Salut");
	                //Ajout du bouton à notre contentPane
	                
	               this.add(label, BorderLayout.CENTER);
	                this.setContentPane(pan);
	                this.setVisible(true);
	                
	        }

			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(e.getActionCommand() == "Get Class Name") 
	                        label.setText("Vous avez cliqué sur le bouton 1");
	            if(e.getActionCommand() == "Get Attributes")
	                        label.setText("Vous avez cliqué sur le bouton 2");
	        }

				      
}

