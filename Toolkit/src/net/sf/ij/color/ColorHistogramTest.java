/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
package net.sf.ij.color;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ColorProcessor;
import junit.framework.TestCase;

import java.awt.*;
import java.io.File;

/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ColorHistogramTest extends TestCase {
    public ColorHistogramTest(String test) {
        super(test);
    }

    public void testHistogram() throws Exception {
        final File imageFile = new File("test_images/clown24.tif");

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


        net.sf.ij.color.ColorHistogram colorHistogram = new net.sf.ij.color.ColorHistogram();
        colorHistogram.setBinsPerBand(2);
        colorHistogram.run((ColorProcessor) imp.getProcessor());

        int[][][] bins = colorHistogram.getBins();
        Color[][][] binColors = colorHistogram.getBinColors();
        for (int r = 0; r < bins.length; r++) {
            int[][] binGB = bins[r];
            for (int g = 0; g < binGB.length; g++) {
                int[] binB = binGB[g];
                for (int b = 0; b < binB.length; b++) {
                    int count = binB[b];
                    System.out.println("[" + r + "," + g + "," + b + "]: " + count);
                }

            }
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