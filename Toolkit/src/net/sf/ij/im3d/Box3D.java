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
 *  Represents a 3D box oriented along the axis xyz.
 *
 *@author     Jarek Sacha
 *@created    April 29, 2002
 *@version    $Revision: 1.2 $
 */

public class Box3D {

  /**
   *  Location of the corner with smallest coordinates
   */
  public int x, y, z;

  /**
   *  Box dimensions
   */
  public int width, height, depth;


  /**
   *  Constructor for the Point3D object
   */
  public Box3D() { }


  /**
   *  Return a point representing the corner with smallest coordinates.
   *
   *@return    Point representing the corner with smallest coordinates.
   */
  public Point3D origin() {
    return new Point3D(x, y, z);
  }
}
