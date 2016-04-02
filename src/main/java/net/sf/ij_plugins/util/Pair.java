/*
 * IJ-Plugins
 * Copyright (C) 2002-2016 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Latest release available at http://sourceforge.net/projects/ij-plugins/
 */

package net.sf.ij_plugins.util;

/**
 * Helper class that represents par of values. Useful for returning two values from a method.
 *
 * @author Jarek Sacha
 * @since Oct 16, 2009 10:40:55 PM
 */
public final class Pair<A, B> {

    final A first;
    final B second;


    public Pair(final A first, final B second) {
        this.first = first;
        this.second = second;
    }


    public A getFirst() {
        return first;
    }


    public B getSecond() {
        return second;
    }


    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }


    @Override
    public boolean equals(final Object other) {
        return other instanceof Pair<?, ?> &&
                equals(first, ((Pair<?, ?>) other).first) &&
                equals(second, ((Pair<?, ?>) other).second);
    }


    @Override
    public int hashCode() {
        if (first == null) {
            return (second == null) ? 0 : second.hashCode() + 1;
        } else if (second == null) {
            return first.hashCode() + 2;
        } else {
            return first.hashCode() * 17 + second.hashCode();
        }
    }


    private static boolean equals(final Object x, final Object y) {
        return (x == null && y == null) || (x != null && x.equals(y));
    }
}
