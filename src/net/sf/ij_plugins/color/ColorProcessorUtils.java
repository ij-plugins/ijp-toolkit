/*
 * Image/J Plugins
 * Copyright (C) 2002-2009 Jarek Sacha
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
package net.sf.ij_plugins.color;

import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import net.sf.ij_plugins.util.Validate;

/**
 * <p/>
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @since Apr 5, 2005 9:20:35 PM
 */
public class ColorProcessorUtils {
    private ColorProcessorUtils() {
    }

    /**
     * Splits ColorProcessor into ByteProcessors representing each of three bands (red, gree, and blue).
     *
     * @param cp input color processor
     * @return ByteProcessor for each band.
     * @see #mergeRGB(ij.process.ByteProcessor[])
     */
    static public ByteProcessor[] splitRGB(final ColorProcessor cp) {
        final int width = cp.getWidth();
        final int height = cp.getHeight();

        final ByteProcessor redBp = new ByteProcessor(width, height);
        final ByteProcessor greenBp = new ByteProcessor(width, height);
        final ByteProcessor blueBp = new ByteProcessor(width, height);

        final byte[] redPixels = (byte[]) redBp.getPixels();
        final byte[] greenPixels = (byte[]) greenBp.getPixels();
        final byte[] bluePixels = (byte[]) blueBp.getPixels();

        cp.getRGB(redPixels, greenPixels, bluePixels);

        return new ByteProcessor[]{redBp, greenBp, blueBp};
    }

    /**
     * Merges RGB bands into a ColorProcessor.
     *
     * @param bps ByteProcessor for red, green, and blue band.
     * @return merged bands
     */
    static public ColorProcessor mergeRGB(final ByteProcessor[] bps) {

        Validate.argumentNotNull(bps, "bps");
        if (bps.length != 3) {
            throw new IllegalArgumentException("Size of array 'bps' has to equal 3, got "
                    + bps.length + ".");
        }

        final int width = bps[0].getWidth();
        final int height = bps[0].getHeight();

        if (!(width == bps[1].getWidth() && height == bps[1].getHeight()
                && width == bps[2].getWidth() && height == bps[2].getHeight())) {
            throw new IllegalArgumentException("All imput processor have to be of the same size.");
        }

        byte[][] pixels = new byte[3][];
        for (int i = 0; i < bps.length; i++) {
            pixels[i] = (byte[]) bps[i].getPixels();
        }

        ColorProcessor dest = new ColorProcessor(width, height);

        dest.setRGB(pixels[0], pixels[1], pixels[2]);

        return dest;
    }
}
