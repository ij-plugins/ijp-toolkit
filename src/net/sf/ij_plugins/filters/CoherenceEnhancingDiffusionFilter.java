/***
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;
import net.sf.ij_plugins.operators.Neighborhood3x3;
import net.sf.ij_plugins.operators.PixelIterator;


/**
 * Based on filter described in J. Weickert,  "Coherence-Enhancing Diffusion Filtering",
 * International Journal of Computer Vision, 1999, vol.31, p.111-127.
 *
 * @author Jarek Sacha
 */
public class CoherenceEnhancingDiffusionFilter {

    private double noiseScale = 0.5;
    private double integrationScale = 1;
    private double c1 = 0.001;
    private double c2 = 1;
    private final double k = 0.01;
    private final double beta = 4;
    private int numberOfIterations = 1;
    private double timeStep = 1;
    private final float[] gradientXKernel = {
            -3f / 32f, 0f, 3f / 32f, -10f / 32f, 0f, 10f / 32f, -3f / 32f, 0f,
            3f / 32f
    };
    private final float[] gradientYKernel = {
            3f / 32f, 10f / 32f, 3f / 32f, 0f, 0f, 0f, -3f / 32f, -10f / 32f,
            -3f / 32f
    };

//    private float[] gradientXKernel = {
//        -1f / 8f, 0f, 1f / 8f,
//        -2f / 8f, 0f, 2f / 8f,
//        -1f / 8f, 0f, 1f / 8f
//    };
//    private float[] gradientYKernel = {
//        1f / 8f, 2f / 8f, 1f / 8f,
//        0f, 0f, 0f,
//        -1f / 8f, -2f / 8f, -1f / 8f
//    };

    public double getNoiseScale() {
        return noiseScale;
    }

    public void setNoiseScale(final double noiseScale) {
        this.noiseScale = noiseScale;
    }

    public double getIntegrationScale() {
        return integrationScale;
    }

    public void setIntegrationScale(final double integrationScale) {
        this.integrationScale = integrationScale;
    }

    public double getC1() {
        return c1;
    }

    public void setC1(final double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(final double c2) {
        this.c2 = c2;
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public void setNumberOfIterations(final int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    public double getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(final double timeStep) {
        this.timeStep = timeStep;
    }

    public FloatProcessor run(final FloatProcessor src) {
        final int width = src.getWidth();
        final int height = src.getHeight();

        //        FloatProcessor in = new FloatProcessor(width, height);
        final FloatProcessor gX = new FloatProcessor(width, height);
        final FloatProcessor gY = new FloatProcessor(width, height);

        // Second derivatives
        final FloatProcessor gXX = new FloatProcessor(width, height);
        final FloatProcessor gXY = new FloatProcessor(width, height);
        final FloatProcessor gYX = new FloatProcessor(width, height);
        final FloatProcessor gYY = new FloatProcessor(width, height);

        final FloatProcessor j1 = new FloatProcessor(width, height);
        final FloatProcessor j2 = new FloatProcessor(width, height);

        final GaussianSmoothFilter gaussian = new GaussianSmoothFilter();
        gaussian.setStandardDeviation(noiseScale);

        final FloatProcessor dest = (FloatProcessor) gaussian.run(src);
//        FloatProcessor dest = (FloatProcessor) src.duplicate();

        final Convolver convolver = new Convolver();

        final ImageStack timeStack = new ImageStack(width, height);

        for (int i = 0; i < numberOfIterations; ++i) {
            IJ.showStatus("Iteration " + (i + 1) + " of " + numberOfIterations);
            IJ.showProgress(i, numberOfIterations);

            // Pre-smooth - scale integration
            gaussian.setStandardDeviation(integrationScale);
            final FloatProcessor in = (FloatProcessor) gaussian.run(dest);

            // First derivatives
            gX.setPixels(in.getPixelsCopy());
            convolver.convolveFloat(gX, gradientXKernel, 3, 3);
            gY.setPixels(in.getPixelsCopy());
            convolver.convolveFloat(gY, gradientYKernel, 3, 3);

            // second derivatives
            gXX.setPixels(gX.getPixelsCopy());
            convolver.convolveFloat(gXX, gradientXKernel, 3, 3);
            gXY.setPixels(gX.getPixelsCopy());
            convolver.convolveFloat(gXY, gradientYKernel, 3, 3);
            gYX.setPixels(gY.getPixelsCopy());
            convolver.convolveFloat(gYX, gradientXKernel, 3, 3);
            gYY.setPixels(gY.getPixelsCopy());
            convolver.convolveFloat(gYY, gradientYKernel, 3, 3);

            //
            // Diffusion tensor
            //
            final float[] destPixels = (float[]) dest.getPixels();

            final PixelIterator iterator = new PixelIterator(in);

            while (iterator.hasNext()) {
                final Neighborhood3x3 n = iterator.next();
                final double j11 = gXX.getPixelValue(n.x, n.y);
                final double j12 = 0.5f * (gXY.getPixelValue(n.x, n.y) +
                        gYX.getPixelValue(n.x, n.y));
                final double j22 = gYY.getPixelValue(n.x, n.y);
                final double dd = j11 - j22;
                final double delta = Math.sqrt(4 * j12 * j12 + dd * dd);
                final double mu1 = 0.5 * (j11 + j22 + delta);
                final double mu2 = 0.5 * (j11 + j22 - delta);

                double cosA = 2 * j12;
                double sinA = j22 - j11 + delta;
                final double l = Math.sqrt(cosA * cosA + sinA * sinA);

                if (l == 0) {
                    continue;
                }

                cosA /= l;
                sinA /= l;

                final double lambda1 = c1;
                final double lambda2 = g1(mu1, mu2);

                //                     (a b)
                // Diffusion tensor D = (b c)
                //
                final double a = (lambda1 * cosA * cosA) + (lambda2 * sinA * sinA);
                final double b = (lambda1 - lambda2) * sinA * cosA;
                final double c = (lambda1 * sinA * sinA) + (lambda2 * cosA * cosA);

                // Flux components
                final double dxu = gX.getPixelValue(n.x, n.y);
                final double dyu = gY.getPixelValue(n.x, n.y);
                j1.putPixelValue(n.x, n.y, (a * dxu) + (b * dyu));
                j2.putPixelValue(n.x, n.y, (b * dxu) + (c * dyu));
            }

            convolver.convolveFloat(j1, gradientXKernel, 3, 3);
            convolver.convolveFloat(j2, gradientYKernel, 3, 3);

            iterator.rewind();

            double maxUpdate = 0;
            final FloatProcessor debugIm = new FloatProcessor(width, height);

            while (iterator.hasNext()) {
                final Neighborhood3x3 n = iterator.next();
                final double du = j1.getPixelValue(n.x, n.y) +
                        j2.getPixelValue(n.x, n.y);
                destPixels[n.offset] += (timeStep * du);

                final double delta = Math.abs(du);
                maxUpdate = Math.max(delta, maxUpdate);
                debugIm.putPixelValue(n.x, n.y, du);
            }

            timeStack.addSlice("" + i, debugIm);

            System.out.println("Iteration: " + i + ", max update: " + maxUpdate);
        }

        IJ.showProgress(numberOfIterations, numberOfIterations);

        new ImagePlus("du", timeStack).show();

        return dest;
    }

    double g1(final double mu1, final double mu2) {
        if (mu1 == mu2) {
            return c1;
        } else {
            final double dMu = mu1 - mu2;
            return c1 + ((1 - c1) * Math.exp(-c2 / (dMu * dMu)));
        }

    }

    double g2(final double mu1, final double mu2) {
        final double dMu = mu1 - mu2;
        return Math.max(c1, 1 - Math.exp(-dMu * dMu / k / k));
    }

    double g3(final double mu1, final double mu2) {
        return c1 + (1 - c1) * Math.pow((mu1 - mu2) / (mu1 + mu2), beta);
    }
}
