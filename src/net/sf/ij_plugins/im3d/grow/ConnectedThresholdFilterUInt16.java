/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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
package net.sf.ij_plugins.im3d.grow;

import ij.ImageStack;
import net.sf.ij_plugins.im3d.Point3DInt;

/**
 * Simple region growing algorithm that extracts all pixels connected to the seed as long as they
 * intensities are within given threshold limits. Min limit is inclusive, max limit is exclusive.
 *
 * @author Jarek Sacha
 * @since April 29, 2002
 */

public class ConnectedThresholdFilterUInt16 extends ConnectedThresholdFilterBase {
    /**
     * Source pixels
     */
    protected short[][] srcPixels = null;


    /*
    *
    */
    @Override
    protected final void createHandleToSrcPixels(final ImageStack src) {
        final Object[] imageArray = src.getImageArray();

        final int n = src.getSize();
        srcPixels = new short[n][];
        for (int z = 0; z < n; ++z) {
            srcPixels[z] = (short[]) imageArray[z];
            if (!(imageArray[z] instanceof short[])) {
                throw new IllegalArgumentException("Expecting stack of byte images.");
            }
        }
    }


    /*
    *
    */
    @Override
    protected final void checkForGrow(final int x, final int y, final int z) {
        if (x < xMin || x >= xMax ||
                y < yMin || y >= yMax ||
                z < zMin || z >= zMax) {
            return;
        }

        final int offset = y * xSize + x;
        if (destPixels[z][offset] == BACKGROUND) {
            final int value = srcPixels[z][offset] & 0xffff;
            if (value >= valueMin && value < valueMax) {
                destPixels[z][offset] = MARKER;
                candidatePoints.addLast(new Point3DInt(x, y, z));
            } else {
                destPixels[z][offset] = NOT_MEMBER;
            }
        }
    }
}
