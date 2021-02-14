/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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

package ij_plugins.toolkit.ui.progress;

/**
 * Example of using {@link ProgressAccumulator}.
 *
 * @author Jarek Sacha
 */
final public class ProgressExample3 {
    public static void main(String[] args) {

        // Create counters
        CounterWithProgress4J counter1 = new CounterWithProgress4J('+');
        CounterWithProgress4J counter2 = new CounterWithProgress4J('*');

        // Create accumulator that will observer progress of both counters
        ProgressAccumulator accumulator = new ProgressAccumulator();
        // Register counters as progress reporters, use different weight for each
        System.out.println("Counter 1, weight 3, mark: " + counter1.getMarker());
        accumulator.addProgressReporter(counter1, 3, " + ");
        System.out.println("Counter 2, weight 1, mark: " + counter1.getMarker());
        accumulator.addProgressReporter(counter2, 1, " * ");

        // Request that only progress changes above minimum be reported
        accumulator.setMinimumChange(0.1);

        // Add progress listener
        accumulator.addProgressListener(new ProgressListener() {
            public void progressNotification(ProgressEvent e) {
                System.out.println("\nProgress listener: " + Math.round(e.progress() * 100) + "% "
                        + e.message());
            }
        });

        // Count using each counter in turn.
        System.out.println("Counter '+'");
        counter1.count(100);
        System.out.println("Counter '*'");
        counter2.count(100);
    }

}



