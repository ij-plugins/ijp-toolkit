/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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

package net.sf.ij_plugins.im3d.morphology;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.RankFilters;
import net.sf.ij_plugins.im3d.Util;


/**
 * Morphological operations in 3D.
 *
 * @author Jarek Sacha
 * @since April 30, 2002
 */

public class Morpho {

    private final static int MIN_VALUE = 0;
    private final static int MAX_VALUE = 255;

    private byte[][] srcPixels = null;
    private byte[][] destPixels = null;
    private int xSize;
    private int zSize;
    private int xMin, xMax;
    private int yMin, yMax;
    private int zMin, zMax;


    /**
     * Compute 3D morphological dilation (max) of <code>src</code> image.
     *
     * @param src input image
     * @return dilation filtered input image.
     */
    public static ImagePlus dilate(final ImagePlus src) {
        final ImageStack srcStack = src.getStack();
        final ImageStack destStack = Util.duplicateEmpty(srcStack);
        new Morpho().dilate(srcStack, destStack);
        final ImagePlus dest = src.createImagePlus();
        dest.setStack(destStack);
        dest.setTitle(src.getTitle() + "+Dilate3D");
        return dest;
    }


    /**
     * Perform morphological dilation (max) of <code>src</code> image, write results to
     * <code>dest</code> image. <code>src</code> and <code>dest</code> must be of the same type and
     * size.
     *
     * @param src  Source image.
     * @param dest Destination image.
     */
    public void dilate(final ImageStack src, final ImageStack dest) {

        initialize(src, dest);

        // Iterate through ROI pixels
        IJ.showProgress(0);
        for (int z = zMin; z < zMax; ++z) {

            final byte[] thisDestSlice = destPixels[z];
            for (int y = yMin; y < yMax; ++y) {

                final int destOffset = y * xSize;
                for (int x = xMin; x < xMax; ++x) {

                    // Iterate through neighborhood and find minimum value
                    int maxValue = MIN_VALUE;
                    for (int dz = -1; dz <= 1; ++dz) {
                        final int zz = z + dz;
                        if (zz < zMin || zz >= zMax) {
                            continue;
                        }
                        final byte[] thisNhbSlice = srcPixels[zz];
                        for (int dy = -1; dy <= 1; ++dy) {
                            final int yy = y + dy;
                            if (yy < yMin || yy >= yMax) {
                                continue;
                            }
                            final int nhbOffset = yy * xSize;
                            for (int dx = -1; dx <= 1; ++dx) {
                                final int xx = x + dx;
                                if (xx < xMin || xx >= xMax) {
                                    continue;
                                }

                                final int value = thisNhbSlice[nhbOffset + xx] & 0xff;
                                if (value > maxValue) {
                                    maxValue = value;
                                }
                            }
                        }
                    }

                    thisDestSlice[destOffset + x] = (byte) (maxValue & 0xff);
                }
            }
            IJ.showProgress((z + 1.0) / zSize);
        }
    }


    /**
     * Compute 3D morphological erosion (min) of <code>src</code> image.
     *
     * @param src input image
     * @return dilation filtered input image.
     */
    public static ImagePlus erode(final ImagePlus src) {
        final ImageStack srcStack = src.getStack();
        final ImageStack destStack = Util.duplicateEmpty(srcStack);
        new Morpho().erode(srcStack, destStack);
        final ImagePlus dest = src.createImagePlus();
        dest.setStack(destStack);
        dest.setTitle(src.getTitle() + "+Erode3D");
        return dest;
    }


    /**
     * Perform morphological erosion (min) of <code>src</code> image, write results to
     * <code>dest</code> image. <code>src</code> and <code>dest</code> must be of the same type and
     * size.
     *
     * @param src  Source image.
     * @param dest Destination image.
     */
    public void erode(final ImageStack src, final ImageStack dest) {

        initialize(src, dest);

        // Iterate through ROI pixels
        IJ.showProgress(0);
        for (int z = zMin; z < zMax; ++z) {

            final byte[] thisDestSlice = destPixels[z];
            for (int y = yMin; y < yMax; ++y) {

                final int destOffset = y * xSize;
                for (int x = xMin; x < xMax; ++x) {

                    // Iterate through neighborhood and find minimum value
                    int minValue = MAX_VALUE;
                    for (int dz = -1; dz <= 1; ++dz) {
                        final int zz = z + dz;
                        if (zz < zMin || zz >= zMax) {
                            continue;
                        }
                        final byte[] thisNhbSlice = srcPixels[zz];
                        for (int dy = -1; dy <= 1; ++dy) {
                            final int yy = y + dy;
                            if (yy < yMin || yy >= yMax) {
                                continue;
                            }
                            final int nhbOffset = yy * xSize;
                            for (int dx = -1; dx <= 1; ++dx) {
                                final int xx = x + dx;
                                if (xx < xMin || xx >= xMax) {
                                    continue;
                                }

                                final int value = thisNhbSlice[nhbOffset + xx] & 0xff;
                                if (value < minValue) {
                                    minValue = value;
                                }
                            }
                        }
                    }

                    thisDestSlice[destOffset + x] = (byte) (minValue & 0xff);
                }
            }
            IJ.showProgress((z + 1.0) / zSize);
        }
    }


    /**
     * Compute 3D median filter of an image, assuming equally sized pixels
     *
     * @param src input image
     * @return median filtered input image.
     */
    public static ImagePlus median(final ImagePlus src) {
        final ImageStack destStack = median(src.getStack());
        final ImagePlus dest = src.createImagePlus();
        dest.setStack(destStack);
        dest.setTitle(src.getTitle() + "+Median3D");
        return dest;
    }


    public static ImageStack median(final ImageStack src) {
        final ImageStack dest = Util.duplicateEmpty(src);
        new Morpho().median(src, dest);
        return dest;
    }


    /**
     * Perform morphological erosion (min) of <code>src</code> image, write results to
     * <code>dest</code> image. <code>src</code> and <code>dest</code> must be of the same type and
     * size.
     *
     * @param src  Source image.
     * @param dest Destination image.
     */
    public void median(final ImageStack src, final ImageStack dest) {

        initialize(src, dest);

        // Iterate through ROI pixels
        IJ.showProgress(0);
        for (int z = zMin; z < zMax; ++z) {

            final byte[] thisDestSlice = destPixels[z];
            for (int y = yMin; y < yMax; ++y) {

                final int destOffset = y * xSize;
                for (int x = xMin; x < xMax; ++x) {

                    // Iterate through neighborhood and accumulate values for median computation
                    final float[] values = new float[3 * 3 * 3];
                    int nbValues = 0;
                    for (int dz = -1; dz <= 1; ++dz) {
                        final int zz = z + dz;
                        if (zz < zMin || zz >= zMax) {
                            continue;
                        }
                        final byte[] thisNhbSlice = srcPixels[zz];
                        for (int dy = -1; dy <= 1; ++dy) {
                            final int yy = y + dy;
                            if (yy < yMin || yy >= yMax) {
                                continue;
                            }
                            final int nhbOffset = yy * xSize;
                            for (int dx = -1; dx <= 1; ++dx) {
                                final int xx = x + dx;
                                if (xx < xMin || xx >= xMax) {
                                    continue;
                                }

                                final int value = thisNhbSlice[nhbOffset + xx] & 0xff;
                                values[nbValues] = value;
                                nbValues++;
                            }
                        }
                    }

                    // Find median value
                    final int medianValue = median(values, nbValues);
                    thisDestSlice[destOffset + x] = (byte) (medianValue & 0xff);
                }
            }
            IJ.showProgress((z + 1.0) / zSize);
        }
    }


    static int median(final float[] values, final int nbValues) {
        final int m;
        if (nbValues < 1) {
            throw new IllegalArgumentException("Argument 'nbValues' cannot be less than 1.");
        }

        final int n = nbValues / 2;
        if (nbValues % 2 == 1) {
            m = Math.round(RankFilters.findNthLowestNumber(values, nbValues, n));
        } else {
            final float m1 = RankFilters.findNthLowestNumber(values, nbValues, n - 1);
            final float m2 = RankFilters.findNthLowestNumber(values, nbValues, n);
            m = Math.round((m1 + m2) / 2f);
        }

        return m;
    }

    /*
    *
    */


    private void initialize(final ImageStack src, final ImageStack dest) {
        xSize = src.getWidth();
        xMin = 0;
        xMax = xSize;
        final int ySize = src.getHeight();
        yMin = 0;
        yMax = ySize;
        zSize = src.getSize();
        zMin = 0;
        zMax = zSize;

        final Object[] srcImageArray = src.getImageArray();
        final Object[] destImageArray = dest.getImageArray();

        // Create pixel handles
        srcPixels = new byte[zSize][];
        destPixels = new byte[zSize][];
        for (int z = 0; z < zSize; ++z) {
            if (!(srcImageArray[z] instanceof byte[])) {
                throw new IllegalArgumentException("Expecting source stack of byte images.");
            }
            srcPixels[z] = (byte[]) srcImageArray[z];

            if (!(destImageArray[z] instanceof byte[])) {
                throw new IllegalArgumentException("Expecting destination stack of byte images.");
            }
            destPixels[z] = (byte[]) destImageArray[z];

        }
    }
}
