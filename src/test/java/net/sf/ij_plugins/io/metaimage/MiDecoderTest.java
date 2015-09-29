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

package net.sf.ij_plugins.io.metaimage;

import ij.ImagePlus;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;


/**
 * Unit test for {@link net.sf.ij_plugins.io.metaimage.MiDecoder}.
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


}