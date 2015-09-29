/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * 3D version of the k-means algorithm.
 *
 * @author Jarek Sacha
 */
public final class KMeans3D extends KMeans<ImageStack> {

    private ImageStack stack;

    public KMeans3D() {
        this(new KMeansConfig());
    }

    public KMeans3D(final KMeansConfig config) {
        super(config);
    }

    /**
     * Perform k-means clustering of the input <code>stack</code>.
     *
     * @param stack stack representing a 3D image.
     * @return segmented image.
     */
    public ImageStack run(final ImageStack stack) {

        if (stack.getSize() < 1) {
            throw new IllegalArgumentException("Input stack cannot be empty");
        }

        this.stack = stack;

        // Run clustering
        cluster();

        return encodeSegmentedImage();
    }

    protected int numberOfValues() {
        return 1;
    }

    private ImageStack encodeSegmentedImage() {
        // Encode output image
        final ImageStack dest = new ImageStack(stack.getWidth(), stack.getHeight());
        for (int z = 0; z < stack.getSize(); z++) {
            dest.addSlice(stack.getSliceLabel(z + 1), new ByteProcessor(stack.getWidth(), stack.getHeight()));
        }
        final StackPixelIterator iterator = newPixelIterator();
        while (iterator.hasNext()) {
            final float[] v = iterator.next();
            final int c = KMeansUtils.closestCluster(v, clusterCenters);
            dest.setVoxel(iterator.getX(), iterator.getY(), iterator.getZ(), c);
        }
        return dest;
    }

    protected ImageStack encodeCentroidValueImage() {

        final int width = stack.getWidth();
        final int height = stack.getHeight();
//        final int numberOfValues = vp.getNumberOfValues();
        final ImageStack dest = new ImageStack(width, height);
        for (int i = 0; i < stack.getSize(); ++i) {
            dest.addSlice(stack.getSliceLabel(i + 1), stack.getProcessor(i + 1).duplicate());
        }

        assert numberOfValues() == 1;
        final StackPixelIterator iterator = newPixelIterator();
        while (iterator.hasNext()) {
            final float[] v = iterator.next();
            final int c = closestCluster(v);
            dest.setVoxel(iterator.getX(), iterator.getY(), iterator.getZ(), clusterCenters[c][0]);
        }

        return dest;
    }

    protected StackPixelIterator newPixelIterator() {
        return new StackPixelIterator(stack);
    }

    protected float[][] initializeClusterCenters() {
        final Random random = createRandom();

        final int nbClusters = config.getNumberOfClusters();
        final int width = stack.getWidth();
        final int height = stack.getHeight();
        final int depth = stack.getSize();
        final int nbPixels = width * height * depth;

        // Cluster centers
        final List<float[]> centers = new ArrayList<>();
        // Location of pixels used as cluster centers
        final List<Point3D> centerLocation = new ArrayList<>();

        // Choose one center uniformly at random from among pixels
        {
            final Point3D p = toPoint(random.nextInt(nbPixels), width, height);
            centerLocation.add(p);
            centers.add(new float[]{(float) stack.getVoxel(p.x, p.y, p.z)});
        }

        final double[] dp2 = new double[nbPixels];
        while (centers.size() < nbClusters) {
            assert centers.size() == centerLocation.size();

            // For each data point p compute D(p), the distance between p and the nearest center that
            // has already been chosen.
            double sum = 0;
            final float[][] centersArray = centers.toArray(new float[centers.size()][]);
            for (int offset = 0; offset < nbPixels; offset++) {
                final Point3D p = toPoint(offset, width, height);

                // Test that this is not a repeat of already selected center
                if (centerLocation.contains(p)) {
                    continue;
                }

                // Distance to closest cluster
                final float[] v = new float[]{(float) stack.getVoxel(p.x, p.y, p.z)};
                final int cci = KMeansUtils.closestCluster(v, centersArray);
                sum += KMeansUtils.distanceSqr(v, centersArray[cci]);
                dp2[offset] = sum;
            }


            // Add one new data point at random as a new center, using a weighted probability distribution where
            // a point p is chosen with probability proportional to D(p)^2
            final double r = random.nextDouble() * sum;
            for (int offset = 0; offset < nbPixels; offset++) {
                final Point3D p = toPoint(offset, width, height);

                // Test that this is not a repeat of already selected center
                if (centerLocation.contains(p)) {
                    continue;
                }

                if (dp2[offset] >= r) {
                    centerLocation.add(p);
                    final float[] v = new float[]{(float) stack.getVoxel(p.x, p.y, p.z)};
                    centers.add(v);
                    break;
                }
            }
        }

        return centers.toArray(new float[centers.size()][]);
    }

    private static Point3D toPoint(final int offset, final int width, final int height) {
        final int wh = width * height;
        final int z = offset / wh;
        final int zOffset = offset - z * wh;
        final int y = zOffset / width;
        final int x = zOffset - y * width;
        return new Point3D(x, y, z);
    }

    private final static class Point3D {
        final int x;
        final int y;
        final int z;

        private Point3D(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point3D) {
                final Point3D o = (Point3D) obj;
                return x == o.x && y == o.y && z == o.z;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return x + y + z;
        }
    }
}
