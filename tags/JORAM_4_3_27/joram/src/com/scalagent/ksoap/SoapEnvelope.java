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

import java.io.*;

import org.kxml.*;
import org.kxml.io.*;
import org.kxml.parser.*;

import com.scalagent.ksoap.marshal.*;

public class SoapEnvelope {
	
  Object body;
  ClassMap classMap;
  String encodingStyle; 

  public SoapEnvelope() {
    classMap = new ClassMap();
    new MarshalBase64().register(classMap);
    new MarshalHashtable().register(classMap);
    new MarshalVector().register(classMap);
  }

  public void setEncodingStyle(String encodingStyle) {
    this.encodingStyle = encodingStyle;
  }

  public void setBody(SoapObject body) {
    this.body = body;
  }
 
  public Object getBody() {
    return body;
  } 

  public void read(AbstractXmlParser parser) throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapEnvelope.read(" + parser + ")");

    readHead(parser);
    readBody(parser);
    readTail(parser);
  }

  public void readHead(AbstractXmlParser parser) throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapEnvelope.readHead(" + parser + ")");

    parser.skip();
    StartTag tag = null;
    Attribute attr = null;
    try {
      tag = (StartTag) parser.read(Xml.START_TAG, classMap.env,"Envelope");
      attr = tag.getAttribute(classMap.env,"encodingStyle");
    } catch (ParseException exc) {
      // read text befor head
      // Tomcat cookies,....
      parser.read();
      parser.skip();
      tag = (StartTag) parser.read(Xml.START_TAG, classMap.env,"Envelope");
      attr = tag.getAttribute(classMap.env,"encodingStyle");
    }
    if (attr != null) encodingStyle = attr.getValue();

    parser.skip();
    
    if (parser.peek(Xml.START_TAG,classMap.env,"Header")) {
      parser.read();
      parser.skip();

      while (parser.peek().getType() != Xml.END_TAG) {
        parser.ignoreTree();
        parser.skip();
      }
      
      parser.read(Xml.END_TAG,classMap.env,"Header");
    }
    
    parser.skip();
    tag = (StartTag) parser.read(Xml.START_TAG,classMap.env,"Body");
    attr = tag.getAttribute(classMap.env,"encodingStyle");
    if (attr != null) encodingStyle = attr.getValue();
  }


  public void readBody(AbstractXmlParser parser) throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapEnvelope.readBody(" + parser + ")");

    parser.skip();
    
    if (parser.peek(Xml.START_TAG,classMap.env,"Fault")) {
      SoapFault fault = new SoapFault();
      fault.parse(parser);
      body = fault;
      if (KSoapTracing.dbgReader)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "SoapEnvelope.readBody : Fault = " + fault);
    } else {
      if (KSoapTracing.dbgReader)
        KSoapTracing.log(KSoapTracing.DEBUG,
                         "SoapEnvelope.readBody befor SoapReader(...).read()");
      body = new SoapReader(parser,classMap).read();
    }
  }

  public void readTail(AbstractXmlParser parser) throws IOException {
    if (KSoapTracing.dbgReader)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapEnvelope.readdTail(" + parser + ")");
    parser.skip();
    parser.read(Xml.END_TAG,classMap.env,"Body");
    parser.skip();
    parser.read(Xml.END_TAG,classMap.env,"Envelope");
  }

  public void write(AbstractXmlWriter writer) throws IOException {
    if (KSoapTracing.dbgWriter)
      KSoapTracing.log(KSoapTracing.DEBUG,
                       "SoapEnvelope.write(" + writer + ")");
    writer.startTag(classMap.prefixMap,classMap.env,"Envelope");
    writer.startTag(classMap.env,"Body");
    writer.attribute(classMap.env,"encodingStyle",
                     encodingStyle == null ? classMap.enc : encodingStyle);
    new SoapWriter(writer,classMap).write((SoapObject) body);
    writer.endTag();
    writer.endTag();
  }
}
