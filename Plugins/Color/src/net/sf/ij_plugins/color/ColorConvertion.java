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

/**
 * Color space conversion utility.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class ColorConvertion {
    /**
     * Conversion from RGB to XYZ assuming Observer. = 2°, Illuminant = D65.<p/>
     * <p/>
     * Conversion based on formulas provided at http://www.easyrgb.com/math.php?MATH=M2#text2
     * <pre>
     * var_R = ( R / 255 )        //R = From 0 to 255
     * var_G = ( G / 255 )        //G = From 0 to 255
     * var_B = ( B / 255 )        //B = From 0 to 255
     * <p/>
     * if ( var_R > 0.04045 ) var_R = ( ( var_R + 0.055 ) / 1.055 ) ^ 2.4
     * else                   var_R = var_R / 12.92
     * if ( var_G > 0.04045 ) var_G = ( ( var_G + 0.055 ) / 1.055 ) ^ 2.4
     * else                   var_G = var_G / 12.92
     * if ( var_B > 0.04045 ) var_B = ( ( var_B + 0.055 ) / 1.055 ) ^ 2.4
     * else                   var_B = var_B / 12.92
     * <p/>
     * var_R = var_R * 100
     * var_G = var_G * 100
     * var_B = var_B * 100
     * <p/>
     * //Observer. = 2°, Illuminant = D65
     * X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805
     * Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722
     * Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505
     * </pre>
     *
     * @param src  source RGB values. Only first 3 values are used.
     * @param dest destinaltion XYZ values. Only first 3 values are used.
     */
    public static void convertRGBToXYZ(float[] src, float[] dest) {
        double var_R = src[0] / 255;        //R = From 0 to 255
        double var_G = src[1] / 255;        //G = From 0 to 255
        double var_B = src[2] / 255;        //B = From 0 to 255

        if (var_R > 0.04045) {
            var_R = Math.pow((var_R + 0.055) / 1.055, 2.4);
        } else {
            var_R = var_R / 12.92;
        }

        if (var_G > 0.04045) {
            var_G = Math.pow((var_G + 0.055) / 1.055, 2.4);
        } else {
            var_G = var_G / 12.92;
        }

        if (var_B > 0.04045) {
            var_B = Math.pow((var_B + 0.055) / 1.055, 2.4);
        } else {
            var_B = var_B / 12.92;
        }

        var_R *= 100;
        var_G *= 100;
        var_B *= 100;

        //Observer. = 2°, Illuminant = D65
        double x = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        double y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        double z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;

        dest[0] = (float) x;
        dest[1] = (float) y;
        dest[2] = (float) z;
    }

    /**
     * Conversion from XYZ color space to CIELab color space assuming Observer. = 2°, Illuminant =
     * D65.<p/>
     * <p/>
     * Conversion based on formulas provided at http://www.easyrgb.com/math.php?MATH=M2#text2
     * <pre>
     * var_X = X /  95.047          //Observer = 2°, Illuminant = D65
     * var_Y = Y / 100.000
     * var_Z = Z / 108.883
     * <p/>
     * if ( var_X > 0.008856 ) var_X = var_X ^ ( 1/3 )
     * else                    var_X = ( 7.787 * var_X ) + ( 16 / 116 )
     * if ( var_Y > 0.008856 ) var_Y = var_Y ^ ( 1/3 )
     * else                    var_Y = ( 7.787 * var_Y ) + ( 16 / 116 )
     * if ( var_Z > 0.008856 ) var_Z = var_Z ^ ( 1/3 )
     * else                    var_Z = ( 7.787 * var_Z ) + ( 16 / 116 )
     * <p/>
     * CIE-L* = ( 116 * var_Y ) - 16
     * CIE-a* = 500 * ( var_X - var_Y )
     * CIE-b* = 200 * ( var_Y - var_Z )
     * </pre>
     *
     * @param src
     * @param dest
     */
    public static void convertXYZtoLab(float[] src, float[] dest) {
        //Observer = 2°, Illuminant = D65
        double var_X = src[0] / 95.047;
        double var_Y = src[1] / 100.000;
        double var_Z = src[2] / 108.883;

        if (var_X > 0.008856) {
            var_X = Math.pow(var_X, 1.0 / 3.0);
        } else {
            var_X = (7.787 * var_X) + (16.0 / 116.0);
        }

        if (var_Y > 0.008856) {
            var_Y = Math.pow(var_Y, 1.0 / 3.0);
        } else {
            var_Y = (7.787 * var_Y) + (16.0 / 116.0);
        }

        if (var_Z > 0.008856) {
            var_Z = Math.pow(var_Z, 1.0 / 3.0);
        } else {
            var_Z = (7.787 * var_Z) + (16.0 / 116.0);
        }

        double l = 116 * var_Y - 16;
        double a = 500 * (var_X - var_Y);
        double b = 200 * (var_Y - var_Z);
        dest[0] = (float) l;
        dest[1] = (float) a;
        dest[2] = (float) b;
    }
}
