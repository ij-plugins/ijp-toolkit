/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * Unit test for {@link net.sf.ij_plugins.io.metaimage.MiDecoder}.
 *
 * @author Jarek Sacha
 */
public final class MiDecoderTest {

    @Test
    public void testRead1() throws Exception {
        final File inFile = new File("test/data/Image0075.mhd");
        final ImagePlus imp = MiDecoder.open(inFile);
        assertNotNull(imp);
        assertEquals(256, imp.getWidth());
        assertEquals(256, imp.getHeight());
        assertEquals(1, imp.getStackSize());
        assertEquals(ImagePlus.GRAY8, imp.getType());

    }


    @Test
    public void testReadLocal() throws Exception {
        final File inFile = new File("test/data/RFE-3091583_mha-internal/neuron.mha");
        final ImagePlus imp = MiDecoder.open(inFile);
        assertNotNull(imp);
        assertEquals(192, imp.getWidth());
        assertEquals(192, imp.getHeight());
        assertEquals(1, imp.getStackSize());
        assertEquals(ImagePlus.GRAY16, imp.getType());

    }

}