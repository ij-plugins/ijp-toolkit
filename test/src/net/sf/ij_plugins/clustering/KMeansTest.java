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

package net.sf.ij_plugins.clustering;

import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import net.sf.ij_plugins.io.IOUtils;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


/**
 * @author Jarek Sacha
 */
public final class KMeansTest {


    @Test
    public void test01() throws java.lang.Exception {
        final File imageFile = new File("test/data/clown24.png");
        final double tolerance = 0.01;
        final double[][] expectedCenters = {
                {182.389, 108.690, 45.733},
                {115.623, 51.116, 20.329},
                {30.557, 10.600, 5.617},
                {224.589, 187.087, 151.137},
        };

        assertTrue("File exists", imageFile.exists());

        // Read test image
        final ImagePlus imp = IOUtils.openImage(imageFile);

        assertTrue("Expecting color image.", imp.getType() == ImagePlus.COLOR_RGB);

        // Convert RGB to a stack
        new ImageConverter(imp).convertToRGBStack();
        new StackConverter(imp).convertToGray32();

        final KMeans.Config config = new KMeans.Config();
        config.setNumberOfClusters(4);
        config.setRandomizationSeedEnabled(true);
        config.setRandomizationSeed(48);
        final KMeans kmeans = new KMeans(config);
        final long start = System.currentTimeMillis();
        final ImageProcessor ip = kmeans.run(imp.getStack());
        final long stop = System.currentTimeMillis();
        System.out.println("time: " + (stop - start) + "ms.");

        assertNotNull(ip);

        float[][] centers = kmeans.getClusterCenters();
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers[i].length; j++) {
                System.out.println("center[" + i + "][" + j + "]: " + centers[i][j]);
            }
        }

        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers[i].length; j++) {
                assertEquals("center[" + i + "][" + j + "]",
                        expectedCenters[i][j], centers[i][j], tolerance);
            }
        }

        System.out.println("Steps: " + kmeans.getNumberOfStepsToConvergence());

//        final ImagePlus imp1 = new ImagePlus("K-means", ip);
//        final FileSaver saver = new FileSaver(imp1);
//        saver.saveAsTiff("kmeans-output.tif");
    }


    public void testColor() {
//    ColorProcessor r = new ColorProcessor(stack.getWidth(), stack.getHeight());
//
//    // Encode output image
//    byte[] pixels = new byte[bandSize];
//    int[] x = new int[bands.length];
//    int[] v = new int[3];
//    int width = stack.getWidth();
////    int height = stack.getHeight();
//    for (int i = 0; i < pixels.length; i++) {
//      for (int j = 0; j < bands.length; ++j) {
//        x[j] = bands[j][i] & 0xff;
//      }
//      int c = closestCluster(x, clusterCenters);
//      pixels[i] = (byte) (c & 0xff);
//
//      v[0] = (int) java.lang.Math.round(clusterCenters[c][0]);
//      v[1] = (int) java.lang.Math.round(clusterCenters[c][1]);
//      v[2] = (int) java.lang.Math.round(clusterCenters[c][2]);
//      int yy = i / width;
//      int xx = i % width;
//      r.putPixel(xx, yy, v);
//    }
//
//    java.lang.System.out.println("Cluster centers");
//    for (int i = 0; i < clusterCenters.length; i++) {
//      double[] clusterCenter = clusterCenters[i];
//      java.lang.System.out.print("(");
//      for (int j = 0; j < clusterCenter.length; j++) {
//        double vv = clusterCenter[j];
//        java.lang.System.out.print(" " + vv + " ");
//      }
//      java.lang.System.out.println(")");
//    }
//
//
    }
}