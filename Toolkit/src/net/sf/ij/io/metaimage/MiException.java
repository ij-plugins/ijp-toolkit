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

/**
 * Exception specific to MetaImage I/O classes.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $ $Date: 2005-05-28 04:06:51 $
 * @created June 18, 2002
 */
public class MiException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the MetaImageException object
     *
     * @param message Message.
     */
    public MiException(String message) {
        super(message);

    }
}
