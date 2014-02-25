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

import ij.gui.ProgressBar;
import ij.process.ByteProcessor;

/**
 * @author Jarek Sacha
 */
interface IRunningUInt8Filter {
    /**
     * Performs filtering by applaying to all pixels the operator of given width and height. Pixels
     * outside of input image or outside the rectangular ROI defined in the input image are
     * ignored.
     *
     * @param src          input image.
     * @param filterWidth  filter width
     * @param filterHeight filter height
     * @return image containing filtered image.
     */

    ByteProcessor run(ByteProcessor src, int filterWidth, int filterHeight);

    /**
     * Assigns a progress bar to this filter. Set {@code progressBar} to {@code null} to
     * disable the progress bar.
     */

    void setProgressBar(ProgressBar progressBar);
}
