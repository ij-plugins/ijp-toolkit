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
package net.sf.ij_plugins.operators;

import ij.process.FloatProcessor;

import java.awt.*;
import java.util.Iterator;

/**
 * @author Jarek Sacha
 * @version $ Revision: $
 */

public class PixelIterator implements Iterator {
    private final int width;
    private final int height;
    private final Rectangle roi;
    private final float[] pixels;
    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int rowOffset;
    private int x;
    private int y;
    private final Neighborhood3x3 neighborhood3x3 = new Neighborhood3x3();

    public PixelIterator(FloatProcessor fp) {
        roi = fp.getRoi();
        width = fp.getWidth();
        height = fp.getHeight();
        pixels = (float[]) fp.getPixels();
        xMin = roi.x + 1;
        xMax = roi.x + roi.width - 2;
        yMin = roi.y + 1;
        yMax = roi.y + roi.height - 2;
        rowOffset = width;
        rewind();
    }

    public boolean hasNext() {
        return x < xMax || y < yMax;
    }

    public Object next() {
        // Update center location
        if (x < xMax) {
            ++x;
        } else {
            if (y < yMax) {
                x = xMin;
                ++y;
            }
//      IJ.showProgress(y, yMax);
        }
        // ENH: offset can be incremented rather than computed each time
        int offset = x + y * width;

        // Update neighborhod information
        neighborhood3x3.neighbor4 = pixels[offset - rowOffset - 1];
        neighborhood3x3.neighbor3 = pixels[offset - rowOffset];
        neighborhood3x3.neighbor2 = pixels[offset - rowOffset + 1];

        neighborhood3x3.neighbor5 = pixels[offset - 1];
        neighborhood3x3.center = pixels[offset];
        neighborhood3x3.neighbor1 = pixels[offset + 1];

        neighborhood3x3.neighbor6 = pixels[offset + rowOffset - 1];
        neighborhood3x3.neighbor7 = pixels[offset + rowOffset];
        neighborhood3x3.neighbor8 = pixels[offset + rowOffset + 1];

        neighborhood3x3.x = x;
        neighborhood3x3.y = y;
        neighborhood3x3.offset = offset;

        return neighborhood3x3;
    }

    public void remove() {
        throw new UnsupportedOperationException("Metod remove() not supported.");
    }

    /**
     * Reset iterator to its initial position
     */
    public void rewind() {
        x = xMin - 1;
        y = yMin;
    }


}