/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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

package ij_plugins.toolkit.im3d.grow;

import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij_plugins.toolkit.im3d.Point3DInt;
import ij_plugins.toolkit.io.IOUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;


/**
 * @author Jarek Sacha
 * @since Sep 17, 2009 7:50:32 PM
 */
public final class SRG3DTest {


    @Ignore("Testing result expected pixel by pixel will fail since original SGR algorithm is ambiguous on borders.")
    @Test
    public void test1() throws Exception {

        final int xMax = 64;
        final int yMax = 64;
        final int zMax = 64;

        final ImageStack imageStack = createStack(xMax, yMax, zMax);
        fill(new Point3DInt(0, 0, 0), new Point3DInt(64, 64, 64), imageStack, 10);
        fill(new Point3DInt(20, 22, 24), new Point3DInt(40, 41, 45), imageStack, 20);
        fill(new Point3DInt(30, 35, 30), new Point3DInt(51, 53, 55), imageStack, 30);

        final Point3DInt[][] seeds = {
                {new Point3DInt(1, 1, 1)},
                {new Point3DInt(25, 25, 28)},
                {new Point3DInt(31, 36, 31)}};

        final SRG3D srg = new SRG3D();
        srg.setImage(imageStack);
        srg.setSeeds(SRG3D.toSeedImage(seeds, xMax, yMax, zMax));
        final long startTime = System.currentTimeMillis();
        srg.run();
        final long endTime = System.currentTimeMillis();
        final ImageStack markers = srg.getRegionMarkers();

        System.out.println("Run time: " + (endTime - startTime) + "ms.");

        IOUtils.saveAsTiff(imageStack, new File("tmp", "SRG_source.tif"));
        IOUtils.saveAsTiff(markers, new File("tmp", "SRG_markers.tif"));

        // Validate
        assertEquals(imageStack, markers, 10);
    }


    @Test
    public void test2() throws Exception {

        final int xMax = 64;
        final int yMax = 64;
        final int zMax = 64;

        final ImageStack imageStack = createStack(xMax, yMax, zMax);
        fill(new Point3DInt(0, 0, 0), new Point3DInt(64, 64, 64), imageStack, 10);
        fill(new Point3DInt(20, 22, 24), new Point3DInt(40, 41, 45), imageStack, 20);
        fill(new Point3DInt(30, 35, 30), new Point3DInt(51, 53, 55), imageStack, 30);

        final Point3DInt[][] seeds = {
                {new Point3DInt(1, 1, 1)},
                {new Point3DInt(25, 25, 28)},
                {new Point3DInt(31, 36, 31)}};

        final SRG3D srg = new SRG3D();
        srg.setImage(imageStack);
        srg.setSeeds(SRG3D.toSeedImage(seeds, xMax, yMax, zMax));
        srg.run();
        final ImageStack markers = srg.getRegionMarkers();

        // Validate
        Assert.assertEquals(1, markers.getProcessor(10).get(10, 10));
        Assert.assertEquals(2, markers.getProcessor(27).get(25, 30));
        Assert.assertEquals(3, markers.getProcessor(40).get(40, 40));
    }


    private void assertEquals(final ImageStack expected, final ImageStack actual, final int multiplier) {
        assertNotNull(expected);
        assertNotNull(actual);
        Assert.assertEquals(expected.getSize(), actual.getSize());
        Assert.assertEquals(expected.getWidth(), actual.getWidth());
        Assert.assertEquals(expected.getHeight(), actual.getHeight());
        for (int z = 0; z < expected.getSize(); z++) {
            for (int y = 0; y < expected.getHeight(); y++) {
                for (int x = 0; x < expected.getWidth(); x++) {
                    Assert.assertEquals("(" + x + "," + y + "," + z + ")",
                            expected.getProcessor(z + 1).get(x, y),
                            actual.getProcessor(z + 1).get(x, y) * multiplier);
                }
            }
        }

    }


    private void fill(final Point3DInt min, final Point3DInt max, final ImageStack imageStack, final int value) {
        for (int z = min.z; z < max.z; z++) {
            final ImageProcessor p = imageStack.getProcessor(z + 1);
            for (int y = min.y; y < max.y; y++) {
                for (int x = min.x; x < max.x; x++) {
                    p.set(x, y, value);
                }
            }
        }
    }


    private ImageStack createStack(final int xMax, final int yMax, final int zMax) {
        final ImageStack r = new ImageStack(xMax, yMax);
        for (int z = 0; z < zMax; z++) {
            r.addSlice("" + z, new ByteProcessor(xMax, yMax));
        }

        return r;
    }
}