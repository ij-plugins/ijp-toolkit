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
package net.sf.ij.im3d.grow;

import ij.*;

import java.util.*;

import net.sf.ij.im3d.*;

/**
 *  Simple region growing algorithm that extracts all pixels connected to the
 *  seed as long as they intensities are within given threshold limits. Min
 *  limit is inclusive, max limit is exclusive.
 *
 *@author     Jarek Sacha
 *@created    April 29, 2002
 *@version    $Revision: 1.1 $
 */

public class ConnectedThresholdFilter {

  /**  Value of a member pixel in output image */
  public final static byte MARKER = (byte) 0xff;

  /**  Value of background (not member) pixel in output image */
  public final static byte BACKGROUND = (byte) 0x00;

  /**
   *  Used to mark pixels that were determined to not be members. This value is
   *  changed to BACKGROUND in the output image.
   */
  private final static byte NOT_MEMBER = (byte) 0x01;

  /**  Grow candidates */
  private LinkedList candidatePoints = new LinkedList();

  private byte[][] srcPixels = null;
  private byte[][] destPixels = null;
  private int xSize, ySize, zSize;
  private int xMin, xMax;
  private int yMin, yMax;
  private int zMin, zMax;
  private int valueMin, valueMax;


  /**  Constructor for the ConnectedThresholdFilter object */
  public ConnectedThresholdFilter() { }


  /**
   *  Set max threshold value.
   *
   *@param  valueMax  The new ValueMax value
   */
  public void setValueMax(int valueMax) {
    this.valueMax = valueMax;
  }


  /**
   *  Set min threshold value.
   *
   *@param  valueMin  The new ValueMin value
   */
  public void setValueMin(int valueMin) {
    this.valueMin = valueMin;
  }


  /**
   *  Gets the ValueMax attribute of the ConnectedThresholdFilter object
   *
   *@return    The ValueMax value
   */
  public int getValueMax() {
    return valueMax;
  }


  /**
   *  Gets the ValueMin attribute of the ConnectedThresholdFilter object
   *
   *@return    The ValueMin value
   */
  public int getValueMin() {
    return valueMin;
  }


  /**
   *  Main processing method for the ConnectedThresholdFilter object
   *
   *@param  src   Input image.
   *@param  seed  Seed point.
   *@return       Image in which extracted pixels have value MARKER all other
   *      pixels have value BACKGROUND.
   */
  public ImageStack run(ImageStack src, Point3D seed) {

    initialize(src);

    // Verify that seed point can be a mamber point.
    checkForGrow(seed.x, seed.y, seed.z);

    // Iterate while there are still candidates to chesk.
    while (!candidatePoints.isEmpty()) {
      Point3D p = (Point3D) candidatePoints.removeFirst();
      checkForGrow(p.x - 1, p.y, p.z);
      checkForGrow(p.x + 1, p.y, p.z);
      checkForGrow(p.x, p.y - 1, p.z);
      checkForGrow(p.x, p.y + 1, p.z);
      checkForGrow(p.x, p.y, p.z - 1);
      checkForGrow(p.x, p.y, p.z + 1);
    }

    // Remove NOT_MEMBER markers and create output image.
    return createOutputStack();
  }


  /*
   *
   */
  /**
   *  Description of the Method
   *
   *@param  src  Description of the Parameter
   */
  private void initialize(ImageStack src) {
    candidatePoints.clear();
    xSize = src.getWidth();
    xMin = 0;
    xMax = xSize;
    ySize = src.getHeight();
    yMin = 0;
    yMax = ySize;
    zSize = src.getSize();
    zMin = 0;
    zMax = zSize;

    Object[] imageArray = src.getImageArray();

    srcPixels = new byte[zSize][];
    for (int z = 0; z < zSize; ++z) {
      srcPixels[z] = (byte[]) imageArray[z];
      if (!(imageArray[z] instanceof byte[])) {
        throw new IllegalArgumentException("Expecting stack of byte images.");
      }
    }

    destPixels = new byte[zSize][];
    int sliceSize = xSize * ySize;
    for (int z = 0; z < zSize; ++z) {
      destPixels[z] = new byte[sliceSize];
    }
  }


  /**
   *  Check if point with coordinates (x,y,z) is a new candidate. Point is a
   *  candidate if 1) its coordinates are within ROI, 2) it was not yet
   *  analyzed, 3) its value is within limits. <p>
   *
   *  This method modifies 'candidatePoints' and 'destPixels'.
   *
   *@param  x
   *@param  y
   *@param  z
   */
  private void checkForGrow(int x, int y, int z) {
    if (x < xMin || x >= xMax ||
        y < yMin || y >= yMax ||
        z < zMin || z >= zMax) {
      return;
    }

    int offset = y * xSize + x;
    if (destPixels[z][offset] == BACKGROUND) {
      int value = srcPixels[z][offset] & 0xff;
      if (value >= valueMin && value < valueMax) {
        destPixels[z][offset] = MARKER;
        candidatePoints.addLast(new Point3D(x, y, z));
      } else {
        destPixels[z][offset] = NOT_MEMBER;
      }
    }
  }


  /*
   *
   */
  /**
   *  Description of the Method
   *
   *@return    Description of the Return Value
   */
  private ImageStack createOutputStack() {
    ImageStack dest = new ImageStack(xSize, ySize);
    int sliceSize = xSize * ySize;
    for (int z = 0; z < zSize; ++z) {
      byte[] slicePixels = destPixels[z];
      for (int i = 0; i < sliceSize; ++i) {
        if (slicePixels[i] == NOT_MEMBER) {
          slicePixels[i] = BACKGROUND;
        }
      }
      dest.addSlice(null, slicePixels);
    }

    return dest;
  }
}
