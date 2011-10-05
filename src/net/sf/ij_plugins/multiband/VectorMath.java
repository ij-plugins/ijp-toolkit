/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
package net.sf.ij_plugins.multiband;

/**
 * In-place vector operations
 */
public final class VectorMath {

    private VectorMath() {
    }


    public static void add(final float[] a, final float[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] += b[i];
        }
    }


    public static void subtract(final float[] a, final float[] b) {
        for (int i = 0; i < a.length; i++) {
            a[i] -= b[i];
        }
    }


    public static double distance(final float[] a, final float[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            final double d = a[i] - b[i];
            s += d * d;
        }

        return Math.sqrt(s);
    }


    public static double distance(final double[] a, final double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            final double d = a[i] - b[i];
            s += d * d;
        }

        return Math.sqrt(s);
    }


    public static double distanceSqr(final float[] a, final double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            final double d = a[i] - b[i];
            s += d * d;
        }

        return s;
    }

}
