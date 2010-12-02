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

package net.sf.ij_plugins.io;

import ij.IJ;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.IJPluginsException;
import net.sf.ij_plugins.util.progress.DefaultProgressReporter;

import java.io.*;


/**
 * Interprets image intensity as height and writes a surface represented by the height image
 * in <a href="http://en.wikipedia.org/wiki/STL_%28file_format%29">STL format</a>.
 *
 * @author Jarek Sacha
 * @since 11/29/10 10:49 PM
 */
public final class ExportToSTL extends DefaultProgressReporter {

    /**
     * Write height image in ASCII variant of STL format.
     *
     * @param file        output file
     * @param ip          image to be saved
     * @param pixelWidth  pixel width
     * @param pixelHeight pixel height
     * @throws IJPluginsException if IO error occurred
     */
    public void writeASCII(final File file,
                           final ImageProcessor ip,
                           final double pixelWidth,
                           final double pixelHeight) throws IJPluginsException {

        final String solidName = "IJ";

        final String statusMessage = "Saving STL to: " + file.getAbsolutePath();
        notifyProgressListeners(0, statusMessage);
        final FileWriter writer;
        try {
            writer = new FileWriter(file);
        } catch (final IOException e) {
            throw new IJPluginsException("Error creating STL writer. " + e.getMessage(), e);
        }
        try {
            writer.write("solid " + solidName + "\n");
            for (int x = 0; x < ip.getWidth() - 1; x++) {
                notifyProgressListeners(x / (ip.getWidth() - 1d), statusMessage);
                for (int y = 0; y < ip.getHeight() - 1; y++) {

                    final double x0 = x * pixelWidth;
                    final double x1 = (x + 1) * pixelWidth;
                    final double y0 = y * pixelHeight;
                    final double y1 = (y + 1) * pixelWidth;
                    final double z00 = ip.getPixelValue(x, y);
                    final double z10 = ip.getPixelValue(x + 1, y);
                    final double z01 = ip.getPixelValue(x, y + 1);
                    final double z11 = ip.getPixelValue(x + 1, y + 1);

                    final double[][] vs1 = {{x0, y0, z00}, {x1, y1, z11}, {x0, y1, z01}};
                    writeFacetASCII(writer, vs1);

                    final double[][] vs2 = {{x0, y0, z00}, {x1, y0, z10}, {x1, y1, z11}};
                    writeFacetASCII(writer, vs2);
                }
            }
            IJ.showProgress(ip.getWidth() - 1, ip.getWidth() - 1);

            writer.write("endsolid " + solidName + "\n");
        } catch (final IOException e) {
            throw new IJPluginsException("Error writing to STL file. " + e.getMessage(), e);
        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                e.printStackTrace();
                IJ.log("Error closing STL writer. " + e.getMessage());
            }
        }

        notifyProgressListeners(1, statusMessage);
    }


    /**
     * Write height image in binary variant of STL format.
     *
     * @param file        output file
     * @param ip          image to be saved
     * @param pixelWidth  pixel width
     * @param pixelHeight pixel height
     * @throws IJPluginsException if IO error occurred
     */
    public void writeBinary(final File file,
                            final ImageProcessor ip,
                            final double pixelWidth,
                            final double pixelHeight) throws IJPluginsException {

        final String statusMessage = "Saving STL to: " + file.getAbsolutePath();
        notifyProgressListeners(0, statusMessage);
        final OutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new IJPluginsException("Error creating STL output stream. " + e.getMessage(), e);
        }
        try {
            // write empty header of 80 bytes
            out.write(new byte[80]);

            // Write numbers of triangles UINT32
            final int xMax = ip.getWidth() - 1;
            final int yMax = ip.getHeight() - 1;
            final int nbTriangles = xMax * yMax * 2;
            writeInt32(out, nbTriangles);

            for (int x = 0; x < xMax; x++) {
                notifyProgressListeners(x / (double) (xMax), statusMessage);
                for (int y = 0; y < yMax; y++) {

                    final double x0 = x * pixelWidth;
                    final double x1 = (x + 1) * pixelWidth;
                    final double y0 = y * pixelHeight;
                    final double y1 = (y + 1) * pixelWidth;
                    final double z00 = ip.getPixelValue(x, y);
                    final double z10 = ip.getPixelValue(x + 1, y);
                    final double z01 = ip.getPixelValue(x, y + 1);
                    final double z11 = ip.getPixelValue(x + 1, y + 1);

                    final double[][] vs1 = {{x0, y0, z00}, {x1, y1, z11}, {x0, y1, z01}};
                    writeFacetBinary(out, vs1);

                    final double[][] vs2 = {{x0, y0, z00}, {x1, y0, z10}, {x1, y1, z11}};
                    writeFacetBinary(out, vs2);
                }
            }
            IJ.showProgress(xMax, xMax);
        } catch (final IOException e) {
            throw new IJPluginsException("Error writing to STL file. " + e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (final IOException e) {
                e.printStackTrace();
                IJ.log("Error closing STL writer. " + e.getMessage());
            }
        }

        notifyProgressListeners(1, statusMessage);
    }


    private static void writeFacetASCII(final Writer writer, final double[][] v) throws IOException {
        // facet normal ni nj nk
        //   outer loop
        //     vertex v1x v1y v1z
        //     vertex v2x v2y v2z
        //     vertex v3x v3y v3z'
        //   endloop
        // endfacet

        final double x0 = v[0][0];
        final double y0 = v[0][1];
        final double z0 = v[0][2];
        final double x1 = v[1][0];
        final double y1 = v[1][1];
        final double z1 = v[1][2];
        final double x2 = v[2][0];
        final double y2 = v[2][1];
        final double z2 = v[2][2];

        /* Compute edge vectors */
        final double x10 = x1 - x0;
        final double y10 = y1 - y0;
        final double z10 = z1 - z0;
        final double x12 = x1 - x2;
        final double y12 = y1 - y2;
        final double z12 = z1 - z2;

        /* Compute the cross product */
        final double cpx = (z10 * y12) - (y10 * z12);
        final double cpy = (x10 * z12) - (z10 * x12);
        final double cpz = (y10 * x12) - (x10 * y12);

        /* Normalize the result to get the unit-length facet normal */
        final double r = Math.sqrt(cpx * cpx + cpy * cpy + cpz * cpz);
        final double nx = cpx / r;
        final double ny = cpy / r;
        final double nz = cpz / r;

        writer.write("facet normal " + nx + " " + ny + " " + nz + "\n");
        writer.write("\touter loop\n");
        writer.write("\t\tvertex " + x0 + " " + y0 + " " + z0 + "\n");
        writer.write("\t\tvertex " + x1 + " " + y1 + " " + z1 + "\n");
        writer.write("\t\tvertex " + x2 + " " + y2 + " " + z2 + "\n");
        writer.write("\tendloop\n");
        writer.write("endfacet\n");
    }


    private static void writeFacetBinary(final OutputStream out, final double[][] v) throws IOException {
        // REAL32[3]       -    Normal vector
        // REAL32[3]       -    Vertex 1
        // REAL32[3]       -    Vertex 2
        // REAL32[3]       -    Vertex 3
        // UINT16          -    Attribute byte count


        final double x0 = v[0][0];
        final double y0 = v[0][1];
        final double z0 = v[0][2];
        final double x1 = v[1][0];
        final double y1 = v[1][1];
        final double z1 = v[1][2];
        final double x2 = v[2][0];
        final double y2 = v[2][1];
        final double z2 = v[2][2];

        /* Compute edge vectors */
        final double x10 = x1 - x0;
        final double y10 = y1 - y0;
        final double z10 = z1 - z0;
        final double x12 = x1 - x2;
        final double y12 = y1 - y2;
        final double z12 = z1 - z2;

        /* Compute the cross product */
        final double cpx = (z10 * y12) - (y10 * z12);
        final double cpy = (x10 * z12) - (z10 * x12);
        final double cpz = (y10 * x12) - (x10 * y12);

        /* Normalize the result to get the unit-length facet normal */
        final double r = Math.sqrt(cpx * cpx + cpy * cpy + cpz * cpz);
        final double nx = cpx / r;
        final double ny = cpy / r;
        final double nz = cpz / r;

        writeReal32(out, nx);
        writeReal32(out, ny);
        writeReal32(out, nz);
        writeReal32(out, x0);
        writeReal32(out, y0);
        writeReal32(out, z0);
        writeReal32(out, x1);
        writeReal32(out, y1);
        writeReal32(out, z1);
        writeReal32(out, x2);
        writeReal32(out, y2);
        writeReal32(out, z2);

        out.write(new byte[2]);
    }


    private static void writeInt32(final OutputStream out, final int v) throws IOException {
        // Write int in little-endian (Intel) order
        final byte[] buffer = new byte[4];
        buffer[0] = (byte) v;
        buffer[1] = (byte) (v >> 8);
        buffer[2] = (byte) (v >> 16);
        buffer[3] = (byte) (v >> 24);
        out.write(buffer);
    }


    private static void writeReal32(final OutputStream out, final double v) throws IOException {
        // Write float in little-endian (Intel) order
        final byte[] buffer = new byte[4];
        final int tmp = Float.floatToRawIntBits((float) v);
        buffer[0] = (byte) tmp;
        buffer[1] = (byte) (tmp >> 8);
        buffer[2] = (byte) (tmp >> 16);
        buffer[3] = (byte) (tmp >> 24);
        out.write(buffer);
    }
}
