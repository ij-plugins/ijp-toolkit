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
 *  MetaImage tags.
 *
 *@author     Jarek Sacha
 *@created    June 18, 2002
 *@version    $Revision: 1.1 $ $Date: 2002-07-19 02:45:18 $
 */
public class MiTag extends Enumeration {

  /**  Number of dimensions, e.g. '3'.  */
  public static MiTag NDims = new MiTag("NDims");
  /**
   *  Size for each of the dimensions separated by spaces, e.g. '256 256 480'.
   */
  public static MiTag DimSize = new MiTag("DimSize");

  /**
   *  Atomic data type.
   *
   *@see    MiElementType
   */
  public static MiTag ElementType = new MiTag("ElementType");

  /**
   *  Number of bytes before start of the image data. If <code>HeaderSize</code>
   *  is equal -1 then header size should be calculated from the file size and
   *  image data size. Default value is 0.
   */
  public static MiTag HeaderSize = new MiTag("HeaderSize");

  /**
   *  Size of each voxel, e.g. '2.5 2.5 8'. Optional, by default is equal to
   *  <code>ElementSpacing</code>, if that is not specified the it is 1 in each
   *  dimension.
   */
  public static MiTag ElementSize = new MiTag("ElementSize");

  /**
   *  Spacing between voxels (for instance, MRI images often use overlaping
   *  slices), e.g. '2.5 2.5 8'. Optional, default is 1 in each dimension.
   */
  public static MiTag ElementSpacing = new MiTag("ElementSpacing");

  /**
   *  Equal to 'True' if image data are stored using MSB (most significant bye)
   *  order, otherwise 'False'. Default is the same as native type for the
   *  platfor on which program runs. This tag is not required, however, it use
   *  is highly recommanded to avoid problems reading images on other platforms.
   *  <br>
   *  Note: MSB is native for Java and most computers not using Intel compatible
   *  processors, e.g. Mac or most UNIX computers (excluding most flavors of
   *  Linux that run on Intel compatible processors).
   */
  public static MiTag ElementByteOrderMSB = new MiTag("ElementByteOrderMSB");

  /**
   *  Location of the image data file. This tag must be last in the MetaImage
   *  header.
   */
  public static MiTag ElementDataFile = new MiTag("ElementDataFile");


  /**
   *  Constructor for the MiTag object
   *
   *@param  name  Description of the Parameter
   */
  private MiTag(String name) {
    super(name);
  }
}

