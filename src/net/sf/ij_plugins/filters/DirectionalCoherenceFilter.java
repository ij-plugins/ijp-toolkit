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
package net.sf.ij_plugins.filters;

import ij.process.FloatProcessor;
import net.sf.ij_plugins.operators.Neighborhood3x3;
import net.sf.ij_plugins.operators.PixelIterator;


/**
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class DirectionalCoherenceFilter {
    private double spaceScale = 0.5;

    public double getSpaceScale() {
        return spaceScale;
    }

    public void setSpaceScale(double spaceScale) {
        this.spaceScale = spaceScale;
    }

    public FloatProcessor run(FloatProcessor src) {
        // Pre-smooth - noise reduction
        GaussianSmoothFilter gaussian = new GaussianSmoothFilter();
        gaussian.setStandardDeviation(spaceScale);
        src = (FloatProcessor) gaussian.run(src);

        int width = src.getWidth();
        int height = src.getHeight();

        FloatProcessor dest = new FloatProcessor(width, height);
        float[] destPixels = (float[]) dest.getPixels();

        PixelIterator iterator = new PixelIterator(src);

        while (iterator.hasNext()) {
            Neighborhood3x3 n = iterator.next();
            float gX = 0.5f * (n.neighbor1 - n.neighbor5);
            float gY = 0.5f * (n.neighbor7 - n.neighbor3);
            float gXX = gX * gX;
            float gXY = gX * gY;
            float gYY = gY * gY;
            float dd = gXX - gYY;
            double delta = Math.sqrt((4 * gXY * gXY) + (dd * dd));

            //            double lambda1 = 0.5*(gXX+gYY+delta);
            //            double lambda2 = 0.5*(gXX+gYY-delta);
            float coherence = (float) (delta * delta);
            destPixels[n.offset] = coherence;
        }

        return dest;
    }
}
