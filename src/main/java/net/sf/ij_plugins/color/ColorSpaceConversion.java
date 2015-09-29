/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import net.sf.ij_plugins.multiband.VectorProcessor;
import net.sf.ij_plugins.util.Validate;
import net.sf.ij_plugins.util.progress.ProgressEvent;
import net.sf.ij_plugins.util.progress.ProgressListener;


/**
 * Basic color space conversion utilities, assuming two degree observer and illuminant D65.
 *
 * @author Jarek Sacha
 */
public final class ColorSpaceConversion {

    // D65 white point values from Chromatic Adaptation table at http://www.brucelindbloom.com/Eqn_ChromAdapt.html
    private static final double X_D65 = 0.95047;
    private static final double Y_D65 = 1.0000;
    private static final double Z_D65 = 1.08883;


    // Coefficients from http://www.brucelindbloom.com/Eqn_XYZ_to_Lab.html
    private static final double CIE_EPSILON = 0.008856;
    private static final double CIE_KAPPA = 903.3;
    private static final double CIE_KAPPA_EPSILON = CIE_EPSILON * CIE_KAPPA;


    private ColorSpaceConversion() {
    }


    /**
     * XYZ to sRGB conversion based on conversion coefficients at: http://www.brucelindbloom.com/Eqn_XYZ_to_RGB.html
     * <br>
     * Conversion from CIE XYZ to sRGB as defined in the IEC 619602-1 standard
     * (http://www.colour.org/tc8-05/Docs/colorspace/61966-2-1.pdf)
     * <pre>
     * r_linear = +3.2404542 * X - 1.5371385 * Y - 0.4985314 * Z;
     * g_linear = -0.9692660 * X + 1.8760108 * Y + 0.0415560 * Z;
     * b_linear = +0.0556434 * X - 0.2040259 * Y + 1.0572252 * Z;
     * r = r_linear &gt; 0.0031308
     *       ? 1.055 * Math.pow(r_linear, (1 / 2.4)) - 0.055
     *       : 12.92 * r_linear;
     * g = g_linear &gt; 0.0031308
     *       ? 1.055 * Math.pow(g_linear, (1 / 2.4)) - 0.055
     *       : 12.92 * g_linear;
     * b = b_linear &gt; 0.0031308
     *       ? 1.055 * Math.pow(b_linear, (1 / 2.4)) - 0.055
     *       : 12.92 * b_linear;
     * R = r * 255
     * G = r * 255
     * B = r * 255
     * </pre>
     *
     * @param xyz input CIE XYZ values.
     * @param rgb output sRGB values in [0, 255] range.
     */
    public static void xyzToRGB(final float[] xyz, final float[] rgb) {
        final double x = xyz[0];
        final double y = xyz[1];
        final double z = xyz[2];

        final double r_linear = +3.2404542 * x - 1.5371385 * y - 0.4985314 * z;
        final double g_linear = -0.9692660 * x + 1.8760108 * y + 0.0415560 * z;
        final double b_linear = +0.0556434 * x - 0.2040259 * y + 1.0572252 * z;

        final double r = r_linear > 0.0031308
                ? 1.055 * Math.pow(r_linear, (1 / 2.4)) - 0.055
                : 12.92 * r_linear;

        final double g = g_linear > 0.0031308
                ? 1.055 * Math.pow(g_linear, (1 / 2.4)) - 0.055
                : 12.92 * g_linear;

        final double b = b_linear > 0.0031308
                ? 1.055 * Math.pow(b_linear, (1 / 2.4)) - 0.055
                : 12.92 * b_linear;

        rgb[0] = (float) (r * 255);
        rgb[1] = (float) (g * 255);
        rgb[2] = (float) (b * 255);
    }


    /**
     * Conversion from  CIE L*a*b* to CIE XYZ assuming Observer. = 2°, Illuminant = D65.
     * Conversion based on formulas provided at http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
     *
     * @param lab input CIE L*a*b* values.
     * @param xyz output CIE XYZ values.
     */
    public static void labToXYZ(final float[] lab, final float[] xyz) {

        final double l = lab[0];
        final double a = lab[1];
        final double b = lab[2];


        final double yr = l > CIE_KAPPA_EPSILON
                ? Math.pow((l + 16) / 116, 3)
                : l / CIE_KAPPA;

        final double fy = yr > CIE_EPSILON
                ? (l + 16) / 116.0
                : (CIE_KAPPA * yr + 16) / 116.0;

        final double fx = a / 500 + fy;
        final double fx3 = fx * fx * fx;

        final double xr = fx3 > CIE_EPSILON
                ? fx3
                : (116 * fx - 16) / CIE_KAPPA;

        final double fz = fy - b / 200.0;
        final double fz3 = fz * fz * fz;
        final double zr = fz3 > CIE_EPSILON
                ? fz3
                : (116 * fz - 16) / CIE_KAPPA;

        xyz[0] = (float) (xr * X_D65);
        xyz[1] = (float) (yr * Y_D65);
        xyz[2] = (float) (zr * Z_D65);
    }


    /**
     * sRGB to XYZ conversion based on conversion coefficients at:  http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html
     * <br>
     * See also conversion from sRGB to CIE XYZ  as defined in the IEC 619602-1 standard
     * (http://www.colour.org/tc8-05/Docs/colorspace/61966-2-1.pdf), though it uses approximated coefficients.
     * <pre>
     * r = R / 255;
     * g = G / 255;
     * b = B / 255;
     * r_linear = r &gt; 0.04045
     *          ? Math.pow((r + 0.055) / 1.055, 2.4)
     *          : r / 12.92;
     * g_linear = g &gt; 0.04045
     *          ? Math.pow((g + 0.055) / 1.055, 2.4)
     *          : g / 12.92;
     * b_linear = b &gt; 0.04045
     *          ? Math.pow((b + 0.055) / 1.055, 2.4)
     *          : b / 12.92;
     * X = 0.4124564 * r_linear + 0.3575761 * g_linear + 0.1804375 * b_linear;
     * Y = 0.2126729 * r_linear + 0.7151522 * g_linear + 0.0721750 * b_linear;
     * Z = 0.0193339 * r_linear + 0.1191920 * g_linear + 0.9503041 * b_linear;
     * </pre>
     *
     * @param rgb source sRGB values. Size of array <code>rgb</code> must be at least 3. If size of
     *            array <code>rgb</code> larger than three then only first 3 values are used.
     * @param xyz destination CIE XYZ values. Size of array <code>xyz</code> must be at least 3. If
     *            size of array <code>xyz</code> larger than three then only first 3 values are
     *            used.
     */
    public static void rgbToXYZ(final float[] rgb, final float[] xyz) {
        final double r = rgb[0] / 255;
        final double g = rgb[1] / 255;
        final double b = rgb[2] / 255;

        final double r_linear = r > 0.04045
                ? Math.pow((r + 0.055) / 1.055, 2.4)
                : r / 12.92;

        final double g_linear = g > 0.04045
                ? Math.pow((g + 0.055) / 1.055, 2.4)
                : g / 12.92;

        final double b_linear = b > 0.04045
                ? Math.pow((b + 0.055) / 1.055, 2.4)
                : b / 12.92;

        final double x = 0.4124564 * r_linear + 0.3575761 * g_linear + 0.1804375 * b_linear;
        final double y = 0.2126729 * r_linear + 0.7151522 * g_linear + 0.0721750 * b_linear;
        final double z = 0.0193339 * r_linear + 0.1191920 * g_linear + 0.9503041 * b_linear;

        xyz[0] = (float) x;
        xyz[1] = (float) y;
        xyz[2] = (float) z;
    }


    /**
     * Conversion from  CIE XYZ to CIE L*a*b* assuming Observer. = 2°, Illuminant = D65.
     * Conversion based on formulas provided at http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
     *
     * @param xyz source CIE XYZ values. Size of array <code>xyz</code> must be at least 3. If size of
     *            array <code>xyz</code> larger than three then only first 3 values are used.
     * @param lab destination CIE L*a*b* values. Size of array <code>lab</code> must be at least 3.
     *            If size of array <code>lab</code> larger than three then only first 3 values are
     *            used.
     */
    public static void xyzToLab(final float[] xyz, final float[] lab) {
        final double xr = xyz[0] / X_D65;
        final double yr = xyz[1] / Y_D65;
        final double zr = xyz[2] / Z_D65;

        final double fx = xr > CIE_EPSILON
                ? Math.pow(xr, 1.0 / 3.0)
                : (CIE_KAPPA * xr + 16) / 116.0;

        final double fy = yr > CIE_EPSILON
                ? Math.pow(yr, 1.0 / 3.0)
                : (CIE_KAPPA * yr + 16) / 116.0;

        final double fz = zr > CIE_EPSILON
                ? Math.pow(zr, 1.0 / 3.0)
                : (CIE_KAPPA * zr + 16) / 116.0;

        lab[0] = (float) (116 * fy - 16);
        lab[1] = (float) (500 * (fx - fy));
        lab[2] = (float) (200 * (fy - fz));
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
            ColorSpaceConversion.rgbToXYZ(pixel, tmp);
            ColorSpaceConversion.xyzToLab(tmp, pixel);
        }
        IJ.showProgress(pixels.length, pixels.length);

        return vp;
    }


    /**
     * Convert between sRGB and XYZ color image representation.
     *
     * @param cp RGB image to be converted
     * @return XYZ image represented by {@link VectorProcessor}.
     */
    public static VectorProcessor rgbToXYZVectorProcessor(final ColorProcessor cp) {

        final VectorProcessor vp = new VectorProcessor(cp);
        final float[][] pixels = vp.getPixels();

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(pixels.length / 10, 1);
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            // Replace sRGB content with XYZ
            ColorSpaceConversion.rgbToXYZ(pixel, pixel);
        }
        IJ.showProgress(pixels.length, pixels.length);

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
            ColorSpaceConversion.labToXYZ(pixel, tmpXYZ);
            ColorSpaceConversion.xyzToRGB(tmpXYZ, tmpRGB);
            final int r = Math.min(Math.max(Math.round(tmpRGB[0]), 0), 255);
            final int g = Math.min(Math.max(Math.round(tmpRGB[1]), 0), 255);
            final int b = Math.min(Math.max(Math.round(tmpRGB[2]), 0), 255);
            red[i] = (byte) (r & 0xff);
            green[i] = (byte) (g & 0xff);
            blue[i] = (byte) (b & 0xff);
        }
        IJ.showProgress(pixels.length, pixels.length);

        final ColorProcessor cp = new ColorProcessor(width, height);
        cp.setRGB(red, green, blue);

        return cp;
    }

    /**
     * Convert between CIE L*a*b* and XYZ color image representation.
     *
     * @param vp L*a*b* image represented by {@link VectorProcessor}.
     * @return XYZ image represented by a {@link VectorProcessor}.
     */
    public static VectorProcessor labToXYZVectorProcessor(final VectorProcessor vp) {
        final float[][] pixels = vp.getPixels();
        final int width = vp.getWidth();
        final int height = vp.getHeight();

        final VectorProcessor dest = new VectorProcessor(width, height, 3);
        final float[][] destPixels = dest.getPixels();

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(pixels.length / 10, 1);
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            final float[] xyz = destPixels[i];
            ColorSpaceConversion.labToXYZ(pixel, xyz);
        }
        IJ.showProgress(pixels.length, pixels.length);

        return dest;
    }


    /**
     * Convert between XYZ and RGB color image representation.
     *
     * @param vp XYZ image represented by {@link VectorProcessor}.
     * @return RGB image represented by a {@link ColorProcessor}.
     */
    public static ColorProcessor xyzToColorProcessor(final VectorProcessor vp) {
        final float[][] pixels = vp.getPixels();
        final float[] rgb = new float[3];

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
            ColorSpaceConversion.xyzToRGB(pixel, rgb);
            final int r = Math.min(Math.max(Math.round(rgb[0]), 0), 255);
            final int g = Math.min(Math.max(Math.round(rgb[1]), 0), 255);
            final int b = Math.min(Math.max(Math.round(rgb[2]), 0), 255);
            red[i] = (byte) (r & 0xff);
            green[i] = (byte) (g & 0xff);
            blue[i] = (byte) (b & 0xff);
        }
        IJ.showProgress(pixels.length, pixels.length);


        final ColorProcessor cp = new ColorProcessor(width, height);
        cp.setRGB(red, green, blue);

        return cp;
    }

    /**
     * Convert between CIE XYZ and L*a*b* color image representation.
     *
     * @param vp XYZ image represented by {@link VectorProcessor}.
     * @return L*a*b* image represented by a {@link VectorProcessor}.
     */
    public static VectorProcessor xyzToLabVectorProcessor(final VectorProcessor vp) {
        final float[][] pixels = vp.getPixels();
        final int width = vp.getWidth();
        final int height = vp.getHeight();

        final VectorProcessor dest = new VectorProcessor(width, height, 3);
        final float[][] destPixels = dest.getPixels();

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(pixels.length / 10, 1);
        for (int i = 0; i < pixels.length; i++) {
            if (i % progressStep == 0) {
                IJ.showProgress(i, pixels.length);
            }
            final float[] pixel = pixels[i];
            final float[] lab = destPixels[i];
            ColorSpaceConversion.xyzToLab(pixel, lab);
        }
        IJ.showProgress(pixels.length, pixels.length);

        return dest;
    }


    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <br>
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
     * @param rgb source RGB (cannot be null).
     * @param ybr destination Y, Cb, and Cr (cannot be null).
     */
    static void rgbToYCbCr(final byte[] rgb, final byte[] ybr) {
        final int r = 0xff & rgb[0];
        final int g = 0xff & rgb[1];
        final int b = 0xff & rgb[2];

        final double y = 16 + 0.25678906250000 * r + 0.50412890625000 * g + 0.09790625000000 * b;
        final double cb = 128 - 0.14822265625000 * r - 0.29099218750000 * g + 0.43921484375000 * b;
        final double cr = 128 + 0.43921484375000 * r - 0.36778906250000 * g - 0.07142578125000 * b;

        ybr[0] = (byte) (0xff & Math.min(Math.max(Math.round(y), 0), 255));
        ybr[1] = (byte) (0xff & Math.min(Math.max(Math.round(cb), 0), 255));
        ybr[2] = (byte) (0xff & Math.min(Math.max(Math.round(cr), 0), 255));
    }


    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <br>
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

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(1, nbPixels / 10);
        final byte[] rgb = new byte[3];
        final byte[] ybr = new byte[3];
        for (int i = 0; i < nbPixels; i++) {
            rgb[0] = rPixels[i];
            rgb[1] = gPixels[i];
            rgb[2] = bPixels[i];

            rgbToYCbCr(rgb, ybr);

            yPixels[i] = ybr[0];
            cbPixels[i] = ybr[1];
            crPixels[i] = ybr[2];

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
     * Equivalent to calling <code>rgbToYCbCr(rgb, null)</code>.
     *
     * @param rgb input image in sRGB color space.
     * @return array of ByteProcessor representing color planes: Y, Cb, and Cr.
     * @see #rgbToYCbCr(ij.process.ColorProcessor, net.sf.ij_plugins.util.progress.ProgressListener)
     */
    public static ByteProcessor[] rgbToYCbCr(final ColorProcessor rgb) {
        return rgbToYCbCr(rgb, null);
    }


    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <br>
     * "digital 8-bit RGB" from YCbCb (601)
     * <pre>
     * ========================================================================
     * R = 1/256 * (298.081952524118 * (Y -16) +   0.001711624973 * (Cb - 128) + 408.582641764512 * (Cr-128))
     * G = 1/256 * (298.081952524118 * (Y -16) - 100.290891128080 * (Cb - 128) - 208.120396471735 * (Cr-128))
     * B = 1/256 * (298.081952524118 * (Y -16) + 516.412147108167 * (Cb - 128) -   0.000466679809 * (Cr-128))
     * </pre>
     *
     * @param ybr source Y, Cb, and Cr (cannot ne null).
     * @param rgb destination RGB (cannot be null).
     */
    public static void ycbcrToRGB(final byte[] ybr, final byte[] rgb) {
        final int y = (0xff & ybr[0]) - 16;
        final int cb = (0xff & ybr[1]) - 128;
        final int cr = (0xff & ybr[2]) - 128;

        final double r = 1.16438262704734 * y - 0.00000668603505 * cb + 1.59602594439262 * cr;
        final double g = 1.16438262704734 * y - 0.39176129346906 * cb - 0.81297029871772 * cr;
        final double b = 1.16438262704734 * y + 2.01723494964128 * cb - 0.00000182296800 * cr;

        rgb[0] = (byte) (0xff & Math.min(Math.max(Math.round(r), 0), 255));
        rgb[1] = (byte) (0xff & Math.min(Math.max(Math.round(g), 0), 255));
        rgb[2] = (byte) (0xff & Math.min(Math.max(Math.round(b), 0), 255));
    }


    /**
     * Converts image pixels from RGB color space to YCbCr color space. Uses formulas provided at:
     * <a href="http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30">
     * http://www.poynton.com/notes/colour_and_gamma/ColorFAQ.html#RTFToC30</a>. See also:
     * <a href="http://en.wikipedia.org/wiki/YCbCr">http://en.wikipedia.org/wiki/YCbCr</a>.
     * <br>
     * "digital 8-bit RGB" from YCbCb (601)
     * <pre>
     * ========================================================================
     * R = 1/256 * (298.081952524118 * (Y -16) +   0.001711624973 * (Cb - 128) + 408.582641764512 * (Cr-128))
     * G = 1/256 * (298.081952524118 * (Y -16) - 100.290891128080 * (Cb - 128) - 208.120396471735 * (Cr-128))
     * B = 1/256 * (298.081952524118 * (Y -16) + 516.412147108167 * (Cb - 128) -   0.000466679809 * (Cr-128))
     * </pre>
     *
     * @param ybr              source image, array of color bands: Y, Cb, Cr, respectively.
     * @param progressListener progress listener, can be null.
     * @return RGB image.
     */
    public static ColorProcessor ycbcrToRGB(final ByteProcessor[] ybr, final ProgressListener progressListener) {

        Validate.argumentNotNull(ybr, "ybr");

        if (ybr.length != 3) {
            throw new IllegalArgumentException("Argument's 'ybr' length must be 3, got " + ybr.length + ".");
        }

        final String progressMessage = "Converting YCbCr to RGB...";
        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(ybr, 0.0, progressMessage));
        }

        final int width = ybr[0].getWidth();
        final int height = ybr[0].getHeight();
        final int nbPixels = width * height;

        final byte[] rPixels = new byte[nbPixels];
        final byte[] gPixels = new byte[nbPixels];
        final byte[] bPixels = new byte[nbPixels];

        final byte[] yPixels = (byte[]) ybr[0].getPixels();
        final byte[] cbPixels = (byte[]) ybr[1].getPixels();
        final byte[] crPixels = (byte[]) ybr[2].getPixels();

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressStep = Math.max(1, nbPixels / 10);
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
                progressListener.progressNotification(new ProgressEvent(ybr, i / (double) nbPixels, progressMessage));
            }
        }

        final ColorProcessor dest = new ColorProcessor(width, height);
        dest.setRGB(rPixels, gPixels, bPixels);

        if (progressListener != null) {
            progressListener.progressNotification(new ProgressEvent(ybr, 1.0, progressMessage));
        }

        return dest;
    }


    /**
     * Converts image pixels from RGB color space to YCbCr color space.
     * Equivalent to calling <code>ycbcrToRGB(ybr, null)</code>.
     *
     * @param ybr array of ByteProcessor representing color planes: Y, Cb, and Cr.
     * @return ColorProcessor representing image in sRGB color space.
     * @see #ycbcrToRGB(ij.process.ByteProcessor[], net.sf.ij_plugins.util.progress.ProgressListener)
     */
    public static ColorProcessor ycbcrToRGB(final ByteProcessor[] ybr) {
        return ycbcrToRGB(ybr, null);
    }
}
