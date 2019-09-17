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

package net.sf.ij_plugins.ui

/**
 * Package `progress` contains tools for reporting progress of computations.
 * Classes can report progress extending trait [[net.sf.ij_plugins.ui.progress.ProgressReporter]].
 * Progress can be observed extendfing trait [[net.sf.ij_plugins.ui.progress.ProgressListener]].
 *
 *
 * Example usage:
 *
 * {{{
 * class CounterWithProgress(marker: Char) extends ProgressReporter {
 *   def count(max: Int) {
 *     val progressIncrement = Math.max(max / 10, 1)
 *
 *     println("Counting " + max + " '" + marker + "'.")
 *
 *     for (i <- 1 to max) {
 *       print(marker)
 *       if (i % progressIncrement == 0) notifyProgressListeners(i, max)
 *     }
 *
 *     println("\nCounting done.")
 *   }
 * }
 *
 * object ProgressReporterExample extends App {
 *   // Create counter
 *   val counter = new CounterWithProgress('+')
 *
 *   // Add progress listener
 *   counter.addProgressListener(e => println(f"\nProgress notification: ${e.progressPercent}%3.0f%%"))
 *
 *   // Count
 *   counter.count(100)
 * }
 * }}}
 */
package object progress {

}
