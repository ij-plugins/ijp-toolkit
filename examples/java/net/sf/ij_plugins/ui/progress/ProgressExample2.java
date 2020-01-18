/*
 * IJ-Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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

package net.sf.ij_plugins.ui.progress;

/**
 * Simple example of creating and listening to {@link net.sf.ij_plugins.ui.progress.ProgressEvent}'s.
 *
 * @author Jarek Sacha
 */
final public class ProgressExample2 {
    public static void main(String[] args) {

        // Create counter
        final CounterWithProgress4J counter = new CounterWithProgress4J('+');

        // Add progress listener
        counter.addProgressListener(e -> System.out.println("\nProgress listener: " + Math.round(e.progressPercent()) + "%"));

        // Count
        counter.count(100);
    }

}



