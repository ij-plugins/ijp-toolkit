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
import ij.gui.ProgressBar;
import ij.plugin.filter.Duplicater;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.StackConverter;

import java.awt.*;

/**
 * Represents vector valued image. Value at each pixel in the image is a vector of floating point
 * numbers.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.3 $
 */
public class VectorProcessor {
    final int width;
    final int height;
    final int numberOfValues;
    final float[][] pixels;
    Rectangle roi;
    private ProgressBar progressBar;


    public VectorProcessor(final ColorProcessor cp) {
        this(new ImagePlus("", cp));
    }

    public VectorProcessor(final ImagePlus imp) {
        this(convertToFloatStack(imp));
    }

    /**
     * @param stack a stack of {@link FloatProcessor}s.
     */
    public VectorProcessor(final ImageStack stack) {

        // Create vector valued representation
        width = stack.getWidth();
        height = stack.getHeight();
        numberOfValues = stack.getSize();
        pixels = new float[width * height][numberOfValues];
        Object[] slices = stack.getImageArray();
        for (int i = 0; i < numberOfValues; ++i) {
            float[] values = (float[]) slices[i];
            for (int j = 0; j < values.length; j++) {
                pixels[j][i] = values[j];
            }
        }

        roi = new Rectangle(0, 0, width, height);
    }


    /**
     * @return width of the image.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return height of the image.
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return number of values at each pixel in the image.
     */
    public int getNumberOfValues() {
        return numberOfValues;
    }

    /**
     * Gives direct access to pixel values in the image first index is the pixel number (between 0
     * and width*height-1), the second index references within each pixel value.
     *
     * @return reference to the array containing pixel values in the image.
     */
    public float[][] getPixels() {
        return pixels;
    }

    /**
     * @return region of interest within the image.
     */
    public Rectangle getRoi() {
        return roi;
    }

    /**
     * @see {@link #getRoi()}
     */
    public void setRoi(Rectangle roi) {
        this.roi = roi;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    /**
     * @return pixel value iterator.
     */
    public PixelIterator pixelIterator() {
        return new VectorProcessor.PixelIterator();
    }

    /**
     * @return pixel value iterator.
     */
    public Iterator iterator() {
        return new VectorProcessor.Iterator();
    }

    /**
     * Convert VectorProessor to FloatProcessor stack
     *
     * @return
     */
    public ImagePlus toFloatStack() {
        ImageStack stack = new ImageStack(width, height);
        for (int i = 0; i < numberOfValues; ++i) {
            FloatProcessor fp = new FloatProcessor(width, height);
            float[] values = (float[]) fp.getPixels();
            for (int j = 0; j < values.length; j++) {
                values[j] = pixels[j][i];
            }
            stack.addSlice("band " + 0, fp);
        }
        return new ImagePlus("From VectorProcessor", stack);
    }

    static final private ImageStack convertToFloatStack(ImagePlus imp) {
        // TODO: remove duplicate method in KMeansClusteringPlugin

        imp = duplicate(imp);

        // Remember scaling setup
        boolean doScaling = ImageConverter.getDoScaling();

        try {
            // Disable scaling
            ImageConverter.setDoScaling(false);

            if (imp.getType() == ImagePlus.COLOR_RGB) {
                if (imp.getStackSize() > 1) {
                    throw new RuntimeException("Unsupported image type: stack of COLOR_RGB");
                }
                final ImageConverter ic = new ImageConverter(imp);
                ic.convertToRGBStack();
            }

            if (imp.getStackSize() > 1) {
                final StackConverter sc = new StackConverter(imp);
                sc.convertToGray32();
            } else {
                final ImageConverter ic = new ImageConverter(imp);
                ic.convertToGray32();
            }

            final ImageStack stack = imp.getStack();
            // FIXME: make sure that there are no memory leaks
//            imp.flush();
            return stack;
        } finally {
            // Restore original scaling option
            ImageConverter.setDoScaling(doScaling);
        }
    }

    static private ImagePlus duplicate(final ImagePlus imp) {
        // TODO: remove duplicate method in KMeansClusteringPlugin
        final Duplicater duplicater = new Duplicater();
        duplicater.setup(null, imp);
        return duplicater.duplicateStack(imp, imp.getTitle() + "-duplicate");
    }

    /**
     * Return pixel value at coordinates (<code>x</code>, <code>y</code>).
     */
    public float[] get(final int x, final int y, float[] dest) {
        if (x < 0 || x >= width || y < 0 || y >= width) {
            throw new IllegalArgumentException("Value of coordinates (x,y) is out of range.");
        }
        if (dest == null) {
            dest = new float[numberOfValues];
        } else {
            if (dest.length != numberOfValues) {
                throw new IllegalArgumentException("Invalid leghth of array dest.");
            }
        }
        final int offset = x + y * width;
        final float[] v = pixels[offset];
        System.arraycopy(v, 0, dest, 0, v.length);
        return dest;
    }

    /**
     * Set value of pixel value at coordinates (<code>x</code>, <code>y</code>).
     */
    public void set(final int x, final int y, float[] v) {
        if (x < 0 || x >= width || y < 0 || y >= width) {
            throw new IllegalArgumentException("Value of coordinates (x,y) is out of range.");
        }
        if (v == null) {
            throw new IllegalArgumentException("Argument 'v' cannot be null.");
        }
        if (v.length != numberOfValues) {
            throw new IllegalArgumentException("Invalid size of argument 'v' expecting " + numberOfValues
                    + ", got " + v.length + ".");
        }
        final int offset = x + y * width;
        final float[] s = pixels[offset];
        System.arraycopy(v, 0, s, 0, v.length);
    }


    /**
     * Represents 3x3 neighbourhood. the center pixel is <code>p5</code>. Pixels <code>p1</code> to
     * <code>p3</code> are in the top row, <code>p4</code> to <code>p6</code> in the middle, and
     * <code>p7</code> to <code>p9</code> in the bottom of the neighbourhood.
     */
    public static class Neighborhood3x3 {
        float[] p1,
                p2,
                p3,
                p4,
                p5,
                p6,
                p7,
                p8,
                p9;
        int x,
                y,
                offset;
    }

    /**
     * Iterator over 3x3 neighbourhood of vector valued pixels.
     */
    public class PixelIterator implements java.util.Iterator {
        final int xMin = roi.x - 1;
        final int xMax = roi.x + roi.width - 1;
        ;
        final int rowOffset = width;
        final int yMin = roi.y;
        final int yMax = roi.y + roi.height - 1;
        int x = roi.x;
        int y = roi.y;
        float[] value = new float[numberOfValues];

        private PixelIterator() {
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public boolean hasNext() {
            return x < xMax || y < yMax;
        }

        public Object next() {
            // Update center location
            if (x < xMax) {
                ++x;
            } else {
                if (y < yMax) {
                    x = xMin;
                    ++y;
                }
                if (progressBar != null) {
                    progressBar.show(y - yMin, yMax - yMin);
                }
            }
            int offset = x + y * width;

            value = pixels[offset];

            return value;
        }

        /**
         * Not suported.
         */
        public void remove() {
            throw new UnsupportedOperationException("Method remove() not supported.");
        }

        public int getOffset() {
            return x + y * width;
        }
    }


    /**
     * Iterator over 3x3 neighbourhood of vector valued pixels.
     */
    public class Iterator implements java.util.Iterator {
        final int xMin = roi.x - 1;
        final int xMax = roi.x + roi.width - 1;
        ;
        final int rowOffset = width;
        final int yMin = roi.y;
        final int yMax = roi.y + roi.height - 1;
        int x = roi.x;
        int y = roi.y;
        final Neighborhood3x3 neighborhood3x3 = new Neighborhood3x3();

        private Iterator() {
        }

        public boolean hasNext() {
            return x < xMax || y < yMax;
        }

        public Object next() {
            // Update center location
            if (x < xMax) {
                ++x;
            } else {
                if (y < yMax) {
                    x = xMin;
                    ++y;
                }
                if (progressBar != null) {
                    progressBar.show(y - yMin, yMax - yMin);
                }
            }
            int offset = x + y * width;

            // Update neighbourhood information
            neighborhood3x3.p1 = pixels[offset - rowOffset - 1];
            neighborhood3x3.p2 = pixels[offset - rowOffset];
            neighborhood3x3.p3 = pixels[offset - rowOffset + 1];

            neighborhood3x3.p4 = pixels[offset - 1];
            neighborhood3x3.p5 = pixels[offset];
            neighborhood3x3.p6 = pixels[offset + 1];

            neighborhood3x3.p7 = pixels[offset + rowOffset - 1];
            neighborhood3x3.p8 = pixels[offset + rowOffset];
            neighborhood3x3.p9 = pixels[offset + rowOffset + 1];

            neighborhood3x3.x = x;
            neighborhood3x3.y = y;
            neighborhood3x3.offset = offset;

            return neighborhood3x3;
        }

        /**
         * Not suported.
         */
        public void remove() {
            throw new UnsupportedOperationException("Method remove() not supported.");
        }

    }
}
