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
 *  Representation of boolean values in MetaImage header files.
 *
 *@author     Jarek Sacha
 *@created    June 18, 2002
 *@version    $Revision: 1.1 $ $Date: 2002-07-19 02:45:18 $
 */
public class MiBoolean extends Enumeration {

  /**
   *  Represents boolean symbol 'True' used by MetaImage file format.
   */
  public static MiBoolean True = new MiBoolean("True");
  /**
   *  Represents boolean symbol 'False' used by MetaImage file format.
   */
  public static MiBoolean False = new MiBoolean("False");


  /**
   *  Constructor for the MiBoolean object
   *
   *@param  name  Description of the Parameter
   */
  private MiBoolean(String name) {
    super(name);
  }
}
