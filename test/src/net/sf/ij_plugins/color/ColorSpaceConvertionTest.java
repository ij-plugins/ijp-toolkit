/***
 * Image/J Plugins
 * Copyright (C) 2002-2004 Jarek Sacha
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
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ColorSpaceConvertionTest extends TestCase {
    private static final float TOLERANCE = 0.006f;

    public ColorSpaceConvertionTest(String test) {
        super(test);
    }

    public void testRGBToXYZToLabToXYZtoRGB() {
        final float[] rgb = {0, 0, 0};
        final float[] xyz = {0, 0, 0};


        rgb[0] = 243;
        rgb[1] = 201;
        rgb[2] = 203;
        ColorSpaceConvertion.rgbToXYZ(rgb, xyz);
//        assertEquals(68.63, xyz[0], TOLERANCE);
//        assertEquals(65.14, xyz[1], TOLERANCE);
//        assertEquals(65.46, xyz[2], TOLERANCE);

        final float[] lab = {0, 0, 0};
        ColorSpaceConvertion.xyzToLab(xyz, lab);
//        assertEquals(84.56, lab[0], TOLERANCE);
//        assertEquals(15.13, lab[1], TOLERANCE);
//        assertEquals(4.58, lab[2], TOLERANCE);

        final float[] xyz1 = {0, 0, 0};
        ColorSpaceConvertion.labToXYZ(lab, xyz1);
        assertEquals(xyz[0], xyz1[0], TOLERANCE);
        assertEquals(xyz[1], xyz1[1], TOLERANCE);
        assertEquals(xyz[2], xyz1[2], TOLERANCE);

        final float[] rgb1 = {0, 0, 0};
        ColorSpaceConvertion.xyzToRGB(xyz1, rgb1);
        assertEquals(rgb[0], rgb1[0], TOLERANCE);
        assertEquals(rgb[1], rgb1[1], TOLERANCE);
        assertEquals(rgb[2], rgb1[2], TOLERANCE);

    }


//    public void testRange() throws Exception {
//        float[] pixelSrc = new float[3];
//        float[] pixelDest = new float[3];
//        float[] tmp = new float[3];
//        float[] min = new float[3];
//        float[] max = new float[3];
//        for (int i = 0; i < 3; ++i) {
//            min[i] = Float.MAX_VALUE;
//            max[i] = Float.MIN_VALUE;
//        }
//        for (int r = 0; r < 256; ++r) {
//            pixelSrc[0] = r;
//            for (int g = 0; g < 256; ++g) {
//                pixelSrc[1] = g;
//                for (int b = 0; b < 256; ++b) {
//                    pixelSrc[2] = b;
//                    ColorSpaceConvertion.rgbToXYZ(pixelSrc, tmp);
//                    ColorSpaceConvertion.xyzToLab(tmp, pixelDest);
//                    for (int i = 0; i < 3; ++i) {
//                        min[i] = Math.min(min[i], pixelDest[i]);
//                        max[i] = Math.max(max[i], pixelDest[i]);
//                    }
//                }
//            }
//        }
//
//        System.out.println("L range:" + min[0] + " - " + max[0]);
//        System.out.println("a range:" + min[1] + " - " + max[1]);
//        System.out.println("b range:" + min[2] + " - " + max[2]);
//
//    }

    /**
     * The fixture set up called before every test method
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method
     */
    protected void tearDown() throws Exception {
    }
}