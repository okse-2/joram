/*
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
 * fr.dyade.aaa.util, fr.dyade.aaa.ip, fr.dyade.aaa.mom, and fr.dyade.aaa.joram,
 * released May 24, 2000. 
 * 
 * The Initial Developer of the Original Code is Dyade. The Original Code and
 * portions created by Dyade are Copyright Bull and Copyright INRIA.
 * All Rights Reserved.
 */

package fr.dyade.aaa.util;

public final class QuickSort {

  /** RCS version number of this file: $Revision: 1.6 $ */
  public static final String RCS_VERSION="@(#)$Id: QuickSort.java,v 1.6 2002-01-16 12:46:47 joram Exp $";

  public static void sort(Comparable v[], int low, int up) {
    int i, last;
    Comparable t, x;

    if (low < up) {
      t = v[low];
      last = low;
      for (i = low +1; i <= up; ++i) {
	if (t.compareTo(v[i]) > 0) {
	  last ++;
	  x = v[last]; v[last] = v[i]; v[i] = x; // swap(last, i);
	}
      }
      x = v[low]; v[low] = v[last]; v[last] = x; // swap(low, last);
      sort(v, low, last -1);
      sort(v, last +1, up);
    }
  }
}
