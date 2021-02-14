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
 * Example of {@link ProgressReporter4J}
 *
 * @author Jarek Sacha
 */
public class CounterWithProgress4J extends ProgressReporter4J {
    private final char marker;

    public CounterWithProgress4J(char marker) {
        this.marker = marker;
    }

    public char getMarker() {
        return marker;
    }

    public void count(final int max) {

        // Calculate increment, make sure that different/larger than 0 otherwise '%' operation will fail.
        final int progressIncrement = Math.max(max / 10, 1);

        System.out.println("Counting " + max + " '" + marker + "'.");
        for (int i = 0; i < max; i++) {
            System.out.print(marker);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i % progressIncrement == 0) {
                this.notifyProgressListeners(i / (double) max);
            }
        }
        this.notifyProgressListeners(1);
        System.out.println("\nCounting done.");
    }
}
