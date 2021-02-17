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
package ij_plugins.toolkit.filters;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import ij_plugins.toolkit.io.IOUtils;
import junit.framework.TestCase;

/**
 * @author Jarek Sacha
 */
public class FastMedianUInt8Test extends TestCase {
    public FastMedianUInt8Test(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void test_BUG_1198520() throws Exception {
        final String inputImage = "test/data/blobs_noise_50x50.png";
        final ImagePlus imp = IOUtils.openImage(inputImage);
        final ByteProcessor src = (ByteProcessor) imp.getProcessor();

        final FastMedianUInt8 fastMedianUInt8 = new FastMedianUInt8();

        // BUG 1198520 was throwing "java.lang.ArithmeticException: / by zero"
        try {
            final ByteProcessor dest = fastMedianUInt8.run(src, 11, 11);
            assertNotNull(dest);
        } catch (ArithmeticException ex) {
            fail("BUG 1198520");
        }
    }
}