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

import java.io.IOException;

import java.io.StreamTokenizer;
import java.io.StringReader;

import java.util.ArrayList;

/**
 *  Title: Description: Copyright: GPL 2002 Company:
 *
 * @author     Jarek Sacha
 * @created    July 16, 2002
 * @version    $Revision: 1.2 $
 */

public class TextUtil {

  /**  Constructor for the TextUtil object */
  private TextUtil() { }


  /**
   *  Description of the Method
   *
   * @param  str           Description of the Parameter
   * @param  defaultValue  Description of the Parameter
   * @return               Description of the Return Value
   */
  public static float parseFloat(String str, float defaultValue) {
    float f = defaultValue;
    if (str != null) {
      try {
        f = Float.parseFloat(str);
      }
      catch (NumberFormatException ex) {
      }
    }

    return f;
  }


  /**
   *  Description of the Method
   *
   * @param  str           Description of the Parameter
   * @param  defaultValue  Description of the Parameter
   * @return               Description of the Return Value
   */
  public static int parseInt(String str, int defaultValue) {
    int i = defaultValue;
    if (str != null) {
      try {
        i = Integer.parseInt(str);
      }
      catch (NumberFormatException ex) {
      }
    }

    return i;
  }


  /**
   *  Parse string as an array of integers separated by white space.
   *
   * @param  str  Description of the Parameter
   * @return      Description of the Return Value
   */
  public static int[] parseIntArray(String str) {
    StringReader reader = new StringReader(str);
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    tokenizer.parseNumbers();

    ArrayList tokens = new ArrayList();
    try {
      int id = tokenizer.nextToken();
      while (id != StreamTokenizer.TT_EOF) {
        if (id != StreamTokenizer.TT_NUMBER) {
          throw new IllegalArgumentException("Cannot parse string as an array of integers...");
        }
        tokens.add(new Integer((int) tokenizer.nval));
        id = tokenizer.nextToken();
      }

      if (tokens.size() < 1) {
        throw new IllegalArgumentException("Input string does not conain any numbers.");
      }
    }
    catch (IOException ex) {
      throw new IllegalArgumentException("Unexpected error extacting tokens: " + ex);
    }

    int[] a = new int[tokens.size()];
    for (int i = 0; i < a.length; ++i) {
      a[i] = ((Integer) tokens.get(i)).intValue();
    }

    return a;
  }

  /**
   *  Parse string as an array of floats separated by white space.
   *
   * @param  str  Description of the Parameter
   * @return      Description of the Return Value
   */
  public static float[] parseFloatArray(String str) {
    StringReader reader = new StringReader(str);
    StreamTokenizer tokenizer = new StreamTokenizer(reader);
    tokenizer.parseNumbers();

    ArrayList tokens = new ArrayList();
    try {
      int id = tokenizer.nextToken();
      while (id != StreamTokenizer.TT_EOF) {
        if (id != StreamTokenizer.TT_NUMBER) {
          throw new IllegalArgumentException("Cannot parse string as an array of integers...");
        }
        tokens.add(new Float(tokenizer.nval));
        id = tokenizer.nextToken();
      }

      if (tokens.size() < 1) {
        throw new IllegalArgumentException("Input string does not conain any numbers.");
      }
    }
    catch (IOException ex) {
      throw new IllegalArgumentException("Unexpected error extacting tokens: " + ex);
    }

    float[] f = new float[tokens.size()];
    for (int i = 0; i < f.length; ++i) {
      f[i] = ((Float) tokens.get(i)).floatValue();
    }

    return f;
  }
}
