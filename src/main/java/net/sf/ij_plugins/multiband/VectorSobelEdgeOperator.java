/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
package net.sf.ij_plugins.multiband;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.*;

/**
 * Sobel edge detector for vector valued images.
 *
 * @author Jarek Sacha
 */
public class VectorSobelEdgeOperator {


    public static FloatProcessor run(final ImagePlus imp) {
        final VectorProcessor vp = new VectorProcessor(imp);
        return run(vp);
    }


    public static FloatProcessor run(final VectorProcessor vp) {

        final int width = vp.getWidth();
        final int height = vp.getHeight();
        final int nbBands = vp.getNumberOfValues();

        final FloatProcessor dest = new FloatProcessor(width, height);

        final Rectangle roi = new Rectangle(1, 1, width - 2, height - 2);
        vp.setRoi(roi);

        final VectorProcessor.Iterator iterator = vp.iterator();
        while (iterator.hasNext()) {
            final VectorProcessor.Neighborhood3x3 vn = iterator.next();
            // 3x3 Sobel filter
            //sum1 = p1 + 2 * p2 + p3 - p7 - 2 * p8 - p9;
            final float[] sum11 = new float[nbBands];
            VectorMath.add(sum11, vn.p1);
            VectorMath.add(sum11, vn.p2);
            VectorMath.add(sum11, vn.p2);
            VectorMath.add(sum11, vn.p3);
            final float[] sum12 = new float[nbBands];
            VectorMath.add(sum12, vn.p7);
            VectorMath.add(sum12, vn.p8);
            VectorMath.add(sum12, vn.p8);
            VectorMath.add(sum12, vn.p9);

            // sum2 = p1 + 2 * p4 + p7 - p3 - 2 * p6 - p9;
            final float[] sum21 = new float[nbBands];
            VectorMath.add(sum21, vn.p1);
            VectorMath.add(sum21, vn.p4);
            VectorMath.add(sum21, vn.p4);
            VectorMath.add(sum21, vn.p7);
            final float[] sum22 = new float[nbBands];
            VectorMath.add(sum22, vn.p3);
            VectorMath.add(sum22, vn.p6);
            VectorMath.add(sum22, vn.p6);
            VectorMath.add(sum22, vn.p9);

            final double d1 = VectorMath.distance(sum11, sum12);
            final double d2 = VectorMath.distance(sum21, sum22);
            final double d = Math.max(d1, d2);

            dest.putPixelValue(vn.x, vn.y, d);
        }

        return dest;
    }
}
