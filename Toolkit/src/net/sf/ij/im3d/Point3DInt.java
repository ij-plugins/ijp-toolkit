/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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
package net.sf.ij.im3d;

/**
 *  Represents a 3D point.
 *
 * @author     Jarek Sacha
 * @created    April 29, 2002
 * @version    $Revision: 1.1 $
 */

public class Point3DInt {

  /**  x coordinate. */
  public int x;
  /**  y coordinate. */
  public int y;
  /**  z coordinate. */
  public int z;


  /**  Constructor for the Point3D object */
  public Point3DInt() { }


  /**
   *  Constructor for the Point3D object
   *
   * @param  x  x coordinate.
   * @param  y  y coordinate.
   * @param  z  z coordinate.
   */
  public Point3DInt(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }


  /**
   *  Returns a string representing Point3D coordinates: (x,y,z).
   *
   * @return    String representing Point3D coordinates.
   */
  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }
}
