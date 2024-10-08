/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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

package ij_plugins.toolkit.io.metaimage;

import ij.ImagePlus;
import ij.measure.Calibration;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


/**
 * Unit test for {@link MiDecoder}.
 *
 * @author Jarek Sacha
 */
public final class MiDecoderTest {

    @Test
    public void testRead1() throws Exception {
        final File inFile = new File("test/data/Image0075.mhd");
        final ImagePlus[] imps = MiDecoder.open(inFile);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertEquals(256, imp.getWidth());
        assertEquals(256, imp.getHeight());
        assertEquals(1, imp.getStackSize());
        assertEquals(ImagePlus.GRAY8, imp.getType());
    }


    @Test
    public void testReadLocal() throws Exception {
        final File inFile = new File("test/data/RFE-3091583_mha-internal/neuron.mha");
        final ImagePlus[] imps = MiDecoder.open(inFile);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertEquals(192, imp.getWidth());
        assertEquals(192, imp.getHeight());
        assertEquals(1, imp.getStackSize());
        assertEquals(ImagePlus.GRAY16, imp.getType());
    }

    @Test
    public void mhaVirtualStack() throws MiException {
        final File inFile = new File("test/data/mri-stack.mha");
        final ImagePlus[] imps = MiDecoder.open(inFile, true);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertTrue(imp.getStack().isVirtual());
        assertEquals(186, imp.getWidth());
        assertEquals(226, imp.getHeight());
        assertEquals(27, imp.getStackSize());
        assertEquals(ImagePlus.GRAY8, imp.getType());
    }

    @Test
    public void mhaNoVirtualStack() throws MiException {
        final File inFile = new File("test/data/mri-stack.mha");
        final ImagePlus[] imps = MiDecoder.open(inFile, false);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertFalse("isVirtual", imp.getStack().isVirtual());
        assertEquals(186, imp.getWidth());
        assertEquals(226, imp.getHeight());
        assertEquals(27, imp.getStackSize());
        assertEquals(ImagePlus.GRAY8, imp.getType());
    }

    @Test
    public void testReadOffset() throws Exception {
        final File inFile = new File("test/data/Issue_001+2/fixed.mhd");
        final ImagePlus[] imps = MiDecoder.open(inFile);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertEquals("width", 305, imp.getWidth());
        assertEquals("heights", 311, imp.getHeight());
        assertEquals("slices", 11, imp.getStackSize());
        assertEquals("image type", ImagePlus.GRAY32, imp.getType());

        // Test if origin is read correctly, Issue #1.
        final Calibration cal = imp.getCalibration();
        final double elementSpacing = 0.04;
        assertEquals("xOrigin", 0 / elementSpacing, cal.xOrigin, 0.001);
        assertEquals("yOrigin", 0 / elementSpacing, cal.yOrigin, 0.001);
        assertEquals("zOrigin", 10.4 / elementSpacing, cal.zOrigin, 0.001);
    }

    @Test
    public void testReadITKTags() throws Exception {
        final File inFile = new File("test/data/Issue_001+2/fixed+itk.mhd");
        final ImagePlus[] imps = MiDecoder.open(inFile);
        assertNotNull(imps);
        assertEquals(1, imps.length);
        final ImagePlus imp = imps[0];
        assertEquals("width", 305, imp.getWidth());
        assertEquals("heights", 311, imp.getHeight());
        assertEquals("slices", 11, imp.getStackSize());
        assertEquals("image type", ImagePlus.GRAY32, imp.getType());
    }

}