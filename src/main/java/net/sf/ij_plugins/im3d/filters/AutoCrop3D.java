/*
 * IJ-Plugins
 * Copyright (C) 2002-2019 Jarek Sacha
 * Author's email: jpsacha at gmail dot com
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

package net.sf.ij_plugins.im3d.filters;

import ij.ImagePlus;
import ij.ImageStack;
import net.sf.ij_plugins.im3d.Box3D;
import net.sf.ij_plugins.im3d.Util;


/**
 * @author Jarek Sacha
 * @since 3/9/11 4:27 PM
 */
public final class AutoCrop3D {

    private AutoCrop3D() {
    }


    public static ImagePlus run(final ImagePlus imp) {
        final ImageStack src = imp.getStack();
        final Box3D bb = Util.getBoundingBox(src);
        final ImageStack dest = Util.clip(src, bb);

        final ImagePlus impDest = imp.createImagePlus();
        impDest.setStack(dest);
        impDest.setTitle(imp.getTitle() + "+AutoCrop");
        Util.offsetOrigin(impDest, bb.origin());

        return impDest;
    }
}
