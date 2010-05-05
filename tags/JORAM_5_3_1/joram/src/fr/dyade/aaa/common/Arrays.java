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
package fr.dyade.aaa.common;

/**
 * This class contains various methods for sorting and searching int arrays.
 */
public class Arrays {
  /**
   * Sorts the specified array of ints into ascending numerical order.
   * The sorting algorithm is a tuned quicksort.
   *
   * @param a the array to be sorted.
   */
  public static void sort(short[] a) {
    sort1(a, 0, a.length);
  }

  /**
   * Sorts the specified range of the specified array of ints into
   * ascending numerical order.
   */
  public static void sort(short[] a, int fromIndex, int toIndex) {
    rangeCheck(a.length, fromIndex, toIndex);
    sort1(a, fromIndex, toIndex-fromIndex);
  }

  /**
   * Sorts the specified sub-array of integers into ascending order.
   */
  private static void sort1(short x[], int off, int len) {
    // Insertion sort on smallest arrays
    if (len < 7) {
      for (int i=off; i<len+off; i++)
        for (int j=i; j>off && x[j-1]>x[j]; j--)
          swap(x, j, j-1);
      return;
    }

    // Choose a partition element, v
    int m = off + len/2;       // Small arrays, middle element
    if (len > 7) {
      int l = off;
      int n = off + len - 1;
      if (len > 40) {        // Big arrays, pseudomedian of 9
        int s = len/8;
        l = med3(x, l,     l+s, l+2*s);
        m = med3(x, m-s,   m,   m+s);
        n = med3(x, n-2*s, n-s, n);
      }
      m = med3(x, l, m, n); // Mid-size, med of 3
    }
    int v = x[m];

    // Establish Invariant: v* (<v)* (>v)* v*
    int a = off, b = a, c = off + len - 1, d = c;
    while(true) {
      while (b <= c && x[b] <= v) {
        if (x[b] == v)
          swap(x, a++, b);
        b++;
      }
      while (c >= b && x[c] >= v) {
        if (x[c] == v)
          swap(x, c, d--);
        c--;
      }
      if (b > c)
        break;
      swap(x, b++, c--);
    }

    // Swap partition elements back to middle
    int s, n = off + len;
    s = Math.min(a-off, b-a  );  vecswap(x, off, b-s, s);
    s = Math.min(d-c,   n-d-1);  vecswap(x, b,   n-s, s);

    // Recursively sort non-partition-elements
    if ((s = b-a) > 1)
      sort1(x, off, s);
    if ((s = d-c) > 1)
      sort1(x, n-s, s);
  }

  /**
   * Swaps x[a] with x[b].
   */
  private static void swap(short x[], int a, int b) {
    short t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

  /**
   * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
   */
  private static void vecswap(short x[], int a, int b, int n) {
    for (int i=0; i<n; i++, a++, b++)
      swap(x, a, b);
  }

  /**
   * Returns the index of the median of the three indexed integers.
   */
  private static int med3(short x[], int a, int b, int c) {
    return (x[a] < x[b] ?
            (x[b] < x[c] ? b : x[a] < x[c] ? c : a) :
            (x[b] > x[c] ? b : x[a] > x[c] ? c : a));
  }

  /**
   * Check that fromIndex and toIndex are in range, and throw an
   * appropriate exception if they aren't.
   */
  private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
    if (fromIndex > toIndex)
      throw new IllegalArgumentException("fromIndex(" + fromIndex +
                                         ") > toIndex(" + toIndex+")");
    if (fromIndex < 0)
      throw new ArrayIndexOutOfBoundsException(fromIndex);
    if (toIndex > arrayLen)
      throw new ArrayIndexOutOfBoundsException(toIndex);
  }

  /**
   * Searches the specified array of ints for the specified value using the
   * binary search algorithm. The array <strong>must</strong> be sorted (as
   * by the <tt>sort</tt> method, above) prior to making this call.
   */
  public static int binarySearch(short[] a, int key) {
    int low = 0;
    int high = a.length-1;

    while (low <= high) {
      int mid =(low + high)/2;
      int midVal = a[mid];

      if (midVal < key)
        low = mid + 1;
      else if (midVal > key)
        high = mid - 1;
      else
        return mid; // key found
    }
    return -(low + 1);  // key not found.
  }

  /**
   * Returns <tt>true</tt> if the two specified arrays of shorts are
   * <i>equal</i> to one another.
   */
  public static boolean equals(short[] a, short a2[]) {
    if (a==a2)
      return true;
    if (a==null || a2==null)
      return false;

    int length = a.length;
    if (a2.length != length)
      return false;

    for (int i=0; i<length; i++)
      if (a[i] != a2[i])
        return false;

    return true;
  }
}
