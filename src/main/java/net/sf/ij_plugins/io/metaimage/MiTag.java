/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
 * Author's email: jsacha at users dot sourceforge dot net
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

package net.sf.ij_plugins.io.metaimage;

import net.sf.ij_plugins.util.Enumeration;


/**
 * MetaImage tags.
 *
 * @author Jarek Sacha
 * @since June 18, 2002
 */
public final class MiTag extends Enumeration {

    /**
     * Number of dimensions, e.g. '3'.
     */
    public static final MiTag N_DIMS = new MiTag("NDims");

    /**
     * Size for each of the dimensions separated by spaces, e.g. '256 256 480'.
     */
    public static final MiTag DIM_SIZE = new MiTag("DimSize");

    /**
     * Compressed Data
     */
    public static final MiTag COMPRESSED_DATA = new MiTag("CompressedData");

    /**
     * Atomic data type.
     *
     * @see net.sf.ij_plugins.io.metaimage.MiElementType
     */
    public static final MiTag ELEMENT_TYPE = new MiTag("ElementType");

    /**
     * Number of bytes before start of the image data. If <code>HeaderSize</code> is equal -1 then
     * header size should be calculated from the file size and image data size. Default value is 0.
     */
    public static final MiTag HEADER_SIZE = new MiTag("HeaderSize");

    /**
     * MET_INT - Number of values (of type <code>ElementType</code>) per voxel
     */
    public static final MiTag ELEMENT_NUMBER_OF_CHANNELS = new MiTag("ElementNumberOfChannels");

    /**
     * Size of each voxel, e.g. '2.5 2.5 8'. Optional, by default is equal to
     * <code>ElementSpacing</code>, if that is not specified the it is 1 in each dimension.
     */
    public static final MiTag ELEMENT_SIZE = new MiTag("ElementSize");

    /**
     * Spacing between voxels (for instance, MRI images often use overlapping slices), e.g. '2.5 2.5
     * 8'. Optional, default is 1 in each dimension.
     */
    public static final MiTag ELEMENT_SPACING = new MiTag("ElementSpacing");

    /**
     * Equal to 'True' if image data are stored using MSB (most significant bye) order, otherwise
     * 'False'. Default is the same as native type for the platform on which program runs. This tag
     * is not required, however, it use is highly recommended to avoid problems reading images on
     * other platforms. <br> Note: MSB is native for Java and most computers not using Intel
     * compatible processors, e.g. Mac or most UNIX computers (excluding most flavors of Linux that
     * run on Intel compatible processors).
     */
    public static final MiTag ELEMENT_BYTE_ORDER_MSB = new MiTag("ElementByteOrderMSB");

    /**
     * Location of the image data file. This tag must be last in the MetaImage header.
     */
    public static final MiTag ELEMENT_DATA_FILE = new MiTag("ElementDataFile");

    /**
     * ObjectType
     */
    public static final MiTag OBJECT_TYPE = new MiTag("ObjectType");
    /**
     * TransformType
     */
    public static final MiTag TRANSFORM_TYPE = new MiTag("TransformType");
    /**
     * ID
     */
    public static final MiTag ID = new MiTag("ID");
    /**
     * ParentID
     */
    public static final MiTag PARENT_ID = new MiTag("ParentID");
    /**
     * BinaryData
     */
    public static final MiTag BINARY_DATA = new MiTag("BinaryData");
    /**
     * BinaryDataByteOrderMSB
     */
    public static final MiTag BINARY_DATA_BYTE_ORDER_MSB = new MiTag("BinaryDataByteOrderMSB");
    /**
     * Color
     */
    public static final MiTag COLOR = new MiTag("Color");

    public static final MiTag TRANSFORM_MATRIX = new MiTag("TransformMatrix");
    public static final MiTag OFFSET = new MiTag("Offset");
    public static final MiTag CENTER_OF_ROTATION = new MiTag("CenterOfRotation");
    public static final MiTag ANATOMICAL_ORIENTATION = new MiTag("AnatomicalOrientation");


    /**
     * Constructor for the MiTag object
     *
     * @param name Description of the Parameter
     */
    private MiTag(final String name) {
        super(name);
    }
}

