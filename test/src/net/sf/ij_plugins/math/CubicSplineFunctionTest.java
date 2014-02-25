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
public class CubicSplineFunctionTest extends TestCase {

    // TODO: Add less trivial tests of cubic splines

    private static final double TOLERANCE = 1e-10;

    public CubicSplineFunctionTest(String test) {
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

    public void testConstant1() throws Exception {
        final Point2D.Double[] cp = {
                new Point2D.Double(0, 1),
                new Point2D.Double(1, 1),
                new Point2D.Double(2.5, 1),
                new Point2D.Double(3, 1)};
        CubicSplineFunction spline = new CubicSplineFunction(cp);

        final double[] xaTest = {0, 0.5, 1, 1.3, 1.75, 2, 2.1, 2.99};
        for (final double x : xaTest) {
            double y = spline.evaluate(x);
            assertEquals(1, y, TOLERANCE);
        }
    }

    public void testRamp1() throws Exception {
        final Point2D.Double[] cp = {
                new Point2D.Double(0, 0),
                new Point2D.Double(1, 1),
                new Point2D.Double(2.5, 2.5),
                new Point2D.Double(3, 3)};
        CubicSplineFunction spline = new CubicSplineFunction(cp, 1, 1);

        final double[] xaTest = {0, 0.5, 1, 1.3, 1.75, 2, 2.1, 2.99};
        for (final double x : xaTest) {
            double y = spline.evaluate(x);
            assertEquals(x, y, TOLERANCE);
        }
    }

    public void testRamp1XY() throws Exception {
        final double[] x = {0, 1, 2.5, 3,};
        final double[] y = {0, 1, 2.5, 3,};
        CubicSplineFunction spline = new CubicSplineFunction(x, y, 1, 1);

        final double[] xaTest = {0, 0.5, 1, 1.3, 1.75, 2, 2.1, 2.99};
        for (final double xx : xaTest) {
            double yy = spline.evaluate(xx);
            assertEquals(xx, yy, TOLERANCE);
        }
    }

}