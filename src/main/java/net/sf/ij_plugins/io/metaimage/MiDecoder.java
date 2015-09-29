/*
 * Image/J Plugins
 * Copyright (C) 2002-2013 Jarek Sacha
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

package net.sf.ij_plugins.io.metaimage;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.VirtualStack;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.RandomAccessStream;
import ij.plugin.FileInfoVirtualStack;
import ij.process.ImageProcessor;
import net.sf.ij_plugins.util.Pair;
import net.sf.ij_plugins.util.TextUtil;
import net.sf.ij_plugins.util.Validate;

import java.io.*;


/**
 * Decodes an image file in MetaImage format. MetaImage is one of the formats supported by ITK
 * (http://www.itk.org). More information about MetaImage, including C++ code, can be found at
 * http://caddlab.rad.unc.edu/technologies/MetaImage/ . This implementation is intended to be
 * compatible with ITK version of MetaImage.
 *
 * @author Jarek Sacha
 * @since July 31, 2002
 */
public final class MiDecoder {

    // TODO Validate MetaImage tag dependency (some tags need always be present, some only if other tags are present, etc.)


    /**
     * Symbol separating a tag from its value in the MetaImage header.
     */
    private static final String ASSIGNMENT_SYMBOL = "=";


    private MiDecoder() {
    }

    /**
     * Load image from a MetaImage file. Number of returned images is equal to the number of channels in the file.
     *
     * @param file MetaImage file.
     * @return Decoded image.
     * @throws MiException In case of IO errors.
     */
    public static ImagePlus[] open(final File file) throws MiException {
        return open(file, false);
    }

    /**
     * Load image from a MetaImage file. Number of returned images is equal to the number of channels in the file.
     *
     * @param file    MetaImage file.
     * @param virtual Specifies whether a virtual stack should be used.
     * @return Decoded image.
     * @throws MiException In case of IO errors.
     */
    public static ImagePlus[] open(final File file, final boolean virtual) throws MiException {
        final MiDecoder miDecoder = new MiDecoder();

        // Read image header
        final Pair<FileInfo, Integer> p = miDecoder.decodeHeader(file);
        final FileInfo fileInfo = p.getFirst();
        final int elementNumberOfChannels = p.getSecond();
        if (elementNumberOfChannels > 1) {
            // Trick FileOpener to read all channels, they will be later separated
            fileInfo.width *= elementNumberOfChannels;
        }

        if (IJ.debugMode) {
            IJ.log("FileInfo: " + fileInfo.toString());
        }

        if (elementNumberOfChannels > 1 && virtual) {
            throw new MiException("MetaImage Reader does not support virtual stacks for multi-channel images.");
        }

        // Read binary image data
        final ImagePlus imp;
        if (virtual) {
            final VirtualStack virtualStack = new FileInfoVirtualStack(fileInfo, false);
            imp = new ImagePlus(fileInfo.fileName, virtualStack);
        } else {
            final FileOpener fileOpener = new FileOpener(fileInfo);
            imp = fileOpener.open(false);
        }

        if (imp == null) {
            throw new MiException("Unable to read image data from '" + fileInfo.fileName + "'.");
        }

        if (elementNumberOfChannels > 1) {
            // Decode multiple channels
            // Values for each channel are stored next to each other.
            // Extract channels by copying every other pixel value to a separate image

            final int finalWidth = imp.getWidth() / elementNumberOfChannels;
            final int finalHeight = imp.getHeight();
            final ImageStack[] channelStacks = new ImageStack[elementNumberOfChannels];
            for (int c = 0; c < elementNumberOfChannels; c++) {
                channelStacks[c] = new ImageStack(finalWidth, finalHeight);
            }

            final ImageStack inputStack = imp.getStack();
            final int pixelsPerChannel = finalWidth * finalHeight;
            for (int slice = 1; slice <= imp.getStackSize(); slice++) {

                final ImageProcessor ip = inputStack.getProcessor(slice);
                final String label = inputStack.getSliceLabel(slice);

                // Copy pixel values to separate channels
                for (int c = 0; c < elementNumberOfChannels; c++) {
                    final ImageProcessor channel = ip.createProcessor(finalWidth, finalHeight);
                    for (int n = 0; n < pixelsPerChannel; n++) {
                        channel.setf(n, ip.getf(n * elementNumberOfChannels + c));
                    }
                    channelStacks[c].addSlice(label, channel);
                }
            }

            // Add ImagePlus wrapper to each channel's stack
            final ImagePlus[] imps = new ImagePlus[elementNumberOfChannels];
            for (int c = 0; c < elementNumberOfChannels; c++) {
                imps[c] = imp.createImagePlus();
                imps[c].setStack(channelStacks[c]);
                imps[c].setTitle(imp.getTitle() + " channel " + c);
            }

            return imps;

        } else {
            return new ImagePlus[]{imp};
        }
    }


    private MiTagValuePair extractTagAndValue(final String line) throws MiException {

        Validate.argumentNotNull(line, "line");

        final int position = line.indexOf(ASSIGNMENT_SYMBOL);
        if (position < 0) {
            throw new MiException(
                    "Missing tag or value: unable to locate the assignment symbol '"
                            + ASSIGNMENT_SYMBOL + "'.");
        }
        final String tagName = line.substring(0, position).trim();
        final MiTagValuePair tag = new MiTagValuePair();
        try {
            tag.id = (MiTag) MiTag.DIM_SIZE.byName(tagName);
        } catch (final IllegalArgumentException ex) {
            throw new MiException("'" + tagName + "' is not a valid MetaImage tag name.", ex);
        }

        tag.value = line.substring(position + 1, line.length()).trim();

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
    private Pair<FileInfo, Integer> decodeHeader(final File file) throws MiException {
        final FileInfo fileInfo = new FileInfo();
        int elementNumberOfChannels = 1;
        final BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (final FileNotFoundException ex) {
            throw new MiException("Error opening file " + ex.getMessage(), ex);
        }

        int lineNb = 0;
        boolean elementSpacingDefined = false;
        boolean localElementalData = false;
        try {
            String line = reader.readLine();
            // Keep track of line number for error reporting.
            int nDims = -1;
            // Iterate through header lines
            for (; line != null; line = reader.readLine()) {
                ++lineNb;

                // Parse line, throw exception if tag's name is invalid.
                final MiTagValuePair tag = extractTagAndValue(line);

                // TAG: NDim
                if (tag.id == MiTag.N_DIMS) {
                    nDims = TextUtil.parseInt(tag.value, -1);
                    if (nDims < 2 || nDims > 3) {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.N_DIMS + "'. Expecting value equal either 2 or 3, got '"
                                + tag.value + "'. Header line=" + lineNb + ".");
                    }
                }
                // TAG: DimSize
                else if (tag.id == MiTag.DIM_SIZE) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.DIM_SIZE
                                + "' but there was no tag '" + MiTag.N_DIMS
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    final int[] dimSize = TextUtil.parseIntArray(tag.value);
                    if (dimSize.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DIM_SIZE
                                + "' does not match number of dimensions in tag '" + MiTag.N_DIMS
                                + "'. Header line=" + lineNb + ".");
                    }

                    fileInfo.width = dimSize[0];
                    fileInfo.height = dimSize[1];
                    if (dimSize.length > 2) {
                        fileInfo.nImages = dimSize[2];
                    }
                }
                // TAG: BinaryDataByteOrderMSB
                else if (tag.id == MiTag.BINARY_DATA_BYTE_ORDER_MSB) {
                    if (tag.value.compareToIgnoreCase("true") == 0) {
                        fileInfo.intelByteOrder = false;
                    } else if (tag.value.compareToIgnoreCase("false") == 0) {
                        fileInfo.intelByteOrder = true;
                    } else {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.BINARY_DATA_BYTE_ORDER_MSB
                                + "'. Expecting either 'true' or 'false', got '"
                                + tag.value + "'.");
                    }
                }
                // TAG: ElementByteOrderMSB
                else if (tag.id == MiTag.COMPRESSED_DATA) {
                    if (tag.value.compareToIgnoreCase("false") != 0) {
                        throw new MiException("Data compression not supported. Got '"
                                + MiTag.COMPRESSED_DATA
                                + "="
                                + tag.value + "'.");
                    }
                }
                // TAG: ElementByteOrderMSB
                else if (tag.id == MiTag.ELEMENT_BYTE_ORDER_MSB) {
                    if (tag.value.compareToIgnoreCase("true") == 0) {
                        fileInfo.intelByteOrder = false;
                    } else if (tag.value.compareToIgnoreCase("false") == 0) {
                        fileInfo.intelByteOrder = true;
                    } else {
                        throw new MiException("Invalid value of header tag '"
                                + MiTag.ELEMENT_BYTE_ORDER_MSB
                                + "'. Expecting either 'true' or 'false', got '"
                                + tag.value + "'.");
                    }
                }
                // TAG: ElementSpacing
                else if (tag.id == MiTag.ELEMENT_SPACING) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.ELEMENT_SPACING
                                + "' but there was no tag '" + MiTag.N_DIMS
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    final float[] elementSpacing = TextUtil.parseFloatArray(tag.value);
                    if (elementSpacing.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DIM_SIZE
                                + "' does not match number of dimensions in tag '"
                                + MiTag.ELEMENT_SPACING + "'. Header line=" + lineNb + ".");
                    }
                    fileInfo.pixelWidth = elementSpacing[0];
                    fileInfo.pixelHeight = elementSpacing[1];
                    if (elementSpacing.length > 2) {
                        fileInfo.pixelDepth = elementSpacing[2];
                    }
                    fileInfo.unit = "mm";
                    elementSpacingDefined = true;

                }
                // TAG: ElementNumberOfChannels
                else if (tag.id == MiTag.ELEMENT_NUMBER_OF_CHANNELS) {
                    elementNumberOfChannels = TextUtil.parseInt(tag.value, 1);
                    if (elementNumberOfChannels < 1) {
                        throw new MiException("Number of channels in '"
                                + MiTag.ELEMENT_NUMBER_OF_CHANNELS
                                + "' cannot be less than 1, got " + elementNumberOfChannels
                                + "'. Header line=" + lineNb + ".");
                    }
//                    fileInfo.samplesPerPixel = elementNumberOfChannels;
                }
                // TAG: ElementSize
                else if (tag.id == MiTag.ELEMENT_SIZE && !elementSpacingDefined) {
                    if (nDims == -1) {
                        throw new MiException("Got tag '" + MiTag.ELEMENT_SIZE
                                + "' but there was no tag '" + MiTag.N_DIMS
                                + "' yet. Header line=" + lineNb + ".");
                    }

                    final float[] elementSize = TextUtil.parseFloatArray(tag.value);
                    if (elementSize.length != nDims) {
                        throw new MiException("Number of dimensions in tag '"
                                + MiTag.DIM_SIZE
                                + "' does not match number of dimensions in tag '"
                                + MiTag.ELEMENT_SIZE + "'. Header line=" + lineNb + ".");
                    }
                    fileInfo.pixelWidth = elementSize[0];
                    fileInfo.pixelHeight = elementSize[1];
                    if (elementSize.length > 2) {
                        fileInfo.pixelDepth = elementSize[2];
                    }
                    fileInfo.unit = "mm";
                }
                // TAG: ElementType
                else if (tag.id == MiTag.ELEMENT_TYPE) {
                    final MiElementType elementType;
                    try {
                        elementType = (MiElementType) MiElementType.MET_CHAR.byName(tag.value);
                    } catch (final IllegalArgumentException ex) {
                        throw new MiException("Invalid element type '" + line + "'.", ex);
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
                else if (tag.id == MiTag.HEADER_SIZE) {
                    try {
                        final int headerSize = Integer.parseInt(tag.value);
                        if (headerSize < 0) {
                            throw new MiException("Header size cannot be negative '" + line + "'.");
                        }
                        fileInfo.offset = headerSize;
                    } catch (final NumberFormatException ex) {
                        throw new MiException("Unable to parse value of tag '" + line + "' as integer.", ex);
                    }
                }
                // TAG: ElementDataFile
                else if (tag.id == MiTag.ELEMENT_DATA_FILE) {
                    if ("LOCAL".compareTo(tag.value) == 0) {
                        localElementalData = true;
                        fileInfo.directory = file.getAbsoluteFile().getParent() + File.separator;
                        fileInfo.fileName = file.getName();
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

        } catch (final IOException ex) {
            throw new MiException("Error parsing line " + lineNb + " of the MetaImage header. " + ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // If data is local, determine the offset
        if (localElementalData) {
            // Determine offset to image data, skip 'lineNb' line feeds

            final RandomAccessStream in;
            try {
                //noinspection IOResourceOpenedButNotSafelyClosed
                in = new RandomAccessStream(new BufferedInputStream(new FileInputStream(file)));
            } catch (final FileNotFoundException ex) {
                throw new MiException("Error reading file: '" + file.getAbsolutePath() + "'. " + ex.getMessage(), ex);
            }

            try {
                int lineFeedCount = 0;
                do {
                    final int v = in.read();
                    if (v == '\n') {
                        lineFeedCount++;
                    } else if (v == -1) {
                        throw new MiException("Unexpected end of file while searching for image data offset.");
                    }
                } while (lineFeedCount < lineNb);
                fileInfo.longOffset = in.getLongFilePointer();
            } catch (final IOException ex) {
                throw new MiException("Exception while locating offset to image data. " + ex.getMessage(), ex);
            } finally {
                try {
                    in.close();
                } catch (final IOException ex) {
                    ex.printStackTrace();
                }

            }

        }
        return new Pair<>(fileInfo, elementNumberOfChannels);
    }


    /**
     * Helper class to represent a MetaImage tag and its value.
     *
     * @author Jarek Sacha
     * @since August 3, 2002
     */
    private static class MiTagValuePair {

        /**
         * Tag id
         */
        private MiTag id;
        /**
         * Tag value
         */
        private String value;
    }
}
