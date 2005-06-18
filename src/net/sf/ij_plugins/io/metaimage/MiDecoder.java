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
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;
import net.sf.ij_plugins.util.TextUtil;

import java.io.*;

/**
 * Decodes an image file in MetaImage format. MetaImage is one of the formats supported by ITK
 * (http://www.itk.org). More information about MetaImage, including C++ code, can be found at
 * http://caddlab.rad.unc.edu/technologies/MetaImage/ . This implementation is intended to be
 * compatible with ITK version of MetaImage.
 *
 * @author Jarek Sacha
 * @version $Revision: 1.1 $
 * @created July 31, 2002
 * @todo Validate MetaImage tag dependency (some tags need always be present, some only if other
 * tags are present, etc.)
 */

public class MiDecoder implements PlugIn {
    final private static String DIALOG_CAPTION = "MetaImage Reader";

    /**
     * Symbol separating a tag from its value in the MetaImage header.
     */
    final public static String ASSIGNMENT_SYMBOL = "=";


    /**
     * Constructor for the MiDecoder object
     */
    public MiDecoder() {
    }


    /**
     * Load image from a MetaImage file.
     *
     * @param file MetaImage file.
     * @return Decoded image.
     * @throws MiException In case of IO errors.
     */
    public static ImagePlus open(File file) throws MiException {
        MiDecoder miDecoder = new MiDecoder();

        // Read image header
        FileInfo fileInfo = miDecoder.decodeHeader(file);
        if (IJ.debugMode) {
            System.out.println("FileInfo: " + fileInfo.toString());
        }

        // Read binary image data
        FileOpener fileOpener = new FileOpener(fileInfo);
        ImagePlus imp = fileOpener.open(false);
        if (imp == null) {
            throw new MiException("Unable to read image data from '"
                    + fileInfo.fileName + "'.");
        }

        return imp;
    }


    /*
    *
    */
    private MiTagValuePair extractTagAndValue(String line) throws MiException {

        if (line == null) {
            throw new IllegalArgumentException("Argument 'line' cannot be null.");
        }

        int pos = line.indexOf(ASSIGNMENT_SYMBOL);
        if (pos < 0) {
            throw new MiException(
                    "Missing tag or value: unable to locate the assignment symbol '"
                            + ASSIGNMENT_SYMBOL + "'.");
        }
        String tagName = line.substring(0, pos).trim();
        MiTagValuePair tag = new MiTagValuePair();
        try {
            tag.id = (MiTag) MiTag.DimSize.byName(tagName);
        }
        catch (IllegalArgumentException ex) {
            throw new MiException("'" + tagName + "' is not a valid MetaImage tag name.");
        }

        tag.value = line.substring(pos + 1, line.length()).trim();

        return tag;
    }


    /**
     * Open file MetaImage header file and decode image information. Decoded information is
     * represented as FileInfo so standard ImageJ methods can be used to read image data.
     *
     * @param file MetaImage header file.
     * @return MetaImage header information converted to FileInfo format.
     * @throws MiException In case of I/O errors or incorrect header format.
     */
    private FileInfo decodeHeader(File file) throws MiException {
        final FileInfo fileInfo = new FileInfo();
        final BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException ex) {
            throw new MiException("Error opening file " + ex.getMessage());
        }

        int lineNb = 0;
        boolean elementSpacingDefined = false;
        try {
            String line = reader.readLine();
            // Keep track of line number for error reporting.
            int nDims = -1;
            // Iterate through header lines
            for (; line != null; line = reader.readLine()) {
                ++lineNb;

                // Parse line, throw exception if tag's name is invalid.
                MiTagValuePair tag = extractTagAndValue(line);

                // TAG: NDim
                if (tag.id == MiTag.NDims) {
                    nDims = TextUtil.parseInt(tag.value, -1);
                    if (nDims < 2 || nDims > 3) {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.NDims + "'. Expecting value equal either 2 or 3, got '"
                                + tag.value + "'. Header line=" + lineNb + ".");
                    }
                }
                // TAG: DimSize
                else if (tag.id == MiTag.DimSize) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.DimSize
                                + "' but there was no tag '" + MiTag.NDims
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    int[] dimSize = TextUtil.parseIntArray(tag.value);
                    if (dimSize.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DimSize
                                + "' does not match number of dimensions in tag '" + MiTag.NDims
                                + "'. Header line=" + lineNb + ".");
                    }

                    fileInfo.width = dimSize[0];
                    fileInfo.height = dimSize[1];
                    if (dimSize.length > 2) {
                        fileInfo.nImages = dimSize[2];
                    }
                }
                // TAG: BinaryDataByteOrderMSB
                else if (tag.id == MiTag.BinaryDataByteOrderMSB) {
                    if (tag.value.compareToIgnoreCase("true") == 0) {
                        fileInfo.intelByteOrder = false;
                    } else if (tag.value.compareToIgnoreCase("false") == 0) {
                        fileInfo.intelByteOrder = true;
                    } else {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.BinaryDataByteOrderMSB
                                + "'. Expecting either 'true' or 'false', got '"
                                + tag.value + "'.");
                    }
                }
                // TAG: ElementByteOrderMSB
                else if (tag.id == MiTag.ElementByteOrderMSB) {
                    if (tag.value.compareToIgnoreCase("true") == 0) {
                        fileInfo.intelByteOrder = false;
                    } else if (tag.value.compareToIgnoreCase("false") == 0) {
                        fileInfo.intelByteOrder = true;
                    } else {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.ElementByteOrderMSB
                                + "'. Expecting either 'true' or 'false', got '"
                                + tag.value + "'.");
                    }
                }
                // TAG: ElementSpacing
                else if (tag.id == MiTag.ElementSpacing) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.ElementSpacing
                                + "' but there was no tag '" + MiTag.NDims
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    float[] elementSpacing = TextUtil.parseFloatArray(tag.value);
                    if (elementSpacing.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DimSize
                                + "' does not match number of dimensions in tag '"
                                + MiTag.ElementSpacing + "'. Header line=" + lineNb + ".");
                    }
                    fileInfo.pixelWidth = elementSpacing[0];
                    fileInfo.pixelHeight = elementSpacing[1];
                    if (elementSpacing.length > 2) {
                        fileInfo.pixelDepth = elementSpacing[2];
                    }
                    fileInfo.unit = "mm";
                    elementSpacingDefined = true;

                }
                // TAG: ElementSize
                else if (tag.id == MiTag.ElementSize && !elementSpacingDefined) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.ElementSize
                                + "' but there was no tag '" + MiTag.NDims
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    float[] elementSize = TextUtil.parseFloatArray(tag.value);
                    if (elementSize.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DimSize
                                + "' does not match number of dimensions in tag '"
                                + MiTag.ElementSize + "'. Header line=" + lineNb + ".");
                    }
                    fileInfo.pixelWidth = elementSize[0];
                    fileInfo.pixelHeight = elementSize[1];
                    if (elementSize.length > 2) {
                        fileInfo.pixelDepth = elementSize[2];
                    }
                    fileInfo.unit = "mm";
                }
                // TAG: ElementType
                else if (tag.id == MiTag.ElementType) {
                    final MiElementType elementType;
                    try {
                        elementType = (MiElementType) MiElementType.MET_CHAR.byName(tag.value);
                    }
                    catch (IllegalArgumentException ex) {
                        throw new MiException("Invalid element type '" + line + "'.");
                    }
                    if (elementType == MiElementType.MET_UCHAR) {
                        fileInfo.fileType = FileInfo.GRAY8;
                    } else if (elementType == MiElementType.MET_SHORT) {
                        fileInfo.fileType = FileInfo.GRAY16_SIGNED;
                    } else if (elementType == MiElementType.MET_USHORT) {
                        fileInfo.fileType = FileInfo.GRAY16_UNSIGNED;
                    } else if (elementType == MiElementType.MET_INT) {
                        fileInfo.fileType = FileInfo.GRAY32_INT;
                    } else if (elementType == MiElementType.MET_FLOAT) {
                        fileInfo.fileType = FileInfo.GRAY32_FLOAT;
                    } else {
                        throw new MiException("Unsupported element type '" + line + "'.");
                    }
                }
                // TAG: HeaderSize
                else if (tag.id == MiTag.HeaderSize) {
                    try {
                        int headerSize = Integer.parseInt(tag.value);
                        if (headerSize < 0) {
                            throw new MiException("Header size cannot be negative '"
                                    + line + "'.");
                        }
                        fileInfo.offset = headerSize;
                    }
                    catch (NumberFormatException ex) {
                        throw new MiException("Unable to parse value of tag '" + line
                                + "' as integer.");
                    }
                }
                // TAG: ElementDataFile
                else if (tag.id == MiTag.ElementDataFile) {
                    if ("LOCAL".compareTo(tag.value) == 0) {
                        throw new MiException(
                                "Unsupported format MetaImage format variant. Tag '"
                                        + line + "' is not supported.");
                    } else {
                        // Try to determine absolute path to image data
                        // Try relative path
                        File f = new File(file.getParent(), tag.value);
                        if (!f.exists()) {
                            // Assume 'absolute' path
                            f = new File(tag.value);
                        }
                        fileInfo.directory = f.getParent() + File.separator;
                        fileInfo.fileName = f.getName();
                    }
                    // ElementDataFile is always the last tag in the header.
                    break;
                }
            }

        }
        catch (Exception ex) {
            throw new MiException("Error parsing line "
                    + lineNb + " of the MetaImage header. " + ex.getMessage());
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException ex) {
                }
            }
        }

        return fileInfo;
    }


    /**
     * Helper class to represent a MetaImage tag and its value.
     *
     * @author Jarek Sacha
     * @created August 3, 2002
     */
    private static class MiTagValuePair {
        /**
         * Tag id
         */
        public MiTag id;
        /**
         * Tag value
         */
        public String value;
    }


    /**
     * Main processing method for the MiDecoder object
     *
     * @param arg Description of the Parameter
     */
    public void run(String arg) {
        try {
            // Get file name
            OpenDialog openDialog = new OpenDialog("Open as MetaImage...", arg);
            if (openDialog.getFileName() == null) {
                return;
            }

            File file = new File(openDialog.getDirectory(), openDialog.getFileName());
            try {
                IJ.showStatus("Opening MetaImage: " + file.getName());
                long tStart = System.currentTimeMillis();
                ImagePlus imp = MiDecoder.open(file);
                long tStop = System.currentTimeMillis();
                imp.show();
                IJ.showStatus("MetaImage loaded in " + (tStop - tStart) + " ms.");
            }
            catch (MiException ex) {
                ex.printStackTrace();
                IJ.showMessage(DIALOG_CAPTION, "Error opening image: '"
                        + file.getAbsolutePath() + "'\n" + ex.getMessage());
            }
            catch (Exception ex) {
                ex.printStackTrace();
                IJ.showMessage(DIALOG_CAPTION, "Error opening image: '"
                        + file.getAbsolutePath() + "'\n" + ex);
            }

        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
