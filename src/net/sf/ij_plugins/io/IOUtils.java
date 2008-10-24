/*
 * Image/J Plugins
 * Copyright (C) 2002-2008 Jarek Sacha
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
 *
 */
package net.sf.ij_plugins.io;

import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.IOException;

/**
 * I/O utilities.
 *
 * @author Jarek Sacha
 */
public class IOUtils {

    private IOUtils() {
    }


    /**
     * Open an image using ImageJ image reader.
     *
     * @param file image file.
     * @return read image
     * @throws IOException when image cannot be open.
     */
    public static ImagePlus openImage(final File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Image does not exist: '" + file.getAbsolutePath() + "'.");
        }

        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(file.getAbsolutePath());
        if (imp == null) {
            throw new IOException("Cannot open image: '" + file.getAbsolutePath() + "'.");
        }

        return imp;
    }


    /**
     * Open an image using ImageJ image reader.
     *
     * @param fileName image file name.
     * @return read image
     * @throws IOException when image cannot be open.
     * @see #openImage(java.io.File)
     */
    public static ImagePlus openImage(final String fileName) throws IOException {
        return openImage(new File(fileName));
    }


    /**
     * Save image to a file using image TIFF encoder.
     *
     * @param imp  image.
     * @param file destination file.
     * @throws IOException when image cannot be saved.
     */
    public static void saveAsTiff(final ImagePlus imp, final File file) throws IOException {
        final FileSaver fileSaver = new FileSaver(imp);
        final boolean ok;
        if (imp.getStackSize() > 1) {
            ok = fileSaver.saveAsTiffStack(file.getAbsolutePath());
        } else {
            ok = fileSaver.saveAsTiff(file.getAbsolutePath());
        }
        if (!ok) {
            throw new IOException("Error saving image to file: '" + file.getAbsolutePath() + "'.");
        }
    }


    /**
     * Save image to a file using image TIFF encoder.
     *
     * @param ip   image.
     * @param file destination file.
     * @throws IOException when image cannot be saved.
     */
    public static void saveAsTiff(final ImageProcessor ip, final File file) throws IOException {
        saveAsTiff(new ImagePlus(file.getName(), ip), file);
    }
}
