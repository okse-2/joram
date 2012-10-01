/*
 * Copyright (C) 2001 - 2009 ScalAgent Distributed Technologies
 * Copyright (C) 1996 - 2000 BULL
 * Copyright (C) 1996 - 2000 INRIA
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
 * Initial developer(s): Dyade
 * Contributor(s): ScalAgent Distributed Technologies
 */
package fr.dyade.aaa.common;

import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides a set of static functions for building string
 * representations of complex structures.
 */
public class Strings {
  /**
   * Controls the default formatting of lists of objects.
   * By default lists with a number of elements up to <code>listMax</code> are
   * entirely printed. A value of <code>-1</code> leads to complete printing of
   * the list, whatever its size.
   * <p>
   * This variable, when used in an agent server, may be set by the debug
   * variable <code>Debug.var.fr.dyade.aaa.util.listMax</code>. Its default value
   * is <code>10</code>.
   */
  public static int listMax = 10;

  /**
   * Controls the default formatting of lists of objects.
   * By default lists with a number of elements greater than <code>listMax</code>
   * are partially printed, with the <code>listBorder</code> leading and trailing
   * elements.
   * <p>
   * This variable, when used in an agent server, may be set by the debug
   * variable <code>Debug.var.fr.dyade.aaa.util.listBorder</code>. Its default value is
   * is <code>3</code>.
   */
  public static int listBorder = 3;
  
  /**
   * Provides a string representation of an object. Checks if there exists
   * in this class a specialized <code>toString</code> function for the object
   * class, or calls the <code>toString</code> function of the object.
   *
   * @param output	a buffer to print the object into
   * @param obj		the object to print
   */
  public static final void toString(StringBuffer output, Object obj) {
    if (obj == null) {
      output.append("null");
      return;
    }

    if (obj instanceof String) {
      toString(output, (String) obj);
      return;
    }
    if (obj instanceof List) {
      toString(output, (List) obj);
      return;
    }
    if (obj instanceof Collection) {
      toString(output, (Collection) obj);
      return;
    }
    if (obj instanceof Map) {
      toString(output, (Map) obj);
      return;
    }
    if (obj instanceof Map.Entry) {
      toString(output, (Map.Entry) obj);
      return;
    }

    Class type = obj.getClass();
    if (type.isArray()) {
      toString(output, obj, type.getComponentType());
      return;
    }

    try {
      Class[] argstype = new Class[1];
      argstype[0] = Class.forName("java.lang.StringBuffer");
      Method method = type.getMethod("toString", argstype);
      Object[] args = new Object[1];
      args[0] = output;
      method.invoke(obj, args);
      return;
    } catch (Exception exc) {}

    output.append(obj.toString());
  }

  /**
   * Provides a string representation of an object.
   * Calls <code>toString(StringBuffer)</code>.
   *
   * @param obj		the object to print
   * @return		a string representation of the object
   */
  public static final String toString(Object obj) {
    if (obj == null)
      return "null";

    if (obj instanceof String) {
      // this avoids creating a useless StringBuffer
      return toString((String) obj);
    }

    StringBuffer output = new StringBuffer();
    toString(output, obj);
    return output.toString();
  }

  /**
   * Provides a Java string literal representing the parameter string.
   * This includes surrounding double quotes, and quoted special characters,
   * including UTF escape sequences when necessary.
   * <p>
   * This function works only for ASCII character encoding, and assumes
   * this is the default encoding.
   *
   * @param output	a byte buffer to print the object into
   * @param str		the string to print
   */
  public static final void toByteArray(ByteArrayOutputStream output, String str) {
    if (str == null) return;

    output.write(34);	// '"'

    int max = str.length();
    for (int i = 0; i < max; i ++) {
      // gets the numeric value of the unicode character
      int b = str.charAt(i);
      if ((b >= 32) && (b <= 126)) {
        // ASCII printable character, includes '"' and '\\'
        switch (b) {
        case 34:	// '"'
        case 92:	// '\\'
          output.write(92);	// '\\'
          break;
        }
        output.write(b);
      } else {
        output.write(92);	// '\\'
        switch (b) {
        case 8:		// backspace
          output.write(98);	// 'b'
          break;
        case 9:		// horizontal tab
          output.write(116);	// 't'
          break;
        case 10:	// linefeed
          output.write(110);	// 'n'
          break;
        case 12:	// form feed
          output.write(102);	// 'f'
          break;
        case 13:	// carriage return
          output.write(114);	// 'r'
          break;
        default:
          // UTF escape sequence
          output.write(117);	// 'u'
        int b3 = b >> 4;
      int b4 = b & 0xf;
      if (b4 < 10)
        b4 += 48;		// '0'
      else
        b4 += 87;		// 'a' - 10
      int b2 = b3 >> 4;
      b3 &= 0xf;
      if (b3 < 10)
        b3 += 48;		// '0'
      else
        b3 += 87;		// 'a' - 10
      int b1 = b2 >> 4;
      b2 &= 0xf;
      if (b2 < 10)
        b2 += 48;		// '0'
      else
        b2 += 87;		// 'a' - 10
      if (b1 < 10)
        b1 += 48;		// '0'
      else
        b1 += 87;		// 'a' - 10
      output.write(b1);
      output.write(b2);
      output.write(b3);
      output.write(b4);
      break;
        }
      }
    }

    output.write(34);	// '"'
  }

  /**
   * Provides a Java string literal representing the parameter string.
   * This includes surrounding double quotes, and quoted special characters,
   * including UTF escape sequences when necessary.
   * <p>
   * This function works only for ASCII character encoding, and assumes
   * this is the default encoding.
   *
   * @param output	a string buffer to print the object into
   * @param str		the string to print
   */
  public static final void toString(StringBuffer output, String str) {
    if (str == null) {
      output.append("null");
      return;
    }

    output.append(toString(str));
  }

  /**
   * Provides a Java string literal representing the parameter string.
   * This includes surrounding double quotes, and quoted special characters,
   * including UTF escape sequences when necessary.
   * <p>
   * This function works only for ASCII character encoding, and assumes
   * this is the default encoding.
   *
   * @param str		the string to print
   * @return		a Java string literal representation of the string
   */
  public static final String toString(String str) {
    if (str == null)
      return "null";

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    toByteArray(buffer, str);
    return buffer.toString();
  }

  /**
   * Provides a string representation of an array.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output,
                                    Object obj, Class type) {
    toString(output, obj, type, listMax, listBorder);
  }

  /**
   * Provides a string representation of an array.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements. A value
   * of <code>-1</code> for <code>listMax</code> leads to complete printing of the list,
   * whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    Object obj, Class type,
                                    int listMax, int listBorder) {
    if (obj == null) {
      output.append("null");
      return;
    }

    if (type.isPrimitive()) {
      if (type == Boolean.TYPE)
        toString(output, (boolean[]) obj);
      else if (type == Character.TYPE)
        toString(output, (char[]) obj);
      else if (type == Byte.TYPE)
        toString(output, (byte[]) obj);
      else if (type == Short.TYPE)
        toString(output, (short[]) obj);
      else if (type == Integer.TYPE)
        toString(output, (int[]) obj);
      else if (type == Long.TYPE)
        toString(output, (long[]) obj);
      else if (type == Float.TYPE)
        toString(output, (float[]) obj);
      else if (type == Double.TYPE)
        toString(output, (double[]) obj);
      return;
    }

    Object[] tab = (Object[]) obj;

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        toString(output, tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        toString(output, tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        toString(output, tab[size - i]);
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an array.
   * Calls <code>toString(StringBuffer, Object, Class)</code>.
   *
   * @param output	a buffer to print the object into
   * @param tab		the array to print
   * @param type	the type of the array components
   */
  public static final void toStringArray(StringBuffer output, Object tab) {
    if (tab == null) {
      output.append("null");
      return;
    }

    Class type = tab.getClass();
    if (! type.isArray()) {
      toString(output, tab);
      return;
    }

    toString(output, tab, type.getComponentType());
  }

  /**
   * Provides a string representation of an array.
   * Calls <code>toString(StringBuffer, Object, Class)</code>.
   *
   * @param tab		the array to print
   * @return		a string representation of the array
   * 
   * @see #toString(StringBuffer, Object, Class)
   */
  public static final String toStringArray(Object tab) {
    if (tab == null)
      return "null";

    Class type = tab.getClass();
    if (! type.isArray())
      return toString(tab);

    StringBuffer output = new StringBuffer();
    toString(output, tab, type.getComponentType());
    return output.toString();
  }
  
  /**
   * Provides a string representation of an array of booleans.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, boolean[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of booleans.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    boolean[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }
  
  /**
   * Provides a string representation of an array of bytes.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, byte[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of bytes.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    byte[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an array of chars.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, char[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of chars.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    char[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an array of shorts.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, short[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of shorts.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    short[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an array of ints.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, int[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of ints.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    int[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }
  
  /**
   * Provides a string representation of an array of longs.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, long[] tab) {
    toString(output, tab, listMax, listBorder);
  }

  /**
   * Provides a string representation of an array of longs.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    long[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an array of floats.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, float[] tab) {
    toString(output, tab, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an array of floats.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    float[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }
  
  /**
   * Provides a string representation of an array of doubles.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, double[] tab) {
    toString(output, tab, listMax, listBorder);
  }


  /**
   * Provides a string representation of an array of doubles.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    double[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }
  
  /**
   * Provides a string representation of an array of objects.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, Object[] tab) {
    toString(output, tab, listMax, listBorder);
  }


  /**
   * Provides a string representation of an array of objects.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param obj         the array to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    Object[] tab,
                                    int listMax,
                                    int listBorder) {
    if (tab == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = tab.length;
    output.append(size);
    if (listMax == -1 || size <= listMax) {
      for (int i = 0; i < size; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        output.append(tab[i]);
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        output.append(tab[size - i]);
      }
    }
    output.append(")");
  }
  
  /**
   * Provides a string representation of a list of objects.
   * This includes Vectors.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param list        the list of <code>Object</code> objects to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, List list) {
    toString(output, list, listMax, listBorder);
  }

  /**
   * Provides a string representation of a list of objects.
   * This includes Vectors.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param list	the list of <code>Object</code> objects to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    List list,
                                    int listMax,
                                    int listBorder) {
    if (list == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = list.size();
    output.append(size);
    if (listMax == -1 || size <= listMax || size <= (listBorder*2)) {
      for (Iterator it = list.iterator(); it.hasNext();) {
        output.append(",");
        toString(output, it.next());
      }
    } else {
      int border = size / 2;
      if (listBorder < border)
        border = listBorder;
      for (int i = 0; i < border; i ++) {
        output.append(",");
        toString(output, list.get(i));
      }
      output.append(",..");
      for (int i = border; i > 0; i --) {
        output.append(",");
        toString(output, list.get(size - i));
      }
    }
    output.append(")");
  }

  /**
   * Provides a string representation of a list of objects.
   * Calls <code>toString(StringBuffer, ...)</code>.
   *
   * @param list	the list of <code>Object</code> objects to print
   * @return		a string representation of the list
   */
  public static final String toString(List list) {
    if (list == null)
      return "null";

    StringBuffer output = new StringBuffer();
    toString(output, list);
    return output.toString();
  }

  /**
   * Provides a string representation of an unordered Collection of objects.
   * This includes HashSets.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   *
   * @param output      a buffer to print the object into
   * @param set   the collection to print
   * @param type        the type of the array components
   * 
   * @see #listMax
   * @see #listBorder
   */
  public static final void toString(StringBuffer output, Collection set) {
    toString(output, set, listMax, listBorder);
  }
  
  /**
   * Provides a string representation of an unordered Collection of objects.
   * This includes HashSets.
   * <p>
   * Lists with a number of elements greater than <code>listMax</code> are partially
   * printed, with the <code>listBorder</code> leading and trailing elements.
   * <p>
   * A value of <code>-1</code> for <code>listMax</code> leads to complete printing of
   * the list, whatever its size.
   *
   * @param output      a buffer to print the object into
   * @param set		the collection to print
   * @param type        the type of the array components
   * @param listMax     Controls the formatting of lists of objects.
   * @param listBorder  Controls the formatting of lists of objects.
   */
  public static final void toString(StringBuffer output,
                                    Collection set,
                                    int listMax,
                                    int listBorder) {
    if (set == null) {
      output.append("null");
      return;
    }

    output.append("(");
    int size = set.size();
    output.append(size);
    if (listMax != -1 && size > listMax)
      size = listBorder;
    for (Iterator it = set.iterator(); size > 0; size --) {
      output.append(",");
      toString(output, it.next());
    }
    output.append(")");
  }

  /**
   * Provides a string representation of an unordered Collection of objects.
   * Calls <code>toString(StringBuffer, ...)</code>.
   *
   * @param list	the collection to print
   * @return		a string representation of the list
   */
  public static final String toString(Collection set) {
    if (set == null)
      return "null";

    StringBuffer output = new StringBuffer();
    toString(output, set);
    return output.toString();
  }

  /**
   * Provides a string representation of a Map.
   * This includes HashTables.
   * Uses the <code>listMax</code> and <code>listBorder</code> variables.
   *
   * @param output	a buffer to print the object into
   * @param map		the map to print
   */
  public static final void toString(StringBuffer output, Map map) {
    if (map == null) {
      output.append("null");
      return;
    }

    toString(output, map.values());
  }

  /**
   * Provides a string representation of a Map.
   * Calls <code>toString(StringBuffer, ...)</code>.
   *
   * @param map		the map to print
   * @return		a string representation of the map
   */
  public static final String toString(Map map) {
    if (map == null)
      return "null";

    StringBuffer output = new StringBuffer();
    toString(output, map);
    return output.toString();
  }

  /**
   * Provides a string representation of a Map entry.
   *
   * @param output	a buffer to print the object into
   * @param entry	the map entry to print
   */
  public static final void toString(StringBuffer output, Map.Entry entry) {
    if (entry == null) {
      output.append("null");
      return;
    }
    output.append("(");
    output.append(entry.getKey());
    output.append(",");
    output.append(entry.getValue());
    output.append(")");
  }

}
