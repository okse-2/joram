package com.nortel.oam.test1.gui;

import com.nortel.oam.test1.common.MessagesRateSetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashBoardConsumer implements ActionListener, MessagesRateSetter {
  private PerfUpdater perfUpdater;

  public DashBoardConsumer() {
    initUI();
  }

  private void initUI() {
    JFrame jFrame = new JFrame("Joram DashBoardConsumer");
    JPanel mainPanel = new JPanel();
    jFrame.setContentPane(mainPanel);
    GridBagLayout layout = new GridBagLayout();
    mainPanel.setLayout(layout);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.fill = GridBagConstraints.HORIZONTAL;

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    JLabel mesLabel = new JLabel("Msg rate measured: ");
    layout.addLayoutComponent(mesLabel, constraints);
    mainPanel.add(mesLabel);

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    JLabel valueLabel = new JLabel("?");
    layout.addLayoutComponent(valueLabel, constraints);
    mainPanel.add(valueLabel);
    perfUpdater = new PerfUpdater(valueLabel);

    constraints.gridx = 2;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    JButton exitButton = new JButton("Exit");
    exitButton.addActionListener(this);
    exitButton.setActionCommand("EXIT");
    layout.addLayoutComponent(exitButton, constraints);
    mainPanel.add(exitButton);

    jFrame.pack();
    jFrame.show();
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("EXIT")) {
      synchronized (this) {
        this.notifyAll();
      }
    }
  }

  public void setMessagesRate(float msgRate) {
    perfUpdater.setRate(msgRate);
    SwingUtilities.invokeLater(perfUpdater);
  }

  class PerfUpdater implements Runnable {
    private JLabel jlabel;
    private float rate;

    public PerfUpdater(JLabel jlabel) {
      this.jlabel = jlabel;
    }

    public void setRate(float rate) {
      this.rate = rate;
    }

    public void run() {
      jlabel.setText(String.valueOf(rate));
    }
  }
}
