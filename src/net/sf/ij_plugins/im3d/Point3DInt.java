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

package net.sf.ij_plugins.im3d;

import net.sf.ij_plugins.util.Validate;

/**
 * Represents a 3D point.
 *
 * @author Jarek Sacha
 * @since April 29, 2002
 */
final public class Point3DInt implements Comparable<Point3DInt> {
    /**
     * x coordinate.
     */
    public final int x;
    /**
     * y coordinate.
     */
    public final int y;
    /**
     * z coordinate.
     */
    public final int z;


    /**
     * Constructor for the Point3D object
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param z z coordinate.
     */
    public Point3DInt(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }


    /**
     * Returns a string representing Point3D coordinates: (x,y,z).
     *
     * @return String representing Point3D coordinates.
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Point3DInt && compareTo((Point3DInt) obj) == 0;
    }


    @Override
    public int compareTo(final Point3DInt p) {
        Validate.argumentNotNull(p, "p");

        if (x < p.x) {
            return -1;
        } else if (x > p.x) {
            return 1;
        } else if (y < p.y) {
            return -1;
        } else if (y > p.y) {
            return 1;
        } else if (z < p.z) {
            return -1;
        } else if (z > p.z) {
            return 1;
        } else {
            return 0;
        }
    }
}
