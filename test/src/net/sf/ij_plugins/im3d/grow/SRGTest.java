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

package net.sf.ij_plugins.im3d.grow;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ByteProcessor;
import junit.framework.TestCase;

import java.awt.*;
import java.io.File;

/**
 * Unit test for SRG.
 *
 * @author Jarek Sacha
 * @version $ Revision: $
 *          <p/>
 *          //TODO: Verify that segmentation is correct.
 */
public class SRGTest extends TestCase {
    private static final String OUTPUT_DIR = "tmp";

    private static final String BLOBS_FILE_NAME = "test/data/blobs.png";
    private static final String RAMP_FILE_NAME = "test/data/ramp.png";

    public SRGTest(String test) {
        super(test);
    }

    public void testRamp() throws Exception {
        // Load test image
        final ByteProcessor image;
        {
            Opener opener = new Opener();
            ImagePlus imp = opener.openImage(RAMP_FILE_NAME);
            if (imp == null) {
                throw new Exception("Cannot open image: " + RAMP_FILE_NAME);
            }
            image = (ByteProcessor) imp.getProcessor();
        }

        Point[][] seeds = {
                {new Point(1, 198)}, // dark
                {new Point(198, 1)} // bright
        };

        // Setup region growing
        SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(seeds);
        srg.setNumberOfAnimationFrames(50);

        // Run growing
        srg.run();

        ByteProcessor regionMask = srg.getRegionMarkers();
        ImagePlus imp1 = new ImagePlus("Region Mask", regionMask);

        new File(OUTPUT_DIR).mkdirs();

        FileSaver fileSaver = new FileSaver(imp1);
        if (!fileSaver.saveAsTiff(OUTPUT_DIR + "/srg_ramp_test_output.tif")) {
            throw new Exception("Error saving output image.");
        }

        FileSaver fileSaver1 = new FileSaver(new ImagePlus("Growth", srg.getAnimationStack()));
        if (!fileSaver1.saveAsTiffStack(OUTPUT_DIR + "/srg_ramp_test_animation.tif")) {
            throw new Exception("Error saving output image.");
        }

    }

    public void testRampWithMask() throws Exception {
        // Load test image
        final ByteProcessor image;
        {
            final Opener opener = new Opener();
            final ImagePlus imp = opener.openImage(RAMP_FILE_NAME);
            if (imp == null) {
                throw new Exception("Cannot open image: " + RAMP_FILE_NAME);
            }
            image = (ByteProcessor) imp.getProcessor();
        }

        final Point[][] seeds = {
                {new Point(90, 100)}, // dark
                {new Point(110, 100)} // bright
        };

        final ByteProcessor mask = new ByteProcessor(image.getWidth(), image.getHeight());
        mask.setRoi(50, 50, 100, 100);
        mask.setColor(255);
        mask.fill();

        // Setup region growing
        final SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(seeds);
        srg.setMask(mask);
        srg.setNumberOfAnimationFrames(50);

        // Run growing
        srg.run();

        final ByteProcessor regionMarkers = srg.getRegionMarkers();
        ImagePlus imp1 = new ImagePlus("Region Markers", regionMarkers);

        new File(OUTPUT_DIR).mkdirs();

        final FileSaver fileSaver = new FileSaver(imp1);
        if (!fileSaver.saveAsTiff(OUTPUT_DIR + "/srg_ramp_with_mask_test_output.tif")) {
            throw new Exception("Error saving output image.");
        }

        final FileSaver fileSaver1 = new FileSaver(new ImagePlus("Growth", srg.getAnimationStack()));
        if (!fileSaver1.saveAsTiffStack(OUTPUT_DIR + "/srg_ramp_with_mask_test_animation.tif")) {
            throw new Exception("Error saving output image.");
        }

        // Mask
        assertEquals(0, regionMarkers.get(10, 10));
        assertEquals(0, regionMarkers.get(190, 190));
        // Region 1
        assertEquals(1, regionMarkers.get(60, 60));
        // Region 2
        assertEquals(2, regionMarkers.get(120, 60));
    }


    public void testBlobs() throws Exception {
        // Load test image
        final ByteProcessor image;
        {
            Opener opener = new Opener();
            ImagePlus imp = opener.openImage(BLOBS_FILE_NAME);
            if (imp == null) {
                throw new Exception("Cannot open image: " + BLOBS_FILE_NAME);
            }
            image = (ByteProcessor) imp.getProcessor();
        }


        Point[][] seeds = {
                {new Point(107, 144)}, // Background
                {new Point(91, 159)}, // Blob 1
                {new Point(119, 143)}, // Blob 2
        };

        // Setup region growing
        SRG srg = new SRG();
        srg.setImage(image);
        srg.setSeeds(seeds);
        srg.setNumberOfAnimationFrames(50);

        // Run growing
        srg.run();

        ByteProcessor regionMask = srg.getRegionMarkers();
        ImagePlus imp = new ImagePlus("Region Mask", regionMask);

        new File(OUTPUT_DIR).mkdirs();

        FileSaver fileSaver = new FileSaver(imp);
        if (!fileSaver.saveAsTiff(OUTPUT_DIR + "/srg_test_output.tif")) {
            throw new Exception("Error saving output image.");
        }

        FileSaver fileSaver1 = new FileSaver(new ImagePlus("Growth", srg.getAnimationStack()));
        if (!fileSaver1.saveAsTiffStack(OUTPUT_DIR + "/srg_test_animation.tif")) {
            throw new Exception("Error saving output image.");
        }
    }

    /**
     * The fixture set up called before every test method
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method
     */
    protected void tearDown() throws Exception {
    }
}