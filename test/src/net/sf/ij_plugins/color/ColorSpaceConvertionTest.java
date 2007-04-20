/*
 * Image/J Plugins
 * Copyright (C) 2002-2007 Jarek Sacha
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
 * Unit test for {@linkColorSpaceConvertion}.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class ColorSpaceConvertionTest extends TestCase {

    public ColorSpaceConvertionTest(String test) {
        super(test);
    }

    public void testXyzToRGB() {
        final float[][] xyzs = {
                {0f, 0f, 0f},
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
        };

        final float[][] expectedRGBs = {
                {0f, 0f, 0f},
                {235.002002f, 201.823319f, 222.814012f},
                {82.703072f, 41.953316f, 59.580238f},
                {320.842703f, 182.940311f, 209.648766f},
        };

        final float[] rgb = new float[3];
        final float tolerance = 0.05f;
        for (int i = 0; i < xyzs.length; i++) {
            final float[] xyz = xyzs[i];
            final float[] expextedRGB = expectedRGBs[i];
            ColorSpaceConvertion.xyzToRGB(xyz, rgb);
            assertEquals("R " + i + " ", expextedRGB[0], rgb[0], tolerance);
            assertEquals("G " + i + " ", expextedRGB[1], rgb[1], tolerance);
            assertEquals("B " + i + " ", expextedRGB[2], rgb[2], tolerance);
        }
    }

    public void testRGBToXYZ() {
        final float[][] rgbs = {
                {0f, 0f, 0f},
                {235.002002f, 201.823319f, 222.814012f},
                {82.703072f, 41.953316f, 59.580238f},
                {320.842703f, 182.940311f, 209.648766f},
        };

        final float[][] expectedXYZs = {
                {0f, 0f, 0f},
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
        };


        final float[] xyz = new float[3];
        final float tolerance = 0.0005f;
        for (int i = 0; i < rgbs.length; i++) {
            final float[] rgb = rgbs[i];
            final float[] expextedXYZ = expectedXYZs[i];
            ColorSpaceConvertion.rgbToXYZ(rgb, xyz);
            assertEquals("X " + i + " ", expextedXYZ[0], xyz[0], tolerance);
            assertEquals("Y " + i + " ", expextedXYZ[1], xyz[1], tolerance);
            assertEquals("Z " + i + " ", expextedXYZ[2], xyz[2], tolerance);
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
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
        };


        final float[] xyz = new float[3];
        final float tolerance = 0.0005f;
        for (int i = 0; i < labs.length; i++) {
            final float[] lab = labs[i];
            final float[] expextedXYZ = expectedXYZs[i];
            ColorSpaceConvertion.labToXYZ(lab, xyz);
            assertEquals("X " + i + " ", expextedXYZ[0], xyz[0], tolerance);
            assertEquals("Y " + i + " ", expextedXYZ[1], xyz[1], tolerance);
            assertEquals("Z " + i + " ", expextedXYZ[2], xyz[2], tolerance);
        }

    }

    public void testXyzToLab() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        final float[][] xyzs = {
                {0f, 0f, 0f},
                {0.686331f, 0.651398f, 0.786343f},
                {0.051720f, 0.038003f, 0.046779f},
                {0.982560f, 0.744397f, 0.699335f},
        };

        final float[][] expectedLabs = {
                {0f, 0f, 0f},
                {84.55571f, 15.145741f, -6.0582967f},
                {23.00000f, 21.368510f, -2.8035000f},
                {89.12989f, 52.419366f, +8.7064660f}
        };


        final float[] lab = new float[3];
        final float tolerance = 0.05f;
        for (int i = 0; i < xyzs.length; i++) {
            final float[] xyz = xyzs[i];
            final float[] expectedLab = expectedLabs[i];
            ColorSpaceConvertion.xyzToLab(xyz, lab);
            assertEquals("L* " + i + " ", expectedLab[0], lab[0], tolerance);
            assertEquals("a* " + i + " ", expectedLab[1], lab[1], tolerance);
            assertEquals("b* " + i + " ", expectedLab[2], lab[2], tolerance);
        }
    }


    public void testRGBToXYZToLabToXYZtoRGB() {
        // Test values generated using  http://www.brucelindbloom.com/index.html?ColorCalculator.html
        final float tolerance = 0.0005f;
        final float[] rgb = {243, 201, 203};

        final float[] xyz = new float[3];
        ColorSpaceConvertion.rgbToXYZ(rgb, xyz);
        assertEquals(0.686271, xyz[0], tolerance);
        assertEquals(0.651414, xyz[1], tolerance);
        assertEquals(0.654552, xyz[2], tolerance);

        final float[] lab = new float[3];
        ColorSpaceConvertion.xyzToLab(xyz, lab);
        assertEquals(84.555724f, lab[0], tolerance);
        assertEquals(15.135177f, lab[1], tolerance);
        assertEquals(4.584037f, lab[2], tolerance);

        final float[] xyz1 = new float[3];
        ColorSpaceConvertion.labToXYZ(lab, xyz1);
        assertEquals(xyz[0], xyz1[0], tolerance);
        assertEquals(xyz[1], xyz1[1], tolerance);
        assertEquals(xyz[2], xyz1[2], tolerance);

        final float[] rgb1 = new float[3];
        ColorSpaceConvertion.xyzToRGB(xyz1, rgb1);
        assertEquals(rgb[0], rgb1[0], 0.001);
        assertEquals(rgb[1], rgb1[1], 0.006);
        assertEquals(rgb[2], rgb1[2], 0.002);
    }


    public void testRgbToYCbCr() {
    }
}