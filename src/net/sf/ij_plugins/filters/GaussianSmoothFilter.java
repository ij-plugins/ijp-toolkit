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
package net.sf.ij_plugins.filters;

import ij.plugin.filter.GaussianBlur;
import ij.process.ImageProcessor;


/**
 * @author Jarek Sacha
 */
public class GaussianSmoothFilter {
    private double standardDeviation = 1;

    /**
     * @return Gaussian kernel standard deviation.
     */
    public double getStandardDeviation() {
        return standardDeviation;
    }

    /**
     * @param standardDeviation new Gaussian kernel standard deviation.
     */
    public void setStandardDeviation(final double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    /**
     * @return effective radius of the Gaussian kernel.
     */
    public double kernelRadius() {
        return standardDeviation * 2;
    }

    /**
     * Run Gaussian smooth filtering.
     *
     * @param ip input image.
     * @return filtered image.
     */
    public ImageProcessor run(final ImageProcessor ip) {
        final ImageProcessor dest = ip.duplicate();
        runInPlace(dest);

        return dest;
    }

    /**
     * Run Gaussian smooth filtering in place.
     *
     * @param ip image to be filtered.
     */
    public void runInPlace(final ImageProcessor ip) {
        final GaussianBlur gaussianBlur = new GaussianBlur();
        gaussianBlur.blur(ip, kernelRadius());
    }
}
