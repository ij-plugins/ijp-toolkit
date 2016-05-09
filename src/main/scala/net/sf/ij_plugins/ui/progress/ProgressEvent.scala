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
 * Event used to notify listeners about current value of progress. Allowed `progress` values are between 0.0 and 1.0.
 * 0.31 means 31% progress.
 */
class ProgressEvent(val source: Any,
                    val progress: Double,
                    val message: String) {
  require(0 <= progress && progress <= 1, s"Progress=$progress must be in between 0 and 1.")

  def this(source: Any, progress: Double) = this(source, progress, "")

  def progressPercent = progress * 100
}