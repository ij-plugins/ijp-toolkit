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
package net.sf.ij_plugins.im3d;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

/**
 * Utility methods for 3D objects.
 *
 * @author Jarek Sacha
 * @since April 30, 2002
 */

public class Util {
    /**
     * Constructor for the Util object
     */
    private Util() {
    }


    /**
     * Computes a bounding box for an image. A bounding box attempts to exclude voxels with value
     * zero that are close to image borders.
     *
     * @param src Input image (GRAY8).
     * @return Bounding box for non-zero voxels.
     */
    public static Box3D getBoundingBox(final ImageStack src) {
        final int w = src.getWidth();
        final int h = src.getHeight();
        final int d = src.getSize();

        final Object[] pixels = src.getImageArray();

        // Minimal corner
        int xMin = w;
        int yMin = h;
        int zMin = d;
        int xMax = -1;
        int yMax = -1;
        int zMax = -1;
        for (int z = 0; z < d; ++z) {
            final byte[] slice = (byte[]) pixels[z];
            for (int y = 0; y < h; ++y) {
                final int offset = y * w;
                for (int x = 0; x < w; ++x) {
                    final int v = slice[x + offset] & 0xff;
                    if (v > 0) {
                        if (xMin > x) {
                            xMin = x;
                        }
                        if (yMin > y) {
                            yMin = y;
                        }
                        if (zMin > z) {
                            zMin = z;
                        }
                        if (xMax < x) {
                            xMax = x;
                        }
                        if (yMax < y) {
                            yMax = y;
                        }
                        if (zMax < z) {
                            zMax = z;
                        }
                    }
                }
            }
        }

        final Box3D bb = new Box3D();
        bb.x = xMin;
        bb.y = yMin;
        bb.z = zMin;
        bb.width = xMax - xMin + 1;
        bb.height = yMax - yMin + 1;
        bb.depth = zMax - zMin + 1;

        if (ij.IJ.debugMode) {
            System.out.println("Bounding box " + bb.origin() + " by ["
                    + bb.width + "," + bb.height + "," + bb.depth + "].");
        }

        return bb;
    }


    /**
     * Create new image of the same type and size as the input image.
     *
     * @param src Input image.
     * @return Duplicate of <code>src</code> without copying voxel values.
     */
    public static ImageStack duplicateEmpty(final ImageStack src) {
        final int xSize = src.getWidth();
        final int ySize = src.getHeight();
        final int zSize = src.getSize();

        final ImageStack dest = new ImageStack(xSize, ySize);
        for (int z = 1; z <= zSize; ++z) {
            dest.addSlice(src.getSliceLabel(z),
                    src.getProcessor(z).createProcessor(xSize, ySize));
        }

        dest.setColorModel(src.getColorModel());

        return dest;
    }


    /**
     * Clip image <code>src</code> to volume of interest <code>voi</code>.
     *
     * @param src Input image.
     * @param voi Volume of interest.
     * @return Clipped image.
     */
    public static ImageStack clip(final ImageStack src, final Box3D voi) {

        final ImageStack dest = new ImageStack(voi.width, voi.height);

        for (int i = voi.z + 1; i <= (voi.z + voi.depth); ++i) {
            final ImageProcessor ip = src.getProcessor(i);
            ip.setRoi(voi.x, voi.y, voi.width, voi.height);
            dest.addSlice(src.getSliceLabel(i), ip.crop());
        }

        return dest;
    }


    /**
     * Decode value of the origin property of image <code>imp</code>. The origin is stored in
     * property values 'origin.x', 'origin.y', and 'origin.z'. If a properly is missing or cannot be
     * parsed as a number it is assumed to be equal zero.
     *
     * @param imp Input image.
     * @return Point representing the origin.
     */
    public static Point3D decodeOrigin(final ImagePlus imp) {
        final Point3D origin = new Point3D();
        final Calibration calibration = imp.getCalibration();
        origin.x = (float) calibration.xOrigin;
        origin.y = (float) calibration.yOrigin;
        origin.z = (float) calibration.zOrigin;

        return origin;
    }


    /**
     * Add offset to the value of the origin property of image <code>imp</code>.
     *
     * @param imp    Input image.
     * @param offset Value of the offset.
     * @see net.sf.ij_plugins.im3d.Util#decodeOrigin
     */
    public static void offsetOrigin(final ImagePlus imp, final Point3D offset) {
        final Calibration calibration = imp.getCalibration();
        calibration.xOrigin += offset.x;
        calibration.yOrigin += offset.y;
        calibration.zOrigin += offset.z;
        imp.setCalibration(calibration);
    }


    /**
     * Add origin property to image <code>imp</code>.
     *
     * @param imp    Input image.
     * @param origin New value of the origin.
     * @see net.sf.ij_plugins.im3d.Util#decodeOrigin
     */
    public static void encodeOrigin(final ImagePlus imp, final Point3D origin) {
        final Calibration calibration = imp.getCalibration();
        calibration.xOrigin = origin.x;
        calibration.yOrigin = origin.y;
        calibration.zOrigin = origin.z;
        imp.setCalibration(calibration);
    }

}
