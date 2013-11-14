/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

import net.sf.ij_plugins.IJPluginsRuntimeException;
import net.sf.ij_plugins.util.Validate;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Defines a 1D piecewise liner function mapping.
 *
 * @author Jarek Sacha
 */
public class PiecewiseFunction implements IFunction, Cloneable {

    protected double[] xa;
    protected double[] ya;


    public PiecewiseFunction(final double[] x, final double[] y) {
        Validate.argumentNotNull(x, "x");
        Validate.argumentNotNull(y, "y");
        Validate.isTrue(x.length == y.length, "Arrays X and y must be of the same length, " +
                "got :" + x.length + " and " + y.length + ".");

        final Point2D[] p = new Point2D[x.length];
        for (int i = 0; i < p.length; i++) {
            p[i] = new Point.Double(x[i], y[i]);
        }
        initialize(p);
    }

    public PiecewiseFunction(final Point2D[] controlPoints) {
        Validate.argumentNotNull(controlPoints, "controlPoints");
        initialize(controlPoints);
    }

    private void initialize(final Point2D[] controlPoints) {
        // Sort points by increasing value of 'x'
        sort(controlPoints);

        this.xa = new double[controlPoints.length];
        this.ya = new double[controlPoints.length];
        for (int i = 0; i < controlPoints.length; i++) {
            Point2D controlPoint = controlPoints[i];
            this.xa[i] = controlPoint.getX();
            this.ya[i] = controlPoint.getY();
        }

        // Validate that 'x' coordinates are unique
        for (int i = 1; i < xa.length; i++) {
            if (xa[i] == xa[i - 1]) {
                throw new IllegalArgumentException("Piecewise function control points must have unique values of 'x' coordinate.");
            }

        }

    }


    public double evaluate(final double x) {
        if (x < xa[0] || x > xa[xa.length - 1]) {
            throw new IllegalArgumentException("Argument x=" + x
                    + " is outside of the range of this function [" + xa[0] + ", "
                    + xa[xa.length - 1] + "].");
        }

        // TODO: optimize performance when calls are made with consecutive/similar values of x

        // We will find the right place in the table by means of bisection.
        // This is optimal if sequential calls to this routine are at random
        // values of x. If sequential calls are in order, and closely spaced,
        // one would do better to store previous values of klo and khi and test
        // if they remain appropriate in the next call.

        int klo = 0;
        int khi = xa.length - 1;
        while ((khi - klo) > 1) {
            final int k = (khi + klo) >> 1;
            if (xa[k] > x) {
                khi = k;
            } else {
                klo = k;
            }
        }

        final double x1 = xa[klo];
        final double x2 = xa[khi];
        final double y1 = ya[klo];
        final double y2 = ya[khi];

        final double h = x2 - x1;
        if (h == 0) {
            throw new IJPluginsRuntimeException("Values of array 'xa' must be distinct.");
        }

        return (x - x1) * (y2 - y1) / (x2 - x1) + y1;
    }

    public double[] evaluate(final double[] x) {
        Validate.argumentNotNull(x, "x");
        final double[] y = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            y[i] = evaluate(x[i]);
        }
        return y;
    }


    @SuppressWarnings({"UnusedDeclaration"})
    public Point2D[] getControlPoints() {
        Point2D.Double[] cp = new Point2D.Double[xa.length];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = new Point2D.Double(xa[i], ya[i]);
        }
        return cp;
    }


    public PiecewiseFunction clone() throws CloneNotSupportedException {
        final PiecewiseFunction r = (PiecewiseFunction) super.clone();

        r.xa = new double[xa.length];
        System.arraycopy(xa, 0, r.xa, 0, xa.length);

        r.ya = new double[ya.length];
        System.arraycopy(ya, 0, r.ya, 0, ya.length);

        return r;
    }


    /**
     * Sort points by increasing value of 'x', value of y is ignored.
     *
     * @param controlPoints array of points to be sorted in place.
     */
    public static void sort(final Point2D[] controlPoints) {
        if (controlPoints == null) {
            throw new IllegalArgumentException("Argument 'controlPoints' cannot be null.");
        }

        Arrays.sort(controlPoints, new Comparator<Point2D>() {
            public int compare(final Point2D p1, final Point2D p2) {
                if (p1.equals(p2)) {
                    return 0;
                }

                return p1.getX() < p2.getX()
                        ? -1
                        : (p1.getX() > p2.getX() ? 1 : 0);
            }
        });

    }
}
