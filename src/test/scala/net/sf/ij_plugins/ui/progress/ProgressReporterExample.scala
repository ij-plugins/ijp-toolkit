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

package net.sf.ij_plugins.ui.progress

/**
 * Example of creating and listening to `ProgressReporter`
 */
object ProgressReporterExample extends App {

  // Create counter
  val counter = new CounterWithProgress('+')

  // Add progress listener
  counter.addProgressListener(e => println(f"\nProgress notification: ${e.progressPercent}%3.0f%%"))

  // Count
  counter.count(100)
}

/**
 * Example of using `ProgressReporter`
 */
class CounterWithProgress(marker: Char) extends ProgressReporter {

  def count(max: Int) {
    val progressIncrement = Math.max(max / 10, 1)

    println("Counting " + max + " '" + marker + "'.")

    for (i <- 1 to max) {
      print(marker)
      if (i % progressIncrement == 0) notifyProgressListeners(i, max)
    }

    println("\nCounting done.")
  }
}