/*
 * Copyright (C) 2006 - 2011 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util;

import java.io.File;
import java.io.IOException;

/**
 *  This interface defines a repository for serializable objects and bytes
 * array.
 * 
 *  Note: Be careful the constructor must be public to allow newInstance from
 * another package.
 *  
 * @see NTransaction
 */
public interface Repository {
  /**
   * Initializes the repository.
   */
  public void init(Transaction transaction, File dir) throws IOException;

  /**
   * Gets a list of persistent objects that name corresponds to prefix.
   *
   * @return The list of corresponding names.
   */
  public String[] list(String prefix) throws IOException;

  /**
   * Save the corresponding bytes array.
   */
  public void save(String dirName, String name, byte[] content) throws IOException;

  /**
   * Loads the byte array.
   *
   * @return The loaded bytes array.
   */
  public byte[] load(String dirName, String name) throws IOException;

//   /**
//    * Loads the object.
//    *
//    * @return The loaded object or null if it does not exist.
//    */
//   public Object loadobj(String dirName, String name) throws IOException, ClassNotFoundException;

  /**
   * Deletes the corresponding objects in repository.
   */
  public void delete(String dirName, String name) throws IOException;

  /**
   * Commits all changes to the repository.
   */
  public void commit() throws IOException;

  /**
   * Closes the repository.
   */
  public void close() throws IOException;

  /**
   * Returns the number of save operation to repository.
   *
   * @return The number of save operation to repository.
   */
  public int getNbSavedObjects();

  /**
   * Returns the number of delete operation on repository.
   *
   * @return The number of delete operation on repository.
   */
  public int getNbDeletedObjects();

  /**
   * Returns the number of useless delete operation on repository.
   *
   * @return The number of useless delete operation on repository.
   */
  public int getNbBadDeletedObjects();

  /**
   * Returns the number of load operation from repository.
   *
   * @return The number of load operation from repository.
   */
  public int getNbLoadedObjects();
}
