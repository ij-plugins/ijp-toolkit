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

package net.sf.ij_plugins.ui.progress


import net.sf.ij_plugins.util.Validate

import scala.collection.mutable


/**
  * Utility for aggregating progress events from multiple
  * [[net.sf.ij_plugins.ui.progress.ProgressListener ProgressReporter]]'s
  *
  * @author Jarek Sacha
  */
object ProgressAccumulator {

  private case class Data(weight: Double, message: String)

}

class ProgressAccumulator extends ProgressReporter with ProgressListener {
  private val _reporters = new mutable.LinkedHashMap[ProgressReporter, ProgressAccumulator.Data]
  private var _minimumChange: Double = 0.01
  private var _lastReportedProgress: Double = -1

  def minimumChange: Double = _minimumChange

  def minimumChange_=(minimumChange: Double): Unit = {
    require(0 <= minimumChange && minimumChange <= 1,
      s"Argument 'minimumChange' cannot be less than 0 or more than 1 $minimumChange].")
    this._minimumChange = minimumChange
  }

  /** Java-convention compatible setter for minimumChange */
  def setMinimumChange(minimumChange: Double): Unit = {
    this.minimumChange = minimumChange
  }

  /**
    * Add progress reporter with default weight of `1`. If reporter already exists its
    * weight will be changed to `1`.
    *
    * @param reporter reporter
    */
  def addProgressReporter(reporter: ProgressReporter): Unit = {
    addProgressReporter(reporter, 1)
  }

  /**
    * Add progress `reporter` with given `weight`. If reporter already exists
    * its `weight` and `message` will be updated.
    *
    * @param reporter reporter
    * @param weight   weight
    */
  def addProgressReporter(reporter: ProgressReporter, weight: Double): Unit = {
    addProgressReporter(reporter, weight, null)
  }

  /**
    * Add progress `reporter` with given `weight`. If reporter already exists
    * its `weight` and `message` will be updated.
    *
    * @param reporter reporter
    * @param weight   weight
    * @param message  message that will be reported when this reporter send progress event. If
    *                 `null` the original message send by reporter will be used.
    */
  def addProgressReporter(reporter: ProgressReporter, weight: Double, message: String): Unit = {
    if (reporter == null) return
    _reporters.synchronized {
      if (!_reporters.contains(reporter)) {
        reporter.addProgressListener(this)
      }
      _reporters.put(reporter, ProgressAccumulator.Data(weight, message))
    }
  }

  def removeProgressReporter(reporter: ProgressReporter): Unit = {
    _reporters.synchronized {
      _reporters.remove(reporter)
      reporter.removeProgressListener(this)
    }
  }

  def removeAllProgressReporter(): Unit = {
    _reporters.synchronized {
      for (progressReporter <- _reporters.keySet) {
        progressReporter.removeProgressListener(this)
      }
      _reporters.clear()
    }
  }

  override def progressNotification(event: ProgressEvent): Unit = {
    Validate.argumentNotNull(event, "event")

    val source = event.source match {
      case Some(s) => s
      case None =>
        throw new IllegalArgumentException("Event source cannot be empty.")
    }

    if (!_reporters.contains(source))
      throw new RuntimeException("Received notification from unregistered reporter: " + source)

    // Sum all weight
    var weightSum: Double = 0
    var progressSum: Double = 0
    for ((reporter, data) <- _reporters) {
      weightSum += data.weight
      val progress = reporter.currentProgress
      progressSum += progress * data.weight
    }
    assert(progressSum >= 0)
    assert(weightSum > 0)

    val progress = progressSum / weightSum
    val data = _reporters(source)
    val message = Option(data.message).getOrElse(event.message)

    if ((progress - _lastReportedProgress) > minimumChange) {
      _lastReportedProgress = progress
      notifyProgressListeners(progress, message)
    } else {
      setCurrentProgress(progress)
    }
  }
}

