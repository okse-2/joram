/*
 * Copyright (C) 2002 - ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
 *
 * The contents of this file are subject to the Joram Public License,
 * as defined by the file JORAM_LICENSE.TXT 
 * 
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License on the Objectweb web site
 * (www.objectweb.org). 
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific terms governing rights and limitations under the License. 
 * 
 * The Original Code is Joram, including the java packages fr.dyade.aaa.agent,
 * fr.dyade.aaa.ip, fr.dyade.aaa.joram, fr.dyade.aaa.mom, and
 * fr.dyade.aaa.util, released May 24, 2000.
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 *
 * The present code contributor is ScalAgent Distributed Technologies.
 */
package fr.dyade.aaa.joram;

import java.net.MalformedURLException;
import java.util.StringTokenizer;

/**
 * The <code>JoramUrl</code> class is used for building URLs locating Joram
 * servers and administered objects.
 */
public class JoramUrl
{
  /** Joram communication protocol. */
  private static final String PROTO = "joram";
  /** Separator in URLs. */
  private static final String SEP = "/";
  /** URL host name. */
  private String host;
  /** URL port. */
  private int port;
  /** URL agent id. */
  private String agentId = null;
  
 
  /**
   * Constructs a Joram url from a given host, a given port and a given agent
   * id.
   * 
   * @param host  Host name.
   * @param port  Port number.
   * @param agentId  Agent id, may be <code>null</code>.
   */
  public JoramUrl(String host, int port, String agentId)
  {
    this.host = host;
    this.port = port;
    this.agentId = agentId;
  }

  /**
   * Constructs a Joram url from a given String.
   *
   * @param jUrl  String url like: "joram://host:port/agentId"
   */
  public JoramUrl(String jUrl) throws MalformedURLException
  {
    int i = jUrl.indexOf("://");

    if (i == -1)
      throw new MalformedURLException(jUrl);

    String url = jUrl.substring(i + 3);
    StringTokenizer sT = new StringTokenizer(url, ":" + SEP, false);
    host = sT.nextToken();
    port = Integer.valueOf(sT.nextToken()).intValue();

    if (sT.hasMoreTokens())
      agentId = sT.nextToken();
  }


  /** Returns this url as a String. */
  public String toString()
  { 
    if (agentId == null)
      return PROTO + ":" + SEP + SEP + host + ":" + port;
    return PROTO + ":" + SEP + SEP + host + ":" + port + SEP + agentId;
  }

  /** Returns this url protocol. */
  public String getProtocol()
  {
    return PROTO;
  }

  /** Returns this url host. */
  public String getHost()
  { 
    return host;
  }
    
  /** Returns this url port. */
  public int getPort()
  {
    return port;
  }

  /** Returns this url agent id. */
  public String getAgentId()
  {
    return agentId;
  }

  /** Returns this url. */
  public JoramUrl getJoramUrl()
  {
    return new JoramUrl(host, port, agentId);
  }

  /** Checks equality of this url with a given object. */
  public boolean equals(Object obj)
  {
    if (obj instanceof JoramUrl)
      return obj.toString().equals(toString());
    return false;
  }
}
