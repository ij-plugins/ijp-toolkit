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


import java.util.Arrays;

/**
 * @author Jarek Sacha
 */
public class RunningMedianOperator implements IRunningMedianFloatOperator {
    private Packet[] packets;
    private int updatablePacket = 0;
    private float median = 0;
    private boolean needsUpdate = false;
    private int lowerSetSize = 0;
    private int upperSetSize = 0;
    private boolean revaluateMedian = false;

    public RunningMedianOperator() {
    }

    @Override
    public void reset(final int maxPackets, final int maxElementsPerPacket) {
        packets = new Packet[maxPackets];
        for (int i = 0; i < packets.length; ++i) {
            packets[i] = new Packet(maxElementsPerPacket);
        }

        // Just to ensure that initial state is constistent with 'clear' state
        clear();
    }


    @Override
    public void push(final int length, final float[] data) {

        final Packet packet = packets[updatablePacket];

        // Check input parameters
        if (length < 0 || length > packet.data.length) {
            throw new IllegalArgumentException("Argument 'length' out of range, got "
                    + length + ", the range is [" + 0 + "," + packet.data.length + "].");
        }
        if (data == null && length != 0) {
            throw new IllegalArgumentException("Argument 'data' cannot be 'null' when argument "
                    + "'length' is non zero.");

        }
        if (data.length < length) {
            throw new IllegalArgumentException("Size of argument 'data' cannot be less than "
                    + "value of argument 'length'.");

        }

        // Process numbers being removed
        for (int i = 0; i < packet.size; ++i) {
            final float v = packet.data[i];
            if (v < median) {
                --lowerSetSize;
            } else {
                --upperSetSize;
            }
            if (median == v) {
                revaluateMedian = true;
            }
        }


        // Process numbers being added
//        System.arraycopy(data, 0, packet.data, 0, length);
        for (int i = 0; i < length; ++i) {
            final float v = data[i];
            packet.data[i] = v;
            if (v < median) {
                ++lowerSetSize;
            } else {
                ++upperSetSize;
            }
        }

        Arrays.sort(packet.data, 0, length);
        packet.size = length;
        packet.split = 0;
        while (packet.split < packet.size && packet.data[packet.split] < median) {
            ++packet.split;
        }

        needsUpdate = true;
        updatablePacket = (updatablePacket + 1) % packets.length;
    }

    @Override
    public float evaluate() {
        while (needsUpdate) {

            if (revaluateMedian) {
                updateSetCounts();
            }

            if (lowerSetSize > upperSetSize) {

                // Move values between upper and lower sets
                final float movedValue = moveOneUp();

//                assert movedValue <= median;
//                assert !Float.isNaN(movedValue);

                if (Float.isNaN(movedValue)) {
                    needsUpdate = false;
                } else {
                    --lowerSetSize;
                    ++upperSetSize;

                    median = movedValue;

//                    assert movedValue <= median;
//                    assert !Float.isNaN(movedValue);
//                    assert lowerSetSize >= 0;
                }


            } else if (lowerSetSize < upperSetSize - 1) {

                // Move values between upper and lower sets
                final float movedValue = moveOneDown();

                if (Float.isNaN(movedValue)) {
                    needsUpdate = false;
                } else {


                    ++lowerSetSize;
                    --upperSetSize;

                    median = movedValue;

                    assert movedValue >= median;
                    assert !Float.isNaN(movedValue);
                    assert upperSetSize >= 0;
                }

            } else {

                if (revaluateMedian) {

                    median = findSmallestInUpperSet();
                    revaluateMedian = false;
                }

                needsUpdate = false;
            }
        }

        return median;
    }

    @Override
    public void clear() {
        updatablePacket = 0;
        median = 0;
        needsUpdate = false;
        lowerSetSize = 0;
        upperSetSize = 0;
        revaluateMedian = false;

        for (final Packet packet : packets) {
            packet.size = 0;
            packet.split = 0;
            Arrays.fill(packet.data, 0);
        }
    }


    private void updateSetCounts() {
        lowerSetSize = 0;
        upperSetSize = 0;
        for (final Packet packet : packets) {
            lowerSetSize += packet.split;
            upperSetSize += packet.size - packet.split;
        }
    }

    private float findSmallestInUpperSet() {
        // Find largest key in the lower set
        float minValue = Float.POSITIVE_INFINITY;
        for (final Packet packet : packets) {
            if (packet.split < packet.size && packet.data[packet.split] < minValue) {
                minValue = packet.data[packet.split];
            }
        }


        return Float.isInfinite(minValue) ? Float.NaN : minValue;

    }

    private float moveOneUp() {
        // Find largest key in the lower set
        int selection = -1;
        float firstLargest = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < packets.length; ++i) {
            final Packet packet = packets[i];
            if (packet.split > 0
                    && packet.data[packet.split - 1] > firstLargest) {
                selection = i;
                firstLargest = packet.data[packet.split - 1];
            }
        }


        if (selection >= 0) {
            final Packet packet = packets[selection];
            --packet.split;
            final float movedValue = packet.data[packet.split];

            assert firstLargest == movedValue;

            return movedValue;
        } else {
            return Float.NaN;
        }

    }

    private float moveOneDown() {
        // Find smallest key in the upper set
        int selection = -1;
        float firstSmallest = Float.POSITIVE_INFINITY;
        float secondSmallest = Float.POSITIVE_INFINITY;
        for (int i = 0; i < packets.length; ++i) {
            final Packet packet = packets[i];
            if (packet.split < packet.size) {
                final float v = packet.data[packet.split];
                if (v < firstSmallest) {
                    selection = i;
                    if (secondSmallest > firstSmallest) {
                        secondSmallest = firstSmallest;
                    }
                    firstSmallest = v;
                } else if (v < secondSmallest) {
                    secondSmallest = v;
                }
            }
        }


        if (selection >= 0) {
            final Packet packet = packets[selection];
            ++packet.split;

            if (packet.split < packet.size && packet.data[packet.split] < secondSmallest) {
                secondSmallest = packet.data[packet.split];
            }

            return secondSmallest;
        } else {
            return Float.NaN;
        }
    }


    private static class Packet {
        int split;
        int size;
        final float[] data;

        Packet(final int maxSize) {
            data = new float[maxSize];
        }
    }
}
