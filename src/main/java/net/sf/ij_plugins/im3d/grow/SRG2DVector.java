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

package net.sf.ij_plugins.im3d.grow;

import ij.process.ColorProcessor;
import net.sf.ij_plugins.multiband.VectorMath;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;


/**
 * <p>
 * Seeded region growing algorithm based on article by Rolf Adams and Leanne Bischof, "Seeded Region
 * Growing", <i>IEEE Transactions on Pattern Analysis and Machine Intelligence</i>, vol. 16, no. 6,
 * June 1994.
 * </p>
 * <p>
 * The algorithm assumes that seeds for objects and the background be provided.
 * Seeds are used to compute initial mean gray level for each region.
 * The condition of growth is difference of a gray level of a candidate pixel and mean grey level
 * intensity of a neighboring region.
 * At each step of the algorithm a candidate with a smallest difference to some neighboring
 * region is added to that region and all neighboring points of that that are not yet assigned to
 * any region are added to candidate list.
 * </p>
 * <p>
 * Part of the image that will be segmented can be restricted by setting a mask.
 * </p>
 * <p>
 * Progress of segmentation can be recorded on a animation stack.
 * To enable recording set {@code numberOfAnimationFrames} to a value larger than 0.
 * </p>
 * <p>
 * An example of segmenting an image is below. Seeds are set for three regions: background, blob 1, and blob 2.
 * </p>
 * <pre>
 * // Setup growing
 * final ByteProcessor image = ...;
 * final Point[][] seeds = {
 *         {new Point(107, 144)}, // Background
 *         {new Point(91, 159)},  // Blob 1
 *         {new Point(119, 143)}, // Blob 2
 *     };
 * final SRG srg = new SRG();
 * srg.setImage(image);
 * srg.setSeeds(seeds);
 * srg.setNumberOfAnimationFrames(50);
 * // Run growing
 * srg.run();
 * // Extract results
 * final ByteProcessor regionMask = srg.getRegionMarkers();
 * final ImageStack animationStack = srg.getAnimationStack();
 * </pre>
 *
 * @author Jarek Sacha
 */
public final class SRG2DVector extends SRG2DBase {

    private VectorProcessor image;
    private float[][] imagePixels;


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final VectorProcessor image) {
        Validate.argumentNotNull(image, "image");
        this.image = image.duplicate();
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ColorProcessor image) {
        Validate.argumentNotNull(image, "image");
        this.image = new VectorProcessor(image);
    }


    @Override
    protected void initializeImageStructures() {
        xSize = image.getWidth();
        ySize = image.getHeight();
        imagePixels = image.getPixels();
    }


    @Override
    protected double distanceFromMean(final int offset, final RegionInfo regionInfo) {
        final float[] value = imagePixels[offset];
        return VectorMath.distanceSqr(value, ((RegionInfoVector) regionInfo).mean());
    }


    @Override
    protected RegionInfo newRegionInfo(final int originalSeedID) {
        return new RegionInfoVector(image, originalSeedID);
    }


    private static class RegionInfoVector extends RegionInfo {

        private long pointCount;
        private final double[] sumIntensity;
        private final VectorProcessor image;


        public RegionInfoVector(final VectorProcessor image, final int originalSeedID) {
            super(originalSeedID);
            this.image = image;
            sumIntensity = new double[image.getNumberOfValues()];
        }


        @Override
        public void addPoint(final Point point) {
            ++pointCount;
            final float[] b = new float[sumIntensity.length];
            add(sumIntensity, image.get(point.x, point.y, b));
        }


        public double[] mean() {
            if (pointCount == 0) {
                return new double[sumIntensity.length];
            } else {
                return divide(sumIntensity, pointCount);
            }
        }


        private static void add(final double[] a, final float[] b) {
            for (int i = 0; i < a.length; i++) {
                a[i] += b[i];
            }
        }


        private static double[] divide(final double[] a, final double b) {
            final double[] r = new double[a.length];
            for (int i = 0; i < a.length; i++) {
                r[i] = a[i] / b;
            }

            return r;
        }

    }


}
