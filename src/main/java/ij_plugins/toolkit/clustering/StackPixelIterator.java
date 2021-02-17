/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package ij_plugins.toolkit.clustering;

import ij.ImageStack;

import java.util.NoSuchElementException;

/**
 * Iterates over all pixels in a stack. Assumes that no new slices are added to the stack
 *
 * @author Jarek Sacha
 */
final class StackPixelIterator implements java.util.Iterator<float[]> {

    private final int xSize;
    private final int ySize;
    private final int zSize;

    private int x;
    private int y;
    private int z;

    private final ImageStack stack;


    StackPixelIterator(final ImageStack stack) {
        xSize = stack.getWidth();
        ySize = stack.getHeight();
        zSize = stack.getSize();
        this.stack = stack;
    }

    @Override
    public boolean hasNext() {
        return x < (xSize - 1) || y < (ySize - 1) || z < (zSize - 1);
    }

    @Override
    public float[] next() {
        if (x < (xSize - 1)) {
            x++;
        } else {
            if (y < (ySize - 1)) {
                x = 0;
                y++;
            } else {
                if (z < (zSize - 1)) {
                    x = 0;
                    y = 0;
                    z++;
                } else {
                    throw new NoSuchElementException("There are no more pixels in the stack");
                }
            }
        }

        return new float[]{(float) stack.getVoxel(x, y, z)};
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Method remove() is not supported.");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
