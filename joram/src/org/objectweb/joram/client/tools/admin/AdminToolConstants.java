/*
 * Created on Oct 26, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
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
 * Initial developer(s): Alexander Fedorowicz
 * Contributor(s):
 */
package org.objectweb.joram.client.tools.admin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.ImageIcon;

/**
 * @author afedoro
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AdminToolConstants {
	// Main window size
  protected static final Dimension STARTUP_SIZE = new Dimension(800, 500);

  // Initial ratio between navigation and information panels
  protected static final double DIVIDER_PROPORTION = 0.4;

	// Connection status bar message colors
	protected static final Color COLOR_CONNECTED = new Color(0x00, 0xa0, 0x00);
	protected static final Color COLOR_DISCONNECTED = new Color(0xff, 0x00, 0x00);

	// Icons used in the admin tool
  static final ImageIcon expandedIcon =
  	new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/expanded.png")));

  static final ImageIcon collapsedIcon =
  	new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/collapsed.png")));

  static final ImageIcon serverIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/server.png")));

  static final ImageIcon queueIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/queue.png")));

  static final ImageIcon userIcon =
  	new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/user.png")));

  static final ImageIcon jndiIcon =
  	new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/jndi.png")));

  static final ImageIcon refreshIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/refresh.png")));

  static final ImageIcon stopIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/stop.png")));

  static final ImageIcon trashIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/trash.png")));

  static final ImageIcon lockIcon =
  	new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/lock.png")));

  static final ImageIcon homeIcon =
    new ImageIcon(Toolkit.getDefaultToolkit().getImage(AdminTool.class.getResource("/org/objectweb/joram/client/tools/admin/icons/home.png")));
}
