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

package net.sf.ij_plugins.ui.progress

import scala.collection.mutable

/**
  * Reports progress of an operation to its listeners. Progress starts at 0.0 and finishes at 1.0,
  * so 0.3 means 30% progress.
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
trait ProgressReporter {

  private final val progressListeners: mutable.Set[ProgressListener] = mutable.Set.empty[ProgressListener]
  private var _currentProgress: Double = .0

  /**
    * Report what fraction of the processing task was already completed.
    * Progress value is between 0 and 1 (meaning 100% completed).
    * For instance, 0.25 means that 25% con the processing was completed.
    *
    * @return current progress.
    */
  def currentProgress: Double = _currentProgress

  /**
    * Add progress listener.
    *
    * @param l listener to add.
    */
  def addProgressListener(l: ProgressListener) {
    progressListeners synchronized {
      progressListeners += l
    }
  }

  /**
    * Add progress listener.
    */
  def addProgressListener[R](op: (ProgressEvent => R)) {
    progressListeners synchronized {
      progressListeners += new ProgressListener {
        override def progressNotification(e: ProgressEvent) = op(e)
      }
    }
  }

  /**
    * Remove specific progress listener.
    *
    * @param l listener to remove.
    */
  def removeProgressListener(l: ProgressListener) {
    progressListeners synchronized {
      progressListeners -= l
    }
  }

  /**
    * Remove all progress listener.
    */
  def removeAllProgressListener() {
    for (progressListener <- progressListeners) removeProgressListener(progressListener)
  }

  /**
    * Update value of `currentProgress`, value must be not less than 0 and not more than 1.
    * <br>
    * This method is used when progress needs to be updated <strong>without notifying the listeners</strong>.
    * To update progress and update listeners use `notifyProgressListeners(double)`.
    *
    * @param progress new value of `currentProgress`. The value must be not less than 0 and not more than 1.
    * @see #notifyProgressListeners()
    * @see #notifyProgressListeners(double)
    * @see #notifyProgressListeners(double, String)
    */
  private def setCurrentProgress(progress: Double) {
    require(progress >= 0 && progress <= 1, "Argument progress cannot be less than 0 or more than 1 [" + progress + "].")
    _currentProgress = progress
  }

  /**
    * Notify listeners of current value of progress.
    * Progress message is an empty string.
    *
    * @see #notifyProgressListeners(double)
    */
  protected def notifyProgressListeners() {
    progressListeners synchronized {
      if (progressListeners.nonEmpty) {
        val e = new ProgressEvent(this, currentProgress)
        progressListeners.foreach(_.progressNotification(e))
      }
    }
  }

  /**
    * Set new progress value and notify listeners.
    *
    * @param progress new value of `currentProgress`. The value must be not less than 0 and not more than 1.
    * @param message  message that will be send to listeners within `ProgressEvent`
    */
  protected def notifyProgressListeners(progress: Double, message: String) {
    setCurrentProgress(progress)
    progressListeners synchronized {
      if (progressListeners.nonEmpty) {
        val e = new ProgressEvent(ProgressReporter.this, currentProgress, message)
        progressListeners.foreach(_.progressNotification(e))
      }
    }
  }

  protected def notifyProgressListeners(progress: Double) {
    notifyProgressListeners(progress, "")
  }

  /**
    * Set new progress value and notify listeners. Progress is calculated from `count` and `max` values:
    * {{{
    *   progress = count.toDouble / max.toDouble
    * }}}
    *
    * @param message message that will be send to listeners within `ProgressEvent`
    */
  protected def notifyProgressListeners(count: Long, max: Long, message: String = "") {
    notifyProgressListeners(count.toDouble / max.toDouble, message)
  }
}


