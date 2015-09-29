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

package net.sf.ij_plugins.io.vtk;

import net.sf.ij_plugins.util.Enumeration;


/**
 * Represent possible values of scalar names for the SCALARS tag.
 *
 * @author Jarek Sacha
 * @since June 21, 2002
 */

final class VtkScalarType extends Enumeration {

    /**
     * bit
     */
    public static final VtkScalarType BIT = new VtkScalarType("bit");
    /**
     * unsigned_char
     */
    public static final VtkScalarType UNSIGNED_CHAR = new VtkScalarType("unsigned_char");
    /**
     * char
     */
    public static final VtkScalarType CHAR = new VtkScalarType("char");
    /**
     * unsigned_short
     */
    public static final VtkScalarType UNSIGNED_SHORT = new VtkScalarType("unsigned_short");
    /**
     * short
     */
    public static final VtkScalarType SHORT = new VtkScalarType("short");
    /**
     * unsigned_int
     */
    public static final VtkScalarType UNSIGNED_INT = new VtkScalarType("unsigned_int");
    /**
     * int
     */
    public static final VtkScalarType INT = new VtkScalarType("int");
    /**
     * unsigned_long
     */
    public static final VtkScalarType UNSIGNED_LONG = new VtkScalarType("unsigned_long");
    /**
     * long
     */
    public static final VtkScalarType LONG = new VtkScalarType("long");
    /**
     * float
     */
    public static final VtkScalarType FLOAT = new VtkScalarType("float");
    /**
     * double
     */
    public static final VtkScalarType DOUBLE = new VtkScalarType("double");


    private VtkScalarType(final String name) {
        super(name);
    }
}
