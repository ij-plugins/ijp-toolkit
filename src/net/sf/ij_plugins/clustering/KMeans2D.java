/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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

package net.sf.ij_plugins.clustering;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 2D version of the k-means algorithm. Works with multi-band images.
 *
 * @author Jarek Sacha
 */
public final class KMeans2D extends KMeans<ByteProcessor> {

//    private Rectangle roi;
//    private ByteProcessor mask;

    private VectorProcessor vp;
    private ImageStack clusterAnimation;

    public KMeans2D() {
        this(new KMeansConfig());
    }

    public KMeans2D(final KMeansConfig config) {
        super(config);
    }

    static ByteProcessor encodeSegmentedImage(final VectorProcessor vp, final float[][] clusterCenters) {
        // Encode output image
        final ByteProcessor dest = new ByteProcessor(vp.getWidth(), vp.getHeight());
        final VectorProcessor.PixelIterator iterator = vp.pixelIterator();
        while (iterator.hasNext()) {
            final float[] v = iterator.next();
            final int c = KMeansUtils.closestCluster(v, clusterCenters);
            dest.putPixel(iterator.getX(), iterator.getY(), c);
        }
        return dest;
    }

    private static Point toPoint(final int offset, final int width) {
        final int y = offset / width;
        final int x = offset - y * width;
        return new Point(x, y);
    }

    /**
     * Perform k-means clustering of the input <code>stack</code>. Elements of the
     * <code>stack</code> must be of type <code>FloatProcessor</code>.
     *
     * @param stack stack representing a multi-band image.
     * @return segmented image.
     */
    public ByteProcessor run(final ImageStack stack) {

        Validate.isTrue(stack.getSize() > 0, "Input stack cannot be empty.");
        Validate.isTrue(
                stack.getProcessor(1) instanceof FloatProcessor,
                "Slices on the stack must be floating point images (FloatProcessor).");

        vp = new VectorProcessor(stack);

        // TODO: add support for using ROI. ROI of the first slice is applied to all slices.
//    Rectangle roi = stack.getProcessor(1).getRoi();
//    int[] mask = stack.getProcessor(1).getMask();

        // TODO Verify that ROI and mask are consistent with the input image.

        // Run clustering
        cluster();

        return encodeSegmentedImage(vp, clusterCenters);
    }

    /**
     * Return stack representing clustering optimization. This will return not <code>null</code>
     * value only when configuration parameters <code>clusterAnimationEnabled</code> is set to
     * true.
     *
     * @return stack representing cluster optimization, can return <code>null</code>.
     */
    public ImageStack getClusterAnimation() {
        return clusterAnimation;
    }

    protected int numberOfValues() {
        return vp.getNumberOfValues();
    }

    protected ImageStack encodeCentroidValueImage() {
        return KMeansUtils.encodeCentroidValueImage(clusterCenters, vp);
    }

    protected java.util.Iterator<float[]> newPixelIterator() {
        return vp.pixelIterator();
    }

    protected float[][] initializeClusterCenters() {
        final Random random = createRandom();

        final int nbClusters = config.getNumberOfClusters();
        final int width = vp.getWidth();
        final int height = vp.getHeight();
        final int nbPixels = width * height;

        // Cluster centers
        final List<float[]> centers = new ArrayList<>();
        // Location of pixels used as cluster centers
        final List<Point> centerLocation = new ArrayList<>();

        // Choose one center uniformly at random from among pixels
        {
            final Point p = toPoint(random.nextInt(nbPixels), width);
            centerLocation.add(p);
            centers.add(vp.get(p.x, p.y));
        }

        final double[] dp2 = new double[nbPixels];
        while (centers.size() < nbClusters) {
            assert centers.size() == centerLocation.size();

            // For each data point p compute D(p), the distance between p and the nearest center that
            // has already been chosen.
            double sum = 0;
            final float[][] centersArray = centers.toArray(new float[centers.size()][]);
            for (int offset = 0; offset < nbPixels; offset++) {
                final Point p = toPoint(offset, width);

                // Test that this is not a repeat of already selected center
                if (centerLocation.contains(p)) {
                    continue;
                }

                // Distance to closest cluster
                final float[] v = vp.get(p.x, p.y);
                final int cci = KMeansUtils.closestCluster(v, centersArray);
                sum += KMeansUtils.distanceSqr(v, centersArray[cci]);
                dp2[offset] = sum;
            }


            // Add one new data point at random as a new center, using a weighted probability distribution where
            // a point p is chosen with probability proportional to D(p)^2
            final double r = random.nextDouble() * sum;
            for (int offset = 0; offset < nbPixels; offset++) {
                final Point p = toPoint(offset, width);

                // Test that this is not a repeat of already selected center
                if (centerLocation.contains(p)) {
                    continue;
                }

                if (dp2[offset] >= r) {
                    centerLocation.add(p);
                    final float[] v = vp.get(p.x, p.y);
                    centers.add(v);
                    break;
                }
            }
        }

        return centers.toArray(new float[centers.size()][]);
    }

    protected boolean supportsClusterAnimation() {
        return true;
    }

    protected void clusterAnimationInitialize() {
        clusterAnimation = new ImageStack(vp.getWidth(), vp.getHeight());
    }

    protected void clusterAnimationAddCurrent(final String title) {
        clusterAnimation.addSlice(title, encodeSegmentedImage(vp, clusterCenters));
    }
}
