/***
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 * @version $Revision: 1.2 $
 */
public class HistogramThresholdTest extends TestCase {
    public HistogramThresholdTest(String test) {
        super(test);
    }


    public void testEntropySingleThreshold_00() {
        final int hist[] = new int[1000];
        final int t = HistogramThreshold.maximumEntropy(hist);
        assertEquals(0, t);
    }

    public void testEntropySingleThreshold_01() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final int t = HistogramThreshold.maximumEntropy(hist);
        assertEquals(8, t);
    }

    public void testEntropySingleThreshold_16() {
        final int histIn[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final int hist[] = new int[10000];
        final int offset = 513;
        System.arraycopy(histIn, 0, hist, offset, histIn.length);
        final int t = HistogramThreshold.maximumEntropy(hist);
        assertEquals(offset + 8, t);
    }


}