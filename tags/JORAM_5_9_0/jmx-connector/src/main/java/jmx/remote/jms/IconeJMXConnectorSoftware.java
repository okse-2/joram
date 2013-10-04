/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2011 ScalAgent Distributed Technologies
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
 * Initial developer(s): Djamel-Eddine Boumchedda
 */
package jmx.remote.jms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class IconeJMXConnectorSoftware {
  private static final int WIDTH = 24;
  private static final int HEIGHT = 24;

  private int value = 0; // valeur
  private TrayIcon trayIcon = null;
  private MyImageRefresh refresh = null;

  public IconeJMXConnectorSoftware() {
    buildTray();
  }

  private void buildTray() {
    if (SystemTray.isSupported()) {
      final SystemTray tray = SystemTray.getSystemTray();
      final PopupMenu popup = new PopupMenu();
      final MenuItem defaultItem = new MenuItem("Quitter");

      defaultItem.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          if (refresh != null)
            refresh.end();
          System.exit(0);
        }
      });

      popup.add(defaultItem);
      trayIcon = new TrayIcon(getImage(), "JMXConnector Software !", popup);

      final ActionListener actionListener = new ActionListener() {
        public void actionPerformed(final ActionEvent e) {
          trayIcon.displayMessage("The server connector is running !",
          "Here is his URL address : service:jmx:jms:///tcp://localhost:6000 ",
          TrayIcon.MessageType.INFO);
        }
      };

      trayIcon.setImageAutoSize(true);
      trayIcon.addActionListener(actionListener);

      try {
        tray.add(trayIcon);
        // démarrage du thread
        refresh = new MyImageRefresh();
        refresh.start();
      } catch (final Exception e) {
        e.printStackTrace();
      }
    } else {
      // ...
    }
  }

  public BufferedImage getImage() {
    // création de l'image
    final BufferedImage img = new BufferedImage(WIDTH, HEIGHT,
    BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2 = img.createGraphics();
    g2.setColor(Color.BLACK);
    g2.drawString("" + value, 2, 15);

    return img;
  }

  private class MyImageRefresh extends Thread {

    private boolean end = false;

    public void run() {
      while (!end) {
        // refresh toute les secondes
        try {
          sleep(1000);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }

        // juste un petit test pour faire bouger le text de l'icon

        if (value < 100)
          value++;
        else
          value = 0;

        // modification de l'image
        synchronized (trayIcon) {
          trayIcon.setImage(getImage());
        }
      }
    }

    public void end() {
      end = true;
    }
  }
}