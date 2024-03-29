/*
 *  IJ-Plugins
 *  Copyright (C) 2002-2021 Jarek Sacha
 *  Author's email: jpsacha at gmail dot com
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

package ij_plugins.toolkit.concurrent;

import ij.process.ImageProcessor;
import ij_plugins.toolkit.ui.progress.ProgressReporter4J;

import java.util.concurrent.Callable;


/**
 * @author Jarek Sacha
 * @since 3/8/11 3:18 PM
 */
public final class ProcessSlicesInParallel extends ProgressReporter4J {

    public static abstract class PCallable extends ProgressReporter4J implements Callable<ImageProcessor> {

        abstract ImageProcessor process() throws Exception;


        final public ImageProcessor call() throws Exception {
            return process();
        }
    }


}
