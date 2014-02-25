/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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
package net.sf.ij_plugins.io.vtk;

import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.ImageStatistics;
import ij.process.StackProcessor;
import ij.process.StackStatistics;
import junit.framework.TestCase;
import net.sf.ij_plugins.io.IOUtils;

import java.io.File;
import java.io.IOException;


/**
 * @author Eric Kischell
 */
public class VtkEncoderDecoderTest extends TestCase {

    private static final double TOLERANCE = 1e-6;

    public VtkEncoderDecoderTest(String test) {
        super(test);
    }

    public void testEncoderDecoder() throws Exception {
        final File imageFile = new File("test/data/test_ij_vtk_color1.tif");
        final String testFile = "test/data/test_ij_vtk_color1.vtk";

        assertTrue("Check file existance", imageFile.exists());

        // Read test image
        final ImagePlus imp = IOUtils.openImage(imageFile.getAbsolutePath());
        assertNotNull("Cannot open image: " + imageFile.getAbsolutePath(), imp);
        assertEquals("Expecting color image.", ImagePlus.COLOR_RGB, imp.getType());

        // Step 1: Encode RGB image
        try {
            VtkEncoder.save(testFile, imp);
        } catch (final IOException ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            fail(msg);
        }
        // Step 2: Decode RGB image
        ImagePlus imp2 = null;
        try {
            imp2 = VtkDecoder.open(testFile);
        } catch (final VtkImageException ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            fail(msg);
        }
        assertNotNull("Cannot decode image: " + testFile, imp2);
        assertEquals("Expecting color decoded image.", ImagePlus.COLOR_RGB, imp2.getType());

        // Step 3: Perform RGB image difference (imp, imp2)
        ImageStack stack1 = imp.getStack();
        StackProcessor sp = new StackProcessor(stack1, imp.getProcessor());
        Calibration cal2 = imp2.getCalibration();
        imp2.getProcessor().setCalibrationTable(cal2.getCTable());
        try {
            sp.copyBits(imp2.getStack(), 0, 0, Blitter.DIFFERENCE);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            fail(msg);
        }
        imp.setStack(null, stack1);
        imp.getProcessor().resetMinAndMax();

        // Step 4: Calculate first order statistics (assert stats == 0)
        ImageStatistics stats = new StackStatistics(imp);
        assertEquals("Expecting diff img pixel count", 256 * 256 * 5, stats.pixelCount);
        assertEquals("Expecting diff img mean", 0.0, stats.mean, TOLERANCE);
        assertEquals("Expecting diff img s.d.", 0.0, stats.stdDev, TOLERANCE);
        assertEquals("Expecting diff img mode", 0.0, stats.dmode, TOLERANCE);
        assertEquals("Expecting diff img min", 0.0, stats.min, TOLERANCE);
        assertEquals("Expecting diff img max", 0.0, stats.max, TOLERANCE);

        // Step 5: Nuke encoded image on disk
        new File(testFile).delete();
    }  // testEncoderDecoder()

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
