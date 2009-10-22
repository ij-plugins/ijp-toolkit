/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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
package net.sf.ij_plugins.util.progress;

/**
 * Example of using {@link ProgressAccumulator}.
 *
 * @author Jarek Sacha
 */
final public class ProgressExample3 {
    public static void main(String[] args) {

        // Create counters
        CounterWithProgress counter1 = new CounterWithProgress('+');
        CounterWithProgress counter2 = new CounterWithProgress('*');

        // Create accumulator that will observer progress of both counters
        ProgressAccumulator accumulator = new ProgressAccumulator();
        // Register counters as progress reporters, use different weight for each
        accumulator.addProgressReporter(counter1, 3, " + ");
        accumulator.addProgressReporter(counter2, 1, " * ");

        // Request that only progress changes above minimum be reported
        accumulator.setMinimumChange(0.1);

        // Add progress listener
        accumulator.addProgressListener(new ProgressListener() {
            public void progressNotification(ProgressEvent e) {
                System.out.println("\nProgress listener: " + Math.round(e.getProgress() * 100) + "% "
                        + e.getMessage());
            }
        });

        // Count using each counter in turn.
        System.out.println("Counter '+'");
        counter1.count(100);
        System.out.println("Counter '*'");
        counter2.count(100);
    }

}



