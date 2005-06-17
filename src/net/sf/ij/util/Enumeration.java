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

import net.sf.ij.IJPluginsRuntimeException;

import java.util.ArrayList;

/**
 * Abstract base class for representing enumeration. Based on pattern presented by Joshua Bloch in
 * "Effective Java" 2001.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.2 $ $Date: 2005-06-17 02:18:25 $
 * @created June 18, 2002
 */
public abstract class Enumeration {
    private static ArrayList allMembers = new ArrayList();

    private String name;


    /**
     * Constructor for the Enumeration object
     *
     * @param name Description of Parameter
     */
    protected Enumeration(String name) {
        // Check that the name is unique
        try {
            byName(name);
            throw new IJPluginsRuntimeException(
                    "An Enumeration cannot have two members with the same name ['"
                            + name + "].");
        }
        catch (IllegalArgumentException ex) {
            // Expect this exception as a confirmation that the name is not on
            // the member list.
        }

        this.name = name;
        allMembers.add(this);
    }


    /**
     * Returns a reference to the named member of this Enumeration, throes IllegalArgumentException
     * of name is not found.<p>
     * <p/>
     * <strong>API NOTE:</strong> This method looks as it should have been defines as static.
     * However, this could lead to an unpredictable behavior of this method due to the way Java
     * initializes static member variables of a class. In particular, if this method was static it
     * would be possible to call it before static member variables of the Enumeration class for
     * which it was called were initialized. As a result if this method was static it could throw
     * IllegalArgumentException even when its argument was a valid member name.
     *
     * @param memberName A name of the Enumeration member.
     * @return Reference to a member with given <code>memberName</code>.
     * @throws IllegalArgumentException If a member with given <code>memberName</code> cannot be
     *                                  found.
     */
    final public Enumeration byName(final String memberName)
            throws IllegalArgumentException {
        for (int i = 0; i < allMembers.size(); ++i) {
            final Enumeration member = (Enumeration) allMembers.get(i);
            if (member.name.equals(memberName)) {
                return member;
            }
        }

        throw new IllegalArgumentException("Invalid member name '" + memberName + "'.");
    }


    /**
     * Return name a
     *
     * @return Description of the Returned Value
     */
    final public String toString() {
        return name;
    }
}
