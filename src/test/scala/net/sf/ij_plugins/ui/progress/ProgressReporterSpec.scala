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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

/**
  * @author Jarek Sacha 
  */
class ProgressReporterSpec extends AnyFlatSpec with should.Matchers {

  private class CounterWithProgress extends ProgressReporter {
    def count(max: Int) {
      for (i <- 1 to max)
        notifyProgressListeners(i, max)
    }
  }

  "ProgressReporter" should "accept progress listener as a lambda" in {

    var testCounter = 0
    testCounter should equal(0)

    val c = new CounterWithProgress()
    c.addProgressListener(_ => testCounter += 1)

    c.count(7)
    testCounter should equal(7)
  }

  "ProgressReporter" should "accept progress listener as an instance of ProgressListener" in {

    var testCounter = 0
    testCounter should equal(0)

    val c = new CounterWithProgress()
    c.addProgressListener(new ProgressListener {
      override def progressNotification(e: ProgressEvent): Unit = {
        testCounter += 2
      }
    })

    c.count(7)
    testCounter should equal(14)
  }
}
