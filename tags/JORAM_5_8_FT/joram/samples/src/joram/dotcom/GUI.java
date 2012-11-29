/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2001 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - Dyade
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
 *
 * Initial developer(s): Frederic Maistre (INRIA)
 * Contributor(s):
 */
package dotcom;

import javax.swing.* ;
import java.awt.* ;
import java.awt.event.* ;

/**
 * Defines the GUI type used to illustrate the demo.<br>
 * General look: 3 panes, labels, radioButtons, buttons.
 *
 * @author	Maistre Frederic
 * 
 * @see		WebOrdering
 * @see		CustomerTreatment
 * @see		InventoryTreatment
 * @see         ControlTreatment
 * @see		DeliveryTreatment
 * @see		Servers
 */
class GUI {
	
  // reference to the calling thread
  Servers server ;

  // graphical attributes
  JFrame mainWindow ;
  JPanel mainPane ;
  JPanel topPane ;
  JPanel middlePane ;
  JPanel bottomPane ;
  
  JLabel idLabel1 ;
  JLabel idLabel2 ;
  JLabel itemLabel1 ;
  JLabel itemLabel2 ;
  JLabel infoLabel ;
  
  // radio buttons
  JRadioButton shoesButton ;
  JRadioButton socksButton ;
  JRadioButton trousersButton ;
  JRadioButton shirtButton ;
  JRadioButton hatButton ;
  ButtonGroup buttonGroup ;
  
  // buttons
  JButton otherButton ;
  JButton sendButton ;
  JButton cancelButton ;
  JButton quitButton ;
  JButton okButton ;
  JButton noButton ;
  JButton closeButton ;
  
  /** 
   * Creates a GUI used to order items (instantiated by WebOrdering).
   *
   * @param title		title of the frame
   * @param ser			calling thread
   * @param x			window's top left corner x location
   * @param y			window's top left corner y location
   */
  GUI(String title, Servers ser, int x, int y) {

    this.server = ser ;
 
    // setting the window 
    idLabel1 = new JLabel("Order number: ", SwingConstants.CENTER) ;
    idLabel2 = new JLabel("1", SwingConstants.CENTER) ;
    
    shoesButton = new JRadioButton("Shoes") ;
    shoesButton.setMnemonic(KeyEvent.VK_1) ;
    shoesButton.setActionCommand("Shoes") ;
    shoesButton.setSelected(true) ;
    socksButton = new JRadioButton("Socks") ;
    socksButton.setMnemonic(KeyEvent.VK_2) ;
    socksButton.setActionCommand("Socks") ;
    trousersButton = new JRadioButton("Trousers") ;
    trousersButton.setMnemonic(KeyEvent.VK_3) ;
    trousersButton.setActionCommand("Trousers") ;
    shirtButton = new JRadioButton("Shirt") ;
    shirtButton.setMnemonic(KeyEvent.VK_4) ;
    shirtButton.setActionCommand("Shirt") ;
    hatButton = new JRadioButton("Hat") ;
    hatButton.setMnemonic(KeyEvent.VK_5) ;
    hatButton.setActionCommand("Hat") ;
    
    buttonGroup = new ButtonGroup() ;
    buttonGroup.add(shoesButton) ;
    buttonGroup.add(socksButton) ;
    buttonGroup.add(trousersButton) ;
    buttonGroup.add(shirtButton) ;
    buttonGroup.add(hatButton) ;
    
    otherButton = new JButton("Other order") ; 
    sendButton = new JButton("Send order(s)") ;  
    cancelButton = new JButton("Cancel order(s)") ;
    quitButton = new JButton("Quit") ;
    
    mainPane = new JPanel() ;
    topPane = new JPanel() ;
    middlePane = new JPanel() ;
    bottomPane = new JPanel() ;
    
    mainPane.setLayout(new GridLayout(3,1)) ;
    topPane.setLayout(new GridLayout(1,2)) ;
    middlePane.setLayout(new GridLayout(1,5)) ;
    bottomPane.setLayout(new GridLayout(1,4)) ;
  
    topPane.add(idLabel1) ;
    topPane.add(idLabel2) ;
    middlePane.add(shoesButton) ;
    middlePane.add(socksButton) ;
    middlePane.add(trousersButton) ;
    middlePane.add(shirtButton) ;
    middlePane.add(hatButton) ;
    bottomPane.add(otherButton) ;
    bottomPane.add(sendButton);
    bottomPane.add(cancelButton) ;
    bottomPane.add(quitButton) ;
    mainPane.add(topPane) ;
    mainPane.add(middlePane) ;
    mainPane.add(bottomPane) ;
  
    mainWindow = new JFrame(title) ; 
    mainWindow.getContentPane().add(mainPane) ; 
  
    // registering a listener for the radio buttons 
    RadioButtonListener radioBListener = new RadioButtonListener(server) ;
    shoesButton.addActionListener(radioBListener) ;
    socksButton.addActionListener(radioBListener) ;
    trousersButton.addActionListener(radioBListener) ;
    shirtButton.addActionListener(radioBListener) ;
    hatButton.addActionListener(radioBListener) ;
    
    /** Method called when pressing the otherButton. */
    otherButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.otherMethod() ;
      }
    }) ; 
  
    /** Method called when pressing the sendButton. */
    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.sendMethod() ;
      }
    }) ; 
    
    /** Method called when pressing the cancelButton. */
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.cancelMethod() ;
      }
    }) ; 
    
    /** Method called when pressing the quitButton. */
    quitButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.quitMethod() ;
      }
    }) ; 

    // last settings
    mainWindow.pack() ;
    mainWindow.setLocation(x,y);
  }
  
  /** 
   * Creates a GUI used to validate incoming OrderMessages 
   * (instantiated by CustomerTreatment, StockTreatment, ControlTreatment).
   *
   * @param title		title of the frame
   * @param okBut		label of okButton
   * @param noButt		label of noButton
   * @param ser			calling thread
   * @param x			window's top left corner x location
   * @param y			window's top left corner y location
   */
  GUI(String title, String okBut, String noBut, Servers ser, int x, int y) {
  	
    this.server = ser ;
 
    // setting the window
    idLabel1 = new JLabel("ORDER ID: ", SwingConstants.CENTER) ;
    idLabel2 = new JLabel();
    itemLabel1 = new JLabel("ITEM: ", SwingConstants.CENTER) ;
    itemLabel2 = new JLabel();
    okButton = new JButton(okBut) ; 
    noButton = new JButton(noBut) ;  
    
    mainPane = new JPanel() ;
    topPane = new JPanel() ;
    middlePane = new JPanel() ;
    bottomPane = new JPanel() ;
    
    mainPane.setLayout(new GridLayout(3,1)) ;
    topPane.setLayout(new GridLayout(1,4)) ;
    middlePane.setLayout(new GridLayout(1,1)) ;
    bottomPane.setLayout(new GridLayout(1,2)) ;
  
    topPane.add(idLabel1) ;
    topPane.add(idLabel2) ;
    topPane.add(itemLabel1) ;
    topPane.add(itemLabel2) ;
    bottomPane.add(okButton);
    bottomPane.add(noButton);
    mainPane.add(topPane) ;
    mainPane.add(middlePane) ;
    mainPane.add(bottomPane) ;
  
    mainWindow = new JFrame(title) ; 
    mainWindow.getContentPane().add(mainPane) ; 
  
    /** Method called when pressing the okButton. */
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.okMethod() ;
      }
    }) ; 
  
    /** Method called when pressing the noButton. */
    noButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.noMethod() ;
      }
    }) ; 

    // last settings
    mainWindow.pack() ;
    mainWindow.setLocation(x,y);
  }
  
  
  
  /** 
   * Creates a GUI used to display a message reception 
   * (instantiated by CustomerTreatement, DeliveryTreatement).
   *
   * @param title		title of the frame
   * @param info		info to display
   * @param ser			calling thread
   * @param x			window's top left corner x location
   * @param y			window's top left corner y location
   */
  GUI(String title, String info, Servers ser, int x, int y) {
    
    this.server = ser ;
    
    // setting the window
    idLabel1 = new JLabel("ORDER ID: ", SwingConstants.CENTER) ;
    idLabel2 = new JLabel() ;
    itemLabel1 = new JLabel("ITEM: ", SwingConstants.CENTER) ;
    itemLabel2 = new JLabel() ;
    infoLabel = new JLabel(info, SwingConstants.CENTER) ;
    closeButton = new JButton("Close") ;  
    
    mainPane = new JPanel() ;
    topPane = new JPanel() ;
    middlePane = new JPanel() ;
    bottomPane = new JPanel() ;
    
    mainPane.setLayout(new GridLayout(3,1)) ;
    topPane.setLayout(new GridLayout(1,4)) ;
    middlePane.setLayout(new GridLayout(1,1)) ;
    bottomPane.setLayout(new GridLayout(1,1)) ;
  
    topPane.add(idLabel1) ;
    topPane.add(idLabel2) ;
    topPane.add(itemLabel1) ;
    topPane.add(itemLabel2) ;
    middlePane.add(infoLabel) ;
    bottomPane.add(closeButton) ;
    mainPane.add(topPane) ;
    mainPane.add(middlePane) ;
    mainPane.add(bottomPane) ;
  
    mainWindow = new JFrame(title) ; 
    mainWindow.getContentPane().add(mainPane) ; 
  
    /** Method called when pressing the closeButton. */
    closeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        server.closeMethod() ;
      }
    }) ; 
  
    // last settings
    mainWindow.pack() ;
    mainWindow.setLocation(x,y);
  }
  
  /**
   * Method called to set the GUI's visibility.
   *
   * @param bool		true or false.
   */
  public void setVisible(boolean bool) {
    mainWindow.setVisible(bool) ;
  }
  
  /**
   * Method called to update idLabel2.
   *
   * @param id			order id.
   */
  public void updateId(int id) {
    idLabel2.setText(Integer.toString(id)) ;
  }
  
  /**
   * Method called to update itemLabel2.
   *
   * @param item		item ordered.
   */
  public void updateItem(String item) {
    itemLabel2.setText(item) ;
  }
}
 
  
/**
 * Listener getting RadioButtons selections.<br>
 * Used by WebOrdering's GUI.
 *
 * @author	Maistre Frederic
 *
 * @see		GUI
 * @see		WebServer
 */
class RadioButtonListener implements ActionListener {
  /** Thread instantiating the GUI. */
  Servers server ;

  /**
   * Creates a RadioButtonListener.
   *
   * @param server		calling thread.
   */
  RadioButtonListener(Servers ser) {
    this.server = ser ;
  }

  /**
   * Method called when selecting a RadioButton.
   */
  public void actionPerformed(ActionEvent e) {
    server.choiceMethod(e.getActionCommand()) ;
  }      
}
