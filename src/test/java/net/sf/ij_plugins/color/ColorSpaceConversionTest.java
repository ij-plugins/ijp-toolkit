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

package net.sf.ij_plugins.color;

import junit.framework.TestCase;


/**
 * Unit test for {@link ColorSpaceConversion}.
 *
 * @author Jarek Sacha
 */
public class ColorSpaceConversionTest extends TestCase {

    public ColorSpaceConversionTest(String test) {
        super(test);
    }


    public void testXyzToRGB() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        final float[][] xyzs = {
                {0f, 0f, 0f},
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
        };

        final float[][] expectedRGBs = {
                {0f, 0f, 0f},
                {234.993465f, 201.824850f, 222.828435f},
                {82.699842f, 41.953780f, 59.584729f},
                {320.831534f, 182.941893f, 209.662412f},
        };

        final float[] rgb = new float[3];
        final float tolerance = 0.0002f;
        for (int i = 0; i < xyzs.length; i++) {
            final float[] xyz = xyzs[i];
            final float[] expectedRGB = expectedRGBs[i];
            ColorSpaceConversion.xyzToRGB(xyz, rgb);
            assertEquals("R " + i + " ", expectedRGB[0], rgb[0], tolerance);
            assertEquals("G " + i + " ", expectedRGB[1], rgb[1], tolerance);
            assertEquals("B " + i + " ", expectedRGB[2], rgb[2], tolerance);
        }
    }


    public void testRGBToXYZ() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        final float[][] rgbs = {
                {0f, 0f, 0f},
                {235.002002f, 201.823319f, 222.814012f},
                {82.703072f, 41.953316f, 59.580238f},
                {320.842703f, 182.940311f, 209.648766f},
        };

        final float[][] expectedXYZs = {
                {0f, 0f, 0f},
                {0.686336f, 0.651398f, 0.786241f},
                {0.051721f, 0.038004f, 0.046773f},
                {0.982596f, 0.744412f, 0.699247f},
        };


        final float[] xyz = new float[3];
        final float tolerance = 0.00001f;
        for (int i = 0; i < rgbs.length; i++) {
            final float[] rgb = rgbs[i];
            final float[] expectedXYZ = expectedXYZs[i];
            ColorSpaceConversion.rgbToXYZ(rgb, xyz);
            assertEquals("X " + i + " ", expectedXYZ[0], xyz[0], tolerance);
            assertEquals("Y " + i + " ", expectedXYZ[1], xyz[1], tolerance);
            assertEquals("Z " + i + " ", expectedXYZ[2], xyz[2], tolerance);
        }


    }


    public void testLabToXYZ() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        final float[][] labs = {
                {0f, 0f, 0f},
                {84.55571f, 15.145741f, -6.0582967f},
                {23.00000f, 21.368510f, -2.8035000f},
                {89.12989f, 52.419366f, +8.7064660f}
        };

        final float[][] expectedXYZs = {
                {0f, 0f, 0f},
                {0.686333f, 0.651398f, 0.786243f},
                {0.051721f, 0.038003f, 0.046773f},
                {0.982563f, 0.744397f, 0.699246f},
        };


        final float[] xyz = new float[3];
        final float tolerance = 0.00001f;
        for (int i = 0; i < labs.length; i++) {
            final float[] lab = labs[i];
            final float[] expectedXYZ = expectedXYZs[i];
            ColorSpaceConversion.labToXYZ(lab, xyz);
            assertEquals("X " + i + " ", expectedXYZ[0], xyz[0], tolerance);
            assertEquals("Y " + i + " ", expectedXYZ[1], xyz[1], tolerance);
            assertEquals("Z " + i + " ", expectedXYZ[2], xyz[2], tolerance);
        }

    }


    public void testXyzToLab() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        // Ref. White: D65
        // RGB Model : sRGB
        // Adaptation: None
        final float[][] xyzs = {
                {0f, 0f, 0f},
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
                {0.686282f, 0.651420f, 0.654469f},
        };

        final float[][] expectedLabs = {
                {0f, 0f, 0f},
                {84.555724f, 15.145162f, -6.065880f},
                {22.999941f, 21.368079f, -2.806380f},
                {89.129886f, 52.418814f, +8.699116f},
                {84.556856f, 15.129607f, +4.586629f},
        };


        final float[] lab = new float[3];
        final float tolerance = 0.00001f;
        for (int i = 0; i < xyzs.length; i++) {
            final float[] xyz = xyzs[i];
            final float[] expectedLab = expectedLabs[i];
            ColorSpaceConversion.xyzToLab(xyz, lab);
            assertEquals("L* " + i + " ", expectedLab[0], lab[0], tolerance);
            assertEquals("a* " + i + " ", expectedLab[1], lab[1], tolerance);
            assertEquals("b* " + i + " ", expectedLab[2], lab[2], tolerance);
        }
    }


    public void testRGBToXYZToLabToXYZtoRGB() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        // Ref. White: D65
        // RGB Model : sRGB
        // Adaptation: None
        final float tolerance = 0.0001f;
        final float[] rgb = {243, 201, 203};

        final float[] xyz = new float[3];
        ColorSpaceConversion.rgbToXYZ(rgb, xyz);
        assertEquals(0.686282, xyz[0], tolerance);
        assertEquals(0.651420, xyz[1], tolerance);
        assertEquals(0.654469, xyz[2], tolerance);

        final float[] lab = new float[3];
        ColorSpaceConversion.xyzToLab(xyz, lab);
        assertEquals(84.556863, lab[0], tolerance);
        assertEquals(15.129605, lab[1], tolerance);
        assertEquals(4.586627, lab[2], tolerance);

        final float[] xyz1 = new float[3];
        ColorSpaceConversion.labToXYZ(lab, xyz1);
        assertEquals(xyz[0], xyz1[0], tolerance);
        assertEquals(xyz[1], xyz1[1], tolerance);
        assertEquals(xyz[2], xyz1[2], tolerance);

        final float[] rgb1 = new float[3];
        ColorSpaceConversion.xyzToRGB(xyz1, rgb1);
        assertEquals(rgb[0], rgb1[0], tolerance);
        assertEquals(rgb[1], rgb1[1], tolerance);
        assertEquals(rgb[2], rgb1[2], tolerance);
    }


    public void testRgbToYCbCr_1() {
        final byte[] rgb = new byte[]{(byte) (0xff & 233), (byte) (0xff & 161), (byte) (0xff & 25)};
        final byte[] ybr = new byte[3];

        ColorSpaceConversion.rgbToYCbCr(rgb, ybr);

        final byte[] ybrExpected = new byte[]{(byte) (0xff & 159), (byte) (0xff & 58), (byte) (0xff & 169)};
        assertEquals("Y", ybrExpected[0], ybr[0]);
        assertEquals("Cb", ybrExpected[1], ybr[1]);
        assertEquals("Cr", ybrExpected[2], ybr[2]);
    }


    public void testRgbToYCbCr_2() {
        final byte[] rgb = new byte[]{(byte) (0xff & 71), (byte) (0xff & 139), (byte) (0xff & 244)};
        final byte[] ybr = new byte[3];

        ColorSpaceConversion.rgbToYCbCr(rgb, ybr);

        final byte[] ybrExpected = new byte[]{(byte) (0xff & 128), (byte) (0xff & 184), (byte) (0xff & 91)};
        assertEquals("Y", ybrExpected[0], ybr[0]);
        assertEquals("Cb", ybrExpected[1], ybr[1]);
        assertEquals("Cr", ybrExpected[2], ybr[2]);
    }


    public void testRgbToYCbCr_3() {
        final byte[] rgb = new byte[]{(byte) (0xff & 246), (byte) (0xff & 40), (byte) (0xff & 248)};
        final byte[] ybr = new byte[3];

        ColorSpaceConversion.rgbToYCbCr(rgb, ybr);

        final byte[] ybrExpected = new byte[]{(byte) (0xff & 124), (byte) (0xff & 189), (byte) (0xff & 204)};
        assertEquals("Y", ybrExpected[0], ybr[0]);
        assertEquals("Cb", ybrExpected[1], ybr[1]);
        assertEquals("Cr", ybrExpected[2], ybr[2]);
    }


    public void testYCbCrToRGB_1() {
        final byte[] ybr = new byte[]{(byte) (0xff & 159), (byte) (0xff & 58), (byte) (0xff & 169)};
        final byte[] rgb = new byte[3];

        ColorSpaceConversion.ycbcrToRGB(ybr, rgb);

        final byte[] rgbExpected = new byte[]{(byte) (0xff & 232), (byte) (0xff & 161), (byte) (0xff & 25)};
        assertEquals("R", rgbExpected[0], rgb[0]);
        assertEquals("G", rgbExpected[1], rgb[1]);
        assertEquals("B", rgbExpected[2], rgb[2]);
    }


    public void testYCbCrToRGB_2() {
        final byte[] ybr = new byte[]{(byte) (0xff & 128), (byte) (0xff & 184), (byte) (0xff & 91)};
        final byte[] rgb = new byte[3];

        ColorSpaceConversion.ycbcrToRGB(ybr, rgb);

        final byte[] rgbExpected = new byte[]{(byte) (0xff & 71), (byte) (0xff & 139), (byte) (0xff & 243)};
        assertEquals("R", rgbExpected[0], rgb[0]);
        assertEquals("G", rgbExpected[1], rgb[1]);
        assertEquals("B", rgbExpected[2], rgb[2]);
    }


    public void testYCbCrToRGB_3() {
        final byte[] ybr = new byte[]{(byte) (0xff & 124), (byte) (0xff & 189), (byte) (0xff & 204)};
        final byte[] rgb = new byte[3];

        ColorSpaceConversion.ycbcrToRGB(ybr, rgb);

        final byte[] rgbExpected = new byte[]{(byte) (0xff & 247), (byte) (0xff & 40), (byte) (0xff & 249)};
        assertEquals("R", rgbExpected[0], rgb[0]);
        assertEquals("G", rgbExpected[1], rgb[1]);
        assertEquals("B", rgbExpected[2], rgb[2]);
    }


}