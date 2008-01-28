/***
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
import ij.process.FloatProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;

import java.awt.*;
import java.util.Random;

/**
 * Pixel-based multi-band image segmentation using k-means clustering algorithm.
 *
 * @author Jarek Sacha
 */
public final class KMeans {
    private static Config config = new Config();

    private Rectangle roi;
    private ByteProcessor mask;

    private VectorProcessor vp;
    private float[][] clusterCenters;
    private ImageStack clusterAnimation;

    public KMeans() {
    }

    public KMeans(final Config config) {
        KMeans.config = config.duplicate();
    }

    public void setRoi(final Rectangle roi) {
        this.roi = roi;
    }

    public void setMask(ByteProcessor mask) {
        this.mask = mask;
    }

    /**
     * Perform k-means clostering of the input <code>stack</code>. Elements of the
     * <code>stack</code> must be of type <code>FloatProcessor</code>.
     *
     * @param stack stack representing a multi-band image.
     */
    final public ByteProcessor run(final ImageStack stack) {

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
     * @return array of clustr centers. First index refers to cluster number.
     */
    final public float[][] getClusterCenters() {
        return clusterCenters;
    }

    /**
     * Return stack representing clustering optimization. This will return not <code>null</code>
     * value only when configuration parameters <code>clusterAnimationEnabled</code> is set to
     * true.
     *
     * @return stack representing cluster optimization, can return <code>null</code>.
     */
    final public ImageStack getClusterAnimation() {
        return clusterAnimation;
    }

    /**
     * Returns stack where discovered clusters can be represented by replacing pixel values in a
     * cluster by the value of the centroid of that cluster.
     */
    final public ImageStack getCentroidValueImage() {
        if (clusterCenters == null) {
            throw new IllegalStateException("Need to perform clustering first.");
        }

        return encodeCentroidValueImage();
    }


    private ByteProcessor encodeSegmentedImage() {
        // Encode output image
        final ByteProcessor dest = new ByteProcessor(vp.getWidth(), vp.getHeight());
        final VectorProcessor.PixelIterator iterator = vp.pixelIterator();
        while (iterator.hasNext()) {
            final float[] v = (float[]) iterator.next();
            final int c = closestCluster(v, clusterCenters);
            dest.putPixel(iterator.getX(), iterator.getY(), c);
        }
        return dest;
    }

    private ImageStack encodeCentroidValueImage() {
        final int width = vp.getWidth();
        final int height = vp.getHeight();
        final int numberOfValues = vp.getNumberOfValues();
        final ImageStack s = new ImageStack(width, height);
        for (int i = 0; i < numberOfValues; ++i) {
            // TODO: Band label should be the same as in the input stack
            s.addSlice("Band i", new FloatProcessor(width, height));
        }

        final VectorProcessor.PixelIterator iterator = vp.pixelIterator();
        final Object[] pixels = s.getImageArray();
        while (iterator.hasNext()) {
            final float[] v = (float[]) iterator.next();
            final int c = closestCluster(v, clusterCenters);
            for (int j = 0; j < numberOfValues; ++j) {
                ((float[]) pixels[j])[iterator.getOffset()] = (float) clusterCenters[c][j];
            }
        }

        return s;
    }


    private void printClusters(final String message) {
        IJ.write(message);
        for (int i = 0; i < clusterCenters.length; i++) {
            final float[] clusterCenter = clusterCenters[i];
            final StringBuffer buffer = new StringBuffer("  (");
            for (int j = 0; j < clusterCenter.length; j++) {
                final float vv = clusterCenter[j];
                buffer.append(" ").append(vv).append(" ");
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
        clusterCenters = generateRandomClusterCenters(vp.getNumberOfValues());
        if (config.isPrintTraceEnabled()) {
            printClusters("Initial clusters");
        }

        if (config.clusterAnimationEnabled) {
            clusterAnimation = new ImageStack(vp.getWidth(), vp.getHeight());
            clusterAnimation.addSlice("Initial", encodeSegmentedImage());
        }

//        public int[] getHistogram() {
//            if (mask!=null)
//                return getHistogram(mask);
//            int[] histogram = new int[256];
//            for (int y=roiY; y<(roiY+roiHeight); y++) {
//                int i = y * width + roiX;
//                for (int x=roiX; x<(roiX+roiWidth); x++) {
//                    int v = pixels[i++] & 0xff;
//                    histogram[v]++;
//                }
//            }
//            return histogram;
//        }
//
//        public int[] getHistogram(ImageProcessor mask) {
//            if (mask.getWidth()!=roiWidth||mask.getHeight()!=roiHeight)
//                throw new IllegalArgumentException(maskSizeError(mask));
//            int v;
//            int[] histogram = new int[256];
//            byte[] mpixels = (byte[])mask.getPixels();
//            for (int y=roiY, my=0; y<(roiY+roiHeight); y++, my++) {
//                int i = y * width + roiX;
//                int mi = my * roiWidth;
//                for (int x=roiX; x<(roiX+roiWidth); x++) {
//                    if (mpixels[mi++]!=0) {
//                        v = pixels[i] & 0xff;
//                        histogram[v]++;
//                    }
//                    i++;
//                }
//            }
//            return histogram;
//        }

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
            VectorProcessor.PixelIterator iterator = vp.pixelIterator();
            while (iterator.hasNext()) {
                final float[] v = (float[]) iterator.next();
                final int c = closestCluster(v, clusterCenters);
                newClusterMeans[c].add(v);
            }

            // Check for convergence
            float distanceSum = 0;
            for (int i = 0; i < clusterCenters.length; i++) {
                final float[] clusterCenter = clusterCenters[i];
                final float[] newClusterCenter = newClusterMeans[i].mean();
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
    private static int closestCluster(final float[] x, final float[][] clusterCenters) {
        double minDistance = Double.MAX_VALUE;
        int closestCluster = -1;
        for (int i = 0; i < clusterCenters.length; i++) {
            final float[] clusterCenter = clusterCenters[i];
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
    private static double distance(final float[] a, final float[] b) {
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            final float d = a[i] - b[i];
            sum += d * d;
        }
        return java.lang.Math.sqrt(sum);
    }

    /**
     * @param p pattern dimensionality (number of bands in the input image)
     * @return
     */
    private float[][] generateRandomClusterCenters(final int p) {

        final Random random = config.isRandomizationSeedEnabled()
                ? new Random(config.getRandomizationSeed())
                : new Random();

        final float[][] centers = new float[config.getNumberOfClusters()][];
        for (int i = 0; i < centers.length; i++) {
            centers[i] = new float[vp.getNumberOfValues()];
            // Make sure that each center is unique
            boolean unique = false;
            int count = 0;
            while (!unique) {
                // Initialize center
                final int sampleX = random.nextInt(vp.getWidth());
                final int sampleY = random.nextInt(vp.getHeight());
                vp.get(sampleX, sampleY, centers[i]);

                // Test if it is not a repeat of already selected center.
                unique = true;
                for (int j = 0; j < i; ++j) {
                    final double d = distance(centers[j], centers[i]);
                    if (d < config.getTolerance()) {
                        unique = false;
                        break;
                    }
                }

                ++count;
                if (count > vp.getWidth() * vp.getHeight()) {
                    throw new RuntimeException("Unable to initialize " + centers.length +
                            " unique cluster centroids.\n" +
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
        final float[] sum;
        int count = 0;

        public MeanElement(final int elementSize) {
            sum = new float[elementSize];
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
                r[i] = sum[i] / count;
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
        private boolean clusterAnimationEnabled = false;
        private boolean printTraceEnabled = false;

        public int getRandomizationSeed() {
            return randomizationSeed;
        }

        public void setRandomizationSeed(final int randomizationSeed) {
            this.randomizationSeed = randomizationSeed;
        }

        /**
         * If <code>true</code>, random number generator will be initalized with a
         * <code>randomizationSeed</code>. If <code>false</code> rundom number generator will be
         * initialized using 'current' time.
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
         * Return tolerance used to determine cluster centroid distance. This tolerance is used to
         * determine if a centroid changed location between iterations.
         *
         * @return cluster centroid location tolerunce.
         */
        public double getTolerance() {
            return tolerance;
        }

        public void setTolerance(final float tolerance) {
            this.tolerance = tolerance;
        }

        /**
         * Return <code>true</code> if when an animation ilustrating cluster optimization is
         * enabled.
         */
        public boolean isClusterAnimationEnabled() {
            return clusterAnimationEnabled;
        }

        public void setClusterAnimationEnabled(final boolean clusterAnimationEnabled) {
            this.clusterAnimationEnabled = clusterAnimationEnabled;
        }

        /**
         * Return <code>true</code> if a trace is printed to the ImageJ's Result window.
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

}
