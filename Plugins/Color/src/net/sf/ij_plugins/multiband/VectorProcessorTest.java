/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij_plugins.multiband;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import junit.framework.TestCase;

import java.util.Random;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class VectorProcessorTest extends TestCase {
    public VectorProcessorTest(String test) {
        super(test);
    }

    public void testConversion() throws Exception {
        final int nbBands = 5;
        final int width = 6;
        final int height = 7;
        final double tolerance = 1e-10;
        // Create a FloatProcessor and fill it with random values
        ImageStack stack = new ImageStack(width, height);
        Random random = new Random(31415);
        for (int i = 0; i < nbBands; ++i) {
            FloatProcessor fp = new FloatProcessor(width, height);
            float[] pixels = (float[]) fp.getPixels();
            for (int j = 0; j < pixels.length; j++) {
                pixels[j] = random.nextFloat();
            }
            stack.addSlice("" + i, fp);
        }

        // Convert to VectorProcessor and back to FloatProcessor stack
        VectorProcessor vp = new VectorProcessor(new ImagePlus("test", stack));
        ImagePlus imp2 = vp.toFloatStack();
        ImageStack newStack = imp2.getStack();
        Object[] oldSlices = stack.getImageArray();
        Object[] newSlices = newStack.getImageArray();
        assertEquals("Number of slices", newSlices.length, oldSlices.length);
        for (int i = 0; i < nbBands; i++) {
            float[] oldPixels = (float[]) oldSlices[i];
            assertNotNull("Old pixels, band " + i, oldPixels);

            float[] newPixels = (float[]) newSlices[i];
            assertNotNull("New pixels, band " + i, newPixels);

            assertEquals("Number of pixels", newPixels.length, oldPixels.length);
            for (int j = 0; j < newPixels.length; j++) {
                assertEquals("Pixel value", newPixels[j], oldPixels[j], tolerance);
            }
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