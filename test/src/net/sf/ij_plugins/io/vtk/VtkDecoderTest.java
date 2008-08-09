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
package net.sf.ij_plugins.io.vtk;

import ij.ImagePlus;
import net.sf.ij_plugins.AbstractImageTest;
import net.sf.ij_plugins.io.IOUtils;

import java.io.File;

/**
 * @author Jarek Sacha
 * @since 11:05:51 PM  Nov 12, 2006
 */
public final class VtkDecoderTest extends AbstractImageTest {
    public VtkDecoderTest(String test) {
        super(test);
    }

    public void testBUG1594780() throws Exception {
        final File extepectedImageFile = new File("test/data/BUG-1594780/test_COLOR_SCALARS_1.png");
        final File actualImageFile = new File("test/data/BUG-1594780/test_COLOR_SCALARS_1.vtk");

        assertTrue(extepectedImageFile.exists());
        final ImagePlus expectedImp = IOUtils.openImage(extepectedImageFile.getAbsolutePath());

        assertTrue(actualImageFile.exists());
        final ImagePlus actualImp = VtkDecoder.open(actualImageFile);

        assertEquals(expectedImp, actualImp, 0);
    }
}