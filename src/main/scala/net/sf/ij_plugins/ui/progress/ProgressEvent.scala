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

/**
  * Event used to notify listeners about current value of progress.
  *
  * Allowed `progress` values are between 0.0 and 1.0. 0.31 means 31% progress.
  */
class ProgressEvent(val source: Option[ProgressReporter],
                    val progress: Double,
                    val message: String) {
  require(0 <= progress && progress <= 1, s"Progress=$progress must be in between 0 and 1.")

  def this(source: ProgressReporter, progress: Double, message: String) = this(Option(source), progress, message)

  def this(source: ProgressReporter, progress: Double) = this(Option(source), progress, "")

  def this(progress: Double) = this(None, progress, "")

  def this(progress: Double, message: String) = this(None, progress, message)

  def progressPercent: Double = progress * 100
}