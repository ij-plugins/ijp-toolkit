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

/**
 * Computes running median of 8 bit unsigned integer values.
 *
 * @author Jarek Sacha
 */
class RunningMedianUInt8Operator implements IRunningUInt8Operator {
    final private int[] histogram = new int[256];
    private int median = 0;
    private long smallerCount = 0;
    private long largerCount = 0;
    private boolean needsUpdate = false;

    @Override
    public void add(final byte v) {
        final int vi = v & 0xFF;
        ++histogram[vi];
        if (vi < median) {
            ++smallerCount;
            needsUpdate = true;
        } else if (vi > median) {
            ++largerCount;
            needsUpdate = true;
        }
    }

    @Override
    public void remove(final byte v) {
        final int vi = v & 0xFF;

        assert histogram[vi] > 0;

        --histogram[vi];
        if (vi < median) {
            --smallerCount;
            needsUpdate = true;
        } else if (vi > median) {
            --largerCount;
            needsUpdate = true;
        }
    }

    @Override
    public boolean contains(final byte v) {
        final int vi = v & 0xFF;
        return histogram[vi] > 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = 0;
        }
        median = 0;
        smallerCount = 0;
        largerCount = 0;
        needsUpdate = false;
    }

    @Override
    public byte evaluate() {
        while (needsUpdate) {

            final int medianCount = histogram[median];

            if (smallerCount > (largerCount + medianCount)) {

                // First non-empty bin below old median bean
                while (histogram[--median] == 0) {
                    ;
                }

                // Update counts
                smallerCount -= histogram[median];
                largerCount += medianCount;

                assert smallerCount >= 0;

            } else if ((smallerCount + medianCount) <= largerCount || medianCount == 0) {

                // First non-empty bin above old median bean
                while (histogram[++median] == 0) {
                    ;
                }

                // Update counts
                smallerCount += medianCount;
                largerCount -= histogram[median];

                assert largerCount >= 0;

            } else {

                // Check if median needs to be calculated as an average between neighboring values
                final long count = smallerCount + medianCount + largerCount;
                if (count % 2 == 0 && smallerCount == (largerCount + medianCount)) {
                    int lowMedian = median;
                    // First non-empty bin below the current median bean
                    while (histogram[--lowMedian] == 0) {
                        ;
                    }
                    final int newMedian = (lowMedian + median + 1) / 2;
                    if (newMedian < median) {
                        largerCount += medianCount;
                    }
                    median = newMedian;
                }

                needsUpdate = false;
            }
        }

        return (byte) (median & 0xFF);
    }
}
