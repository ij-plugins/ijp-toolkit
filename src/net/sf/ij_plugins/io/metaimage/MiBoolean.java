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
 * Representation of boolean values in MetaImage header files.
 *
 * @author Jarek Sacha
 * @since June 18, 2002
 */
public final class MiBoolean extends Enumeration {

    /**
     * Represents boolean symbol 'TRUE' used by MetaImage file format.
     */
    public static final MiBoolean TRUE = new MiBoolean("True");
    /**
     * Represents boolean symbol 'False' used by MetaImage file format.
     */
    public static final MiBoolean FALSE = new MiBoolean("False");


    /**
     * Constructor for the MiBoolean object
     *
     * @param name Description of the Parameter
     */
    private MiBoolean(final String name) {
        super(name);
    }
}
