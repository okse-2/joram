/*
 * Copyright (C) 2000 ScalAgent Distributed Technologies
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
 */
package fr.dyade.aaa.util;

import java.util.Hashtable;
import java.lang.reflect.Method;

public class ConfigVariable{

  static Hashtable primitives = new Hashtable();
  static{
    primitives.put("int", Integer.class);
    primitives.put("long", Long.class);
    primitives.put("double", Double.class);
    primitives.put("float", Float.class);
    primitives.put("short", Short.class);
    primitives.put("boolean", Boolean.class);
  }

  private String name;
  private Class type;
  private Object defaultValue;
  private String info;

  ConfigVariable(String name, Class type, Object defaultValue, String info){
    this.name = name;
    this. type = type;
    this.defaultValue = defaultValue;
    this.info = info;
  }

  public final String getName(){
    return name;
  }

  public final Class getType(){
    return type;
  }

  public final Object getDefault(){
    return defaultValue;
  }

  public final String getInfo(){
    return info;
  }


  public static Class getClass(String type) throws Exception{ 
    if (type == null || type.equals(""))
      throw new Exception ("Type not defined");
    Object o = primitives.get(type);
    if (o != null)
      return (Class)o; 
    try{
      return Class.forName(type);
    }catch(ClassNotFoundException e){
        // allow to type Integer instead of java.lang.Integer
      return Class.forName("java.lang." + type);
    }
  }

  public static Object getObject(Class c, String value) throws Exception{
    if (c.equals(java.lang.String.class))
      return value;Class cp[] = new Class[1];
    cp[0] = java.lang.String.class;
    Method m = c.getMethod("valueOf", cp);
    Object op[] = new Object[1];
    op[0] = value;
    Object res = m.invoke(null, op);
    return res;
  }


}
