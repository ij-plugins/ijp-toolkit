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
public class AbstractRunningMedianOperatorTest extends TestCase {
    protected static final float TOLERANCE = 0.0001f;
    protected static final int filterSize = 3;

    protected IRunningMedianFloatOperator operator = new RunningMedianOperator();

    public AbstractRunningMedianOperatorTest(String test) {
        super(test);
    }

    /**
     * The fixture clean up called after every test method.
     */
    protected void tearDown() throws Exception {
    }

    public void test3x3_1() throws Exception {

        operator.reset(filterSize, filterSize);

        final float[] chunk = new float[filterSize];
        chunk[0] = 206;
        chunk[1] = 202;
        chunk[2] = 201;
        operator.push(3, chunk);
        assertEquals(202, operator.evaluate(), TOLERANCE);

        chunk[0] = 220;
        chunk[1] = 191;
        chunk[2] = 199;
        operator.push(3, chunk);
        assertEquals(202, operator.evaluate(), TOLERANCE);

        chunk[0] = 187;
        chunk[1] = 217;
        chunk[2] = 17;
        operator.push(3, chunk);
        assertEquals(201, operator.evaluate(), TOLERANCE);

        chunk[0] = 252;
        chunk[1] = 240;
        chunk[2] = 221;
        operator.push(3, chunk);
        assertEquals(217, operator.evaluate(), TOLERANCE);
    }

    public void test3x3_2() throws Exception {

        final int filterSize = 3;

        final RunningMedianOperator operator = new RunningMedianOperator();
        operator.reset(filterSize, filterSize);

        //        assertEquals(0, operator.evaluate(), TOLERANCE);

        final float[] chunk = new float[filterSize];
        chunk[0] = 206;
        chunk[1] = 202;
        operator.push(2, chunk);
        chunk[0] = 220;
        chunk[1] = 191;
        operator.push(2, chunk);
        assertEquals(206, operator.evaluate(), TOLERANCE);

        chunk[0] = 187;
        chunk[1] = 217;
        operator.push(2, chunk);
        assertEquals(206, operator.evaluate(), TOLERANCE);


        chunk[0] = 252;
        chunk[1] = 240;
        operator.push(2, chunk);
        assertEquals(220, operator.evaluate(), TOLERANCE);
    }

    public void test3x3_0_18() throws Exception {

        final int filterSize = 3;

        final RunningMedianOperator operator = new RunningMedianOperator();
        operator.reset(filterSize, filterSize);

        final float[] chunk = new float[filterSize];
        chunk[0] = 181;
        chunk[1] = 168;
        chunk[2] = 124;
        operator.push(3, chunk);
        chunk[0] = 255;
        chunk[1] = 220;
        chunk[2] = 185;
        operator.push(3, chunk);
        assertEquals(185, operator.evaluate(), TOLERANCE);

        chunk[0] = 189;
        chunk[1] = 223;
        chunk[2] = 202;
        operator.push(3, chunk);
        assertEquals(189, operator.evaluate(), TOLERANCE);

    }

    public void test3x3_124_12() throws Exception {

        final int filterSize = 3;

        final RunningMedianOperator operator = new RunningMedianOperator();
        operator.reset(filterSize, filterSize);


        final float[] chunk = new float[filterSize];
        chunk[0] = 123;
        chunk[1] = 120;
        chunk[2] = 76;
        operator.push(3, chunk);
        chunk[0] = 160;
        chunk[1] = 130;
        chunk[2] = 165;
        operator.push(3, chunk);
        chunk[0] = 227;
        chunk[1] = 186;
        chunk[2] = 137;
        operator.push(3, chunk);
        assertEquals(137, operator.evaluate(), TOLERANCE);

        chunk[0] = 212;
        chunk[1] = 207;
        chunk[2] = 232;
        operator.push(3, chunk);
        assertEquals(186, operator.evaluate(), TOLERANCE);

        chunk[0] = 242;
        chunk[1] = 6;
        chunk[2] = 210;
        operator.push(3, chunk);
        assertEquals(210, operator.evaluate(), TOLERANCE);
    }


    public void test3x3_0_24() throws Exception {

        final int filterSize = 3;

        final RunningMedianOperator operator = new RunningMedianOperator();
        operator.reset(filterSize, filterSize);


        final float[] chunk = new float[filterSize];
        chunk[0] = 239;
        chunk[1] = 124;
        chunk[2] = 82;
        operator.push(3, chunk);
        chunk[0] = 89;
        chunk[1] = 99;
        chunk[2] = 125;
        operator.push(3, chunk);
        assertEquals(124, operator.evaluate(), TOLERANCE);

        chunk[0] = 124;
        chunk[1] = 162;
        chunk[2] = 159;
        operator.push(3, chunk);
        assertEquals(124, operator.evaluate(), TOLERANCE);

        chunk[0] = 171;
        chunk[1] = 168;
        chunk[2] = 139;
        operator.push(3, chunk);
        assertEquals(139, operator.evaluate(), TOLERANCE);

    }


}