/*
 * IJ-Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package net.sf.ij_plugins.clustering;

import ij.IJ;
import ij.ImageStack;
import net.sf.ij_plugins.util.Validate;

import java.util.Random;

/**
 * Pixel-based multi-band image segmentation using k-means clustering algorithm.
 * <br>
 * k-means clustering performs pixel-based segmentation of multi-band
 * images. An image stack is interpreted as a set of bands corresponding to
 * the same image. For instance, an RGB color images has three bands: red,
 * green, and blue. Each pixels is represented by an n-valued vector , where
 * n is a number of bands, for instance, a 3-value vector [r,g,b] in case of
 * a color image.
 * Each cluster is defined by its centroid in n-dimensional space. Pixels are
 * grouped by their proximity to cluster's centroids.
 * Cluster centroids are determined using a heuristics: initially centroids
 * are randomly initialized and then their location is interactively
 * optimized.
 * For more information on this and other clustering approaches see:
 * Anil K. Jain and Richard C. Dubes, <i>Algorithms for Clustering Data</i>,
 * Prentice Hall, 1988.
 * <a href="http://homepages.inf.ed.ac.uk/rbf/BOOKS/JAIN/Clustering_Jain_Dubes.pdf">http://homepages.inf.ed.ac.uk/rbf/BOOKS/JAIN/Clustering_Jain_Dubes.pdf</a>
 *
 * @author Jarek Sacha
 */
abstract class KMeans<T> {
    final KMeansConfig config;
    float[][] clusterCenters;
    private long numberOfStepsToConvergence;

    KMeans(KMeansConfig config) {
        this.config = config.duplicate();
    }

    /**
     * Perform k-means clustering of the input <code>stack</code>.
     *
     * @return segmented image.
     */
    abstract public T run(final ImageStack stack);

    /**
     * Returns stack where discovered clusters can be represented by replacing pixel values in a
     * cluster by the value of the centroid of that cluster.
     *
     * @return centroid value image
     */
    final public ImageStack getCentroidValueImage() {
        if (clusterCenters == null) {
            throw new IllegalStateException("Need to perform clustering first.");
        }

        return encodeCentroidValueImage();
    }

    final public long getNumberOfStepsToConvergence() {
        return numberOfStepsToConvergence;
    }

    /**
     * Number of values in a pixel
     */
    abstract protected int numberOfValues();

    abstract protected ImageStack encodeCentroidValueImage();

    abstract protected java.util.Iterator<float[]> newPixelIterator();

    /**
     * Initialize clusters using k-means++ approach, see http://en.wikipedia.org/wiki/K-means++
     *
     * @return initial cluster centers.
     */
    abstract protected float[][] initializeClusterCenters();

    boolean supportsClusterAnimation() {
        return false;
    }

    void clusterAnimationInitialize() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    void clusterAnimationAddCurrent(final String title) {
        throw new UnsupportedOperationException("Not implemented.");
    }

    /**
     *
     */
    final void cluster() {

        // Select initial partitioning - initialize cluster centers
        numberOfStepsToConvergence = 0;
        clusterCenters = initializeClusterCenters();
        if (config.isPrintTraceEnabled()) {
            printClusters("Initial clusters");
        }

        if (supportsClusterAnimation() && config.isClusterAnimationEnabled()) {
            clusterAnimationInitialize();
            clusterAnimationAddCurrent("Initial");

        }


        // Optimize cluster centers
        boolean converged = false;
        long count = 0;
        while (!converged) {

            final MeanElement[] newClusterMeans = new MeanElement[config.getNumberOfClusters()];
            for (int i = 0; i < newClusterMeans.length; i++) {
                newClusterMeans[i] = new MeanElement(numberOfValues());
            }

            // Generate a new partition by assigning each pattern to its closest cluster center
            // Compute new cluster centers as the centroids of the clusters
            final java.util.Iterator<float[]> iterator = newPixelIterator();
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
                distanceSum += KMeansUtils.distanceSqr(clusterCenter, newClusterCenter);
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

            if (supportsClusterAnimation() && config.isClusterAnimationEnabled()) {
                clusterAnimationAddCurrent("Iteration " + count);
            }
        }

        this.numberOfStepsToConvergence = count;
    }

    /**
     * Return location of cluster centers.
     *
     * @return array of cluster centers. First index refers to cluster number.
     */
    final public float[][] getClusterCenters() {
        return clusterCenters;
    }

    /**
     * Find index of the cluster closest to the sample {@code x}.
     * {@link #run(ij.ImageStack)} must be run before calling this method.
     *
     * @param x test point, number of values must be the same is input stack size.
     * @return index of the closest cluster.
     * @see #run(ij.ImageStack)
     */
    final public int closestCluster(final float[] x) {
        if (clusterCenters == null) {
            throw new IllegalStateException("Cluster centers not computed, call run(ImageStack) first.");
        }
        Validate.argumentNotNull(x, "x");
        Validate.isTrue(x.length == numberOfValues(),
                "Expecting argument 'x' of length " + numberOfValues() + ", got " + x.length + ".");

        return KMeansUtils.closestCluster(x, clusterCenters);
    }

    final void printClusters(final String message) {
        IJ.log(message);
        for (final float[] clusterCenter : clusterCenters) {
            final StringBuilder buffer = new StringBuilder("  (");
            for (final float vv : clusterCenter) {
                buffer.append(" ").append(vv).append(" ");
            }
            buffer.append(")");
            IJ.log(buffer.toString());
        }
    }

//    abstract protected Iterator<float[]> newPixelIterator();

    final Random createRandom() {
        return config.isRandomizationSeedEnabled()
                ? new Random(config.getRandomizationSeed())
                : new Random();
    }

    /**
     *
     */
    static final class MeanElement {

        private final double[] sum;
        private int count;

        public MeanElement(final int elementSize) {
            sum = new double[elementSize];
        }

        public void add(final float[] x) {
            if (x.length != sum.length) {
                throw new IllegalArgumentException("Invalid element size, got " + x.length + ", expecting" + sum.length);
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
}
