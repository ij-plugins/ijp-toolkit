/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.progress.ProgressEvent;
import net.sf.ij_plugins.util.progress.ProgressListener;

/**
 * Color space conversion utility, assuming two degree observer and illuminant D65. Conversion based
 * on formulas provided at http://www.easyrgb.com/math.php
 *
 * @author Jarek Sacha
 * @version $Revision: 1.5 $
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

    /**
     * Convert between RGB and CIE L*a*b* color image representation.
     *
     * @param cp RGB image to be converted
     * @return CIE L*a*b* image represented by {@link VectorProcessor}.
     */
    public static VectorProcessor rgbToLabVectorProcessor(final ColorProcessor cp) {
        final VectorProcessor vp = new VectorProcessor(cp);
        final float[][] pixels = vp.getPixels();
        final float[] tmp = new float[3];

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(pixels.length / 10, 1);
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            ColorSpaceConvertion.rgbToXYZ(pixel, tmp);
            ColorSpaceConvertion.xyzToLab(tmp, pixel);
        }

        return vp;
    }

    /**
     * Convert between CIE L*a*b* and RGB color image representation.
     *
     * @param vp CIE L*a*b* image represented by {@link VectorProcessor}.
     * @return RGB image represented by a {@link ColorProcessor}.
     */
    public static ColorProcessor labToColorProcessor(final VectorProcessor vp) {
        final float[][] pixels = vp.getPixels();
        final float[] tmpXYZ = new float[3];
        final float[] tmpRGB = new float[3];

        final int width = vp.getWidth();
        final int height = vp.getHeight();
        final int sliceSize = width * height;
        final byte[] red = new byte[sliceSize];
        final byte[] green = new byte[sliceSize];
        final byte[] blue = new byte[sliceSize];

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(pixels.length / 10, 1);
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            ColorSpaceConvertion.labToXYZ(pixel, tmpXYZ);
            ColorSpaceConvertion.xyzToRGB(tmpXYZ, tmpRGB);
            int r = Math.min(Math.max(Math.round(tmpRGB[0]), 0), 255);
            int g = Math.min(Math.max(Math.round(tmpRGB[1]), 0), 255);
            int b = Math.min(Math.max(Math.round(tmpRGB[2]), 0), 255);
            red[i] = (byte) (r & 0xff);
            green[i] = (byte) (g & 0xff);
            blue[i] = (byte) (b & 0xff);
        }

        final ColorProcessor cp = new ColorProcessor(width, height);
        cp.setRGB(red, green, blue);

        return cp;
    }

    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <p/>
     * YCbCb (601) from "digital 8-bit RGB  "
     * <pre>
     * ========================================================================
     * Y  = 16  + 1/256 * (   65.738  * R +  129.057  * G +  25.064  * B)
     * Cb = 128 + 1/256 * ( - 37.945  * R -   74.494  * G + 112.439  * B)
     * Cr = 128 + 1/256 * (  112.439  * R -   94.154  * G -  18.285  * B)
     * ........................................................................
     * R, G, B          in {0, 1, 2, ..., 255}
     * Y                in {16, 17, ..., 235}
     *    with footroom in {1, 2, ..., 15}
     *         headroom in {236, 237, ..., 254}
     *         sync.    in {0, 255}
     * Cb, Cr           in {16, 17, ..., 240}
     * </pre>
     *
     * @param src              source image.
     * @param progressListener progress listener.
     * @return array of color bands: Y, Cb, Cr, respectively.
     */
    public static ByteProcessor[] rgbToYCbCr(final ColorProcessor src, final ProgressListener progressListener) {

        final String progressMessage = "Converting RGB to YCbCr...";
        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(src, 0.0, progressMessage));
        }

        final int width = src.getWidth();
        final int height = src.getHeight();
        final int nbPixels = width * height;

        final byte[] rPixels = new byte[nbPixels];
        final byte[] gPixels = new byte[nbPixels];
        final byte[] bPixels = new byte[nbPixels];
        src.getRGB(rPixels, gPixels, bPixels);

        final byte[] yPixels = new byte[nbPixels];
        final byte[] cbPixels = new byte[nbPixels];
        final byte[] crPixels = new byte[nbPixels];

        final int progressStep = nbPixels / 10;
        for (int i = 0; i < nbPixels; i++) {
            final int r = 0xff & rPixels[i];
            final int g = 0xff & gPixels[i];
            final int b = 0xff & bPixels[i];

            final double y = 16 + 0.25678906250000 * r + 0.50412890625000 * g + 0.09790625000000 * b;
            final double cb = 128 - 0.14822265625000 * r - 0.29099218750000 * g + 0.43921484375000 * b;
            final double cr = 128 + 0.43921484375000 * r - 0.36778906250000 * g - 0.07142578125000 * b;

            yPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(y), 0), 255));
            cbPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(cb), 0), 255));
            crPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(cr), 0), 255));

            if ((progressListener != null) && (i % progressStep == 0)) {
                progressListener.progressNotification(new ProgressEvent(src, i / (double) nbPixels, progressMessage));
            }
        }

        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(src, 1.0, progressMessage));
        }

        return new ByteProcessor[]{
                new ByteProcessor(width, height, yPixels, null),
                new ByteProcessor(width, height, cbPixels, null),
                new ByteProcessor(width, height, crPixels, null),
        };

    }

    /**
     * Converts image pixels from RGB color space to YCbCr color space.
     * Equivalent to calling <code>rgbToYCbCr(src, null)</code>.
     *
     * @see #rgbToYCbCr(ij.process.ColorProcessor, net.sf.ij_plugins.util.progress.ProgressListener)
     */
    public static ByteProcessor[] rgbToYCbCr(final ColorProcessor src) {
        return rgbToYCbCr(src, null);
    }

    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <p/>
     * "digital 8-bit RGB" from YCbCb (601)
     * <pre>
     * ========================================================================
     * R = 1/256 * (298.081952524118 * (Y -16) +   0.001711624973 * (Cb - 128) + 408.582641764512 * (Cr-128))
     * G = 1/256 * (298.081952524118 * (Y -16) - 100.290891128080 * (Cb - 128) - 208.120396471735 * (Cr-128))
     * B = 1/256 * (298.081952524118 * (Y -16) + 516.412147108167 * (Cb - 128) -   0.000466679809 * (Cr-128))
     * </pre>
     *
     * @param src source image, array of color bands: Y, Cb, Cr, respectively.
     * @return RGB image.
     */
    public static ColorProcessor ycbcrToRGB(final ByteProcessor[] src, final ProgressListener progressListener) {

        if (src == null) {
            throw new IllegalArgumentException("Argument 'src' cannot be null.");
        }

        if (src.length != 3) {
            throw new IllegalArgumentException("Argument's 'src' length must be 3, got " + src.length + ".");
        }

        final String progressMessage = "Converting YCbCr to RGB...";
        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(src, 0.0, progressMessage));
        }

        final int width = src[0].getWidth();
        final int height = src[0].getHeight();
        final int nbPixels = width * height;

        final byte[] rPixels = new byte[nbPixels];
        final byte[] gPixels = new byte[nbPixels];
        final byte[] bPixels = new byte[nbPixels];

        final byte[] yPixels = (byte[]) src[0].getPixels();
        final byte[] cbPixels = (byte[]) src[1].getPixels();
        final byte[] crPixels = (byte[]) src[2].getPixels();

        final int progressStep = nbPixels / 10;
        for (int i = 0; i < nbPixels; i++) {

            final int y = (0xff & yPixels[i]) - 16;
            final int cb = (0xff & cbPixels[i]) - 128;
            final int cr = (0xff & crPixels[i]) - 128;

            final double r = 1.16438262704734 * y - 0.00000668603505 * cb + 1.59602594439262 * cr;
            final double g = 1.16438262704734 * y - 0.39176129346906 * cb - 0.81297029871772 * cr;
            final double b = 1.16438262704734 * y + 2.01723494964128 * cb - 0.00000182296800 * cr;

            rPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(r), 0), 255));
            gPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(g), 0), 255));
            bPixels[i] = (byte) (0xff & Math.min(Math.max(Math.round(b), 0), 255));

            if ((progressListener != null) && (i % progressStep == 0)) {
                progressListener.progressNotification(new ProgressEvent(src, i / (double) nbPixels, progressMessage));
            }
        }

        final ColorProcessor dest = new ColorProcessor(width, height);
        dest.setRGB(rPixels, gPixels, bPixels);

        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(src, 1.0, progressMessage));
        }

        return dest;
    }

    /**
     * Converts image pixels from RGB color space to YCbCr color space.
     * Equivalent to calling <code>ycbcrToRGB(src, null)</code>.
     *
     * @see #ycbcrToRGB(ij.process.ByteProcessor[], net.sf.ij_plugins.util.progress.ProgressListener)
     */
    public static ColorProcessor ycbcrToRGB(final ByteProcessor[] src) {
        return ycbcrToRGB(src, null);
    }
}
