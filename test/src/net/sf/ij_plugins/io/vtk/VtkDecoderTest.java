/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package net.sf.ij_plugins.io.vtk;

import ij.ImagePlus;
import net.sf.ij_plugins.AbstractImageTest;

import java.io.File;

/**
 * Date: Nov 12, 2006
 * Time: 11:05:51 PM
 *
 * @author Jarek Sacha
 */
public final class VtkDecoderTest extends AbstractImageTest {
    public VtkDecoderTest(String test) {
        super(test);
    }

    public void testBUG1594780() throws Exception {
        final File extepectedImageFile = new File("test/data/BUG-1594780/test_COLOR_SCALARS_1.png");
        final File actualImageFile = new File("test/data/BUG-1594780/test_COLOR_SCALARS_1.vtk");

        assertTrue(extepectedImageFile.exists());
        final ImagePlus expectedImp = openImage(extepectedImageFile.getAbsolutePath());

        assertTrue(actualImageFile.exists());
        final ImagePlus actualImp = VtkDecoder.open(actualImageFile);

        assertEquals(expectedImp, actualImp, 0);
    }
}