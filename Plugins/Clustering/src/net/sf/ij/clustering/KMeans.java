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
package net.sf.ij.clustering;

import ij.IJ;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;

import java.util.Random;

/**
 * Pixel-based multi-band image segmentation using k-means clustering
 * algorithm.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public final class KMeans {

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
        private boolean clusterAnimationEnabled = false;
        private boolean printTraceEnabled = false;

        public int getRandomizationSeed() {
            return randomizationSeed;
        }

        public void setRandomizationSeed(final int randomizationSeed) {
            this.randomizationSeed = randomizationSeed;
        }

        /**
         * If <code>true</code>, random number generator will be initalized with
         * a <code>randomizationSeed</code>. If <code>false</code> rundom number
         * generator will be initialized using 'current' time.
         *
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
         * Return tolerance used to determine cluster centroid distance. This
         * tolerance is used to determine if a centroid changed location between
         * iterations.
         *
         * @return cluster centroid location tolerunce.
         */
        public double getTolerance() {
            return tolerance;
        }

        public void setTolerance(final double tolerance) {
            this.tolerance = tolerance;
        }

        /**
         * Return <code>true</code> if when an animation ilustrating cluster
         * optimization is enabled.
         */
        public boolean isClusterAnimationEnabled() {
            return clusterAnimationEnabled;
        }

        public void setClusterAnimationEnabled(final boolean clusterAnimationEnabled) {
            this.clusterAnimationEnabled = clusterAnimationEnabled;
        }

        /**
         * Return <code>true</code> if a trace is printed to the ImageJ's Result
         * window.
         */
        public boolean isPrintTraceEnabled() {
            return printTraceEnabled;
        }

        public void setPrintTraceEnabled(final boolean printTraceEnabled) {
            this.printTraceEnabled = printTraceEnabled;
        }

        /**
         * Make duplicate of this object. This a convenience wrapper for {@link
         * #clone()} method.
         *
         * @return duplivcate of this object.
         */
        public Config duplicate() {
            try {
                return (Config) this.clone();
            } catch (java.lang.CloneNotSupportedException e) {
                throw new java.lang.RuntimeException("Error cloning object of class " + getClass().getName() + ".", e);
            }
        }

    }


    private static Config config = new Config();

    private float[][] bands;
    private int bandSize;
    private double[][] clusterCenters;
    private ImageStack clusterAnimation;
    private int width;
    private int height;

    public KMeans() {
    }

    public KMeans(final Config config) {
        KMeans.config = config.duplicate();
    }

    /**
     * Perform k-means clostering of the input <code>stack</code>. Elements of the
     * <code>stack</code> must be of type <code>FloatProcessor</code>.
     *
     * @param stack stack representing a multi-band image.
     */
    final public ByteProcessor run(final ImageStack stack) {

       if(stack.getSize() < 1) {
           throw new IllegalArgumentException("Input stack cannot be empty");
       }

       if(!(stack.getProcessor(1) instanceof FloatProcessor)) {
           throw new IllegalArgumentException("Input stack must contain FloatProcessors");
       }

        // TODO: add support for using ROI. ROI of the first slice is applied to all slices.
//    Rectangle roi = stack.getProcessor(1).getRoi();
//    int[] mask = stack.getProcessor(1).getMask();

        width = stack.getWidth();
        height = stack.getHeight();
        bandSize = width * height;

        // Store reference to band pixels for easier access.
        final int p = stack.getSize();
        bands = new float[p][];
        for (int i = 0; i < bands.length; i++) {
            bands[i] = (float[]) stack.getProcessor(i + 1).getPixels();
        }

        // Run clustering
        cluster();

        return encodeSegmentedImage();
    }

    /**
     * Return location of cluster centers.
     *
     * @return array of clustr centers. First index refers to cluster number.
     */
    final public double[][] getClusterCenters() {
        return clusterCenters;
    }

    /**
     * Return stack representing clustering optimization. This will return not
     * <code>null</code> value only when configuration parameters
     * <code>clusterAnimationEnabled</code> is set to true.
     *
     * @return stack representing cluster optimization, can return
     *         <code>null</code>.
     */
    final public ImageStack getClusterAnimation() {
        return clusterAnimation;
    }


    private ByteProcessor encodeSegmentedImage() {
        // Encode output image
        final byte[] pixels = new byte[bandSize];
        final double[] x = new double[bands.length];
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < bands.length; ++j) {
                x[j] = bands[j][i];
            }
            final int c = closestCluster(x, clusterCenters);
            pixels[i] = (byte) ((c + 1) & 0xff);
        }

        return new ByteProcessor(width, height, pixels, null);
    }

    private void printClusters(final String message) {
        IJ.write(message);
        for (int i = 0; i < clusterCenters.length; i++) {
            final double[] clusterCenter = clusterCenters[i];
            final StringBuffer buffer = new StringBuffer("  (");
            for (int j = 0; j < clusterCenter.length; j++) {
                final double vv = clusterCenter[j];
                buffer.append(" " + vv + " ");
            }
            buffer.append(")");
            IJ.write(buffer.toString());
        }
    }

    /**
     *
     */
    private void cluster() {

        // Select initial partitioning - intialize cluster centers
        clusterCenters = generateRandomClusterCenters(bands.length);
        if (config.isPrintTraceEnabled()) {
            printClusters("Initial clusters");
        }

        if (config.clusterAnimationEnabled) {
            clusterAnimation = new ImageStack(width, height);
            clusterAnimation.addSlice("Initial", encodeSegmentedImage());
        }

        // Optimize cluster centers
        boolean converged = false;
        long count = 0;
        while (!converged) {

            final MeanElement[] newClusterMeans = new MeanElement[config.getNumberOfClusters()];
            for (int i = 0; i < newClusterMeans.length; i++) {
                newClusterMeans[i] = new MeanElement(bands.length);
            }

            // Generate a new partition by assigning each pattern to its closest cluster center
            // Compute new cluster centers as the centroids of the clusters
            final double[] x = new double[bands.length];
            for (int i = 0; i < bandSize; ++i) {
                for (int j = 0; j < bands.length; ++j) {
                    x[j] = bands[j][i];
                }
                final int c = closestCluster(x, clusterCenters);
                newClusterMeans[c].add(x);
            }

            // Check for convergance
            double distanceSum = 0;
            for (int i = 0; i < clusterCenters.length; i++) {
                final double[] clusterCenter = clusterCenters[i];
                final double[] newClusterCenter = newClusterMeans[i].mean();
                distanceSum += distance(clusterCenter, newClusterCenter);
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
     * Return index of closest cluster to point <code>x</code>.
     *
     * @param x
     * @param clusterCenters
     * @return
     */
    private static int closestCluster(final double[] x, final double[][] clusterCenters) {
        double minDistance = java.lang.Double.MAX_VALUE;
        int closestCluster = -1;
        for (int i = 0; i < clusterCenters.length; i++) {
            final double[] clusterCenter = clusterCenters[i];
            final double d = distance(clusterCenter, x);
            if (d < minDistance) {
                minDistance = d;
                closestCluster = i;
            }
        }

        return closestCluster;
    }


    /**
     * Distance between points <code>a</code> and <code>b</code>.
     *
     * @param a
     * @param b
     * @return
     */
    private static double distance(final double[] a, final double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            final double d = a[i] - b[i];
            sum += d * d;
        }
        return java.lang.Math.sqrt(sum);
    }

    /**
     * @param p pattern dimentionality (number of bands in the input image)
     * @return
     */
    private double[][] generateRandomClusterCenters(final int p) {

        final Random random = config.isRandomizationSeedEnabled()
                ? new Random(config.getRandomizationSeed())
                : new Random();

        final double[][] centers = new double[config.getNumberOfClusters()][];
        for (int i = 0; i < centers.length; i++) {
            final double[] center = new double[p];
            centers[i] = center;
            // Make sure that each center is unique
            boolean unique = false;
            int count = 0;
            while (!unique) {
                // Initialize center
                final int sample = random.nextInt(bandSize);
                for (int j = 0; j < center.length; j++) {
                    center[j] = bands[j][sample];
                }

                // Test if it is not a repeat of already selected center.
                unique = true;
                for (int j = 0; j < i; ++j) {
                    final double d = distance(centers[j], center);
                    if (d < config.getTolerance()) {
                        unique = false;
                        break;
                    }
                }

                ++count;
                if (count > 2 * bandSize) {
                    throw new RuntimeException("Unable to initialize " + centers.length +
                            " unique clusater centroids.\n" +
                            "Input image may not have enough unique pixel values.");
                }
            }
        }

        return centers;
    }

    /**
     *
     */
    private static final class MeanElement {
        final double[] sum;
        int count = 0;

        public MeanElement(final int elementSize) {
            sum = new double[elementSize];
        }

        public void add(final double[] x) {
            if (x.length != sum.length) {
                throw new java.lang.IllegalArgumentException("Invalid element size, got " + x.length + ", expecting" + sum.length);
            }

            for (int i = 0; i < x.length; i++) {
                sum[i] += x[i];
            }
            ++count;
        }

        public double[] mean() {
            final double[] r = new double[sum.length];
            for (int i = 0; i < r.length; i++) {
                r[i] = sum[i] / count;
            }

            return r;
        }
    }
}
