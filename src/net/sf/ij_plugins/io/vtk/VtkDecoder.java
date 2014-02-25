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

package net.sf.ij_plugins.io.vtk;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import net.sf.ij_plugins.io.IOUtils;

import java.io.*;
import java.util.StringTokenizer;


/**
 * Reads images form files in <a HREF="http://public.kitware.com/VTK/">VTK</a> format. Only VTK
 * files containing images (VTK structured points) are supported. <p>
 * <br>
 * Limitations <ul> <li> Files in ASCII format are always interpreted as containing
 * <code>float</code> pixels. </ul>
 *
 * @author Jarek Sacha
 * @since July 3, 2002
 */
public final class VtkDecoder {

    private static final int MAX_LINE_SIZE = 260;
    private static final int MAX_HEADER_SIZE = MAX_LINE_SIZE * (2 + 9);

    private FileInfo fileInfo;
    private Calibration calibration;
    private boolean asciiImageData;


    private VtkDecoder() {

    }


    public static ImagePlus open(final String fileName) throws VtkImageException {
        final File file = new File(fileName);
        return open(file);
    }


    /**
     * Load image from a VTK image file.
     *
     * @param file VTK image file.
     * @return Decoded image.
     * @throws VtkImageException In case of IO errors.
     */
    public static ImagePlus open(final File file) throws VtkImageException {
        final VtkDecoder vtkDecoder = new VtkDecoder();
        vtkDecoder.decodeHeader(file);
        return vtkDecoder.readImageData();
    }


    /**
     * Parse value if VTK tag as a string.
     *
     * @param line String containing VTK tag proceeded by a value.
     * @param tag  Expected VTK tag at the beginning of the line.
     * @return The tag's value
     * @throws VtkImageException When the <code>line</code> does not start with a given
     *                           <code>tag</code>.
     */
    private String parseValueAsString(final String line, final VtkTag tag)
            throws VtkImageException {
        if (tag == null) {
            return line;
        }

        final int index = line.indexOf(tag.toString());
        if (index < 0) {
            throw new VtkImageException("Line '" + line
                    + "' does not contain substring '" + tag.toString() + "'.");
        }

        final String value = line.substring(index + tag.toString().length());
        return value.trim();
    }


    /**
     * Parse value if VTK tag as an array of integers.
     *
     * @param line     String containing VTK tag proceeded by a value.
     * @param tag      Expected VTK tag at the beginning of the line.
     * @param nbTokens Expected number of elements in the array.
     * @return The tag's value
     * @throws VtkImageException When the <code>line</code> does not start with a given
     *                           <code>tag</code>, or unable to parse tag value as an array of
     *                           integers.
     */
    private int[] parseValueAsIntArray(final String line, final VtkTag tag, final int nbTokens)
            throws VtkImageException {

        final String valueStr = parseValueAsString(line, tag);
        final StringTokenizer st = new StringTokenizer(valueStr);
        if (st.countTokens() != nbTokens) {
            throw new VtkImageException("Expecting " + nbTokens + " tokens, got "
                    + st.countTokens() + ".");
        }
        final int[] r = new int[nbTokens];
        String token = null;
        try {
            for (int i = 0; i < nbTokens; ++i) {
                token = st.nextToken();
                r[i] = Integer.parseInt(token);
            }
        } catch (final NumberFormatException ex) {
            throw new VtkImageException("Unable to parse token '" + token + "' as integer.", ex);
        }

        return r;
    }


    /**
     * Parse value if VTK tag as an array of floats.
     *
     * @param line     String containing VTK tag proceeded by a value.
     * @param tag      Expected VTK tag at the begriming of the line.
     * @param nbTokens Expected number of elements in the array.
     * @return The tag's value
     * @throws VtkImageException When the <code>line</code> does not start with a given
     *                           <code>tag</code>, or unable to parse tag value as an array of
     *                           floats.
     */
    private float[] parseValueAsFloatArray(final String line, final VtkTag tag, final int nbTokens)
            throws VtkImageException {

        final String valueStr = parseValueAsString(line, tag);
        final StringTokenizer st = new StringTokenizer(valueStr);
        if (st.countTokens() != nbTokens) {
            throw new VtkImageException("Expecting " + nbTokens + " tokens, got "
                    + st.countTokens() + ".");
        }
        final float[] r = new float[nbTokens];
        String token = null;
        try {
            for (int i = 0; i < nbTokens; ++i) {
                token = st.nextToken();
                r[i] = Float.parseFloat(token);
            }
        } catch (final NumberFormatException ex) {
            throw new VtkImageException("Unable to parse token '" + token + "' as integer.", ex);
        }

        return r;
    }


    /**
     * Open file VTK image file and decode header information. Decoded information is stored in
     * <code>fileInfo</code> and <code>calibration</code> member variables, so standard ImageJ
     * methods can be used to read image data. <code>calibration</code> stores pixel dimensions and
     * image origin information.
     *
     * @param file VTK image file.
     * @throws VtkImageException In case of I/O errors or incorrect header format.
     */
    private void decodeHeader(final File file) throws VtkImageException {
        // Reset values of modified member variables
        fileInfo = null;
        calibration = null;

        final FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (final FileNotFoundException e) {
            throw new VtkImageException(e.getMessage(), e);
        }

        final byte[] headerBuffer;
        final int headerBufferSize;
        try {
            // Load header buffer
            headerBuffer = new byte[MAX_HEADER_SIZE];
            headerBufferSize = fileInputStream.read(headerBuffer);
            if (headerBufferSize < 0) {
                throw new IOException("File too short. Cannot read header from: "
                        + file.getName());
            }
        } catch (final IOException ex) {
            throw new VtkImageException(ex.getMessage(), ex);
        } finally {
            try {
                fileInputStream.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        // Allocate new output variables
        fileInfo = new FileInfo();
        calibration = new Calibration();

        //
        // Parse header from buffer
        //
        final LineExtractor lineExtractor = new LineExtractor(headerBuffer, 0, headerBufferSize);
        fileInfo.fileName = file.getName();
        fileInfo.directory = file.getParent();
        if (fileInfo.directory == null) {
            // ij.io.FileOpener may choke on null
            fileInfo.directory = "";
        }
        if (fileInfo.directory != null && fileInfo.directory.length() > 0) {
            fileInfo.directory += File.separator;
        }

        String line = lineExtractor.nextNonEmptyLine();
        // Data file version
        if (!line.startsWith(VtkTag.DATA_FILE_VERSION.toString())) {
            throw new VtkImageException("File does not start with VTK header '"
                    + VtkTag.DATA_FILE_VERSION + "'. Get: " + line);
        }
//        String version = parseValueAsString(line, VtkTag.DATA_FILE_VERSION);

        // Image name
        line = lineExtractor.nextNonEmptyLine();
        if (line.length() > 256) {
            throw new VtkImageException("File too short, unable to read header.");
        }
        fileInfo.description = line;

        // Data type
        line = lineExtractor.nextNonEmptyLine();
        if (line.compareToIgnoreCase(VtkDataFormat.ASCII.toString()) == 0) {
            asciiImageData = true;
        } else if (line.compareToIgnoreCase(VtkDataFormat.BINARY.toString()) == 0) {
            asciiImageData = false;
        } else {
            throw new VtkImageException(
                    "File format error. Expecting either '" + VtkDataFormat.ASCII
                            + "' or '" + VtkDataFormat.BINARY + "'. Got: '" + line
                            + "'. Line number: " + lineExtractor.getCurrentLineNumber()
            );
        }

        // Geometry/topology
        line = lineExtractor.nextNonEmptyLine();
        if (line.startsWith(VtkTag.DATASET.toString())) {
            final String dataSet = parseValueAsString(line, VtkTag.DATASET);
            if (dataSet.trim().compareToIgnoreCase(
                    VtkDataSetType.STRUCTURED_POINTS.toString()) != 0) {
                throw new VtkImageException(
                        "File format error, incorrect data set type. Expecting '"
                                + VtkDataSetType.STRUCTURED_POINTS + "', got '" + dataSet.trim()
                                + "'. Line number: "
                                + lineExtractor.getCurrentLineNumber()
                );
            }
        } else {
            throw new VtkImageException(
                    "File format error. Expecting tag '" + VtkTag.DATASET
                            + "', got: '" + line
                            + "'. Line number: " + lineExtractor.getCurrentLineNumber()
            );
        }

        try {
            line = lineExtractor.nextLine(false).trim();
            while (line != null && line.length() == 0) {
                line = lineExtractor.nextLine(true).trim();
            }
            while (line != null) {
                if (line.startsWith(VtkTag.DIMENSIONS.toString())) {
                    // DIMENSIONS
                    final int[] dim = parseValueAsIntArray(line, VtkTag.DIMENSIONS, 3);
                    if (dim[0] < 1 || dim[1] < 1 || dim[2] < 1) {
                        throw new VtkImageException("Invalid image dimension: " +
                                dim[0] + "x" + dim[1] + "x" + dim[2] + ".");
                    }
                    fileInfo.width = dim[0];
                    fileInfo.height = dim[1];
                    fileInfo.nImages = dim[2];
                } else if (line.startsWith(VtkTag.ORIGIN.toString())) {
                    // ORIGIN
                    final float[] imageOrigin = parseValueAsFloatArray(line, VtkTag.ORIGIN, 3);
                    calibration.xOrigin = imageOrigin[0];
                    calibration.yOrigin = imageOrigin[1];
                    calibration.zOrigin = imageOrigin[2];
                } else if (line.startsWith(VtkTag.SPACING.toString())) {
                    // SPACING
                    final float[] spacing = parseValueAsFloatArray(line, VtkTag.SPACING, 3);
                    if (spacing[0] <= 0 || spacing[1] <= 0 || spacing[2] < 0) {
                        throw new VtkImageException("Invalid image spacing: " +
                                spacing[0] + "x" + spacing[1] + "x" + spacing[2] + ".");
                    }
                    fileInfo.pixelWidth = spacing[0];
                    fileInfo.pixelHeight = spacing[1];
                    fileInfo.pixelDepth = spacing[2];
                    fileInfo.unit = "pixel";
                    calibration.pixelWidth = spacing[0];
                    calibration.pixelHeight = spacing[1];
                    calibration.pixelDepth = spacing[2];
                    calibration.setUnit("pixel");
                } else if (line.startsWith(VtkTag.ASPECT_RATIO.toString())) {
                    // ASPECT_RATIO
                    final float[] spacing = parseValueAsFloatArray(line, VtkTag.ASPECT_RATIO, 3);
                    if (spacing[0] <= 0 || spacing[1] <= 0 || spacing[2] < 0) {
                        throw new VtkImageException("Invalid image aspect ratio: " +
                                spacing[0] + "x" + spacing[1] + "x" + spacing[2] + ".");
                    }
                    fileInfo.pixelWidth = spacing[0];
                    fileInfo.pixelHeight = spacing[1];
                    fileInfo.pixelDepth = spacing[2];
                    fileInfo.unit = "pixel";
                    calibration.pixelWidth = spacing[0];
                    calibration.pixelHeight = spacing[1];
                    calibration.pixelDepth = spacing[2];
                    calibration.setUnit("pixel");
                } else if (line.startsWith(VtkTag.CELL_DATA.toString())) {
                    // CELL_DATA
                    // ignore this tag.
                } else if (line.startsWith(VtkTag.POINT_DATA.toString())) {
                    // POINT_DATA
                    final int[] nbPoints = parseValueAsIntArray(line, VtkTag.POINT_DATA, 1);
                    if (nbPoints[0] < 0) {
                        throw new VtkImageException("Invalid number of image pixels: " + nbPoints[0] + ".");
                    }
                } else if (line.startsWith(VtkTag.SCALARS.toString())) {
                    // SCALARS
                    final StringTokenizer st = new StringTokenizer(line.substring(VtkTag.SCALARS.toString().length()));
                    if (st.hasMoreTokens()) {
//                        String dataName = st.nextToken();
                        st.nextToken();
                    } else {
                        throw new VtkImageException("Error parsing header tag: '"
                                + VtkTag.SCALARS + "'. Cannot extract dataName.");
                    }
                    if (st.hasMoreTokens()) {
                        final String dataType = st.nextToken();
                        if (dataType.compareToIgnoreCase(VtkScalarType.UNSIGNED_CHAR.toString()) == 0) {
                            fileInfo.fileType = FileInfo.GRAY8;
                        } else if (dataType.compareToIgnoreCase(VtkScalarType.SHORT.toString()) == 0) {
                            //@todo: Should calibration function be set here?
                            fileInfo.fileType = FileInfo.GRAY16_SIGNED;
                        } else if (dataType.compareToIgnoreCase(VtkScalarType.UNSIGNED_SHORT.toString()) == 0) {
                            fileInfo.fileType = FileInfo.GRAY16_UNSIGNED;
                        } else if (dataType.compareToIgnoreCase(VtkScalarType.INT.toString()) == 0) {
                            fileInfo.fileType = FileInfo.GRAY32_INT;
                        } else if (dataType.compareToIgnoreCase(VtkScalarType.FLOAT.toString()) == 0) {
                            fileInfo.fileType = FileInfo.GRAY32_FLOAT;
                        } else {
                            throw new VtkImageException("Unsupported data type: '" + dataType + "'.");
                        }
                    } else {
                        throw new VtkImageException("Error parsing header tag: '"
                                + VtkTag.SCALARS + "'. Cannot extract dataType.");
                    }
                    if (st.hasMoreTokens()) {
                        final String numComp = st.nextToken().trim();
                        if (numComp.length() > 0 && !numComp.startsWith("1")) {
                            throw new VtkImageException("Supported number of components for scalars is 1, got '"
                                    + numComp + "'.");
                        }
                    }
                } else if (line.startsWith(VtkTag.COLOR_SCALARS.toString())) {
                    // COLOR_SCALARS
                    final StringTokenizer st = new StringTokenizer(line.substring(VtkTag.COLOR_SCALARS.toString().length()));

                    // dataName
                    if (st.hasMoreTokens()) {
//                        String dataName = st.nextToken();
                        st.nextToken();
                    } else {
                        throw new VtkImageException("Error parsing header tag: '"
                                + VtkTag.COLOR_SCALARS + "'. Cannot extract dataName.");
                    }

                    // nValues
                    final int nValues;
                    if (st.hasMoreTokens()) {
                        final String nValuesStr = st.nextToken().trim();
                        nValues = Integer.parseInt(nValuesStr);
                    } else {
                        throw new VtkImageException("Error parsing header tag: '"
                                + VtkTag.COLOR_SCALARS + "'. Cannot extract number of values (nValues).");
                    }

                    switch (nValues) {
                        case 1:
                            fileInfo.fileType = FileInfo.GRAY8;
                            break;
                        case 3:
                            fileInfo.fileType = FileInfo.RGB;
                            break;
                        default:
                            throw new VtkImageException("Supported number of components for color scalars is 1 or 3, got '"
                                    + nValues + "'.");
                    }
                    //@todo: Should calibration function be reset here to NONE?
                    // COLOR_SCALARS should be the last tag line before the data.
                    break;
                } else if (line.startsWith(VtkTag.LOOKUP_TABLE.toString())) {
                    final String lookupTable = line.substring(VtkTag.LOOKUP_TABLE.toString().length());
                    if (lookupTable == null || !lookupTable.trim().equalsIgnoreCase("default")) {
                        throw new VtkImageException("Unsupported lookup table format. Expecting 'default', got '"
                                + lookupTable + "'.");
                    }
                    // LOOKUP_TABLE should be the last tag line before the data.
                    break;
                } else {
                    throw new VtkImageException("Unsupported file header tag '" + line + "'.");
                }

                line = lineExtractor.nextLine(false).trim();
                while (line != null && line.length() == 0) {
                    line = lineExtractor.nextLine(true).trim();
                }
            }
        } catch (final VtkImageException ex) {
            throw new VtkImageException("Error processing line "
                    + lineExtractor.getCurrentLineNumber() + ". " + ex.getMessage(), ex);
        }

        fileInfo.intelByteOrder = (lineExtractor.getNewLineMode() == LineExtractor.NEW_LINE_MODE_PC);
        fileInfo.offset = lineExtractor.getStartOfNextLine();

        if (IJ.debugMode) {
            IJ.log(fileInfo.toString());
            IJ.log(calibration.toString());
        }
    }


    private ImagePlus readImageData() throws VtkImageException {
        final ImagePlus imp;
        if (!asciiImageData) {
            // Read binary image data
            final FileOpener fileOpener = new FileOpener(fileInfo);
            imp = fileOpener.open(false);
            if (imp == null) {
                throw new VtkImageException("Unable to read image data.");
            }
        } else {
            // Read ASCII image data
            final BufferedReader reader;
            try {
                final File file = new File(fileInfo.directory, fileInfo.fileName);
                reader = new BufferedReader(new FileReader(file));
            } catch (final FileNotFoundException e) {
                throw new VtkImageException(e.getMessage(), e);
            }

            try {
                IOUtils.skip(reader, fileInfo.offset);
                IJ.showProgress(0);
                final ImageStack stack = new ImageStack(fileInfo.width, fileInfo.height);
                final int sliceSize = fileInfo.width * fileInfo.height;
                for (int i = 0; i < fileInfo.nImages; ++i) {
                    final FloatProcessor fp = new FloatProcessor(fileInfo.width, fileInfo.height);
                    readAsText(reader, sliceSize, (float[]) fp.getPixels());
                    stack.addSlice(null, fp);
                    IJ.showProgress((double) i / fileInfo.nImages);
                }
                imp = new ImagePlus(fileInfo.fileName, stack);
                IJ.showProgress(1);
            } catch (final IOException ex) {
                throw new VtkImageException("Error opening VTK image file.\n"
                        + ex.getMessage(), ex);
            } finally {
                try {
                    reader.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }

        final Calibration impCalibration = imp.getCalibration();
        impCalibration.pixelWidth = calibration.pixelWidth;
        impCalibration.pixelHeight = calibration.pixelHeight;
        impCalibration.pixelDepth = calibration.pixelDepth;
        impCalibration.xOrigin = calibration.xOrigin;
        impCalibration.yOrigin = calibration.yOrigin;
        impCalibration.zOrigin = calibration.zOrigin;
        imp.setCalibration(impCalibration);

        return imp;
    }


    private void readAsText(final Reader r, final int size, final float[] pixels) throws IOException {
        final StreamTokenizer tok = new StreamTokenizer(r);
        tok.resetSyntax();
        tok.wordChars(33, 255);
        tok.whitespaceChars(0, ' ');
        tok.parseNumbers();

        int i = 0;
        while (tok.nextToken() != StreamTokenizer.TT_EOF) {
            if (tok.ttype == StreamTokenizer.TT_NUMBER) {
                pixels[i++] = (float) tok.nval;
                if (i == size) {
                    break;
                }
            }
        }
        IJ.showProgress(1.0);
    }


    /**
     * Extract lines of text from a binary buffer.
     *
     * @author Jarek Sacha
     * @since June 21, 2002
     */
    private static final class LineExtractor {

        private static final int NEW_LINE_MODE_NULL = 0;
        private static final int NEW_LINE_MODE_UNKNOWN = -1;
        private static final int NEW_LINE_MODE_MAC = 1;
        private static final int NEW_LINE_MODE_PC = 2;
        private static final int NEW_LINE_MODE_UNIX = 3;
        // '\n'
        private static final char LINE_FEED_CHAR = 13;
        // '\r'
        private static final byte CARRIAGE_RETURN_CHAR = 10;

        private final byte[] buffer;
        private final int dataOffset;
        private final int dataLength;

        private int startOfCurrentLine;
        private int startOfNextLine;
        private int endOfCurrentLine;
        private int lineNumber = -1;
        private String currentLine = null;
        private int newLineMode = NEW_LINE_MODE_NULL;


        /**
         * Constructor for the LineExtractor object
         *
         * @param buffer Buffer containing lines to extract.
         * @param offset Offset at which to start the extraction.
         * @param length Number of bytes to consider for extraction.
         */
        public LineExtractor(final byte[] buffer, final int offset, final int length) {
            this.buffer = buffer;
            this.dataOffset = offset;
            this.dataLength = length;
            this.startOfCurrentLine = offset;
        }


        /**
         * Gets the startOfNextLine attribute of the LineExtractor object
         *
         * @return The startOfNextLine value
         */
        public int getStartOfNextLine() {
            return startOfNextLine;
        }


        /**
         * Current guess about the OS type on which the file was written, based how the text lines
         * are ended. This can be used to guess byte order, For PC it is LSB, for MAC and UNIX it is
         * MSB.
         *
         * @return The NewLineMode value
         */
        public int getNewLineMode() {
            return newLineMode;
        }


        /**
         * Return most recent extracted line.
         *
         * @return String representation of the current line.
         * @throws VtkImageException If no lines were extracted.
         */
        public String getCurrentLine() throws VtkImageException {
            if (lineNumber < 0) {
                return nextLine(false);
            } else {
                return currentLine;
            }
        }


        /**
         * Get number of new line characters following current line.
         *
         * @return The currentNumberOfNewLineChars value
         */
        public int getCurrentNumberOfNewLineChars() {
            return (startOfNextLine - endOfCurrentLine);
        }


        /**
         * Return first of the current new line characters.
         *
         * @return The currentNewLineChar1 value
         */
        public byte getCurrentNewLineChar1() {
            return buffer[endOfCurrentLine];
        }


        /**
         * Return second of the current new line characters (if exists).
         *
         * @return Second of the current new line characters.
         */
        public byte getCurrentNewLineChar2() {
            return buffer[endOfCurrentLine + 1];
        }


        /**
         * Return number of the current line (total number of extracted lines so far).
         *
         * @return The LineNumber value
         */
        public int getCurrentLineNumber() {
            return lineNumber;
        }


        public String nextNonEmptyLine() throws VtkImageException {
            String line = nextLine(true);
            while (line.length() == 0) {
                line = nextLine(true);
            }

            return line;
        }


        /**
         * Extract next line. White space is trimmed from the line.
         *
         * @param lineMustBePresent If 'true' an exception will be thrown if line cannot be extracted.
         * @return Text representation of the extracted line, 'null' if no line was extracted.
         * @throws VtkImageException If a line can not be extracted and <code>lineMustBePresent</code>
         *                           is 'true'.
         */
        public String nextLine(final boolean lineMustBePresent) throws VtkImageException {
            int newPosition = startOfNextLine;
            while (newPosition < buffer.length
                    && buffer[newPosition] != CARRIAGE_RETURN_CHAR
                    && buffer[newPosition] != LINE_FEED_CHAR) {
                ++newPosition;
            }

            if (newPosition >= buffer.length) {
                // Cannot extract complete line.
                currentLine = null;
                if (lineMustBePresent) {
                    throw new VtkImageException(
                            "Unexpected end of buffer. Cannot extract complete line.");
                } else {
                    return currentLine;
                }
            }

            ++lineNumber;
            startOfCurrentLine = startOfNextLine;
            endOfCurrentLine = newPosition;
            if ((newPosition + 1) < buffer.length) {
                final byte nextChar = buffer[newPosition + 1];
                if ((nextChar == CARRIAGE_RETURN_CHAR
                        || nextChar == LINE_FEED_CHAR)
                        && nextChar != buffer[newPosition]) {
                    startOfNextLine = newPosition + 2;
                } else {
                    startOfNextLine = newPosition + 1;
                }
            }

            currentLine = new String(buffer, startOfCurrentLine,
                    (endOfCurrentLine - startOfCurrentLine));

            // Update guess about new line
            if (getCurrentNumberOfNewLineChars() == 2
                    && getCurrentNewLineChar1() == LINE_FEED_CHAR
                    && getCurrentNewLineChar2() == CARRIAGE_RETURN_CHAR) {
                if (newLineMode == NEW_LINE_MODE_NULL
                        || newLineMode == NEW_LINE_MODE_PC) {
                    newLineMode = NEW_LINE_MODE_PC;
                } else {
                    newLineMode = NEW_LINE_MODE_UNKNOWN;
                }
            } else if (getCurrentNumberOfNewLineChars() == 1) {
                if (getCurrentNewLineChar1() == LINE_FEED_CHAR
                        && (newLineMode == NEW_LINE_MODE_NULL
                        || newLineMode == NEW_LINE_MODE_MAC)) {
                    newLineMode = NEW_LINE_MODE_MAC;
                } else if (getCurrentNewLineChar1() == CARRIAGE_RETURN_CHAR
                        && (newLineMode == NEW_LINE_MODE_NULL
                        || newLineMode == NEW_LINE_MODE_UNIX)) {
                    newLineMode = NEW_LINE_MODE_UNIX;
                }
            } else {
                newLineMode = NEW_LINE_MODE_UNKNOWN;
            }
            return currentLine.trim();
        }
    }
}
