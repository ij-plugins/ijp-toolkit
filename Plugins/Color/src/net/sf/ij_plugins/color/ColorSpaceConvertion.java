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
 * Color space conversion utility, assuming two degree observer and illuminant D65. Conversion based
 * on formulas provided at http://www.easyrgb.com/math.php
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $
 */
public class ColorSpaceConvertion {

//    public final static double REF_X = 95.047;
//    public final static double REF_Y = 100.000;
//    public final static double REF_Z = 108.883;
    public final static double REF_X = 100.000;
    public final static double REF_Y = 100.000;
    public final static double REF_Z = 100.000;


    private ColorSpaceConvertion() {
    }

    /**
     * Conversion from XYZ to RGB assuming Observer. = 2°, Illuminant = D65.<p/>
     * <p/>
     * Conversion based on formulas provided at http://www.easyrgb.com/math.php?MATH=M1#text1
     * <pre>
     * ref_X =  95.047        //Observer = 2°, Illuminant = D65
     * ref_Y = 100.000
     * ref_Z = 108.883
     * <p/>
     * var_X = X / 100        //X = From 0 to ref_X
     * var_Y = Y / 100        //Y = From 0 to ref_Y
     * var_Z = Z / 100        //Z = From 0 to ref_Y
     * <p/>
     * var_R = var_X *  3.2406 + var_Y * -1.5372 + var_Z * -0.4986
     * var_G = var_X * -0.9689 + var_Y *  1.8758 + var_Z *  0.0415
     * var_B = var_X *  0.0557 + var_Y * -0.2040 + var_Z *  1.0570
     * <p/>
     * if ( var_R > 0.0031308 ) var_R = 1.055 * ( var_R ^ ( 1 / 2.4 ) ) - 0.055
     * else                     var_R = 12.92 * var_R
     * if ( var_G > 0.0031308 ) var_G = 1.055 * ( var_G ^ ( 1 / 2.4 ) ) - 0.055
     * else                     var_G = 12.92 * var_G
     * if ( var_B > 0.0031308 ) var_B = 1.055 * ( var_B ^ ( 1 / 2.4 ) ) - 0.055
     * else                     var_B = 12.92 * var_B
     * <p/>
     * R = var_R * 255
     * G = var_G * 255
     * B = var_B * 255
     * </pre>
     */

    public static void xyzToRGB(final float[] xyz, final float[] rgb) {
        final double var_X = xyz[0] / REF_X;        //X = From 0 to ref_X
        final double var_Y = xyz[1] / REF_Y;        //Y = From 0 to ref_Y
        final double var_Z = xyz[2] / REF_Z;        //Z = From 0 to ref_Y

        double var_R = var_X * 3.2406 + var_Y * -1.5372 + var_Z * -0.4986;
        double var_G = var_X * -0.9689 + var_Y * 1.8758 + var_Z * 0.0415;
        double var_B = var_X * 0.0557 + var_Y * -0.2040 + var_Z * 1.0570;

        if (var_R > 0.0031308) {
            var_R = 1.055 * Math.pow(var_R, (1 / 2.4)) - 0.055;
        } else {
            var_R = 12.92 * var_R;
        }

        if (var_G > 0.0031308) {
            var_G = 1.055 * Math.pow(var_G, (1 / 2.4)) - 0.055;
        } else {
            var_G = 12.92 * var_G;
        }
        if (var_B > 0.0031308) {
            var_B = 1.055 * Math.pow(var_B, (1 / 2.4)) - 0.055;
        } else {
            var_B = 12.92 * var_B;
        }

        rgb[0] = (float) (var_R * 255);
        rgb[1] = (float) (var_G * 255);
        rgb[2] = (float) (var_B * 255);
    }

    /**
     * Conversion from  CIE L*a*b* to XYZ assuming Observer. = 2°, Illuminant = D65.<p/>
     * <p/>
     * Conversion based on formulas provided at http://www.easyrgb.com/math.php?MATH=M8#text8
     * <pre>
     * var_Y = ( CIE-L* + 16 ) / 116
     * var_X = CIE-a* / 500 + var_Y
     * var_Z = var_Y - CIE-b* / 200
     * <p/>
     * if ( var_Y^3 > 0.008856 ) var_Y = var_Y^3
     * else                      var_Y = ( var_Y - 16 / 116 ) / 7.787
     * if ( var_X^3 > 0.008856 ) var_X = var_X^3
     * else                      var_X = ( var_X - 16 / 116 ) / 7.787
     * if ( var_Z^3 > 0.008856 ) var_Z = var_Z^3
     * else                      var_Z = ( var_Z - 16 / 116 ) / 7.787
     * <p/>
     * X = ref_X * var_X     //ref_X =  95.047  Observer= 2°, Illuminant= D65
     * Y = ref_Y * var_Y     //ref_Y = 100.000
     * Z = ref_Z * var_Z     //ref_Z = 108.883
     * </pre>
     */

    public static void labToXYZ(final float[] lab, final float[] xyz) {
        double var_Y = (lab[0] + 16) / 116;
        double var_X = lab[1] / 500 + var_Y;
        double var_Z = var_Y - lab[2] / 200;

//        if (var_Y ^ 3 > 0.008856)
        if (var_Y > 0.206893) {
            var_Y = Math.pow(var_Y, 3);
        } else {
            var_Y = (var_Y - 16.0 / 116.0) / 7.787;
        }
//        if (var_X ^ 3 > 0.008856)
        if (var_X > 0.206893) {
            var_X = Math.pow(var_X, 3);
        } else {
            var_X = (var_X - 16.0 / 116.0) / 7.787;
        }
//        if (var_Z ^ 3 > 0.008856)
        if (var_Z > 0.206893) {
            var_Z = Math.pow(var_Z, 3);
        } else {
            var_Z = (var_Z - 16.0 / 116.0) / 7.787;
        }

        xyz[0] = (float) (REF_X * var_X);     //ref_X =  95.047  Observer= 2°, Illuminant= D65
        xyz[1] = (float) (REF_Y * var_Y);     //ref_Y = 100.000
        xyz[2] = (float) (REF_Z * var_Z);     //ref_Z = 108.883
    }


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
     * @param src  source RGB values. Size of array <code>src</code> must be at least 3. If size of
     *             array <code>src</code> larger than three then only first 3 values are used.
     * @param dest destinaltion XYZ values. Size of array <code>dest</code> must be at least 3. If
     *             size of array <code>dest</code> larger than three then only first 3 values are
     *             used.
     */
    public static void rgbToXYZ(final float[] src, final float[] dest) {
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

        var_R *= REF_X;
        var_G *= REF_Y;
        var_B *= REF_Z;

        //Observer. = 2°, Illuminant = D65
        final double x = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        final double y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        final double z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;

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
     * @param src  source XYZ values. Size of array <code>src</code> must be at least 3. If size of
     *             array <code>src</code> larger than three then only first 3 values are used.
     * @param dest destinaltion CIE Lab values. Size of array <code>dest</code> must be at least 3.
     *             If size of array <code>dest</code> larger than three then only first 3 values are
     *             used.
     */
    public static void xyzToLab(final float[] src, final float[] dest) {
        //Observer = 2°, Illuminant = D65
        double var_X = src[0] / REF_X;
        double var_Y = src[1] / REF_Y;
        double var_Z = src[2] / REF_Z;

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

        final double l = 116 * var_Y - 16;
        final double a = 500 * (var_X - var_Y);
        final double b = 200 * (var_Y - var_Z);

        dest[0] = (float) l;
        dest[1] = (float) a;
        dest[2] = (float) b;
    }
}
