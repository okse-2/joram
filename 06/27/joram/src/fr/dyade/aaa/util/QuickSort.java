/*
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
 */

package fr.dyade.aaa.util;

public final class QuickSort {


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
