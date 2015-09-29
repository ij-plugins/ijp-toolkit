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
package net.sf.ij_plugins.filters;

import ij.process.FloatProcessor;

import java.awt.*;


/**
 * @author Jarek Sacha
 */
public class FluxAnisotropicDiffusionFilter {
    private int sizeX;
    private int sizeY;
    private FloatProcessor smoothedImage;
    private double sigma = 1;
    private double beta = 0.05;
    private final double k = 10;
    private int numberOfIterations = 1;
    private final double tangCoeff = 1.0;
    private final double epsilon = 1E-2;
    private final boolean smoothedParam = false;

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(final int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public double getSigma() {
        return sigma;
    }

    public void setSigma(final double sigma) {
        this.sigma = sigma;
    }

    public double getBeta() {
        return beta;
    }

    public void setBeta(final double beta) {
        this.beta = beta;
    }

    public FloatProcessor run(final FloatProcessor src) {
        sizeX = src.getWidth();
        sizeY = src.getHeight();

        final FloatProcessor tmpSrc = (FloatProcessor) src.duplicate();
        ;

        final FloatProcessor tmpDest = new FloatProcessor(sizeX, sizeY);

        final GaussianSmoothFilter gaussianSmooth = new GaussianSmoothFilter();

        for (int i = 1; i <= numberOfIterations; i++) {
            // 1. compute the smoothed image
            gaussianSmooth.setStandardDeviation(sigma);

            //                    gaussianSmooth.setRadiusFactors(3.01, 3.01, 0);
            smoothedImage = (FloatProcessor) gaussianSmooth.run(tmpSrc);

            // 2. run iteration
            iterate2D(tmpSrc, tmpDest);

            copy(tmpDest, tmpSrc);
        }

        return tmpSrc;
    }

    private static void copy(final FloatProcessor src, final FloatProcessor dest) {
        final float[] srcPixels = (float[]) src.getPixels();
        final float[] destPixels = (float[]) dest.getPixels();
        System.arraycopy(srcPixels, 0, destPixels, 0, srcPixels.length);
    }

    private double iterate2D(final FloatProcessor inData, final FloatProcessor outData) {
        final double[] _alpha_y = new double[sizeX];
        final double[] _gamma_y = new double[sizeX];

        double _alpha_x = 0;
        double _gamma_x = 0;

        double maxError = 0;
        final Point maxErrorPoint = new Point(-1, -1);

        int nbUnstabilePoints = 0;

        final float[] in = (float[]) inData.getPixels();
        final float[] Iconv = (float[]) smoothedImage.getPixels();

        for (int y = 0; y < (sizeY - 1); ++y) {
            final int offset = y * sizeX;

            for (int x = 0; x < (sizeX - 1); ++x) {
                final int pos = offset + x;

                final double val0 = in[pos];
                final Vector2D grad = new Vector2D();
                final Point2D e0 = new Point2D();
                final Point2D e1 = new Point2D();

                //----- Calcul de alpha1_x, gamma1_x
                // Gradient en (x+1/2,y)
                // et Calcul de e0, e1
                if ((x < (sizeX - 1)) && (y > 0) && (y < (sizeY - 1))) {
                    grad.y = ((in[pos + sizeX] - in[pos - sizeX] +
                            in[pos + sizeX + 1]) - in[pos - sizeX + 1]) / 4.0;
                    e0.y = ((Iconv[pos + sizeX] - Iconv[pos - sizeX] +
                            Iconv[pos + sizeX + 1]) - Iconv[pos - sizeX + 1]) / 4.0;
                } else {
                    grad.y = 0;
                    e0.y = 0;
                }

                if ((x > 0) && (x < (sizeX - 1))) {
                    grad.x = in[pos + 1] - in[pos];
                    e0.x = Iconv[pos + 1] - Iconv[pos];
                } else {
                    grad.x = 0;
                    e0.x = 0;
                }

                double norm = Math.sqrt((e0.x * e0.x) + (e0.y * e0.y));

                if (norm > 1E-5) {
                    e0.x /= norm;
                    e0.y /= norm;
                } else {
                    e0.x = 1.0;
                    e0.y = 0;
                }

                e1.x = -e0.y;
                e1.y = e0.x;

                // Derivees directionnelles
                double u_e0 = (grad.x * e0.x) + (grad.y * e0.y);
                double u_e1 = (grad.x * e1.x) + (grad.y * e1.y);

                double phi0_param;

                if (smoothedParam) {
                    phi0_param = norm;
                } else {
                    phi0_param = u_e0;
                }

                final double alpha1_x = (phi0(phi0_param) * e0.x * e0.x) +
                        (phi1(u_e1) * e1.x * e1.x);

                final double gamma1_x = grad.y * ((e0.y * phi0(phi0_param) * e0.x) +
                        (e1.y * phi1(u_e1) * e1.x));

                //----- Calcul de alpha1_y, gamma1_y
                // Gradient en (x,y+1/2)
                if ((y > 0) && (y < (sizeY - 1))) {
                    //        grad.y = (*(in  +sizeX+sizeX) - *(in) + (*in+sizeX) - (*in-sizeX))/4.0;
                    grad.y = in[pos + sizeX] - in[pos];
                    e0.y = Iconv[pos + sizeX] - Iconv[pos];
                } else {
                    grad.y = 0.0;
                    e0.y = 0.0;
                }

                //  gradient en X
                if ((y < (sizeY - 1)) && (x > 0) && (x < (sizeX - 1))) {
                    grad.x = ((in[pos + 1] - in[pos - 1] + in[pos + 1 + sizeX]) -
                            in[pos - 1 + sizeX]) / 4.0;
                    e0.x = ((Iconv[pos + 1] - Iconv[pos - 1] +
                            Iconv[pos + 1 + sizeX]) - Iconv[pos - 1 + sizeX]) / 4.0;
                } else {
                    grad.x = 0;
                    e0.x = 0.0;
                }

                // Calcul de e0, e1
                norm = Math.sqrt((e0.x * e0.x) + (e0.y * e0.y));

                if (norm > 1E-5) {
                    e0.x /= norm;
                    e0.y /= norm;
                } else {
                    e0.x = 1.0;
                    e0.y = 0.0;
                }

                e1.x = -e0.y;
                e1.y = e0.x;

                // Derivees directionnelles
                u_e0 = (grad.x * e0.x) + (grad.y * e0.y);
                u_e1 = (grad.x * e1.x) + (grad.y * e1.y);

                if (smoothedParam) {
                    phi0_param = norm;
                } else {
                    phi0_param = u_e0;
                }

                final double alpha1_y = (phi0(phi0_param) * e0.y * e0.y) +
                        (phi1(u_e1) * e1.y * e1.y);

                final double gamma1_y = (grad.x * e0.x * phi0(phi0_param) * e0.y) +
                        (grad.x * e1.x * phi1(u_e1) * e1.y);

                //----- Mise a jour de l'image
                double val1 = beta * inData.getPixelValue(x, y);
                double val1div = beta;

                if ((x > 0) && (x < (sizeX - 1))) {
                    val1 += (((alpha1_x * (in[pos + 1])) +
                            (_alpha_x * (in[pos - 1])) + gamma1_x) - _gamma_x);

                    val1div += (alpha1_x + _alpha_x);
                }

                if ((y > 0) && (y < (sizeY - 1))) {
                    val1 += (((alpha1_y * (in[pos + sizeX])) +
                            (_alpha_y[x] * (in[pos - sizeX])) + gamma1_y) -
                            _gamma_y[x]);

                    val1div += (alpha1_y + _alpha_y[x]);
                }

                if (Math.abs(val1div) < 1E-5) {
                    val1 = inData.getPixelValue(x, y);
                } else {
                    val1 /= val1div;
                }

                _alpha_y[x] = alpha1_y;
                _alpha_x = alpha1_x;

                _gamma_y[x] = gamma1_y;
                _gamma_x = gamma1_x;

                if (Math.abs(val1 - val0) > epsilon) {
                    nbUnstabilePoints++;
                }

                if (Math.abs(val1 - val0) > maxError) {
                    maxError = Math.abs(val1 - val0);
                    maxErrorPoint.x = x;
                    maxErrorPoint.y = y;
                }

                outData.putPixelValue(x, y, val1);
            }
        }

        System.out.println("Max maxError at (" + maxErrorPoint.x + "," +
                maxErrorPoint.y + ") = " + maxError);
        System.out.println("Unstabile points: " + nbUnstabilePoints);

        return maxError;
    }

    private double phi0(final double x) {
        return Math.exp(-0.5 * (((x) * (x)) / k / k));
    }

    private double phi1(final double x) {
        return tangCoeff;
    }

    private static class Point2D {
        double x;
        double y;
    }

    private static class Vector2D {
        double x;
        double y;
    }
}
