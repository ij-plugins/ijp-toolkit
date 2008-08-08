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
package net.sf.ij_plugins.multiband;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import junit.framework.TestCase;

import java.util.Random;

/**
 * @author Jarek Sacha
 */
public class VectorProcessorTest extends TestCase {
    public VectorProcessorTest(String test) {
        super(test);
    }


    public void testPixelIterator() {
        final int width = 13;
        final int height = 7;
        final VectorProcessor vp = new VectorProcessor(width, height, 1);
        final VectorProcessor.PixelIterator i = vp.pixelIterator();
        while (i.hasNext()) {
            float[] v = i.next();
            final int x = i.getX();
            final int y = i.getY();
            v[0] = 1000 * (x + 1) + (y + 1) + 21;
        }

        final float[][] pixels = vp.getPixels();
        for (int y = 0; y < height; y++) {
            final int yOffset = y * width;
            for (int x = 0; x < width; x++) {
                final int v = Math.round(pixels[x + yOffset][0] - 21);
                final int xx = (v / 1000) - 1;
                assertEquals("x", x, xx);
                final int yy = (v % 1000) - 1;
                assertEquals("y", y, yy);
            }
        }
    }

    public void testPixelIteratorXY() {
        final int width = 13;
        final int height = 7;
        final VectorProcessor vp = new VectorProcessor(width, height, 1);
        final VectorProcessor.PixelIterator i = vp.pixelIterator();
        while (i.hasNext()) {
            i.next();
            final int x = i.getX();
            final int y = i.getY();
            assertTrue("x=" + x, x >= 0 && x < width);
            assertTrue("y=" + y, y >= 0 && y < height);
        }
    }

    public void testPixelIteratorCount() {
        final int width = 13;
        final int height = 7;
        final VectorProcessor vp = new VectorProcessor(width, height, 1);
        final VectorProcessor.PixelIterator i = vp.pixelIterator();
        int count = 0;
        while (i.hasNext()) {
            i.next();
            count++;
        }

        assertEquals("Count", width * height, count);
    }


    public void testIteratorXY() {
        final int width = 13;
        final int height = 7;
        final VectorProcessor vp = new VectorProcessor(width, height, 1);
        final VectorProcessor.Iterator i = vp.iterator();
        while (i.hasNext()) {
            final VectorProcessor.Neighborhood3x3 n = i.next();
            final int x = n.x;
            final int y = n.y;
            assertTrue("x=" + x, x >= 0 && x < width);
            assertTrue("y=" + y, y >= 0 && y < height);
        }
    }

    public void testIteratorCount() {
        final int width = 13;
        final int height = 7;
        final VectorProcessor vp = new VectorProcessor(width, height, 1);
        final VectorProcessor.Iterator i = vp.iterator();
        int count = 0;
        while (i.hasNext()) {
            i.next();
            count++;
        }

        assertEquals("Count", (width - 2) * (height - 2), count);
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
}