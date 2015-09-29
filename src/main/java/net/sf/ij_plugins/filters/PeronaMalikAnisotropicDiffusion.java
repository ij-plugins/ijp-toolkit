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


/**
 * @author Jarek Sacha
 */
public class PeronaMalikAnisotropicDiffusion extends AbstractAnisotropicDiffusion {
    private boolean bigRegionFunction = true;
    private float k = 10;
    private float inv_k = 1 / k;

    public float getK() {
        return k;
    }

    public void setK(final float k) {
        this.k = k;
        inv_k = 1 / k;
    }

    public boolean isBigRegionFunction() {
        return bigRegionFunction;
    }

    public void setBigRegionFunction(final boolean bigRegionFunction) {
        this.bigRegionFunction = bigRegionFunction;
    }

    /**
     * Perform single diffusion operation
     */
    @Override
    protected void diffuse(final FloatProcessor src, final FloatProcessor dest) {
        final float[] destPixels = (float[]) dest.getPixels();
        final PixelIterator iterator = new PixelIterator(src);

        /* TODO: Update: "Calculate the gradient values for each point" */
        while (iterator.hasNext()) {
            final Neighborhood3x3 n = iterator.next();
            final float[] neighbors = n.getNeighbors();

            // 4-connected neighbors
            double sum4component = 0;

            for (int i = 1; i < neighbors.length; i += 2) {
                final float gradient = neighbors[i] - n.center;
                sum4component += (g(gradient) * gradient);
            }

            //      // 8-connected neighbors
            //      double sum8component = 0;
            //      for (int i = 2; i < neighbors.length; i+=2) {
            //        float gradient = neighbors[i] - n.center;
            //        sum8component += gradient;
            //      }
            //
            //      double sqrt2div2 = .707106781;
            //      double newValue = (n.center + timeStep * (sum4component + sqrt2div2 * sum8component));
            final double newValue = n.center + (getTimeStep() * (sum4component));

            destPixels[n.offset] = (float) newValue;
        }
    }

    /**
     * Function preserving (and enhancing) edges
     */
    public final double g(final double v) {
        if (bigRegionFunction) {
            //            return 1 / (1 + Math.pow(v / k, 2));
            final double h = v * inv_k;

            return 1 / (1 + (h * h));
        } else {
            //            return Math.exp(-Math.pow(v / k, 2));
            final double h = v * inv_k;

            return Math.exp(-h * h);
        }
    }
}
