/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */
package ij_plugins.toolkit.thresholding;

import junit.framework.TestCase;

/**
 * Date: Mar 10, 2007
 * Time: 1:45:52 PM
 *
 * @author Jarek Sacha
 */
public final class MaximumEntropyMultiThresholdTest extends TestCase {
    public MaximumEntropyMultiThresholdTest(String test) {
        super(test);
    }

    public void testEntropyMultipleThreshold_01() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final MaximumEntropyMultiThreshold memt = new MaximumEntropyMultiThreshold();
        final int[] t = memt.maximumEntropy(hist, 1);
        assertNotNull(t);
        assertEquals(t.length, 1);
        assertEquals(8, t[0]);
    }


    public void testEntropyMultipleThreshold_02() {
        final int hist[] = {0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0, 1, 2, 3, 4, 3, 2, 1, 0};
        final MaximumEntropyMultiThreshold memt = new MaximumEntropyMultiThreshold();
        final int[] t = memt.maximumEntropy(hist, 2);
        assertNotNull(t);
        assertEquals(t.length, 2);
        assertEquals(8, t[0]);
        assertEquals(16, t[1]);
    }
}