package com.nortel.oam.test2.gui;

import com.nortel.oam.test2.common.ProgressSetter;
import com.nortel.oam.test2.common.Props;
import com.nortel.oam.test2.common.CloseLock;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class Gauge implements ProgressSetter, WindowListener {
  private int progress = 0;
  private JProgressBar progressBar;
  private JLabel label;
  private ProgressUpdater progressUpdater;
  private String title;
  private CloseLock closeLock;

  public Gauge(String title, CloseLock closeLock) {
    this.title = title;
    this.closeLock = closeLock;
    progressUpdater = new ProgressUpdater();
    initUI();
  }

  private void initUI() {
    JFrame jFrame = new JFrame(title);
    JPanel mainPanel = new JPanel();
    jFrame.setContentPane(mainPanel);
    jFrame.addWindowListener(this);
    GridBagLayout layout = new GridBagLayout();
    mainPanel.setLayout(layout);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = new Insets(5, 5, 5, 5);
    constraints.fill = GridBagConstraints.HORIZONTAL;

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    progressBar = new JProgressBar(0, Props.msgs_count_per_cycle);
    layout.addLayoutComponent(progressBar, constraints);
    mainPanel.add(progressBar);

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
    label = new JLabel(String.valueOf(progress));
    layout.addLayoutComponent(label, constraints);
    mainPanel.add(label);

    jFrame.pack();
    jFrame.show();
  }

  public void setProgress(int progress) {
    this.progress = progress;
    SwingUtilities.invokeLater(progressUpdater);
  }

  class ProgressUpdater implements Runnable {
    public void run() {
      label.setText(String.valueOf(progress));
      progressBar.setValue(progress);
    }
  }

  public void windowActivated(WindowEvent e) {
  }

  public void windowClosed(WindowEvent e) {
  }

  public void windowClosing(WindowEvent e) {
    synchronized (closeLock) {
      System.out.println("Closing...");
      closeLock.closed = true;
      closeLock.notifyAll();
    }
  }

  public void windowDeactivated(WindowEvent e) {
  }

  public void windowDeiconified(WindowEvent e) {
  }

  public void windowIconified(WindowEvent e) {
  }

  public void windowOpened(WindowEvent e) {
  }

}
