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
 *  Repesents a 3D point.
 *
 *@author     Jarek Sacha
 *@created    April 29, 2002
 *@version    $Revision: 1.1 $
 */

public class Point3D {

  /**
   *  Description of the Field
   */
  public int x;
  /**
   *  Description of the Field
   */
  public int y;
  /**
   *  Description of the Field
   */
  public int z;


  /**
   *  Constructor for the Point3D object
   */
  public Point3D() { }


  /**
   *  Constructor for the Point3D object
   *
   *@param  x  Description of Parameter
   *@param  y  Description of Parameter
   *@param  z  Description of Parameter
   */
  public Point3D(int x, int y, int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }


  /**
   *  Description of the Method
   *
   *@return    Description of the Returned Value
   */
  public String toString() {
    return "(" + x + "," + y + "," + z + ")";
  }
}
