/*
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
 *
 */
package net.sf.ij_plugins.clustering;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

import java.io.File;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public final class KMeansTest extends junit.framework.TestCase {
    public KMeansTest(final java.lang.String test) {
        super(test);
    }

    public void test01() throws java.lang.Exception {
        final File imageFile = new File("test/data/clown24.png");
        final double tolerance = 0.01;
        final double[][] expectedCenters = {
                {182.389, 108.690, 45.733},
                {224.589, 187.087, 151.137},
                {30.557, 10.600, 5.617},
                {115.623, 51.116, 20.329},
        };

        assertTrue("File exists", imageFile.exists());

        // Read test image
        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(imageFile.getAbsolutePath());
        if (imp == null) {
            throw new java.lang.Exception("Cannot open image: " + imageFile.getAbsolutePath());
        }

        if (imp.getType() != ImagePlus.COLOR_RGB) {
            throw new java.lang.Exception("Expecting color image.");
        }

        // Convert RGB to a stack
        final ImageConverter ic = new ImageConverter(imp);
        ic.convertToRGBStack();
        final StackConverter sc = new StackConverter(imp);
        sc.convertToGray32();

        final KMeans.Config config = new KMeans.Config();
        config.setNumberOfClusters(4);
        config.setRandomizationSeedEnabled(true);
        config.setRandomizationSeed(31415);
        final KMeans kmeans = new KMeans(config);
        final ImageProcessor ip = kmeans.run(imp.getStack());

        float[][] centers = kmeans.getClusterCenters();
        for (int i = 0; i < centers.length; i++) {
            for (int j = 0; j < centers[i].length; j++) {
                assertEquals("center[" + i + "][" + j + "]",
                        expectedCenters[i][j], centers[i][j], tolerance);
            }
        }

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