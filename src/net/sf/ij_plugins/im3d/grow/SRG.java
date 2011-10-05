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

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
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
public final class SRG extends SRG2DBase {

    // External properties
    private FloatProcessor image;
    private float[] imagePixels;


    /**
     * Set image to be segmented. Supported types are: {@link ByteProcessor}, {@link ShortProcessor}, and {@link FloatProcessor}.
     *
     * @param image image.
     */
    public void setImage(final ImageProcessor image) {
        Validate.argumentNotNull(image, "image");
        if (image instanceof ByteProcessor) {
            setImage((ByteProcessor) image);
        } else if (image instanceof ShortProcessor) {
            setImage((ShortProcessor) image);
        } else if (image instanceof FloatProcessor) {
            setImage((FloatProcessor) image);
        } else {
            throw new IllegalArgumentException("Unsupported image type: " + image.getClass().getName());
        }
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ByteProcessor image) {
        Validate.argumentNotNull(image, "image");
        this.image = (FloatProcessor) image.convertToFloat();
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final ShortProcessor image) {
        Validate.argumentNotNull(image, "image");
        this.image = (FloatProcessor) image.convertToFloat();
    }


    /**
     * Set image to be segmented.
     *
     * @param image image.
     */
    public void setImage(final FloatProcessor image) {
        Validate.argumentNotNull(image, "image");
        this.image = (FloatProcessor) image.duplicate();
    }


    @Override
    protected double distanceFromMean(final int offset, final RegionInfo regionInfo) {
        return Math.abs(imagePixels[offset] - ((RegionInfoScalar) regionInfo).mean());
    }


    @Override
    protected void initializeImageStructures() {
        xSize = image.getWidth();
        ySize = image.getHeight();
        imagePixels = (float[]) image.getPixels();
    }


    @Override
    protected RegionInfo newRegionInfo(final int originalSeedID) {
        return new RegionInfoScalar(image, originalSeedID);
    }


    private static class RegionInfoScalar extends RegionInfo {

        private long pointCount;
        private double sumIntensity;
        private final FloatProcessor image;


        public RegionInfoScalar(final FloatProcessor image, final int originalSeedID) {
            super(originalSeedID);
            this.image = image;
        }


        @Override
        public void addPoint(final Point point) {
            ++pointCount;
            sumIntensity += image.getf(point.x, point.y);
        }


        public double mean() {
            if (pointCount == 0) {
                return 0;
            } else {
                return sumIntensity / pointCount;
            }
        }
    }
}
