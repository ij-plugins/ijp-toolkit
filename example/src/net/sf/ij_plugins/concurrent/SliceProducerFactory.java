/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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

package net.sf.ij_plugins.concurrent;

import ij.ImagePlus;


/**
 * @author Jarek Sacha
 * @since 3/8/11 9:41 PM
 */
final class SliceProducerFactory {

    private SliceProducerFactory() {
    }


    public static BlitterSP[] create(final ImagePlus imp1, final ImagePlus imp2) {

        // Validate size
        if (imp1.getWidth() != imp2.getWidth() || imp1.getHeight() != imp2.getHeight() || imp1.getStackSize() != imp2.getStackSize()) {
            throw new IllegalArgumentException("Images must have the same size, got imp1=" + imp1 + ", imp2=" + imp2);
        }

        // Create SliceProducers
        final BlitterSP[] producers = new BlitterSP[imp1.getNSlices()];
        for (int i = 0; i < imp1.getNSlices(); ++i) {
            producers[i] = new BlitterSP(
                    imp1.getStack().getProcessor(i + 1),
                    imp2.getStack().getProcessor(i + 1));
        }

        return producers;
    }

}
