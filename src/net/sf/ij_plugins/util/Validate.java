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

package net.sf.ij_plugins.util;

/**
 * @author Jarek Sacha
 * @since Oct 22, 2009 7:34:52 PM
 */
public final class Validate {

    private Validate() {
    }


    /**
     * Checks if the object is not {@code null}. If it is {@code null} throws {@link IllegalArgumentException}.
     *
     * @param object argument to be validated.
     * @param name   argument name, used in exception message.
     * @throws IllegalArgumentException if {@code object} is null.
     */
    public static void argumentNotNull(final Object object, final String name) throws IllegalArgumentException {
        if (object == null) {
            throw new IllegalArgumentException(
                    name != null
                            ? "Argument '" + name + "' cannot be null."
                            : "Argument cannot be null.");
        }
    }


    /**
     * <p>Validate that the argument condition is <code>true</code>; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     * <p/>
     * <pre>
     * Validate.isTrue( (i > 0), "The value must be greater than zero");
     * Validate.isTrue( myObject.isOk(), "The object is not OK");
     * </pre>
     *
     * @param expression the boolean expression to check
     * @param message    the exception message if invalid
     * @throws IllegalArgumentException if expression is <code>false</code>
     */
    public static void isTrue(final boolean expression, final String message) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

}
