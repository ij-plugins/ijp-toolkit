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
package net.sf.ij.io.vtk;

import net.sf.ij.util.Enumeration;

/**
 *  Represent possible values of scalar names for the SCALARS tag.
 *
 *@author     Jarek Sacha
 *@created    June 21, 2002
 *@version    $Revision: 1.1 $
 */

class VtkScalarType extends Enumeration {

  /**  bit */
  public final static VtkScalarType BIT = new VtkScalarType("bit");
  /**  unsigned_char */
  public final static VtkScalarType UNSIGNED_CHAR = new VtkScalarType("unsigned_char");
  /**  char */
  public final static VtkScalarType CHAR = new VtkScalarType("char");
  /**  unsigned_short */
  public final static VtkScalarType UNSIGNED_SHORT = new VtkScalarType("unsigned_short");
  /**  short */
  public final static VtkScalarType SHORT = new VtkScalarType("short");
  /**  unsigned_int */
  public final static VtkScalarType UNSIGNED_INT = new VtkScalarType("unsigned_int");
  /**  int */
  public final static VtkScalarType INT = new VtkScalarType("int");
  /**  unsigned_long */
  public final static VtkScalarType UNSIGNED_LONG = new VtkScalarType("unsigned_long");
  /**  long */
  public final static VtkScalarType LONG = new VtkScalarType("long");
  /**  float */
  public final static VtkScalarType FLOAT = new VtkScalarType("float");
  /**  double */
  public final static VtkScalarType DOUBLE = new VtkScalarType("double");


  /**
   *  Constructor for the VtkDataType object
   *
   *@param  name  Description of the Parameter
   */
  private VtkScalarType(String name) {
    super(name);
  }
}
