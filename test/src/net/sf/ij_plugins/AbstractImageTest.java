/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package net.sf.ij_plugins;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import junit.framework.TestCase;

import java.io.File;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public abstract class AbstractImageTest extends TestCase {
    public AbstractImageTest(String test) {
        super(test);
    }

    protected static ImagePlus openImage(final String fileName) throws Exception {
        final File file = new File(fileName);
        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(file.getAbsolutePath());
        if (imp == null) {
            throw new Exception("Cannot open image: " + file.getAbsolutePath());
        }

        return imp;
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
    protected static void assertEquals(final ImagePlus expected, final ImagePlus actual, final double delta) {

        if (expected == actual) {
            return;
        }

        assertNotNull(expected);
        assertNotNull(actual);

        assertEquals("Image width", expected.getWidth(), actual.getWidth());
        assertEquals("Image height", expected.getHeight(), actual.getHeight());
        assertEquals("Image slices", expected.getStackSize(), actual.getStackSize());

        for (int z = 0; z < expected.getStackSize(); ++z) {
            expected.setSlice(z + 1);
            final ImageProcessor ipExpected = expected.getProcessor();
            actual.setSlice(z + 1);
            final ImageProcessor ipActual = actual.getProcessor();

            for (int y = 0; y < expected.getHeight(); ++y) {
                for (int x = 0; x < expected.getWidth(); ++x) {
                    assertEquals(ipExpected.getPixelValue(x, y), ipActual.getPixelValue(x, y), delta);
                }
            }
        }
    }

}
