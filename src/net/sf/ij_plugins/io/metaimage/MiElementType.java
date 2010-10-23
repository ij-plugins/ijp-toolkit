/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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
 * Represents types of element values in MetaImage files.
 *
 * @author Jarek Sacha
 * @since June 18, 2002
 */
public final class MiElementType extends Enumeration {

    /**
     * MET_UCHAR
     */
    public static final MiElementType MET_UCHAR = new MiElementType("MET_UCHAR");
    /**
     * MET_CHAR
     */
    public static final MiElementType MET_CHAR = new MiElementType("MET_CHAR");
    /**
     * MET_USHORT
     */
    public static final MiElementType MET_USHORT = new MiElementType("MET_USHORT");
    /**
     * MET_SHORT
     */
    public static final MiElementType MET_SHORT = new MiElementType("MET_SHORT");
    /**
     * MET_UINT
     */
    public static final MiElementType MET_UINT = new MiElementType("MET_UINT");
    /**
     * MET_INT
     */
    public static final MiElementType MET_INT = new MiElementType("MET_INT");
    /**
     * MET_ULONG
     */
    public static final MiElementType MET_ULONG = new MiElementType("MET_ULONG");
    /**
     * MET_LONG
     */
    public static final MiElementType MET_LONG = new MiElementType("MET_LONG");
    /**
     * MET_FLOAT
     */
    public static final MiElementType MET_FLOAT = new MiElementType("MET_FLOAT");
    /**
     * MET_DOUBLE
     */
    public static final MiElementType MET_DOUBLE = new MiElementType("MET_DOUBLE");


    /**
     * Constructor for the MiElementType object
     *
     * @param name Description of the Parameter
     */
    private MiElementType(final String name) {
        super(name);
    }
}
