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
package net.sf.ij.io.metaimage;

import net.sf.ij.util.Enumeration;

/**
 *  Represents types of element values in MetaImage files.
 *
 *@author     Jarek Sacha
 *@created    June 18, 2002
 *@version    $Revision: 1.2 $ $Date: 2002-08-06 00:53:26 $
 */
public class MiElementType extends Enumeration {

  /**  MET_UCHAR */
  public static MiElementType MET_UCHAR = new MiElementType("MET_UCHAR");
  /**  MET_CHAR */
  public static MiElementType MET_CHAR = new MiElementType("MET_CHAR");
  /**  MET_USHORT */
  public static MiElementType MET_USHORT = new MiElementType("MET_USHORT");
  /**  MET_SHORT */
  public static MiElementType MET_SHORT = new MiElementType("MET_SHORT");
  /**  MET_UINT */
  public static MiElementType MET_UINT = new MiElementType("MET_UINT");
  /**  MET_INT */
  public static MiElementType MET_INT = new MiElementType("MET_INT");
  /**  MET_ULONG */
  public static MiElementType MET_ULONG = new MiElementType("MET_ULONG");
  /**  MET_LONG */
  public static MiElementType MET_LONG = new MiElementType("MET_LONG");
  /**  MET_FLOAT */
  public static MiElementType MET_FLOAT = new MiElementType("MET_FLOAT");
  /**  MET_DOUBLE */
  public static MiElementType MET_DOUBLE = new MiElementType("MET_DOUBLE");


  /**
   *  Constructor for the MiElementType object
   *
   *@param  name  Description of the Parameter
   */
  private MiElementType(String name) {
    super(name);
  }
}
