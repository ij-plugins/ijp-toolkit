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

import java.awt.geom.Point2D;


/**
 * Cubit spline implementation based on "Numerical Recipes in C", 2nd edition.
 *
 * @author Jarek Sacha
 */
public class CubicSplineFunction extends PiecewiseFunction {

    private final double derivativeAtFirst;
    private final double derivativeAtLast;
    private double y2a[];

    public CubicSplineFunction(final double[] x, final double[] y) {
        super(x, y);
        derivativeAtFirst = Double.NaN;
        derivativeAtLast = Double.NaN;
        this.y2a = derivatives();
    }

    public CubicSplineFunction(final double[] x, final double[] y, final double derivativeAtFirst, final double derivativeAtLast) {
        super(x, y);
        this.derivativeAtFirst = derivativeAtFirst;
        this.derivativeAtLast = derivativeAtLast;
        this.y2a = derivatives();
    }

    /**
     * Creates neutral spline, assuming that derivatives at first and last control points are 0.
     *
     * @param controlPoints spline control points.
     */
    public CubicSplineFunction(final Point2D[] controlPoints) {
        this(controlPoints, Double.NaN, Double.NaN);
    }


    /**
     * Creates a spline with given values of derivatives at first and the last point.
     *
     * @param controlPoints     spline control points.
     * @param derivativeAtFirst derivative at first control point.
     * @param derivativeAtLast  derivative at last control point.
     */
    public CubicSplineFunction(final Point2D[] controlPoints, final double derivativeAtFirst, final double derivativeAtLast) {
        super(controlPoints);

        this.derivativeAtFirst = derivativeAtFirst;
        this.derivativeAtLast = derivativeAtLast;
        this.y2a = derivatives();
    }


    /**
     * Required derivative at the first control point.
     *
     * @return derivative at the first control point.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public double getDerivativeAtFirst() {
        return derivativeAtFirst;
    }


    /**
     * Required derivative at the last control point.
     *
     * @return derivative at the last control point.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public double getDerivativeAtLast() {
        return derivativeAtLast;
    }


    /**
     * Arrays <code>xa</code> and <code>ya</code> tabulate a function <i>f</i>, i.e.
     * <code>ya</code><sub><i>i</i></sub> = <i>f</i>(<code>xa</code><sub><i>i</i></sub>), with
     * <code>xa</code><sub>1</sub> < <code>xa</code><sub>2</sub> ... <code>xa</code><sub><i>N</i></sub>,
     * and given values <code>yp1</code> and <code>ypn</code> for the first derivatives for the
     * first derivatives of the interpolating function at points <code>1</code> and <code>N</code>,
     * respectively, this function will return an array that contains the second derivatives of the
     * interpolating function at the tabulated points <code>xa</code><sub><i>i</i></sub>. If
     * <code>yp1</code> is {@link Double#NaN}, the
     * routine is signaled to set the corresponding boundary condition for a natural spline, with
     * zero second derivatives on the boundary.
     *
     * @return spline derivatives.
     */
    private double[] derivatives() {
        final double[] y2 = new double[xa.length];
        final double[] u = new double[xa.length];
        final int n_1 = xa.length - 1;
        final int n_2 = xa.length - 2;

        // The lower boundary condition is either set to be "natural"
        // (derivative = 0) or else to have a specified first derivative.
        if (Double.isNaN(derivativeAtFirst)) {
            y2[0] = u[0] = 0.0;
        } else {
            y2[0] = -0.5;
            u[0] = (3.0 / (xa[1] - xa[0])) * ((ya[1] - ya[0]) / (xa[1] - xa[0]) - derivativeAtFirst);
        }

        // This is the decomposition loop of the triangulation algorithm.
        // y2a and u are used for temporary storage of the decomposed factors
        for (int i = 1; i < n_1; ++i) {
            final double sig = (xa[i] - xa[i - 1]) / (xa[i + 1] - xa[i - 1]);
            final double p = sig * y2[i - 1] + 2.0;
            y2[i] = (sig - 1) / p;
            u[i] = (ya[i + 1] - ya[i]) / (xa[i + 1] - xa[i])
                    - (ya[i] - ya[i - 1]) / (xa[i] - xa[i - 1]);
            u[i] = (6 * u[i] / (xa[i + 1] - xa[i - 1]) - sig * u[i - 1]) / p;
        }

        // The upper boundary condition is either set to be "natural"
        // (derivative = 0) or else to have a specified first derivative.
        final double qn, un;
        if (Double.isNaN(derivativeAtLast)) {
            qn = un = 0.0;
        } else {
            qn = 0.5;
            un = (3.0 / (xa[n_1] - xa[n_2])) * (derivativeAtLast - (ya[n_1] - ya[n_2]) / (xa[n_1] - xa[n_2]));
        }
        y2[n_1] = (un - qn * u[n_2]) / (qn * y2[n_2] + 1.0);
        // The back-substitution loop of the tri-diagonal algorithm
        for (int k = n_2; k >= 0; --k) {
            y2[k] = y2[k] * y2[k + 1] + u[k];
        }

        return y2;
    }


    /**
     * Return cubic-spline interpolated value of <code>y</code>.
     *
     * @param x x-coordinate at which to evaluate the interpolation function.
     * @return interpolated value of <code>y</code>.
     */
    public double evaluate(final double x) {

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

        final double h = xa[khi] - xa[klo];
        if (h == 0) {
            throw new IJPluginsRuntimeException("Values of array 'xa' must be distinct.");
        }
        final double a = (xa[khi] - x) / h;
        final double b = (x - xa[klo]) / h;

        return a * ya[klo] + b * ya[khi] +
                ((a * a * a - a) * y2a[klo]
                        + (b * b * b - b) * y2a[khi]) * (h * h) / 6.0;
    }


    public CubicSplineFunction clone() throws CloneNotSupportedException {
        final CubicSplineFunction r = (CubicSplineFunction) super.clone();

        r.y2a = new double[y2a.length];
        System.arraycopy(y2a, 0, r.y2a, 0, y2a.length);

        return r;
    }

}
