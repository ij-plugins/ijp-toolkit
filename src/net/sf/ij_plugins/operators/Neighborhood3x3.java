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

/**
 * @author Jarek Sacha
 * @version $ Revision: $
 */

public class Neighborhood3x3 {
    private final float[] neighbors = new float[9];
    public float center;
    public float neighbor1
    ,
    neighbor2
    ,
    neighbor3
    ,
    neighbor4
    ,
    neighbor5
    ,
    neighbor6
    ,
    neighbor7
    ,
    neighbor8;
    public int x
    ,
    y
    ,
    offset;

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
