/*
 * Image/J Plugins
 * Copyright (C) 2002-2014 Jarek Sacha
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
package net.sf.ij_plugins.filters;

/**
 * Defines an operator that dynamically accepts updates to values on which it operates. Values are
 * added using {@link #add}, and removed using {@link #remove}. Order in which values are added or
 * removed is not relevant. A value that was not added cannot be removed. Call to {@link #evaluate}
 * returns current value computed by the operator.
 * <br>
 * Values handled by this operator are assumed to be unsigned 8 bit integers, that is integers in
 * range 0 to 255 (inclusive). Here is an example of using the operator and converting between
 * <code>int</code> and unsigned 8 bit integers.
 * <pre>
 *   // Add value to operator converting from int to 8 bit unsigned integer.
 *   IRunningUInt8Operator operator = ...;
 *   operator.add((byte)(13 & 0xFF))
 *   operator.add((byte)(156 & 0xFF))
 *   operator.add((byte)(67 & 0xFF))
 *   // Get result of operator evaluation and convert it to int.
 *   int result = operator.evaluate() & 0xFF;
 * </pre>
 *
 * @author Jarek Sacha
 */
interface IRunningUInt8Operator {
    /**
     * Adds new value to running operator computations.
     *
     * @param v value to be added (assumed to be unsigned 8 bit integer).
     */
    void add(byte v);

    /**
     * Removes an existing value from running operator computations. It is illegal to remove a value
     * that is not contained in the operator (a value must be added before it can be removed)
     *
     * @param v value to be removed (assumed to be unsigned 8 bit integer).
     */
    void remove(byte v);

    /**
     * Evaluate the operator on values it currently contains.
     *
     * @return
     */
    byte evaluate();

    /**
     * Checks is the operator contains a specific value.
     *
     * @param v value to check.
     * @return <code>true</code> is the operator contains the value, <code>false</code> otherwise.
     */
    boolean contains(byte v);

    /**
     * Reset operators internal state (remove all values).
     */
    void clear();
}
