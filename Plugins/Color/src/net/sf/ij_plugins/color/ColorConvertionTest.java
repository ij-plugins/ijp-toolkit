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
public class ColorConvertionTest extends TestCase {
    public ColorConvertionTest(String test) {
        super(test);
    }

    public void testRange() throws Exception {
        float[] pixelSrc = new float[3];
        float[] pixelDest = new float[3];
        float[] tmp = new float[3];
        float[] min = new float[3];
        float[] max = new float[3];
        for (int i = 0; i < 3; ++i) {
            min[i] = Float.MAX_VALUE;
            max[i] = Float.MIN_VALUE;
        }
        for (int r = 0; r < 256; ++r) {
            pixelSrc[0] = r;
            for (int g = 0; g < 256; ++g) {
                pixelSrc[1] = g;
                for (int b = 0; b < 256; ++b) {
                    pixelSrc[2] = b;
                    ColorConvertion.convertRGBToXYZ(pixelSrc, tmp);
                    ColorConvertion.convertXYZtoLab(tmp, pixelDest);
                    for (int i = 0; i < 3; ++i) {
                        min[i] = Math.min(min[i], pixelDest[i]);
                        max[i] = Math.max(max[i], pixelDest[i]);
                    }
                }
            }
        }

        System.out.println("L range:" + min[0] + " - " + max[0]);
        System.out.println("a range:" + min[1] + " - " + max[1]);
        System.out.println("b range:" + min[2] + " - " + max[2]);

    }

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