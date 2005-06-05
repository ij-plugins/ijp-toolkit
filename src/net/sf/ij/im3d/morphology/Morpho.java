/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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
package net.sf.ij.im3d.morphology;

import ij.IJ;
import ij.ImageStack;

/**
 * Morphological operations in 3D.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @created April 30, 2002
 */

public class Morpho {
    private final static int MIN_VALUE = 0;
    private final static int MAX_VALUE = 255;

    private byte[][] srcPixels = null;
    private byte[][] destPixels = null;
    private int xSize
    ,
    ySize
    ,
    zSize;
    private int xMin
    ,
    xMax;
    private int yMin
    ,
    yMax;
    private int zMin
    ,
    zMax;


    /**
     * Perform morphological erosion (min) of <code>src</code> image, write results to
     * <code>dest</code> image. <code>src</code> and <code>dest</code> must be of the same type and
     * size.
     *
     * @param src  Source image.
     * @param dest Destination image.
     */
    public void dilate(ImageStack src, ImageStack dest) {

        initialize(src, dest);

        // Iterate through ROI pixels
        IJ.showProgress(0);
        for (int z = zMin; z < zMax; ++z) {

            byte[] thisDestSlice = destPixels[z];
            for (int y = yMin; y < yMax; ++y) {

                int destOffset = y * xSize;
                for (int x = xMin; x < xMax; ++x) {

                    // Iterate through neighborhood and find minimum value
                    int maxValue = MIN_VALUE;
                    for (int dz = -1; dz <= 1; ++dz) {
                        int zz = z + dz;
                        if (zz < zMin || zz >= zMax) {
                            continue;
                        }
                        byte[] thisNhbSlice = srcPixels[zz];
                        for (int dy = -1; dy <= 1; ++dy) {
                            int yy = y + dy;
                            if (yy < yMin || yy >= yMax) {
                                continue;
                            }
                            int nhbOffset = yy * xSize;
                            for (int dx = -1; dx <= 1; ++dx) {
                                int xx = x + dx;
                                if (xx < xMin || xx >= xMax) {
                                    continue;
                                }

                                int value = thisNhbSlice[nhbOffset + xx] & 0xff;
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
     * Perform morphological erosion (min) of <code>src</code> image, write results to
     * <code>dest</code> image. <code>src</code> and <code>dest</code> must be of the same type and
     * size.
     *
     * @param src  Source image.
     * @param dest Destination image.
     */
    public void erode(ImageStack src, ImageStack dest) {

        initialize(src, dest);

        // Iterate through ROI pixels
        IJ.showProgress(0);
        for (int z = zMin; z < zMax; ++z) {

            byte[] thisDestSlice = destPixels[z];
            for (int y = yMin; y < yMax; ++y) {

                int destOffset = y * xSize;
                for (int x = xMin; x < xMax; ++x) {

                    // Iterate through neighborhood and find minimum value
                    int minValue = MAX_VALUE;
                    for (int dz = -1; dz <= 1; ++dz) {
                        int zz = z + dz;
                        if (zz < zMin || zz >= zMax) {
                            continue;
                        }
                        byte[] thisNhbSlice = srcPixels[zz];
                        for (int dy = -1; dy <= 1; ++dy) {
                            int yy = y + dy;
                            if (yy < yMin || yy >= yMax) {
                                continue;
                            }
                            int nhbOffset = yy * xSize;
                            for (int dx = -1; dx <= 1; ++dx) {
                                int xx = x + dx;
                                if (xx < xMin || xx >= xMax) {
                                    continue;
                                }

                                int value = thisNhbSlice[nhbOffset + xx] & 0xff;
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


    /*
    *
    */
    private void initialize(ImageStack src, ImageStack dest) {
        xSize = src.getWidth();
        xMin = 0;
        xMax = xSize;
        ySize = src.getHeight();
        yMin = 0;
        yMax = ySize;
        zSize = src.getSize();
        zMin = 0;
        zMax = zSize;

        Object[] srcImageArray = src.getImageArray();
        Object[] destImageArray = dest.getImageArray();

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
