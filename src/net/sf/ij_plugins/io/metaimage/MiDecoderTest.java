/*
 * Copyright (c) 2000-2007 Jarek Sacha. All Rights Reserved.
 *
 * Author's e-mail: jsacha at users.sf.net
 *
 */
package net.sf.ij_plugins.io.metaimage;

import ij.ImagePlus;
import junit.framework.TestCase;

import java.io.File;

/**
 * Date: Oct 16, 2007
 * Time: 9:35:27 PM
 *
 * @author Jarek Sacha
 */
public final class MiDecoderTest extends TestCase {
    public MiDecoderTest(String test) {
        super(test);
    }

    public void testRead1() throws Exception {
        final File inFile = new File("test/data/Image0075.mhd");
        final ImagePlus imp = MiDecoder.open(inFile);
        assertNotNull(imp);
        assertEquals(256, imp.getWidth());
        assertEquals(256, imp.getHeight());
        assertEquals(1, imp.getStackSize());
        assertEquals(ImagePlus.GRAY8, imp.getType());

    }
}