/*
 * JORAM: Java(TM) Open Reliable Asynchronous Messaging
 * Copyright (C) 2003 - Bull SA
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
package org.objectweb.joram.mom.dest;


/**
 * The <code>DestinationImplMBean</code> interface defines the JMX
 * instrumentation for administering a JORAM destination.
 */
public interface DestinationImplMBean
{
  /** Returns <code>true</code> if the destination is freely readable. */
  public boolean isFreelyReadable();

  /** Returns <code>true</code> if the destination is freely writeable. */
  public boolean isFreelyWriteable();

  /** Returns the identifiers of the readers on the destination. */
  public String getReaders();

  /** Returns the identifiers of the writers on the destination. */
  public String getWriters();

  /** Deletes the destination. */
  public void delete();

  /**
   * Removes a given writer.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              writer is not a registered writer.
   */
  public void removeWriter(String writerId) throws Exception;

  /**
   * Removes a given reader.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid or if the specified
   *              reader is not a registered reader.
   */
  public void removeReader(String readerId) throws Exception;

  /**
   * Adds a given writer on the destination.
   *
   * @param writerId  Identifier of the writer.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addWriter(String writerId) throws Exception;

  /**
   * Adds a given reader on the destination.
   *
   * @param readerId  Identifier of the reader.
   *
   * @exception Exception  If the identifier is invalid.
   */
  public void addReader(String readerId) throws Exception;

  /**
   * Removes free writing access on the destination.
   */
  public void removeFreeWriting();

  /**
   * Removes free reading access on the destination.
   */
  public void removeFreeReading();

  /**
   * Provides free writing access on the destination.
   */
  public void provideFreeWriting();

  /**
   * Provides free reading access on the destination.
   */
  public void provideFreeReading();
}
