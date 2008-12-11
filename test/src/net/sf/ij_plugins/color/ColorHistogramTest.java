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
package net.sf.ij_plugins.color;

import ij.ImagePlus;
import ij.process.ColorProcessor;
import junit.framework.TestCase;
import net.sf.ij_plugins.io.IOUtils;

import java.awt.Color;
import java.io.File;

/**
 * @author Jarek Sacha
 */
public class ColorHistogramTest extends TestCase {
    public ColorHistogramTest(String test) {
        super(test);
    }

    public void testHistogram() throws Exception {
        final File imageFile = new File("test/data/clown24.png");

        assertTrue("Check file existance", imageFile.exists());

        // Read test image
        final ImagePlus imp = IOUtils.openImage(imageFile);

        if (imp.getType() != ImagePlus.COLOR_RGB) {
            throw new java.lang.Exception("Expecting color image.");
        }


        final ColorHistogram colorHistogram = new net.sf.ij_plugins.color.ColorHistogram();
        colorHistogram.setBinsPerBand(2);
        colorHistogram.run((ColorProcessor) imp.getProcessor());

        final int[][][] bins = colorHistogram.getBins();
        final Color[][][] binColors = colorHistogram.getBinColors();
        assertNotNull(binColors);
        for (int r = 0; r < bins.length; r++) {
            final int[][] binGB = bins[r];
            for (int g = 0; g < binGB.length; g++) {
                final int[] binB = binGB[g];
                for (int b = 0; b < binB.length; b++) {
                    final int count = binB[b];
                    System.out.println("[" + r + "," + g + "," + b + "]: " + count);
                }

            }
        }

    }
}