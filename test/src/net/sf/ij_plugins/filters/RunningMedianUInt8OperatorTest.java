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

import junit.framework.TestCase;

/**
 * @author Jarek Sacha
 */
public class RunningMedianUInt8OperatorTest extends TestCase {
    public RunningMedianUInt8OperatorTest(String test) {
        super(test);
    }

    /**
     * The fixture set up called before every test method.
     */
    protected void setUp() throws Exception {
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void testEvaluate1() throws Exception {
        RunningMedianUInt8Operator medianUInt8Operator = new RunningMedianUInt8Operator();

        medianUInt8Operator.add((byte) (5 & 0xFF));
        assertEquals(5, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (8 & 0xFF));
        assertEquals(7, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (9 & 0xFF));
        assertEquals(8, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (10 & 0xFF));
        assertEquals(9, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (12 & 0xFF));
        assertEquals(9, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (13 & 0xFF));
        assertEquals(10, medianUInt8Operator.evaluate());

        medianUInt8Operator.add((byte) (17 & 0xFF));
        assertEquals(10, medianUInt8Operator.evaluate());

        medianUInt8Operator.remove((byte) (17 & 0xFF));
        assertEquals(10, medianUInt8Operator.evaluate());

        medianUInt8Operator.remove((byte) (5 & 0xFF));
        assertEquals(10, medianUInt8Operator.evaluate());
    }

    public void testEvaluate2() throws Exception {
        RunningMedianUInt8Operator medianUInt8Operator = new RunningMedianUInt8Operator();

        medianUInt8Operator.add((byte) (206 & 0xFF));
        medianUInt8Operator.add((byte) (202 & 0xFF));
        medianUInt8Operator.add((byte) (201 & 0xFF));
        medianUInt8Operator.add((byte) (220 & 0xFF));
        medianUInt8Operator.add((byte) (191 & 0xFF));
        medianUInt8Operator.add((byte) (199 & 0xFF));
        medianUInt8Operator.add((byte) (187 & 0xFF));
        medianUInt8Operator.add((byte) (217 & 0xFF));
        medianUInt8Operator.add((byte) (017 & 0xFF));
        assertEquals(201, medianUInt8Operator.evaluate() & 0xFF);

        medianUInt8Operator.add((byte) (252 & 0xFF));
        medianUInt8Operator.add((byte) (240 & 0xFF));
        medianUInt8Operator.add((byte) (221 & 0xFF));
        medianUInt8Operator.remove((byte) (206 & 0xFF));
        medianUInt8Operator.remove((byte) (202 & 0xFF));
        medianUInt8Operator.remove((byte) (201 & 0xFF));
        assertEquals(217, medianUInt8Operator.evaluate() & 0xFF);
    }

}