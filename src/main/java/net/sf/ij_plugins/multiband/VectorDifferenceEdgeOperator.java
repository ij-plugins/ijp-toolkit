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
 * Finds edges in a vector valued image by computing maximum Euclidean distance within 3x3
 * neighbourhood. The distance is computed between opposite pixels in the neighbourhood, that is
 * four different distances are compared.
 *
 * @author Jarek Sacha
 */
public class VectorDifferenceEdgeOperator {


    public static FloatProcessor run(final ImagePlus imp) {
        final VectorProcessor vp = new VectorProcessor(imp);
        return run(vp);
    }


    public static FloatProcessor run(final VectorProcessor vp) {

        final int width = vp.getWidth();
        final int height = vp.getHeight();

        final FloatProcessor dest = new FloatProcessor(width, height);

        final Rectangle roi = new Rectangle(1, 1, width - 2, height - 2);
        vp.setRoi(roi);

        final VectorProcessor.Iterator iterator = vp.iterator();
        while (iterator.hasNext()) {
            final VectorProcessor.Neighborhood3x3 vn = iterator.next();

            double d = VectorMath.distance(vn.p1, vn.p6);
            d = Math.max(d, VectorMath.distance(vn.p2, vn.p7));
            d = Math.max(d, VectorMath.distance(vn.p3, vn.p8));
            d = Math.max(d, VectorMath.distance(vn.p4, vn.p9));

            dest.putPixelValue(vn.x, vn.y, d);
        }

        return dest;
    }

}
