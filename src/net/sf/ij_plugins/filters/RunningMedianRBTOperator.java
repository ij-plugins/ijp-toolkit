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
public class RunningMedianRBTOperator implements IRunningMedianFloatOperator {
    private Packet[] packets;
    private int updatablePacket = 0;
    private final RedBlackTreeFloat rankTree = new RedBlackTreeFloat();

    public RunningMedianRBTOperator() {
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
            if (!rankTree.remove(v)) {
                throw new RuntimeException("Algorithm bug: internal data inconsistency.");
            }
            //            rankTree.verify();
        }


        // Process numbers being added
        //        System.arraycopy(data, 0, packet.data, 0, length);
        for (int i = 0; i < length; ++i) {
            final float v = data[i];
            packet.data[i] = v;
            rankTree.insert(v);
            //            rankTree.verify();
        }


        packet.size = length;

        updatablePacket = (updatablePacket + 1) % packets.length;
    }

    @Override
    public float evaluate() {
        final int medianRank = rankTree.size() / 2 + 1;

        return rankTree.select(medianRank);
    }

    @Override
    public void clear() {
        updatablePacket = 0;
        rankTree.clear();
        rankTree.verify();


        for (final Packet packet : packets) {
            packet.size = 0;
            Arrays.fill(packet.data, 0);
        }
    }

    private static class Packet {
        int size;
        final float[] data;

        Packet(final int maxSize) {
            data = new float[maxSize];
        }
    }
}
