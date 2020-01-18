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

import java.lang.reflect.InvocationTargetException

import ij.IJ
import javax.swing._


/**
  * Simple adapter for attaching a [[net.sf.ij_plugins.ui.progress.ProgressListener ProgressReporter]] to ImageJ's progress bar.
  *
  * Example use (in Java syntax):
  * {{{
  *   final ProgressReporter r = ...;
  *   final IJProgressBarAdapter progressBarAdapter = new IJProgressBarAdapter();
  *   r.addProgressListener(progressBarAdapter);
  *   ...
  *   r.removeProgressListener(progressBarAdapter);
  * }}}
  * Always remember to remove listeners when processing is done to avoid memory leaks.
  *
  * @author Jarek Sacha
  */
class IJProgressBarAdapter extends ProgressListener {

  override def progressNotification(e: ProgressEvent): Unit = {
    if (SwingUtilities.isEventDispatchThread) {
      showProgress(e)
    } else {
      try {
        SwingUtilities.invokeAndWait(() => showProgress(e))
      }
      catch {
        case ex: InterruptedException => IJ.log("InterruptedException " + ex.getMessage)
        case ex: InvocationTargetException => IJ.log("InvocationTargetException" + ex.getMessage)
      }
    }
  }

  private def showProgress(e: ProgressEvent): Unit = {
    IJ.showStatus(e.message)
    IJ.showProgress(e.progress)
  }
}

