/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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
import net.sf.ij_plugins.util.progress.IJProgressBarAdapter;

/**
 * @author Jarek Sacha
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

    public void testEntropySingleThreshold_Bug2687379() {
        // Bug 2687379: Division by zero in entropy threshold
        final int hist[] = {
                3, 1, 6817, 2327, 2809, 2974, 3719, 2112, 2038, 1920, 1106, 1219, 1091, 1068, 910, 792, 898, 673,
                606, 664, 748, 983, 767, 745, 646, 547, 278, 149, 9, 0, 0, 4389};
        final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
        final int threshold = HistogramThreshold.maximumEntropy(hist, progressBarAdapter);
        assertEquals(12, threshold);
    }


}