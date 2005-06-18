/***
 * Image/J Plugins
 * Copyright (C) 2002-2005 Jarek Sacha
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
package net.sf.ij_plugins.io.vtk;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.ImageWriter;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import java.io.*;

/**
 * Save image in <a HREF="http://public.kitware.com/VTK/">VTK</a> format. Supported image types:
 * GRAY8, GRAY16, GRAY32.
 *
 * @author Jarek Sacha
 * @version 1.0
 * @created April 28, 2002
 */

public class VtkEncoder implements PlugIn {
    private final static String DIALOG_CAPTION = "VTK Writer";
    private final static String TAG_SEPARATOR = " ";
    private final static String vtkFileVersion = "3.0";


    /**
     * Description of the Method
     *
     * @param imp         Description of Parameter
     * @param asciiFormat Description of Parameter
     * @return Description of the Returned Value
     */
    private static String createHeader(final ImagePlus imp, final boolean asciiFormat) {

        final StringBuffer header = new StringBuffer();
        header.append(VtkTag.DATA_FILE_VERSION).append(vtkFileVersion).append("\n");

        header.append(imp.getTitle()).append("\n");

        header.append(asciiFormat ? VtkDataFormat.ASCII : VtkDataFormat.BINARY);
        header.append("\n");

        header.append(VtkTag.DATASET).append(TAG_SEPARATOR)
                .append(VtkDataSetType.STRUCTURED_POINTS).append("\n");

        final int width = imp.getWidth();
        final int height = imp.getHeight();
        final int depth = imp.getStackSize();
        header.append(VtkTag.DIMENSIONS).append(TAG_SEPARATOR).append(width).append(" ")
                .append(height).append(" ").append(depth).append("\n");

        final Calibration c = imp.getCalibration();
        header.append(VtkTag.SPACING).append(TAG_SEPARATOR).append(c.pixelWidth).append(" ")
                .append(c.pixelHeight).append(" ").append(c.pixelDepth).append("\n");

        header.append(VtkTag.ORIGIN).append(TAG_SEPARATOR).append(c.xOrigin).append(" ")
                .append(c.yOrigin).append(" ").append(c.zOrigin + "\n");

        header.append(VtkTag.POINT_DATA).append(TAG_SEPARATOR).append(width * height * depth)
                .append("\n");

        final String scalarName;
        switch (imp.getType()) {
            case ImagePlus.GRAY8:
                scalarName = VtkScalarType.UNSIGNED_CHAR.toString();
                break;
            case ImagePlus.GRAY16:
                scalarName = VtkScalarType.UNSIGNED_SHORT.toString();
                break;
            case ImagePlus.GRAY32:
                scalarName = VtkScalarType.FLOAT.toString();
                break;
            default:
                throw new IllegalArgumentException("Unsupported image type. "
                        + "Only images of types: GRAY8, GRAY16, and GRAY32 are supported.");
        }
        header.append(VtkTag.SCALARS).append(TAG_SEPARATOR).append("volume_scalars ")
                .append(scalarName).append(" 1\n");

        header.append(VtkTag.LOOKUP_TABLE).append(TAG_SEPARATOR).append("default\n");

        return header.toString();
    }


    /**
     * Description of the Method
     *
     * @param fileName Description of Parameter
     * @param imp      Description of Parameter
     * @throws FileNotFoundException Description of Exception
     * @throws IOException           Description of Exception
     */
    private static void saveAsVtkBinary(final String fileName, final ImagePlus imp)
            throws FileNotFoundException, IOException {

        final BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(fileName));

        final String header = createHeader(imp, false);
        bos.write(header.getBytes());

        final ImageWriter imageWriter = new ImageWriter(imp.getFileInfo());
        imageWriter.write(bos);

        bos.close();
    }

    public static void save(final String fileName, final ImagePlus imp) throws IOException {
        saveAsVtkBinary(fileName, imp);
    }


    /**
     * Save image using ASCII variant of the VTK format.
     *
     * @param fileName File name.
     * @param imp      Image to be saved.
     * @throws IOException In case of I/O errors.
     */
    private static void saveAsVtkAscii(String fileName, ImagePlus imp)
            throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

        IJ.showProgress(0);

        String header = createHeader(imp, true);
        writer.write(header);

        int oldSlice = imp.getCurrentSlice();
        int width = imp.getWidth();
        int height = imp.getHeight();
        int depth = imp.getStackSize();
        int sliceSize = width * height;
        Object[] pixels = imp.getStack().getImageArray();
        for (int z = 0; z < depth; ++z) {
            writeArray(pixels[z], sliceSize, writer, width);
            IJ.showProgress((double) z / (double) depth);
        }
        writer.close();
        IJ.showProgress(1);

        imp.setSlice(oldSlice);
    }


    /*
    *
    */

    /**
     * Description of the Method
     *
     * @param a        Description of the Parameter
     * @param length   Description of the Parameter
     * @param writer   Description of the Parameter
     * @param lineSize Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private static void writeArray(Object a, int length,
                                   Writer writer, int lineSize) throws IOException {
        if (a instanceof byte[]) {
            writeArray((byte[]) a, length, writer, lineSize);
        } else if (a instanceof short[]) {
            writeArray((short[]) a, length, writer, lineSize);
        } else if (a instanceof float[]) {
            writeArray((float[]) a, length, writer, lineSize);
        } else {
            throw new IllegalArgumentException("Unsupported array type: " + a);
        }
    }


    /*
    *
    */

    /**
     * Description of the Method
     *
     * @param a        Description of the Parameter
     * @param length   Description of the Parameter
     * @param writer   Description of the Parameter
     * @param lineSize Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private static void writeArray(byte[] a, int length,
                                   Writer writer, int lineSize) throws IOException {
        int c = 0;
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; ++i) {
            buf.append(a[i] & 0xff);
            if (i > 0 && (i % lineSize) == 0) {
                buf.append('\n');
                writer.write(buf.toString());
                buf.delete(0, buf.length());
            } else {
                buf.append(' ');
            }
        }
        writer.write(buf.toString());
    }


    /*
    *
    */

    /**
     * Description of the Method
     *
     * @param a        Description of the Parameter
     * @param length   Description of the Parameter
     * @param writer   Description of the Parameter
     * @param lineSize Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private static void writeArray(short[] a, int length,
                                   Writer writer, int lineSize) throws IOException {
        int c = 0;
        for (int i = 0; i < length; ++i) {
            writer.write("" + (a[i] & 0xffff));
            c++;
            writer.write((c % lineSize) == 0 ? "\n" : " ");
        }
    }


    /*
    *
    */

    /**
     * Description of the Method
     *
     * @param a        Description of the Parameter
     * @param length   Description of the Parameter
     * @param writer   Description of the Parameter
     * @param lineSize Description of the Parameter
     * @throws IOException Description of the Exception
     */
    private static void writeArray(float[] a, int length,
                                   Writer writer, int lineSize) throws IOException {
        int c = 0;
        for (int i = 0; i < length; ++i) {
            writer.write("" + a[i]);
            c++;
            writer.write((c % lineSize) == 0 ? "\n" : " ");
        }
    }


    /**
     * Main processing method for the VtkEncoder plugin
     *
     * @param arg If equal "ASCII" file will be saved in text format otherwise in binary format
     *            (MSB).
     */
    public void run(String arg) {
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.showMessage(DIALOG_CAPTION, "No image to save.");
            return;
        }

        SaveDialog saveDialog
                = new SaveDialog("Save as VTK", imp.getTitle(), ".vtk");

        if (saveDialog.getFileName() == null) {
            return;
        }

        IJ.showStatus("Saving current image as '" + saveDialog.getFileName() + "'...");
        String fileName = saveDialog.getDirectory() + File.separator
                + saveDialog.getFileName();

        try {
            long tStart = System.currentTimeMillis();
            if (arg.compareToIgnoreCase("ASCII") == 0) {
                saveAsVtkAscii(fileName, imp);
            } else {
                saveAsVtkBinary(fileName, imp);
            }
            long tStop = System.currentTimeMillis();
            IJ.showStatus("Saving of '" + saveDialog.getFileName()
                    + "' completed in " + (tStop - tStart) + " ms.");
        } catch (Exception ex) {
            ex.printStackTrace();
            String msg = ex.getMessage();
            if (msg == null) {
                msg = "";
            } else {
                msg = "\n" + msg;
            }

            IJ.showMessage(DIALOG_CAPTION, "Error writing file '" + fileName + "'."
                    + msg);
        }
    }
}
