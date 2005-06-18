/***
 * Image/J Plugins
 * Copyright (C) 2002 Jarek Sacha
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
package net.sf.ij_plugins.io.metaimage;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileInfo;
import ij.io.ImageWriter;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Encode image (including stacks) in MetaImage format. MetaImage is one of the formats supported by
 * ITK (http://www.itk.org). More information about MetaImage, including C++ code, can be found
 * http://caddlab.rad.unc.edu/technologies/MetaImage/ . This implementation is intended to be
 * compatible with ITK version of MetaImage.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @created June 18, 2002
 * @todo Fix MetaImage format for unambiguous element size, e.g. MET_LONG could be 32 bit on 32 bit
 * processors and 64 bit on 64 bit processors. A fix would be to use MET_INT32.
 * @todo Fix MetaImage tag names to distinguish file side and memory side representation, and real
 * word coordinates. For instance ElementType refers to how a pixel value in represented in file,
 * ElementSize refers to size of the of the volume represented by a single pixel in real word
 * coordinates. Naming convention seams to be confusing.
 */

public class MiEncoder implements PlugIn {
    private static String DIALOG_CAPTION = "MetaImage Writer";
    private static String[] HEADER_EXTENSIONS = {".mha", ".mhd"};
    private static String RAW_DATA_EXTENSION = ".raw";
    private static String ASSIGNMENT_SEPARATOR = " = ";
    private static String LINE_SEPARATOR = "\n";


    /**
     * Constructor for the MiEncoder object
     */
    public MiEncoder() {
    }


    /**
     * Write image in MetaImage format. Info header and raw image data are stored in separate
     * files.
     *
     * @param imp          Image to save.
     * @param fileRootName Root file name for image files. Header will have extension ".mha", raw
     *                     image data will have extension ".raw".
     * @throws MiException In case of error when saving the image.
     */
    public static void write(ImagePlus imp, String fileRootName) throws MiException {
        if (imp == null) {
            throw new IllegalArgumentException("Argument 'imp' cannot be null.");
        }
        if (fileRootName == null) {
            throw new IllegalArgumentException("Argument 'fileRootName' cannot be null.");
        }

        //
        // Figure out file names.
        //
        String headerName = null;
        String rawDataName = null;
        // Check if header extension is already present.
        for (int i = 0; i < HEADER_EXTENSIONS.length; ++i) {
            if (fileRootName.endsWith(HEADER_EXTENSIONS[i])) {
                headerName = fileRootName;
                String nameRoot = fileRootName.substring(
                        0, fileRootName.length() - HEADER_EXTENSIONS[i].length());
                rawDataName = nameRoot + RAW_DATA_EXTENSION;
                break;
            }
        }
        // If not present then use default extension.
        if (headerName == null) {
            headerName = fileRootName + HEADER_EXTENSIONS[0];
            rawDataName = fileRootName + RAW_DATA_EXTENSION;
        }

        // Remove path from rawDataName
        String rawDataNameShort = rawDataName;
        int lastSeparatorIndex = rawDataName.lastIndexOf(File.separator);
        if (lastSeparatorIndex >= 0) {
            rawDataNameShort = rawDataName.substring(lastSeparatorIndex + 1);
        }

        //
        // Create MetaImage files
        //
        writeHeader(imp, headerName, rawDataNameShort);
        writeRawImage(imp, rawDataName);
    }


    /**
     * Write only MetaImage header.
     *
     * @param imp             Image for which to write the header.
     * @param headerFileName  Name of the file to write header to.
     * @param rawDataFileName Name of the file where raw image data is saved.
     * @throws MiException In case of error when saving the header.
     */
    public static void writeHeader(ImagePlus imp, String headerFileName,
                                   String rawDataFileName) throws MiException {

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(headerFileName);
            fileWriter.write(createHeaderText(imp, rawDataFileName));
        }
        catch (IOException ex) {
            throw new MiException("Error writing to header file '"
                    + headerFileName + "'.\n" + ex.getMessage());
        }
        finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                }
                catch (IOException ex) {
                    //
                }
            }
        }
    }


    /**
     * Create content of a MetaImage header as string.
     *
     * @param imp             Image for which to write the header.
     * @param rawDataFileName Name of the file where raw image data is saved.
     * @return String containing MetaImage header.
     * @throws MiException In case of error when saving the header.
     */
    public static String createHeaderText(ImagePlus imp, String rawDataFileName)
            throws MiException {
        StringBuffer header = new StringBuffer();

        // NDims
        String nDims = imp.getStackSize() > 1 ? "3" : "2";
        header.append(MiTag.NDims + ASSIGNMENT_SEPARATOR
                + nDims + LINE_SEPARATOR);

        // DimSize
        header.append(MiTag.DimSize + ASSIGNMENT_SEPARATOR
                + imp.getWidth() + " " + imp.getHeight());
        if (imp.getStackSize() > 1) {
            header.append(" " + imp.getStackSize());
        }
        header.append(LINE_SEPARATOR);

        // ElementType
        final String elementType;
        switch (imp.getType()) {
            case ImagePlus.COLOR_256:
            case ImagePlus.COLOR_RGB:
                throw new MiException("COLOR_256 and COLOR_RGB images are not supported.");
            case ImagePlus.GRAY8:
                elementType = MiElementType.MET_UCHAR.toString();
                break;
            case ImagePlus.GRAY16:
                elementType = imp.getFileInfo().fileType == FileInfo.GRAY16_SIGNED
                        ? MiElementType.MET_SHORT.toString()
                        : MiElementType.MET_USHORT.toString();
                break;
            case ImagePlus.GRAY32:
                elementType = imp.getFileInfo().fileType == FileInfo.GRAY32_INT
                        ? MiElementType.MET_UINT.toString()
                        : MiElementType.MET_FLOAT.toString();
                break;
            default:
                throw new MiException("Unrecognized ImagePlus type id: " + imp.getType());
        }
        header.append(MiTag.ElementType + ASSIGNMENT_SEPARATOR
                + elementType + LINE_SEPARATOR);

        // ElementByteOrderMSB
        // JVM always uses MSB, independent of the underlaying hardware platform.
        header.append(MiTag.ElementByteOrderMSB + ASSIGNMENT_SEPARATOR
                + MiBoolean.True + LINE_SEPARATOR);

        // ElementSize
        // ElementSpacing
        String elementSize = null;
        Calibration cal = imp.getCalibration();
        if (cal != null && cal.pixelWidth > 0 && cal.pixelHeight > 0) {
            elementSize = "" + cal.pixelWidth + " " + cal.pixelHeight;
            if (imp.getStackSize() > 1) {
                if (cal.pixelDepth > 0) {
                    elementSize += " " + cal.pixelDepth;
                } else {
                    elementSize = null;
                }
            }
        }
        if (elementSize != null) {
            header.append(MiTag.ElementSize + ASSIGNMENT_SEPARATOR
                    + elementSize + LINE_SEPARATOR);
            header.append(MiTag.ElementSpacing + ASSIGNMENT_SEPARATOR
                    + elementSize + LINE_SEPARATOR);
        }

        // ElementDataFile, this should always be last.
        header.append(MiTag.ElementDataFile + ASSIGNMENT_SEPARATOR
                + rawDataFileName + LINE_SEPARATOR);

        return header.toString();
    }


    /**
     * Save only the raw image data.
     *
     * @param imp      Image to be saved.
     * @param fileName Raw data file name.
     * @throws MiException In case of error when saving the raw data.
     */
    public static void writeRawImage(ImagePlus imp, String fileName) throws MiException {
        File file = new File(fileName);
        FileInfo fileInfo = imp.getFileInfo();
        fileInfo.directory = file.getParent();
        fileInfo.fileName = file.getName();
        fileInfo.fileFormat = FileInfo.RAW;

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileName);
            ImageWriter imageWriter = new ImageWriter(fileInfo);
            imageWriter.write(fileOutputStream);
        }
        catch (IOException ex) {
            throw new MiException("Error writing to raw image file '"
                    + fileName + "'.\n" + ex.getMessage());
        }
        finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }


    /**
     * Main processing method for the MiEncoder object, required by PlugIn interface.
     *
     * @param parm1 Not used.
     */
    public void run(String parm1) {
        // Get current image
        ImagePlus imp = WindowManager.getCurrentImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Get file name
        SaveDialog saveDialog = new SaveDialog(DIALOG_CAPTION,
                imp.getTitle(), HEADER_EXTENSIONS[0]);
        if (saveDialog.getFileName() == null) {
            return;
        }

        File file = new File(saveDialog.getDirectory(), saveDialog.getFileName());
        try {
            write(imp, file.getAbsolutePath());
        }
        catch (MiException ex) {
            IJ.showMessage(DIALOG_CAPTION, ex.getMessage());
            return;
        }

        IJ.showStatus("MetaImage " + saveDialog.getFileName() + " saved.");
    }

}
