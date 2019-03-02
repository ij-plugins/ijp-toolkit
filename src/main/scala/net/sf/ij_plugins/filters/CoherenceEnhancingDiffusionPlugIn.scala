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

package net.sf.ij_plugins.filters

import java.awt.AWTEvent
import java.util.concurrent.atomic.AtomicBoolean

import ij.gui.{DialogListener, GenericDialog}
import ij.plugin.filter.PlugInFilter._
import ij.plugin.filter.{ExtendedPlugInFilter, PlugInFilterRunner}
import ij.process.{Blitter, FloatProcessor, ImageProcessor}
import ij.{IJ, ImagePlus}
import net.sf.ij_plugins.filters.CoherenceEnhancingDiffusionPlugIn._
import net.sf.ij_plugins.ui.progress.{ProgressEvent, ProgressListener}


object CoherenceEnhancingDiffusionPlugIn {
  val FLAGS: Int =
    DOES_8G |
      DOES_16 |
      DOES_32 |
      PARALLELIZE_STACKS |
      ExtendedPlugInFilter.KEEP_PREVIEW

  private val CONFIG = CoherenceEnhancingDiffusion.Config()
  private val debugMode = new AtomicBoolean(false)
  private val TITLE = "Coherence Enhancing Diffusion (ijp-toolkit2)"
}


/**
 * ImageJ plugin that runs `CoherenceEnhancingDiffusion` filter.
 */
final class CoherenceEnhancingDiffusionPlugIn extends ExtendedPlugInFilter with DialogListener {

  private var imp: ImagePlus = null
  private var nPasses = 0
  private var passCount = 0

  def setup(arg: String, imp: ImagePlus): Int = {
    this.imp = imp
    FLAGS
  }

  def showDialog(imp: ImagePlus, command: String, pfr: PlugInFilterRunner): Int = {
    val dialog = new GenericDialog(TITLE) {
      addNumericField("Lambda (>0), limit of diffusion", CONFIG.lambda, 6, 8, "")
      addNumericField("Sigma (>0), smooth for first derivative", CONFIG.sigma, 6, 8, "")
      addNumericField("Rho (>0), smooth for second derivative", CONFIG.rho, 6, 8, "")
      addNumericField("Step_size (<0.25)", CONFIG.stepSize, 6, 8, "")
      addNumericField("m (>1), best keep it equal to 1", CONFIG.m, 6, 8, "")
      addNumericField("Number_of_steps", CONFIG.numberOfSteps, 0)
      addCheckbox("Show_debug_data", debugMode.get)
      addHelp("http://ij-plugins.sourceforge.net/plugins/filters/index.html")
      addPreviewCheckbox(pfr)
    }
    dialog.addDialogListener(this)
    dialog.showDialog()
    if (dialog.wasCanceled) return DONE

    IJ.setupDialog(imp, FLAGS)
  }

  def dialogItemChanged(gd: GenericDialog, e: AWTEvent): Boolean = {
    CONFIG synchronized {
      CONFIG.lambda = gd.getNextNumber
      CONFIG.sigma = gd.getNextNumber
      CONFIG.rho = gd.getNextNumber
      CONFIG.stepSize = gd.getNextNumber
      CONFIG.m = gd.getNextNumber
      CONFIG.numberOfSteps = Math.round(gd.getNextNumber).toInt
    }
    debugMode.set(gd.getNextBoolean)
    true
  }

  def setNPasses(nPasses: Int) {
    this.nPasses = nPasses
    this.passCount = 0
  }

  def run(ip: ImageProcessor) {
    passCount += 1
    val statsMessage = if (nPasses > 1) TITLE + " - pass " + passCount + "/" + nPasses + ". " else TITLE
    IJ.showStatus(statsMessage)
    val src = ip.convertToFloat.asInstanceOf[FloatProcessor]
    val filter = new CoherenceEnhancingDiffusion(CONFIG)
    val progressListener = new ProgressListener {
      def progressNotification(e: ProgressEvent) {
        IJ.showProgress(Math.round(e.progress * 100).asInstanceOf[Int], 100)
        IJ.showStatus(statsMessage + e.message)
      }
    }
    filter.addProgressListener(progressListener)
    val dest = filter.run(src)
    filter.removeProgressListener(progressListener)
    if (debugMode.get) {
      filter.alpha.resetMinAndMax()
      new ImagePlus("alpha", filter.alpha).show()
      filter.c2.resetMinAndMax()
      new ImagePlus("c2", filter.c2).show()
    }
    ip.copyBits(dest, 0, 0, Blitter.COPY)
  }

}

