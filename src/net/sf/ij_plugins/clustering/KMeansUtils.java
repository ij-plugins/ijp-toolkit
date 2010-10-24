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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.StackConverter;
import net.sf.ij_plugins.multiband.VectorProcessor;


/**
 * @author Jarek Sacha
 * @since Sep 29, 2010 2:08:57 PM
 */
final class KMeansUtils {

    private KMeansUtils() {
    }


    public static ImageStack encodeCentroidValueImage(final float[][] clusterCenters, final VectorProcessor vp) {
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
            final float[] v = iterator.next();
            final int c = closestCluster(v, clusterCenters);
            for (int j = 0; j < numberOfValues; ++j) {
                ((float[]) pixels[j])[iterator.getOffset()] = clusterCenters[c][j];
            }
        }

        return s;
    }


    /**
     * Return index of the closest cluster to point <code>x</code>.
     *
     * @param x              point coordinates.
     * @param clusterCenters cluster centers.
     * @return index of the closest cluster
     */
    static int closestCluster(final float[] x, final float[][] clusterCenters) {
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
     * @param a first point.
     * @param b second point.
     * @return distance.
     */
    static double distance(final float[] a, final float[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            final double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }


    static ImagePlus createCentroidImage(final int originalImageType, final ImageStack centroidValueStack) {
        final boolean doScaling = ImageConverter.getDoScaling();
        try {
            ImageConverter.setDoScaling(false);
            final ImagePlus cvImp = new ImagePlus("Cluster centroid values", centroidValueStack);
            if (centroidValueStack.getSize() > 1) {
                final StackConverter stackConverter = new StackConverter(cvImp);
                switch (originalImageType) {
                    case ImagePlus.COLOR_RGB:
                        stackConverter.convertToGray8();
                        final ImageConverter imageConverter = new ImageConverter(cvImp);
                        imageConverter.convertRGBStackToRGB();
                        break;
                    case ImagePlus.GRAY8:
                        stackConverter.convertToGray8();
                        break;
                    case ImagePlus.GRAY16:
                        stackConverter.convertToGray16();
                        break;
                    case ImagePlus.GRAY32:
                        // No action needed
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported input image type: " + originalImageType);
                }
            } else {
                final ImageConverter converter = new ImageConverter(cvImp);
                // Convert image back to original type
                switch (originalImageType) {
                    case ImagePlus.COLOR_RGB:
                        throw new IllegalArgumentException("Internal error: RGB image cannot have a single band.");
                    case ImagePlus.GRAY8:
                        converter.convertToGray8();
                        break;
                    case ImagePlus.GRAY16:
                        converter.convertToGray16();
                        break;
                    case ImagePlus.GRAY32:
                        // No action needed
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported input image type: " + originalImageType);
                }
            }

            return cvImp;
        } finally {
            ImageConverter.setDoScaling(doScaling);
        }

    }
}
