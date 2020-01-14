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

package net.sf.ij_plugins.filters

import java.io.File

import ij.IJ
import ij.process.FloatProcessor

object CoherenceEnhancingDiffusionBenchmark extends App {

  val file = new File("data/fingerprint.png")
  assert(file.exists(), "File exists: " + file.getAbsolutePath)
  val imp = IJ.openImage(file.getAbsolutePath)
  assert(imp != null)

  val ip = imp.getProcessor.convertToFloat().asInstanceOf[FloatProcessor]


  val n = 10
  var min = Long.MaxValue
  for (i <- 0 until n) {
    val start = System.currentTimeMillis()
    val ced = new CoherenceEnhancingDiffusion()
    ced.run(ip)
    val end = System.currentTimeMillis()
    val t = end - start
    println("Time: " + t)
    min = math.min(min, t)
  }
  println("Min Time: " + min)

}
