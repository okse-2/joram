/*
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
 */
package fr.dyade.aaa.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ResolverRepository {
  
  private static Map pkg2resolver = new HashMap();

  public synchronized void registerResolver(Resolver resolver) {
    String[] pkgs = resolver.getResolvedPackages();
    for (int i = 0; i < pkgs.length; i++) {
      pkg2resolver.put(pkgs[i], resolver);
    }
  }

  public Class resolveClass(String name) throws ClassNotFoundException {
    String pkgName = name.substring(0, name.lastIndexOf('.'));
    Resolver resolver = (Resolver) pkg2resolver.get(pkgName);
    if (resolver == null) {
      throw new ClassNotFoundException(name);
    } else {
      return resolver.resolveClass(name);
    }
  }

  public synchronized void unregisterResolver(Resolver resolver) {
    Set entrySet = pkg2resolver.entrySet();
    for (Iterator iterator = entrySet.iterator(); iterator.hasNext();) {
      Entry entry = (Entry) iterator.next();
      if (entry.getValue().equals(resolver)) {
        iterator.remove();
      }
    }
  }

}
