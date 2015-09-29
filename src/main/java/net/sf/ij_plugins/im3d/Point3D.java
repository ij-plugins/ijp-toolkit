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

/**
 * Represents a 3D point.
 *
 * @author Jarek Sacha
 * @since April 29, 2002
 */

public class Point3D {
    /**
     * x coordinate.
     */
    public float x;
    /**
     * y coordinate.
     */
    public float y;
    /**
     * z coordinate.
     */
    public float z;


    /**
     * Constructor for the Point3D object
     */
    public Point3D() {
    }


    /**
     * Constructor for the Point3D object
     *
     * @param x x coordinate.
     * @param y y coordinate.
     * @param z z coordinate.
     */
    public Point3D(final float x, final float y, final float z) {
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
}
