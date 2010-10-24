/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Pixel-based multi-band image segmentation using k-means clustering algorithm.
 *
 * @author Jarek Sacha
 */
public final class KMeans {

    private final Config config;

//    private Rectangle roi;
//    private ByteProcessor mask;

    private VectorProcessor vp;
    private float[][] clusterCenters;
    private ImageStack clusterAnimation;


    public KMeans() {
        this.config = new Config();
    }


    public KMeans(final Config config) {
        this.config = config.duplicate();
    }


    /**
     * Perform k-means clustering of the input <code>stack</code>. Elements of the
     * <code>stack</code> must be of type <code>FloatProcessor</code>.
     *
     * @param stack stack representing a multi-band image.
     * @return segmented image.
     */
    public ByteProcessor run(final ImageStack stack) {

        if (stack.getSize() < 1) {
            throw new IllegalArgumentException("Input stack cannot be empty");
        }

        vp = new VectorProcessor(stack);

        // TODO: add support for using ROI. ROI of the first slice is applied to all slices.
//    Rectangle roi = stack.getProcessor(1).getRoi();
//    int[] mask = stack.getProcessor(1).getMask();

        // TODO Verify that ROI and mask are consistent with the input image.

        // Run clustering
        cluster();

        return encodeSegmentedImage();
    }


    /**
     * Return location of cluster centers.
     *
     * @return array of cluster centers. First index refers to cluster number.
     */
    public float[][] getClusterCenters() {
        return clusterCenters;
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


    /**
     * Returns stack where discovered clusters can be represented by replacing pixel values in a
     * cluster by the value of the centroid of that cluster.
     *
     * @return centroid value image
     */
    public ImageStack getCentroidValueImage() {
        if (clusterCenters == null) {
            throw new IllegalStateException("Need to perform clustering first.");
        }

        return encodeCentroidValueImage();
    }


    /**
     * Find index of the cluster closest to the sample {@code x}.
     * {@link #run(ij.ImageStack)} must be run before calling this method.
     *
     * @param x test point, number of values must be the same is input stack size.
     * @return index of the closest cluster.
     * @see #run(ij.ImageStack)
     */
    public int closestCluster(final float[] x) {
        if (clusterCenters == null) {
            throw new IllegalStateException("Cluster centers not computed, call run(ImageStack) first.");
        }
        Validate.argumentNotNull(x, "x");
        Validate.isTrue(x.length == vp.getNumberOfValues(),
                "Expecting argument 'x' of length " + vp.getNumberOfValues() + ", got " + x.length + ".");

        return KMeansUtils.closestCluster(x, clusterCenters);
    }


    private ByteProcessor encodeSegmentedImage() {
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


    private ImageStack encodeCentroidValueImage() {
        return KMeansUtils.encodeCentroidValueImage(clusterCenters, vp);
    }


    private void printClusters(final String message) {
        IJ.log(message);
        for (final float[] clusterCenter : clusterCenters) {
            final StringBuffer buffer = new StringBuffer("  (");
            for (final float vv : clusterCenter) {
                buffer.append(" ").append(vv).append(" ");
            }
            buffer.append(")");
            IJ.log(buffer.toString());
        }
    }


    /**
     *
     */
    private void cluster() {

        // Select initial partitioning - initialize cluster centers
        clusterCenters = initializeClusterCenters();
        if (config.isPrintTraceEnabled()) {
            printClusters("Initial clusters");
        }

        if (config.clusterAnimationEnabled) {
            clusterAnimation = new ImageStack(vp.getWidth(), vp.getHeight());
            clusterAnimation.addSlice("Initial", encodeSegmentedImage());
        }


        // Optimize cluster centers
        boolean converged = false;
        long count = 0;
        while (!converged) {

            final MeanElement[] newClusterMeans = new MeanElement[config.getNumberOfClusters()];
            for (int i = 0; i < newClusterMeans.length; i++) {
                newClusterMeans[i] = new MeanElement(vp.getNumberOfValues());
            }

            // Generate a new partition by assigning each pattern to its closest cluster center
            // Compute new cluster centers as the centroids of the clusters
            final VectorProcessor.PixelIterator iterator = vp.pixelIterator();
            while (iterator.hasNext()) {
                final float[] v = iterator.next();
                final int c = KMeansUtils.closestCluster(v, clusterCenters);
                newClusterMeans[c].add(v);
            }

            // Check for convergence
            float distanceSum = 0;
            for (int i = 0; i < clusterCenters.length; i++) {
                final float[] clusterCenter = clusterCenters[i];
                final float[] newClusterCenter = newClusterMeans[i].mean();
                distanceSum += KMeansUtils.distance(clusterCenter, newClusterCenter);
            }

            converged = distanceSum < config.getTolerance();

            for (int i = 0; i < clusterCenters.length; i++) {
                clusterCenters[i] = newClusterMeans[i].mean();
            }

            ++count;

            final String message = "k-means iteration " + count + ", cluster error: " + distanceSum;
            IJ.showStatus(message);
            if (config.isPrintTraceEnabled()) {
                printClusters(message);
            }

            if (config.clusterAnimationEnabled) {
                clusterAnimation.addSlice("Iteration " + count, encodeSegmentedImage());
            }
        }
    }


    /**
     * Initialize clusters using k-means++ approach, see http://en.wikipedia.org/wiki/K-means++
     *
     * @return initial cluster centers.
     */
    private float[][] initializeClusterCenters() {
        final Random random = createRandom();

        final int nbClusters = config.getNumberOfClusters();
        final int width = vp.getWidth();
        final int height = vp.getHeight();
        final int nbPixels = width * height;

        // Cluster centers
        final List<float[]> centers = new ArrayList<float[]>();
        // Location of pixels used as cluster centers
        final List<Point> centerLocation = new ArrayList<Point>();

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
                final double d = KMeansUtils.distance(v, centersArray[cci]);
                sum += d * d;
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


    private Random createRandom() {
        return config.isRandomizationSeedEnabled()
                ? new Random(config.getRandomizationSeed())
                : new Random();
    }


    private static Point toPoint(final int offset, final int width) {
        final int y = offset / width;
        final int x = offset - y * width;
        return new Point(x, y);
    }


    /**
     *
     */
    private static final class MeanElement {

        private final double[] sum;
        private int count;


        public MeanElement(final int elementSize) {
            sum = new double[elementSize];
        }


        public void add(final float[] x) {
            if (x.length != sum.length) {
                throw new java.lang.IllegalArgumentException("Invalid element size, got " + x.length + ", expecting" + sum.length);
            }

            for (int i = 0; i < x.length; i++) {
                sum[i] += x[i];
            }
            ++count;
        }


        public float[] mean() {
            final float[] r = new float[sum.length];
            for (int i = 0; i < r.length; i++) {
                r[i] = (float) (sum[i] / count);
            }

            return r;
        }
    }


    /**
     * Configurable parameters of the k-means algorithm.
     */
    public static final class Config implements java.lang.Cloneable {

        /**
         * Seed used to initialize random number generator.
         */
        private int randomizationSeed = 48;
        private boolean randomizationSeedEnabled = true;
        private double tolerance = 0.0001;
        private int numberOfClusters = 4;
        private boolean clusterAnimationEnabled;
        private boolean printTraceEnabled;


        public int getRandomizationSeed() {
            return randomizationSeed;
        }


        public void setRandomizationSeed(final int randomizationSeed) {
            this.randomizationSeed = randomizationSeed;
        }


        /**
         * If <code>true</code>, random number generator will be initialized with a
         * <code>randomizationSeed</code>. If <code>false</code> random number generator will be
         * initialized using 'current' time.
         *
         * @return {@code true} when randomization seed is enabled.
         * @see #getRandomizationSeed()
         */
        public boolean isRandomizationSeedEnabled() {
            return randomizationSeedEnabled;
        }


        public void setRandomizationSeedEnabled(final boolean randomizationSeedEnabled) {
            this.randomizationSeedEnabled = randomizationSeedEnabled;
        }


        public int getNumberOfClusters() {
            return numberOfClusters;
        }


        public void setNumberOfClusters(final int numberOfClusters) {
            this.numberOfClusters = numberOfClusters;
        }


        /**
         * Return tolerance used to determine cluster centroid distance. This tolerance is used to
         * determine if a centroid changed location between iterations.
         *
         * @return cluster centroid location tolerance.
         */
        public double getTolerance() {
            return tolerance;
        }


        public void setTolerance(final float tolerance) {
            this.tolerance = tolerance;
        }


        /**
         * Return <code>true</code> if when an animation illustrating cluster optimization is
         * enabled.
         *
         * @return {@code true} when cluster animation is enabled.
         */
        public boolean isClusterAnimationEnabled() {
            return clusterAnimationEnabled;
        }


        public void setClusterAnimationEnabled(final boolean clusterAnimationEnabled) {
            this.clusterAnimationEnabled = clusterAnimationEnabled;
        }


        /**
         * Return <code>true</code> if a trace is printed to the ImageJ's Result window.
         *
         * @return {@code true} when printing of trace is enabled.
         */
        public boolean isPrintTraceEnabled() {
            return printTraceEnabled;
        }


        public void setPrintTraceEnabled(final boolean printTraceEnabled) {
            this.printTraceEnabled = printTraceEnabled;
        }


        /**
         * Make duplicate of this object. This a convenience wrapper for {@link #clone()} method.
         *
         * @return duplicate of this object.
         */
        public Config duplicate() {
            try {
                return (Config) this.clone();
            } catch (final java.lang.CloneNotSupportedException e) {
                throw new java.lang.RuntimeException("Error cloning object of class " + getClass().getName() + ".", e);
            }
        }
    }

}
