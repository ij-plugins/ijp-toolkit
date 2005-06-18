/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Latest release available at http://sourceforge.net/projects/ij-plugins/
 */
package net.sf.ij_plugins.thresholding;

import junit.framework.TestCase;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class HistogramThresholdTest extends TestCase {
    public HistogramThresholdTest(String test) {
        super(test);
    }


    public void testEntropySingleThreshold_01() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final int t = HistogramThreshold.maximumEntropy(hist);
        assertEquals(8, t);
    }


    public void testEntropyMultipleThreshold_01() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final int[] t = HistogramThreshold.maximumEntropy(hist, 1);
        assertNotNull(t);
        assertEquals(t.length, 1);
        assertEquals(8, t[0]);
    }


    public void testEntropyMultipleThreshold_02() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final int[] t = HistogramThreshold.maximumEntropy(hist, 2);
        assertNotNull(t);
        assertEquals(t.length, 2);
        assertEquals(8, t[0]);
        assertEquals(16, t[1]);
    }


    public void testIintervals001() throws Exception {
        int[][] intervals = HistogramThreshold.intervals(1, 0, 5);
        print(intervals, 1, 0, 5);
    }

    public void testIintervals002() throws Exception {
        int n = 2;
        int min = 0;
        int max = 5;
        int[][] intervals = HistogramThreshold.intervals(n, min, max);
        print(intervals, n, min, max);
    }

    public void testIintervals256() throws Exception {
        int n = 2;
        int min = 0;
        int max = 256;
        int[][] intervals = HistogramThreshold.intervals(n, min, max);
//        print(intervals, n, min, max);
    }


    private static void print(int[][] intervals, int nbDiv, int min, int max) {
        System.out.println("# divisions: " + nbDiv + ", min: " + min + ", max: " + max + ", # intervals: " + intervals.length);
        for (int i = 0; i < intervals.length; i++) {
            int[] interval = intervals[i];
            int lastD = min;
            for (int j = 0; j < interval.length; j++) {
                int d = interval[j];
//                System.out.print("[" + lastD + ", " + d + ") ");
                lastD = d;
            }
//            System.out.println("[" + lastD + ", " + max + ")");
        }
    }

    /**
     * The fixture set up called before every test method
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method
     */
    protected void tearDown() throws Exception {
    }
}