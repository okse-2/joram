<!-- 
 * Copyright (C) 2009 ScalAgent Distributed Technologies
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
 * Initial developer(s): ScalAgent Distributed Technologies
 * Contributor(s): 
 * -->
<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:joram="http:///joram.ow2.org/ecore/joram">
  <xsl:output indent="yes" method="xml" />
  
  <xsl:template match="/">
    <xsl:element name="config">
      <xsl:apply-templates />
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="//managedElements[@xsi:type='joram:JORAM']">
    <xsl:element name="server">
    
      <xsl:attribute name="id">
        <xsl:value-of select="@sid" />
      </xsl:attribute>
      
      <xsl:attribute name="hostname">
        <xsl:value-of select="@hostname" />
      </xsl:attribute>
      
      <xsl:attribute name="name">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      
      <xsl:variable name="currentServerId">
        <xsl:value-of select="@sid" />
      </xsl:variable>
      
      <xsl:for-each select="//managedElements[@xsi:type='joram:NetworkPort' and @joram:server=$currentServerId]">
        <xsl:element name="network">
          <xsl:attribute name="domain">
            <xsl:value-of select="@domain" />
          </xsl:attribute>
          <xsl:attribute name="port">
            <xsl:value-of select="@port" />
          </xsl:attribute>
        </xsl:element>
      </xsl:for-each>
      
      <xsl:for-each select="services">
        <xsl:element name="service">
          <xsl:choose>
            <xsl:when test="@xsi:type='joram:ConnectionManager'">
              <xsl:attribute name="class">
                <xsl:text>org.objectweb.joram.mom.proxies.ConnectionManager</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="args">
                <xsl:value-of select="@user" />
                <xsl:text> </xsl:text>
                <xsl:value-of select="@password" />
              </xsl:attribute>
            </xsl:when>
            <xsl:when test="@xsi:type='joram:TCPProxyService'">
              <xsl:attribute name="class">
               <xsl:text>org.objectweb.joram.mom.proxies.tcp.TcpProxyService</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="args">
                <xsl:value-of select="@port" />
              </xsl:attribute>
            </xsl:when>
            <xsl:when test="@xsi:type='joram:JNDIServer'">
              <xsl:attribute name="class">
               <xsl:text>fr.dyade.aaa.jndi2.server.JndiServer</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="args">
                <xsl:value-of select="@port" />
              </xsl:attribute>
            </xsl:when>
            <xsl:when test="@xsi:type='joram:AdminProxy'">
              <xsl:attribute name="class">
                <xsl:text>fr.dyade.aaa.agent.AdminProxy</xsl:text>
              </xsl:attribute>
              <xsl:attribute name="args">
                <xsl:value-of select="@port" />
              </xsl:attribute>
            </xsl:when>
          </xsl:choose>
        </xsl:element>
      </xsl:for-each>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="//managedElements[@xsi:type='joram:NetworkDomain']">
    <xsl:element name="domain">
      <xsl:attribute name="name">
        <xsl:value-of select="@name" />
      </xsl:attribute>
      <xsl:attribute name="network">
        <xsl:value-of select="@networkClass" />
      </xsl:attribute>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
