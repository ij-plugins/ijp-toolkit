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

package net.sf.ij_plugins.io;

import ij.IJ;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.IJPluginsException;
import net.sf.ij_plugins.util.TextUtil;
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

    enum FileType {ASCII, BINARY}


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
        writeASCII(file, ip, pixelWidth, pixelHeight, 0, 0);
    }


    /**
     * Write height image in ASCII variant of STL format.
     * Absolute location of x in calibrated coordinates is (x-xOrigin)*pixelWidth,
     * where x is pixel index starting at 0.
     *
     * @param file        output file
     * @param ip          image to be saved
     * @param pixelWidth  pixel width
     * @param pixelHeight pixel height
     * @param xOrigin     X origin in pixels
     * @param yOrigin     y origin in pixels
     * @throws IJPluginsException if IO error occurred
     */
    public void writeASCII(final File file,
                           final ImageProcessor ip,
                           final double pixelWidth,
                           final double pixelHeight,
                           final double xOrigin,
                           final double yOrigin) throws IJPluginsException {

        final boolean saveSides = true;
        final String solidName = "IJ";
        ip.resetMinAndMax();

        final String statusMessage = "Saving STL to: " + file.getAbsolutePath();
        notifyProgressListeners(0, statusMessage);
        final OutputStream out;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new IJPluginsException("Error creating STL output stream. " + e.getMessage(), e);
        }
        try {
            write(out, "solid " + solidName + "\n");

            writeFacets(out, ip, pixelWidth, pixelHeight, xOrigin, yOrigin, statusMessage, FileType.ASCII, saveSides);

            write(out, "endsolid " + solidName + "\n");
        } catch (final IOException e) {
            throw new IJPluginsException("Error writing to STL file. " + e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (final IOException e) {
                IJ.log("Error closing STL writer. " + e.getMessage() + "\n" + TextUtil.toString(e));
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
        writeBinary(file, ip, pixelWidth, pixelHeight, 0, 0);
    }


    /**
     * Write height image in binary variant of STL format.
     *
     * @param file        output file
     * @param ip          image to be saved
     * @param pixelWidth  pixel width
     * @param pixelHeight pixel height
     * @param xOrigin     X origin in pixels
     * @param yOrigin     y origin in pixels
     * @throws IJPluginsException if IO error occurred
     */
    public void writeBinary(final File file,
                            final ImageProcessor ip,
                            final double pixelWidth,
                            final double pixelHeight,
                            final double xOrigin,
                            final double yOrigin) throws IJPluginsException {

        final boolean saveSides = true;
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
            final int nbTopTriangles = xMax * yMax * 2;
            final int nbBottomTriangles = 2;
            final int nbSideTriangles = 2 * 2 * (xMax + yMax);
            final int nbTriangles = nbTopTriangles + nbBottomTriangles + nbSideTriangles;
            writeInt32(out, nbTriangles);

            writeFacets(out, ip, pixelWidth, pixelHeight, xOrigin, yOrigin, statusMessage, FileType.BINARY, saveSides);

            IJ.showProgress(xMax, xMax);
            IJ.showStatus("Saved STL to: " + file.getName());
        } catch (final IOException e) {
            throw new IJPluginsException("Error writing to STL file. " + e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (final IOException e) {
                IJ.log("Error closing STL writer. " + e.getMessage() + "\n" + TextUtil.toString(e));
            }
        }

        notifyProgressListeners(1, statusMessage);
    }


    private void writeFacets(final OutputStream out,
                             final ImageProcessor ip,
                             final double pixelWidth,
                             final double pixelHeight,
                             final double xOrigin,
                             final double yOrigin,
                             final String statusMessage,
                             final FileType fileType,
                             final boolean saveSides) throws IOException {

        final double min = ip.getMin();
        final double max = ip.getMax();
        final double minLevel = max - (max - min) * 1.2;

        final int width = ip.getWidth();
        final int height = ip.getHeight();

        // Save top surface
        for (int x = 0; x < width - 1; x++) {
            notifyProgressListeners(x / (ip.getWidth() - 1d), statusMessage);
            for (int y = 0; y < ip.getHeight() - 1; y++) {
                final double xx = x - xOrigin;
                final double yy = y - yOrigin;
                final double[][] r = {
                        {xx * pixelWidth, yy * pixelHeight},
                        {(xx + 1) * pixelWidth, (yy + 1) * pixelWidth}};
                final double[][] z = {
                        {ip.getPixelValue(x, y), ip.getPixelValue(x, y + 1)},
                        {ip.getPixelValue(x + 1, y), ip.getPixelValue(x + 1, y + 1)}};

                write4(out, r, z, fileType);
            }
        }

        if (saveSides) {
            final double xMin = (0 - xOrigin) * pixelWidth;
            final double yMin = (0 - yOrigin) * pixelHeight;
            final double xMax = (width - xOrigin - 1) * pixelWidth;
            final double yMax = (height - yOrigin - 1) * pixelHeight;
            // Save bottom side
            {
                final double[][] r = {{xMin, yMin}, {xMax, yMax}};
                final double[][] z = {{minLevel, minLevel}, {minLevel, minLevel}};

                write4(out, r, z, fileType);
            }

            // Save front side
            for (int x = 0; x < width - 1; x++) {
                final double xx = x - xOrigin;
                final double[][] r = {
                        {xx * pixelWidth, yMin},
                        {(xx + 1) * pixelWidth, yMin}};
                final double[][] z = {
                        {ip.getPixelValue(x, 0), minLevel},
                        {ip.getPixelValue(x + 1, 0), minLevel}};

                write4(out, r, z, fileType);
            }

            // Save back side
            for (int x = 0; x < width - 1; x++) {
                final double xx = x - xOrigin;
                final double[][] r = {
                        {xx * pixelWidth, yMax},
                        {(xx + 1) * pixelWidth, yMax}};
                final double[][] z = {
                        {ip.getPixelValue(x, height - 1), minLevel},
                        {ip.getPixelValue(x + 1, height - 1), minLevel}};

                write4(out, r, z, fileType);
            }

            // Save left side
            for (int y = 0; y < height - 1; y++) {
                final double yy = y - yOrigin;
                final double[][] r = {
                        {xMin, yy * pixelHeight},
                        {xMin, (yy + 1) * pixelHeight}};
                final double[][] z = {
                        {ip.getPixelValue(0, y), ip.getPixelValue(0, y + 1)},
                        {minLevel, minLevel}};

                write4(out, r, z, fileType);
            }

            // Save right side
            for (int y = 0; y < height - 1; y++) {
                final double yy = y - yOrigin;
                final double[][] r = {
                        {xMax, yy * pixelHeight},
                        {xMax, (yy + 1) * pixelHeight}};
                final double[][] z = {
                        {ip.getPixelValue(width - 1, y), ip.getPixelValue(width - 1, y + 1)},
                        {minLevel, minLevel}};

                write4(out, r, z, fileType);
            }

        }

        IJ.showProgress(ip.getWidth() - 1, ip.getWidth() - 1);
    }


    private static void write4(final OutputStream out, final double[][] r, double[][] z, final FileType fileType) throws IOException {
        final double x0 = r[0][0];
        final double x1 = r[1][0];
        final double y0 = r[0][1];
        final double y1 = r[1][1];
        final double z00 = z[0][0];
        final double z10 = z[1][0];
        final double z01 = z[0][1];
        final double z11 = z[1][1];

        final double[][] vs1 = {{x0, y0, z00}, {x1, y1, z11}, {x0, y1, z01}};
        writeFacet(out, vs1, fileType);

        final double[][] vs2 = {{x0, y0, z00}, {x1, y0, z10}, {x1, y1, z11}};
        writeFacet(out, vs2, fileType);

    }


    private static void writeFacet(final OutputStream out, final double[][] v, final FileType fileType) throws IOException {

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

        if (FileType.ASCII == fileType) {
            // facet normal ni nj nk
            //   outer loop
            //     vertex v1x v1y v1z
            //     vertex v2x v2y v2z
            //     vertex v3x v3y v3z'
            //   endloop
            // endfacet
            write(out, "facet normal " + nx + " " + ny + " " + nz + "\n");
            write(out, "\touter loop\n");
            write(out, "\t\tvertex " + x0 + " " + y0 + " " + z0 + "\n");
            write(out, "\t\tvertex " + x1 + " " + y1 + " " + z1 + "\n");
            write(out, "\t\tvertex " + x2 + " " + y2 + " " + z2 + "\n");
            write(out, "\tendloop\n");
            write(out, "endfacet\n");
        } else {
            // REAL32[3] - Normal vector
            // REAL32[3] - Vertex 1
            // REAL32[3] - Vertex 2
            // REAL32[3] - Vertex 3
            // UINT16    - Attribute byte count
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


    private static void write(final OutputStream out, final String s) throws IOException {
        out.write(s.getBytes("UTF-8"));
    }

}
