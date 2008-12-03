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
 * Initial developer(s): Florent Benoit, Benoit Pelletier
 */

package org.objectweb.joram.client.jms.admin;

/**
 * This class defines the methods for serializing an object to XML
 */
public class XmlSerializer {


    /**
     * Return indent spaces.
     * @param indent number of indentation.
     * @return the indent space string.
     */
    public static String indent(int indent) {
        String txt = "";
        for (int i = 0; i < indent; i++) {
            txt += " ";
        }
        return txt;
    }

    /**
     * Return the xml representation of the specified value with the root-element xmlTag
     * @param value String value to represent in XML
     * @param xmlTag tag of the root-element
     * @param indent indent to use
     * @return xml representation of the specified value
     */
    public static String xmlElement(String value, String xmlTag, int indent) {
        if (value == null) {
            return "";
        }

        // else

        StringBuffer sb = new StringBuffer();
        sb.append(indent(indent));
        sb.append("<");
        sb.append(xmlTag);
        sb.append(">");
        sb.append(value);
        sb.append("</");
        sb.append(xmlTag);
        sb.append(">\n");
        return sb.toString();
    }

    /**
     * Return the xml representation of the specified attribute value
     * @param value String value to represent in XML
     * @param xmlTag tag of the attribute
     * @return xml representation of the specified value
     */
    public static String xmlAttribute(String value, String xmlTag) {
        if (value == null) {
            return "";
        }

        // else

        StringBuffer sb = new StringBuffer();
        sb.append(" ");
        sb.append(xmlTag);
        sb.append("=\"");
        sb.append(value);
        sb.append("\"");
        return sb.toString();
    }

}
