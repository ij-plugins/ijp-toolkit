/*
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
 *
 */
package net.sf.ij_plugins;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import org.junit.Assert;

/**
 * @author Jarek Sacha
 */
public final class AssertUtils {
    private AssertUtils() {
    }

    /**
     * Asserts that two images are of the same size and their pixel values are
     * equal concerning a delta. If the expected value is infinity then the
     * delta value is ignored. Images can be of different types as long as pixel
     * values are within delta.
     *
     * @param expected expected image
     * @param actual   actual image
     * @param delta    maximum difference for each pixel value
     */
    public static void equals(final ImagePlus expected, final ImagePlus actual, final double delta) {

        if (expected == actual) {
            return;
        }

        Assert.assertNotNull(expected);
        Assert.assertNotNull(actual);

        Assert.assertEquals("Image width", expected.getWidth(), actual.getWidth());
        Assert.assertEquals("Image height", expected.getHeight(), actual.getHeight());
        Assert.assertEquals("Image slices", expected.getStackSize(), actual.getStackSize());

        for (int z = 0; z < expected.getStackSize(); ++z) {
            expected.setSlice(z + 1);
            final ImageProcessor ipExpected = expected.getProcessor();
            actual.setSlice(z + 1);
            final ImageProcessor ipActual = actual.getProcessor();

            for (int y = 0; y < expected.getHeight(); ++y) {
                for (int x = 0; x < expected.getWidth(); ++x) {
                    Assert.assertEquals(ipExpected.getPixelValue(x, y), ipActual.getPixelValue(x, y), delta);
                }
            }
        }
    }

}