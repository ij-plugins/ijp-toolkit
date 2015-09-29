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
package net.sf.ij_plugins.math;

import junit.framework.TestCase;

import java.awt.geom.Point2D;

/**
 * @author Jarek Sacha
 */
public class PiecewiseFunctionTest extends TestCase {
    private static final double TOLERANCE = 1e-10;

    public PiecewiseFunctionTest(String test) {
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

    public void testSimplePoint() throws Exception {
        Point2D.Double[] controlPoints = {
                new Point2D.Double(0, 0),
                new Point2D.Double(1, 1),
                new Point2D.Double(3, 2),
                new Point2D.Double(4, -2)
        };

        PiecewiseFunction pf = new PiecewiseFunction(controlPoints);

        // Test at control points
        for (final Point2D controlPoint : controlPoints) {
            double x = controlPoint.getX();
            double y = controlPoint.getY();
            assertEquals(y, pf.evaluate(x), TOLERANCE);
        }

        // test between control points
        assertEquals(0.100, pf.evaluate(0.10), TOLERANCE);
        assertEquals(0.750, pf.evaluate(0.75), TOLERANCE);
        assertEquals(1.125, pf.evaluate(1.25), TOLERANCE);
        assertEquals(1.250, pf.evaluate(1.50), TOLERANCE);
        assertEquals(1.500, pf.evaluate(2.00), TOLERANCE);
        assertEquals(1.750, pf.evaluate(2.50), TOLERANCE);
        assertEquals(1.000, pf.evaluate(3.25), TOLERANCE);
        assertEquals(0.000, pf.evaluate(3.50), TOLERANCE);
        assertEquals(-1.000, pf.evaluate(3.75), TOLERANCE);

        // Test outside range
        try {
            assertEquals(-1, pf.evaluate(-1), TOLERANCE);
            fail("Out of range exception should be thrown in previous instruction.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            assertEquals(-4, pf.evaluate(5), TOLERANCE);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public void testSimpleXY() throws Exception {
        final double[] x = {0, 1, 3, 4};
        final double[] y = {0, 1, 2, -2};

        PiecewiseFunction pf = new PiecewiseFunction(x, y);

        // Test at control points
        for (int i = 0; i < x.length; i++) {
            assertEquals(y[i], pf.evaluate(x[i]), TOLERANCE);
        }

        // test between control points
        assertEquals(0.100, pf.evaluate(0.10), TOLERANCE);
        assertEquals(0.750, pf.evaluate(0.75), TOLERANCE);
        assertEquals(1.125, pf.evaluate(1.25), TOLERANCE);
        assertEquals(1.250, pf.evaluate(1.50), TOLERANCE);
        assertEquals(1.500, pf.evaluate(2.00), TOLERANCE);
        assertEquals(1.750, pf.evaluate(2.50), TOLERANCE);
        assertEquals(1.000, pf.evaluate(3.25), TOLERANCE);
        assertEquals(0.000, pf.evaluate(3.50), TOLERANCE);
        assertEquals(-1.000, pf.evaluate(3.75), TOLERANCE);

        // Test outside range
        try {
            assertEquals(-1, pf.evaluate(-1), TOLERANCE);
            fail("Out of range exception should be thrown in previous instruction.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            assertEquals(-4, pf.evaluate(5), TOLERANCE);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


    public void testSorting() throws Exception {
        Point2D.Double[] controlPoints = {
                new Point2D.Double(4, -2),
                new Point2D.Double(0, 0),
                new Point2D.Double(3, 2),
                new Point2D.Double(1, 1),
        };

        PiecewiseFunction pf = new PiecewiseFunction(controlPoints);

        // Test at control points
        for (final Point2D controlPoint : controlPoints) {
            double x = controlPoint.getX();
            double y = controlPoint.getY();
            assertEquals(y, pf.evaluate(x), TOLERANCE);
        }

        // test between control points
        assertEquals(0.100, pf.evaluate(0.10), TOLERANCE);
        assertEquals(0.750, pf.evaluate(0.75), TOLERANCE);
        assertEquals(1.125, pf.evaluate(1.25), TOLERANCE);
        assertEquals(1.250, pf.evaluate(1.50), TOLERANCE);
        assertEquals(1.500, pf.evaluate(2.00), TOLERANCE);
        assertEquals(1.750, pf.evaluate(2.50), TOLERANCE);
        assertEquals(1.000, pf.evaluate(3.25), TOLERANCE);
        assertEquals(0.000, pf.evaluate(3.50), TOLERANCE);
        assertEquals(-1.000, pf.evaluate(3.75), TOLERANCE);

        // Test outside range
        try {
            assertEquals(-1, pf.evaluate(-1), TOLERANCE);
            fail("Out of range exception should be thrown in previous instruction.");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
        try {
            assertEquals(-4, pf.evaluate(5), TOLERANCE);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }


}