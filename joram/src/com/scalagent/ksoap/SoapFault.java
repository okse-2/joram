/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - ScalAgent Distributed Technologies
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
 * The present code contributor is ScalAgent Distributed Technologies.
 *
 * Initial developer(s): Nicolas Tachker (ScalAgent)
 * Contributor(s):
 */
package com.scalagent.ksoap;

import java.util.Vector;
import java.io.*;
import org.kxml.*;
import org.kxml.io.*;
import org.kxml.parser.*;

public class SoapFault extends IOException {

  public String faultcode;
  public String faultstring;
  public String faultactor;
  public Vector detail;

  public void parse(AbstractXmlParser parser) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapFault.parse(" + parser + ")");
    parser.read(Xml.START_TAG,ClassMap.env,"Fault");
    
    while (true) {
      parser.skip();
      ParseEvent event = parser.peek();
      switch (event.getType()) {
        
      case Xml.START_TAG: 
        String name = event.getName();
        
        if (name.equals("detail")) {
          detail = new Vector();
          parser.readTree(detail);
        }
        else {
          parser.read();
          String value = parser.readText();
          parser.read();
          if (name.equals("faultcode"))
            faultcode = value;
          else if (name.equals("faultstring"))
            faultstring = value;
          else if (name.equals("faultactor"))
            faultactor = value;
        }
        break;
        
      case Xml.END_TAG: 
        parser.read();
        return;
      default:
        parser.read();
      }
    }
  }
    
  public void write(AbstractXmlWriter xw) throws IOException {
    if (KSoapTracing.dbg)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapFault.write(" + xw + ")");
    xw.startTag(ClassMap.env, "Fault");
    xw.startTag("faultcode");
    xw.write(""+ faultcode);
    xw.endTag();
    xw.startTag("faultstring");
    xw.write(""+ faultstring);
    xw.endTag();
    xw.startTag("detail");
    if (detail != null) 
      for (int i = 0; i < detail.size(); i++) {
        xw.startTag("item");
        xw.write(""+detail.elementAt(i));
        xw.endTag();
      }
    xw.endTag();
    xw.endTag();
  }

  public String toString() {
    return "SoapFault - faultcode: " + faultcode
      + " faultstring: " + faultstring 
      + " faultactor: " + faultactor
      + " detail: " + detail;
  }
}

