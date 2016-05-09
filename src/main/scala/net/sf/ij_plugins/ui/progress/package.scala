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
