/*
 * IJ-Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
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
 *  Latest release available at https://github.com/ij-plugins/ijp-toolkit/
 */

package net.sf.ij_plugins.color;

/**
 * Reference white point for 2 degree observer. Based on http://en.wikipedia.org/wiki/Standard_illuminant
 *
 * @author Jarek Sacha
 * @since 5/25/11 2:26 PM
 */
public enum WhiteReference {
    A("A", 0.44757, 0.40745, 0.45117, 0.40594, 2856),
    B("B", 0.34842, 0.35161, 0.34980, 0.35270, 4874),
    C("C", 0.31006, 0.31616, 0.31039, 0.31905, 6774),
    D50("D50", 0.34567, 0.35850, 0.34773, 0.35952, 5003),
    D55("D55", 0.33242, 0.34743, 0.33411, 0.34877, 5503),
    D65("D65", 0.31271, 0.32902, 0.31382, 0.33100, 6504),
    D75("D75", 0.29902, 0.31485, 0.29968, 0.31740, 7504),
    E("E", 1d / 3d, 1d / 3d, 1d / 3d, 1d / 3d, 5454),
    F1("F1", 0.31310, 0.33727, 0.31811, 0.33559, 6430),
    F2("F2", 0.37208, 0.37529, 0.37925, 0.36733, 4230),
    F3("F3", 0.40910, 0.39430, 0.41761, 0.38324, 3450),
    F4("F4", 0.44018, 0.40329, 0.44920, 0.39074, 2940),
    F5("F5", 0.31379, 0.34531, 0.31975, 0.34246, 6350),
    F6("F6", 0.37790, 0.38835, 0.38660, 0.37847, 4150),
    F7("F7", 0.31292, 0.32933, 0.31569, 0.32960, 6500),
    F8("F8", 0.34588, 0.35875, 0.34902, 0.35939, 5000),
    F9("F9", 0.37417, 0.37281, 0.37829, 0.37045, 4150),
    F10("F10", 0.34609, 0.35986, 0.35090, 0.35444, 5000),
    F11("F11", 0.38052, 0.37713, 0.38541, 0.37123, 4000),
    F12("F12", 0.43695, 0.40441, 0.44256, 0.39717, 3000);


    public final double x2;
    public final double y2;
    public final double x10;
    public final double y10;
    public final double cct;
    public final String name;


    WhiteReference(final String name, final double x2, final double y2,
                   final double x10, final double y10, final double cct) {
        this.name = name;
        this.x2 = x2;
        this.y2 = y2;
        this.x10 = x10;
        this.y10 = y10;
        this.cct = cct;
    }


    public double X2() {
        return x2 / y2;
    }


    public double Y() {
        return 1;
    }


    public double Z2() {
        return (1 - x2 - y2) / y2;
    }

}
