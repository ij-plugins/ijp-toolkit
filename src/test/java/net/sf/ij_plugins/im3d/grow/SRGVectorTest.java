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

package net.sf.ij_plugins.im3d.grow;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import net.sf.ij_plugins.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;

import static org.junit.Assert.assertNotNull;


/**
 * @author Jarek Sacha
 * @since Sep 7, 2010 8:49:49 PM
 */
public final class SRGVectorTest {

    /**
     * The fixture set up called before every test method.
     */
    @Before
    public void setUp() {
    }


    /**
     * The fixture clean up called after every test method.
     */
    @After
    public void tearDown() {
    }


    @Test
    public void testColor() throws Exception {
        // Load test image
        final ColorProcessor image = (ColorProcessor) IOUtils.openImage("test/data/SRGVector_sample.png").getProcessor();
        final Point[][] seeds = {
                {new Point(100, 20), new Point(200, 200)}, // Blue
                {new Point(180, 75)},  // Red
                {new Point(90, 200)},  // Green
        };

        // Setup region growing
        final SRG2DVector srg = new SRG2DVector();
        srg.setImage(image);
        srg.setSeeds(SRG.toSeedImage(seeds, image.getWidth(), image.getHeight()));
        srg.setNumberOfAnimationFrames(50);

        // Run growing
        srg.run();

        final ByteProcessor regionMask = srg.getRegionMarkers();
        assertNotNull(regionMask);

        final ImagePlus imp1 = new ImagePlus("Region Mask", regionMask);
        final File outputDir = new File("test/data/tmp");
        outputDir.mkdirs();
        IOUtils.saveAsTiff(imp1, new File(outputDir, "SRGVector_sample_color_output.tif"));
        IOUtils.saveAsTiff(srg.getAnimationStack(), new File(outputDir, "SRGVector_sample_color_animation.tif"));
    }


    @Test
    public void testMono() throws Exception {
        // Load test image
        final FloatProcessor image = (FloatProcessor) IOUtils.openImage("test/data/SRGVector_sample.png").getProcessor().convertToFloat();
        final Point[][] seeds = {
                {new Point(100, 20), new Point(200, 200)}, // Blue
                {new Point(180, 75)},  // Red
                {new Point(90, 200)},  // Green
        };

        // Setup region growing
        final SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(SRG.toSeedImage(seeds, image.getWidth(), image.getHeight()));
        srg.setNumberOfAnimationFrames(50);

        // Run growing
        srg.run();

        final ByteProcessor regionMask = srg.getRegionMarkers();
        assertNotNull(regionMask);

        final ImagePlus imp1 = new ImagePlus("Region Mask", regionMask);
        final File outputDir = new File("test/data/tmp");
        outputDir.mkdirs();
        IOUtils.saveAsTiff(imp1, new File(outputDir, "SRGVector_sample_mono_output.tif"));
//        IOUtils.saveAsTiff(imp1, new File(outputDir, "srg_ramp_test_animation.tif"));
    }

}