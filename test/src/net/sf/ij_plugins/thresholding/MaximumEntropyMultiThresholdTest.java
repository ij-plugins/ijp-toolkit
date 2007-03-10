/*
 * Copyright (c) 2007 Your Corporation. All Rights Reserved.
 */
package net.sf.ij_plugins.thresholding;

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