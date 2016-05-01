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

package net.sf.ij_plugins.filters

import ij.plugin.filter.GaussianBlur
import ij.process.{Blitter, FloatBlitter, FloatProcessor}
import ij.{ImagePlus, IJ}
import java.io.File
import net.sf.ij_plugins.filters.CoherenceEnhancingDiffusion._
import net.sf.ij_plugins.ui.progress.ProgressReporter
import org.apache.commons.math3.util.FastMath
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


object CoherenceEnhancingDiffusion {

  case class Config(var lambda: Double = 1,
                    var sigma: Double = 3,
                    var rho: Double = 5,
                    var stepSize: Double = 0.24,
                    var m: Double = 1d,
                    var numberOfSteps: Int = 25)

  def run(src: FloatProcessor, config:Config ): FloatProcessor = {
    new CoherenceEnhancingDiffusion(config).run(src)
  }
}

/**
 * Coherence enhancing diffusion filter based on filter described in J. Weickert,
 * "Coherence-Enhancing Diffusion Filtering", <i>International Journal of Computer Vision</i>, 1999, vol.31, p.111-127.
 */
class CoherenceEnhancingDiffusion(config: Config = Config()) extends ProgressReporter {
  private final val Eps: Double = Math.pow(2.0, -52.0)
  private final val Gamma: Double = 0.01
  private final val GradientKernel: Array[Float] = Array(-0.5f, 0.0f, 0.5f)
  private final val dumpFilenamePrefix: String = "tmp/cef"
  private var sizeX: Int = 0
  private var sizeY: Int = 0
  private var dumpEnabled: Boolean = false
  private var _alpha: FloatProcessor = null
  private var _c2: FloatProcessor = null

  /**
   * Perform filtering.
   * @param src source
   * @return filtered image
   */
  def run(src: FloatProcessor): FloatProcessor = {
    notifyProgressListeners(0, "starting")

    sizeY = src.getHeight
    sizeX = src.getWidth

    val dest = src.duplicate.asInstanceOf[FloatProcessor]
    for (i <- 0 until config.numberOfSteps) {
      notifyProgressListeners(i, config.numberOfSteps, s" step ${i + 1}")

      // Do parallel computations
      // `vF = ... ` variables are created to start parallel computations as early as possible
      // `v <- vF` values from features are extracted only when needed, since `<-` blocks
      val f = for {
      // Gaussian smoothing (sigma)
        filtImage <- Future(gaussian(dest, config.sigma))

        // Gradient in x and y direction
        gradxImF = gradientXF(filtImage)
        gradyImF = gradientYF(filtImage)
        gradxIm <- gradxImF
        // Start `s11` early since it only depends on `gradxIm`, it can be computed while waiting for `gradyIm`
        s11F = Future(multiplyAndSmooth(gradxIm, gradxIm, config.rho))
        gradyIm <- gradyImF
        s12F = Future(multiplyAndSmooth(gradxIm, gradyIm, config.rho))
        s22F = Future(multiplyAndSmooth(gradyIm, gradyIm, config.rho))
        s11 <- s11F
        s12 <- s12F
        s22 <- s22F

        alpha = calcAlpha(s11, s12, s22)
        c2 = calcC2(alpha, config.lambda)
        c1 = Gamma

        ddF = Future(calcDD(s11, s22, alpha, c2, c1))
        d12F = Future(calcD12(s12, alpha, c2, c1))
        dd <- ddF
        d11F = Future(calcD11(dd, c2, c1))
        d22F = Future(calcD22(dd, c2, c1))
        d11 <- d11F
        d12 <- d12F
        d22 <- d22F
      } yield {
        dumpImage("sigma_smooth", i, filtImage)
        dumpImage("gradx", i, gradxIm)
        dumpImage("grady", i, gradyIm)
        dumpImage("s11", i, s11)
        dumpImage("s12", i, s12)
        dumpImage("s22", i, s22)
        dumpImage("alpha", i, alpha)
        dumpImage("c2", i, c2)
        dumpImage("dd", i, dd)
        dumpImage("d11", i, d11)
        dumpImage("d12", i, d12)
        dumpImage("d22", i, d22)
        this._alpha = alpha
        this._c2 = c2
        diffusionStep(dest, d11, d12, d22, config.stepSize)
      }
      dumpImage("diffusionStep", i, dest)

      // Wait till computation step is completed since the next step will need the result
      Await.result(f, Duration.Inf)
    }

    notifyProgressListeners(1)

    dest
  }

  private[this] def gradientXF(src: FloatProcessor): Future[FloatProcessor] = Future {
    val dest = src.duplicate.asInstanceOf[FloatProcessor]
    dest.convolve(GradientKernel, GradientKernel.length, 1)
    dest
  }

  private[this] def gradientYF(src: FloatProcessor): Future[FloatProcessor] = Future {
    val dest = src.duplicate.asInstanceOf[FloatProcessor]
    dest.convolve(GradientKernel, 1, GradientKernel.length)
    dest
  }

  private[this] def gaussian(src: FloatProcessor, sigma: Double): FloatProcessor = {
    val dest = src.duplicate.asInstanceOf[FloatProcessor]
    val gaussianBlur = new GaussianBlur()
    gaussianBlur.showProgress(false)
    gaussianBlur.blurGaussian(dest, sigma, sigma, 0.0001)
    dest
  }

  private[this] def calcAlpha(s11: FloatProcessor, s12: FloatProcessor, s22: FloatProcessor): FloatProcessor = {
    val width = s11.getWidth
    val height = s11.getHeight
    val dest = new FloatProcessor(width, height)

    for (x <- (0 until sizeX).par) {
      for (y <- 0 until height) {
        val a = s11.getf(x, y) - s22.getf(x, y)
        val b = s12.getf(x, y)
        val v = Math.sqrt(a * a + 4 * b * b)
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  def alpha: FloatProcessor = _alpha

  def c2: FloatProcessor = _c2

  private def isDumpEnabled: Boolean = dumpEnabled

  private def setDumpEnabled(dumpEnabled: Boolean) {
    this.dumpEnabled = dumpEnabled
  }

  private[this] def multiplyAndSmooth(ip1: FloatProcessor, fp2: FloatProcessor, sigma: Double): FloatProcessor = {
    val dest = ip1.duplicate.asInstanceOf[FloatProcessor]
    val blitter = new FloatBlitter(dest)
    blitter.copyBits(fp2, 0, 0, Blitter.MULTIPLY)
    gaussian(dest, sigma)
  }


  private[this] def diffusionStep(image: FloatProcessor, c: FloatProcessor, b: FloatProcessor, a: FloatProcessor, step: Double): Double = {
    //y = .5* ( (c_cop).*xop + (a_amo).*xmo - (a_amo + a_apo + c_com + c_cop).*x + (a_apo).*xpo + (c_com).*xom) ...
    //   + .25* ( -1*( (bmo+bop).*xmp + (bpo+bom).*xpm ) + (bpo+bop).*xpp + (bmo+bom).*xmm );
    for (i <- (0 until sizeX).par) {
      for (j <- 0 until sizeY) {
        val currentValue = image.getf(i, j)
        val firstDeriv = (getPixel(i, j, c) + getPixel(i, j - 1, c)) * getPixel(i, j - 1, image) +
          (getPixel(i + 1, j, a) + getPixel(i, j, a)) * getPixel(i + 1, j, image) -
          (getPixel(i - 1, j, a) + getPixel(i + 1, j, a) + 2.0 * getPixel(i, j, a)) * getPixel(i, j, image) -
          (getPixel(i, j - 1, c) + getPixel(i, j + 1, c) + 2.0 * getPixel(i, j, c)) * getPixel(i, j, image) +
          (getPixel(i - 1, j, a) + getPixel(i, j, a)) * getPixel(i - 1, j, image) +
          (getPixel(i, j, c) + getPixel(i, j + 1, c)) * getPixel(i, j + 1, image)
        val secondDeriv = (getPixel(i - 1, j, b) + getPixel(i, j - 1, b)) * getPixel(i - 1, j - 1, image) +
          (getPixel(i + 1, j, b) + getPixel(i, j + 1, b)) * getPixel(i + 1, j + 1, image) -
          (getPixel(i + 1, j, b) + getPixel(i, j - 1, b)) * getPixel(i + 1, j - 1, image) -
          (getPixel(i - 1, j, b) + getPixel(i, j + 1, b)) * getPixel(i - 1, j + 1, image)
        val adder = step * (firstDeriv / 2 + secondDeriv / 4)
        val v = currentValue + adder
        image.setf(i, j, v.toFloat)
      }
    }

    image.getf(0, 0)
  }

  private[this] def calcC2(alpha: FloatProcessor, lambda: Double): FloatProcessor = {
    val dest = new FloatProcessor(sizeX, sizeY)
    val Cm = 7.2848
    val powerOfOne = Math.abs(config.m - 1d) < Float.MinPositiveValue
    for (x <- (0 until sizeX).par) {
      for (y <- 0 until sizeY) {
        val a = alpha.getf(x, y)
        val h1 = (a + Eps) / lambda
        val h2 = if (powerOfOne) h1 else FastMath.pow(h1, config.m)
        val h3 = FastMath.exp(-Cm / h2)
        val v = Gamma + (1 - Gamma) * h3
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  private[this] def calcDD(s11: FloatProcessor, s22: FloatProcessor, alpha: FloatProcessor, c2: FloatProcessor, c1: Double): FloatProcessor = {
    val dest = new FloatProcessor(sizeX, sizeY)
    for (x <- (0 until sizeX).par) {
      for (y <- 0 until sizeY) {
        val v = (c2.getf(x, y) - c1) * (s11.getf(x, y) - s22.getf(x, y)) / (alpha.getf(x, y) + Eps)
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  private[this] def calcD11(dd: FloatProcessor, c2: FloatProcessor, c1: Double): FloatProcessor = {
    val dest = new FloatProcessor(sizeX, sizeY)
    for (x <- (0 until sizeX).par) {
      for (y <- 0 until sizeY) {
        val v = 0.5 * (c1 + c2.getf(x, y) + dd.getf(x, y))
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  private[this] def calcD12(s12: FloatProcessor, alpha: FloatProcessor, c2: FloatProcessor, c1: Double): FloatProcessor = {
    val dest = new FloatProcessor(sizeX, sizeY)
    for (x <- (0 until sizeX).par) {
      for (y <- 0 until sizeY) {
        val v = ((c1 - c2.getf(x, y)) * s12.getf(x, y)) / (alpha.getf(x, y) + Eps)
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  private[this] def calcD22(dd: FloatProcessor, c2: FloatProcessor, c1: Double): FloatProcessor = {
    val dest: FloatProcessor = new FloatProcessor(sizeX, sizeY)
    for (x <- (0 until sizeX).par) {
      for (y <- 0 until sizeY) {
        val v = 0.5 * (c1 + c2.getf(x, y) - dd.getf(x, y))
        dest.setf(x, y, v.toFloat)
      }
    }
    dest
  }

  private[this] def getPixel(x: Int, y: Int, fp: FloatProcessor): Double = {
    val x2 = math.max(0, math.min(x, sizeX - 1))
    val y2 = math.max(0, math.min(y, sizeY - 1))
    fp.getf(x2, y2)
  }

  private[this] def dumpImage(name: String, iteration: Int, fp: FloatProcessor) {
    if (dumpEnabled) {
      val fileName = "%s_%s_%04d.tif".format(dumpFilenamePrefix, name, iteration)
      val file = new File(fileName).getAbsoluteFile
      file.getParentFile.mkdirs()
      IJ.log("Saving " + file)
      IJ.saveAsTiff(new ImagePlus("", fp), file.getPath)
    }
  }
}
