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
package net.sf.ij_plugins.operators;

/**
 * @author Jarek Sacha
 */

/**
 * Pixel numbers:
 * <pre>
 *  4 3 2
 *  5 0 1
 *  6 7 8
 * </pre>
 */
public class Neighborhood3x3 {
    private final float[] neighbors = new float[9];
    /**
     * Value of pixel located at the center of neighborhood, that is at  (<code>x</code>,<code>y</code>).
     */
    public float center;
    /**
     * Value of pixel located at (<code>x+1</code>,<code>y</code>).
     */
    public float neighbor1;
    /**
     * Value of pixel located at (<code>x+1</code>,<code>y-1</code>).
     */
    public float neighbor2;
    /**
     * Value of pixel located at (<code>x</code>,<code>y-1</code>-1).
     */
    public float neighbor3;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>-1).
     */
    public float neighbor4;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>).
     */
    public float neighbor5;
    /**
     * Value of pixel located at (<code>x</code>-1,<code>y</code>+1).
     */
    public float neighbor6;
    /**
     * Value of pixel located at (<code>x</code>,<code>y</code>+1).
     */
    public float neighbor7;
    /**
     * Value of pixel located at (<code>x</code>+1,<code>y</code>+1).
     */
    public float neighbor8;
    public int x;
    public int y;
    public int offset;

    public float[] getNeighbors() {
        neighbors[0] = center;
        neighbors[1] = neighbor1;
        neighbors[2] = neighbor2;
        neighbors[3] = neighbor3;
        neighbors[4] = neighbor4;
        neighbors[5] = neighbor5;
        neighbors[6] = neighbor6;
        neighbors[7] = neighbor7;
        neighbors[8] = neighbor8;

        return neighbors;
    }
}
