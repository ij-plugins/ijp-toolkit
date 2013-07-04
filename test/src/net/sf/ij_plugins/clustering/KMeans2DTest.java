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

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import net.sf.ij_plugins.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;


/**
 * Unit tests for {@link net.sf.ij_plugins.clustering.KMeans2D}
 *
 * @author Jarek Sacha
 */
public final class KMeans2DTest {

    @Test
    public void test01() throws Exception {
        final float tolerance = 0.001f;
        final float[][] expectedCenters = {
                {26.0207f, 50.91606f, 101.7027f},
                {227.0155f, 30.7971f, 30.7971f},
                {126.8642f, 0.0000f, 230.3005f},
        };

        // Read test image
        final File imageFile = new File("test/data/Flamingo.png");
        assertTrue("Input file should exist", imageFile.exists());
        final ImagePlus imp = IOUtils.openImage(imageFile);
        assertTrue("Expecting color image.", imp.getType() == ImagePlus.COLOR_RGB);

        // Convert input color image to a floating point stack
        new ImageConverter(imp).convertToRGBStack();
        new StackConverter(imp).convertToGray32();
        final ImageStack stack = imp.getStack();

        // Setup m-means
        final KMeansConfig config = new KMeansConfig();
        config.setNumberOfClusters(expectedCenters.length);
        config.setRandomizationSeedEnabled(true);
        config.setRandomizationSeed(48);
        final KMeans2D kmeans = new KMeans2D(config);

        // Run k-means to produce cluster image
        final ByteProcessor clusterImage = kmeans.run(stack);
        assertNotNull(clusterImage);
        assertEquals("Output width must match input width", imp.getWidth(), clusterImage.getWidth());
        assertEquals("Output height must match input height", imp.getHeight(), clusterImage.getHeight());
        final float[][] clusterCenters = kmeans.getClusterCenters();
        assertNotNull(clusterCenters);
        assertEquals(expectedCenters.length, clusterCenters.length);

        // Since we are using a fixed randomization seed, the cluster centers should be predictable
        for (int c = 0; c < expectedCenters.length; c++) {
            assertArrayEquals(expectedCenters[c], clusterCenters[c], tolerance);
        }

        // Check cluster codes at some locations on the output clusterImage
        assertEquals(0, clusterImage.getPixel(31, 160));
        assertEquals(0, clusterImage.getPixel(252, 44));
        assertEquals(0, clusterImage.getPixel(241, 238));
        assertEquals(1, clusterImage.getPixel(95, 32));
        assertEquals(1, clusterImage.getPixel(104, 129));
        assertEquals(1, clusterImage.getPixel(241, 124));
        assertEquals(2, clusterImage.getPixel(57, 81));
        assertEquals(2, clusterImage.getPixel(176, 106));
        assertEquals(2, clusterImage.getPixel(143, 277));
    }


    @Test
    @Ignore("Only for benchmarking.")
    public void benchmark01() throws IOException {
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

        final KMeansConfig config = new KMeansConfig();
        config.setNumberOfClusters(4);
        config.setRandomizationSeedEnabled(true);
        config.setRandomizationSeed(48);

        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        final int iterations = 10;
        for (int l = 0; l < iterations; l++) {
            final KMeans2D kmeans = new KMeans2D(config);
            final long start = System.currentTimeMillis();
            final ImageProcessor ip = kmeans.run(imp.getStack());
            final long stop = System.currentTimeMillis();
            final long time = stop - start;
            System.out.println("time: " + time + "ms.");
            totalTime += time;
            minTime = Math.min(time, minTime);

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
        }

        System.out.println("Average time: " + totalTime / (double) iterations + "ms");
        System.out.println("Min time: " + minTime + "ms");

//        final ImagePlus imp1 = new ImagePlus("K-means", ip);
//        final FileSaver saver = new FileSaver(imp1);
//        saver.saveAsTiff("kmeans-output.tif");
    }
}