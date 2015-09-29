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
import net.sf.ij_plugins.operators.Neighborhood3x3;
import net.sf.ij_plugins.operators.PixelIterator;
import net.sf.ij_plugins.util.IJDebug;


/**
 * Speckle Reducing Anisotropic Diffusion (SRAD) filter based on paper:<br> Y. Yu and T. Acton,
 * "Speckle Reducing Anisotropic Diffusion", <i>IEEE Transactions on Image Processing</i>, Vol.11,
 * No.11, 2002.
 *
 * @author Jarek Sacha
 */
public class SRAD extends AbstractAnisotropicDiffusion {

    /**
     * Diffusion coefficient lower threshold
     */
    private double cThreshold = 0.1;

    /**
     * Speckle coefficient of variation in the observer image, for correlated data it should be set
     * to less than 1.
     */
    private double q0 = 1;

    /**
     *
     */
    private double ro = 1d / 6d;

    public double getCThreshold() {
        return cThreshold;
    }

    public void setCThreshold(final double cThreshold) {
        this.cThreshold = cThreshold;
    }

    public double getQ0() {
        return q0;
    }

    public void setQ0(final double q0) {
        this.q0 = q0;
    }

    public double getRo() {
        return ro;
    }

    public void setRo(final double ro) {
        this.ro = ro;
    }


    /**
     * Perform single diffusion operation
     */
    @Override
    protected void diffuse(final FloatProcessor src, final FloatProcessor dest) {
        final float[] destPixels = (float[]) dest.getPixels();
        final FloatProcessor cFP = new FloatProcessor(src.getWidth(), src.getHeight());
        final float[] cPixels = (float[]) cFP.getPixels();
        final PixelIterator iterator = new PixelIterator(src);

        // Pre-calculate diffusion coefficient c(q)
        final double q0t = q0 * Math.exp(-ro * time());
        final double q0t2 = q0t * q0t;
        IJDebug.log("time=" + time() + ", q0=" + q0 + ", q0t=" + q0t);
        while (iterator.hasNext()) {
            final Neighborhood3x3 n = iterator.next();

            // Pixel numbers:
            //  4 3 2
            //  5 0 1
            //  6 7 8

            // Equation (52), h=1
            final double dRI1 = (n.neighbor1 - n.center);
            final double dRI2 = (n.neighbor7 - n.center);
            // Equation (53), h=1
            final double dLI1 = (n.center - n.neighbor5);
            final double dLI2 = (n.center - n.neighbor3);
            // Equation (54), h=1
            final double d2I = (n.neighbor1 + n.neighbor5 + n.neighbor7 + n.neighbor3 - 4 * n.center);

            // Equation (57)
            final double v1 = Math.sqrt(dRI1 * dRI1 + dRI2 + dRI2 + dLI1 * dLI1 + dLI2 * dLI2) / n.center;
            final double v2 = d2I / n.center;
            final double v3 = 1 + v2 / 4;
            final double q = Math.sqrt((v1 * v1 / 2 - v2 * v2 / 16) / (v3 * v3));
            // Equation (33)
            final double cij = 1 / (1 + (q * q - q0t2) / (q0t2 * (1 + q0t2)));

            cPixels[n.offset] = cij > cThreshold ? (float) cij : 0f;
        }

        iterator.rewind();
        final PixelIterator cIterator = new PixelIterator(cFP);
        while (iterator.hasNext()) {
            // FIXME: make sure that the two iterators are correctly synchronized
            final Neighborhood3x3 n = iterator.next();
            final Neighborhood3x3 cn = cIterator.next();

            // Pixel numbers:
            //  4 3 2
            //  5 0 1
            //  6 7 8

            final double cij = cn.center;
            final double ci1j = cn.neighbor1;
            final double cij1 = cn.neighbor7;

            // Equation (58), h=1
            final double d = ci1j * (n.neighbor1 - n.center) + cij * (n.neighbor5 - n.center) +
                    cij1 * (n.neighbor7 - n.center) + cij * (n.neighbor3 - n.center);

            // SRAD update, equation (61)
            final double newValue = n.center + (getTimeStep() / 4d * d);
            destPixels[n.offset] = (float) newValue;
        }
    }

}
