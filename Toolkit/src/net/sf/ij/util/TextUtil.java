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
package net.sf.ij.util;

/**
 *  Title: Description: Copyright: GPL 2002 Company:
 *
 *@author     Jarek Sacha
 *@created    July 16, 2002
 *@version    $Revision: 1.1 $
 */

public class TextUtil {

  /**
   *  Constructor for the TextUtil object
   */
  private TextUtil() { }


  /**
   *  Description of the Method
   *
   *@param  str           Description of the Parameter
   *@param  defaultValue  Description of the Parameter
   *@return               Description of the Return Value
   */
  public static float parseFloat(String str, float defaultValue) {
    float f = defaultValue;
    if (str != null) {
      try {
        f = Float.parseFloat(str);
      } catch (NumberFormatException ex) {
      }
    }

    return f;
  }


  /**
   *  Description of the Method
   *
   *@param  str           Description of the Parameter
   *@param  defaultValue  Description of the Parameter
   *@return               Description of the Return Value
   */
  public static int parseInt(String str, int defaultValue) {
    int i = defaultValue;
    if (str != null) {
      try {
        i = Integer.parseInt(str);
      } catch (NumberFormatException ex) {
      }
    }

    return i;
  }

}
