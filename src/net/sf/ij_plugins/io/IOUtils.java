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
import ij.io.Opener;

import java.io.File;
import java.io.IOException;

/**
 * @author Jarek Sacha
 */
public class IOUtils {
    private IOUtils() {
    }

    public static ImagePlus openImage(final String fileName) throws IOException {
        final File file = new File(fileName);
        if (!file.exists()) {
            throw new IOException("Image does not exist: " + file.getAbsolutePath());
        }

        final Opener opener = new Opener();
        final ImagePlus imp = opener.openImage(file.getAbsolutePath());
        if (imp == null) {
            throw new IOException("Cannot open image: " + file.getAbsolutePath());
        }

        return imp;
    }
}
