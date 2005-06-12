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
package net.sf.ij_plugins.multiband;

import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.awt.*;

/**
 * Finds edges in a vector valued image by computing maximum Euclidian distance between the center
 * pixel and each of the other pixels in a 3x3 neighbourhood. That is, eight different distances are
 * compared.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 */
public class VectorGradientEdgeOperator {
    public static FloatProcessor run(ImagePlus imp) {
        VectorProcessor vp = new VectorProcessor(imp);
        imp = null;
        return run(vp);
    }


    public static FloatProcessor run(VectorProcessor vp) {

        final int width = vp.getWidth();
        final int height = vp.getHeight();

        FloatProcessor dest = new FloatProcessor(width, height);

        Rectangle roi = new Rectangle(1, 1, width - 2, height - 2);
        vp.setRoi(roi);

        VectorProcessor.Iterator iterator = vp.iterator();
        while (iterator.hasNext()) {
            VectorProcessor.Neighborhood3x3 vn = (VectorProcessor.Neighborhood3x3) iterator.next();

            double d = VectorMath.distance(vn.p1, vn.p5);
            d = Math.max(d, VectorMath.distance(vn.p2, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p3, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p4, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p6, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p7, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p8, vn.p5));
            d = Math.max(d, VectorMath.distance(vn.p9, vn.p5));

            dest.putPixelValue(vn.x, vn.y, d);
        }

        return dest;
    }

}
