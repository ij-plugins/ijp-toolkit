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
package ij_plugins.toolkit.filters;

import ij_plugins.toolkit.IJPluginsRuntimeException;
import ij_plugins.toolkit.util.IJPluginsSimpleBeanInfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

/**
 * @author Jarek Sacha
 */
public class AbstractAnisotropicDiffusionBeanInfo extends IJPluginsSimpleBeanInfo {

    public AbstractAnisotropicDiffusionBeanInfo() {
        super(AbstractAnisotropicDiffusion.class);
    }

    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[]{
                    create("numberOfIterations", "Max iterations",
                            "Maximum number of iterations at which diffusion stops, " +
                                    "unless mean square error is reached first."),
                    create("timeStep", "Time step", "Time increment in each iteration."),
                    create("meanSquareError", "Mean square error limit",
                            "If the mean square error of images between two iterations is less than limit diffusion " +
                                    "stops, unless maximum number of iterations is reached first. "),
            };
        } catch (final IntrospectionException e) {
            throw new IJPluginsRuntimeException(e);
        }
    }
}
