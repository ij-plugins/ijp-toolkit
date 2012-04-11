/*
 * Image/J Plugins
 * Copyright (C) 2002-2012 Jarek Sacha
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
package net.sf.ij_plugins.multiband;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.ProgressBar;
import ij.plugin.Duplicator;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.StackConverter;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;


/**
 * Represents vector valued image.
 * Value at each pixel in the image is a vector of floating point numbers.
 *
 * @author Jarek Sacha
 */
public class VectorProcessor {

    private final int width;
    private final int height;
    private final int numberOfValues;
    private final float[][] pixels;
    private Rectangle roi;
    // TODO: use net.sf.ij_plugins.util.progress instead of ij.gui.ProgressBar for more flexibility.
    private ProgressBar progressBar;


    public VectorProcessor(final int width, final int height, final int numberOfValues) {
        this.width = width;
        this.height = height;
        this.numberOfValues = numberOfValues;
        pixels = new float[width * height][numberOfValues];
        roi = new Rectangle(0, 0, width, height);
    }


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
        this(stack.getWidth(), stack.getHeight(), stack.getSize());

        // Copy data
        final Object[] slices = stack.getImageArray();
        for (int i = 0; i < numberOfValues; ++i) {
            final float[] values = (float[]) slices[i];
            for (int j = 0; j < values.length; j++) {
                pixels[j][i] = values[j];
            }
        }
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
     * @param roi new ROI.
     * @see #getRoi()
     */
    public void setRoi(final Rectangle roi) {
        this.roi = roi;
    }


    public ProgressBar getProgressBar() {
        return progressBar;
    }


    public void setProgressBar(final ProgressBar progressBar) {
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
     * Convert VectorProcessor to an array of {@link FloatProcessor}'s.
     *
     * @return this VectorProcessor represented as an array of {@link FloatProcessor}'s
     * @see #toFloatStack()
     */
    public FloatProcessor[] toFloatProcessors() {
        final FloatProcessor[] r = new FloatProcessor[numberOfValues];
        for (int i = 0; i < numberOfValues; ++i) {
            final FloatProcessor fp = new FloatProcessor(width, height);
            final float[] values = (float[]) fp.getPixels();
            for (int j = 0; j < values.length; j++) {
                values[j] = pixels[j][i];
            }
            r[i] = fp;
        }

        return r;
    }


    /**
     * Convert VectorProcessor to ImagePlus with FloatProcessor stack.
     *
     * @param labels labels to be assigned to slice in the stack.
     *               Number of labels must match number of pixel values (slices in the output stack).
     *               This argument cannot be {@code null}.
     * @return ImagePlus representation of this object.
     * @see #toFloatStack()
     * @see #toFloatProcessors()
     */
    public ImagePlus toFloatStack(final String[] labels) {
        Validate.argumentNotNull(labels, "labels");
        Validate.isTrue(labels.length == getNumberOfValues(),
                "Number of labels must match number of pixel values. " +
                        "Expecting " + getNumberOfValues() + " labels, got " + labels.length);

        final ImageStack stack = new ImageStack(width, height);
        final FloatProcessor[] fps = toFloatProcessors();
        for (int i = 0; i < fps.length; i++) {
            stack.addSlice(labels[i], fps[i]);
        }
        return new ImagePlus("From VectorProcessor", stack);
    }


    /**
     * Convert VectorProcessor to ImagePlus with FloatProcessor stack.
     * Slice labels are band numbers starting from 0.
     *
     * @return ImagePlus representation of this object.
     * @see #toFloatStack(String[])
     * @see #toFloatProcessors()
     */
    public ImagePlus toFloatStack() {
        final String[] labels = new String[getNumberOfValues()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = "band " + i;
        }
        return toFloatStack(labels);
    }


    private static ImageStack convertToFloatStack(final ImagePlus src) {
        // TODO: remove duplicate method in KMeansClusteringPlugin

        final ImagePlus imp = duplicate(src);

        // Remember scaling setup
        final boolean doScaling = ImageConverter.getDoScaling();

        try {
            // Disable scaling
            ImageConverter.setDoScaling(false);

            if (imp.getType() == ImagePlus.COLOR_RGB) {
                if (imp.getStackSize() > 1) {
                    throw new RuntimeException("Unsupported image type: stack of COLOR_RGB");
                }
                final ImageConverter converter = new ImageConverter(imp);
                converter.convertToRGBStack();
            }

            if (imp.getStackSize() > 1) {
                final StackConverter converter = new StackConverter(imp);
                converter.convertToGray32();
            } else {
                final ImageConverter converter = new ImageConverter(imp);
                converter.convertToGray32();
            }

            // FIXME: make sure that there are no memory leaks
//            imp.flush();
            return imp.getStack();
        } finally {
            // Restore original scaling option
            ImageConverter.setDoScaling(doScaling);
        }
    }


    private static ImagePlus duplicate(final ImagePlus imp) {
        final Duplicator duplicator = new Duplicator();
        return duplicator.run(imp);
    }


    /**
     * Return pixel value at coordinates (<code>x</code>, <code>y</code>).
     *
     * @param x x
     * @param y y
     * @return pixel value.
     */
    public float[] get(final int x, final int y) {
        return get(x, y, null);
    }


    /**
     * Return pixel value at coordinates (<code>x</code>, <code>y</code>).  Use {@code dest} to store the value.
     *
     * @param x    x
     * @param y    y
     * @param dest array to store pixel value, can be {@code null}.
     * @return pixel value. If {@code dest} is not {@code null} it will be returned.
     */
    public float[] get(final int x, final int y, float[] dest) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Value of coordinates (x,y)=(" + x + "," + y
                    + ") is out of range [" + width + "," + height + "].");
        }
        if (dest == null) {
            dest = new float[numberOfValues];
        } else {
            if (dest.length != numberOfValues) {
                throw new IllegalArgumentException("Invalid length of array dest.");
            }
        }
        final int offset = x + y * width;
        final float[] v = pixels[offset];
        System.arraycopy(v, 0, dest, 0, v.length);
        return dest;
    }


    /**
     * Set value of pixel value at coordinates (<code>x</code>, <code>y</code>).
     *
     * @param x x
     * @param y y
     * @param v pixel value
     */
    public void set(final int x, final int y, final float[] v) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Value of coordinates (x,y) is out of range.");
        }
        Validate.argumentNotNull(v, "v");
        if (v.length != numberOfValues) {
            throw new IllegalArgumentException("Invalid size of argument 'v' expecting " + numberOfValues
                    + ", got " + v.length + ".");
        }
        final int offset = x + y * width;
        final float[] s = pixels[offset];
        System.arraycopy(v, 0, s, 0, v.length);
    }


    public VectorProcessor duplicate() {
        final VectorProcessor r = new VectorProcessor(this.width, this.height, this.numberOfValues);
        r.roi = (Rectangle) (roi != null ? roi.clone() : null);
        // TODO: ignore progress bar?
        r.progressBar = null;

        // copy data
        for (int i = 0; i < pixels.length; ++i) {
            System.arraycopy(pixels[i], 0, r.pixels[i], 0, numberOfValues);
        }

        return r;
    }


    /**
     * Represents 3x3 neighborhood. the center pixel is <code>p5</code>. Pixels <code>p1</code> to
     * <code>p3</code> are in the top row, <code>p4</code> to <code>p6</code> in the middle, and
     * <code>p7</code> to <code>p9</code> in the bottom of the neighborhood.
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
     * Iterator over pixel values.
     */
    public class PixelIterator implements java.util.Iterator<float[]> {

        final int xMin = roi.x;
        final int xMax1 = roi.x + roi.width - 1;
        final int rowOffset = width;
        final int yMin = roi.y;
        final int yMax1 = roi.y + roi.height - 1;
        int x = roi.x - 1;
        int y = roi.y;


        private PixelIterator() {
        }


        public int getX() {
            if (x < xMin || x > xMax1) {
                throw new IllegalStateException("Illegal value of x, " + x + ".");
            }
            return x;
        }


        public int getY() {
            if (y < yMin || y > yMax1) {
                throw new IllegalStateException("Illegal value of y, " + y + ".");
            }
            return y;
        }


        @Override
        public boolean hasNext() {
            return x < xMax1 || y < yMax1;
        }


        @Override
        public float[] next() {
            // Update center location
            if (x < xMax1) {
                ++x;
            } else {
                if (y < yMax1) {
                    x = xMin;
                    ++y;
                }
                if (progressBar != null) {
                    progressBar.show(y - yMin, yMax1 - yMin);
                }
            }

            final int offset = x + y * width;

            return pixels[offset];
        }


        /**
         * Not supported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Method remove() not supported.");
        }


        public int getOffset() {
            return x + y * width;
        }
    }


    /**
     * Iterator over 3x3 neighborhood of vector valued pixels.
     */
    public class Iterator implements java.util.Iterator<Neighborhood3x3> {

        final int xMin = Math.max(roi.x, 1);
        final int xMax = Math.min(roi.x + roi.width, width - 1) - 1;
        final int rowOffset = width;
        final int yMin = Math.max(roi.y, 1);
        final int yMax = Math.min(roi.y + roi.height, height - 1) - 1;
        int x = xMin - 1;
        int y = yMin;
        final Neighborhood3x3 neighborhood3x3 = new Neighborhood3x3();


        private Iterator() {
        }


        @Override
        public boolean hasNext() {
            return x < xMax || y < yMax;
        }


        @Override
        public Neighborhood3x3 next() {
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
            final int offset = x + y * width;

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
         * Not supported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Method remove() not supported.");
        }

    }
}
