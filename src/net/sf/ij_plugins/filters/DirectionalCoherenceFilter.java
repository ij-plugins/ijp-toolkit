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
public class DirectionalCoherenceFilter {
    private double spaceScale = 0.5;

    public double getSpaceScale() {
        return spaceScale;
    }

    public void setSpaceScale(final double spaceScale) {
        this.spaceScale = spaceScale;
    }

    public FloatProcessor run(FloatProcessor src) {
        // Pre-smooth - noise reduction
        final GaussianSmoothFilter gaussian = new GaussianSmoothFilter();
        gaussian.setStandardDeviation(spaceScale);
        src = (FloatProcessor) gaussian.run(src);

        final int width = src.getWidth();
        final int height = src.getHeight();

        final FloatProcessor dest = new FloatProcessor(width, height);
        final float[] destPixels = (float[]) dest.getPixels();

        final PixelIterator iterator = new PixelIterator(src);

        while (iterator.hasNext()) {
            final Neighborhood3x3 n = iterator.next();
            final float gX = 0.5f * (n.neighbor1 - n.neighbor5);
            final float gY = 0.5f * (n.neighbor7 - n.neighbor3);
            final float gXX = gX * gX;
            final float gXY = gX * gY;
            final float gYY = gY * gY;
            final float dd = gXX - gYY;
            final double delta = Math.sqrt((4 * gXY * gXY) + (dd * dd));

            //            double lambda1 = 0.5*(gXX+gYY+delta);
            //            double lambda2 = 0.5*(gXX+gYY-delta);
            final float coherence = (float) (delta * delta);
            destPixels[n.offset] = coherence;
        }

        return dest;
    }
}
