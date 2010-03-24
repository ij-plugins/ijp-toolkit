/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

package net.sf.ij_plugins.im3d.grow;

import ij.process.ByteProcessor;
import net.sf.ij_plugins.util.Pair;


/**
 * @author Jarek Sacha
 * @since Oct 22, 2009
 */
final class SRGSupport {

    static final int MAX_REGION_NUMBER = 253;
    static final byte BACKGROUND_MARK = (byte) 0x00;
    static final byte CANDIDATE_MARK = (byte) 0xff;
    static final byte OUTSIDE_MARK = (byte) 0xfe;


    int[] seedToRegionLookup;


    Pair<int[], Integer> createSeedToRegionLookup(final int[] histogram) {

        final int[] regionToSeedLookup = new int[MAX_REGION_NUMBER + 1];
        seedToRegionLookup = new int[MAX_REGION_NUMBER + 1];
        int regionCount = 0;
        for (int seed = 1; seed < histogram.length; seed++) {
            if (histogram[seed] > 0) {
                if (seed > MAX_REGION_NUMBER) {
                    throw new IllegalArgumentException("Seed ID cannot be larger than " + MAX_REGION_NUMBER
                            + ", got " + seed + ".");
                }

                regionCount++;
                seedToRegionLookup[seed] = regionCount;
                regionToSeedLookup[regionCount] = seed;
            }

        }

        return new Pair<int[], Integer>(regionToSeedLookup, regionCount);
    }


    static void fillOutsideMask(final byte[] pixels, final byte value, final ByteProcessor mask) {
        if (mask != null) {
            final byte[] maskPixels = (byte[]) mask.getPixels();
            for (int i = 0; i < pixels.length; i++) {
                if (maskPixels[i] == 0) {
                    pixels[i] = value;
                }
            }
        }
    }
}
