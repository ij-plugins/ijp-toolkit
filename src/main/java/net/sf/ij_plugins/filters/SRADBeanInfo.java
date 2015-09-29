/*
 * Image/J Plugins
 * Copyright (C) 2002-2011 Jarek Sacha
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
package net.sf.ij_plugins.filters;

import net.sf.ij_plugins.IJPluginsRuntimeException;
import net.sf.ij_plugins.util.IJPluginsSimpleBeanInfo;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * @author Jarek Sacha
 */
public class SRADBeanInfo extends IJPluginsSimpleBeanInfo {

    public SRADBeanInfo() {
        super(SRAD.class);
    }


    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            return new PropertyDescriptor[]{
                    create("cThreshold", "Diffusion coefficient threshold",
                            "When diffusion coefficient is lower than threshold it is set to 0."),
                    create("q0", "Initial coefficient of variation", "Speckle coefficient of variation in the observer image, " +
                            "for correlated data it should be set to less than 1."),
                    create("ro", "Coefficient of variation decay rate",
                            "Spackle coefficient of variation decay rate: q0(t)=q0*exp(-ro*t), ro < 1"),
            };
        } catch (final IntrospectionException e) {
            throw new IJPluginsRuntimeException(e);
        }
    }

    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[]{Introspector.getBeanInfo(beanClass.getSuperclass())};
        } catch (final IntrospectionException e) {
            throw new IJPluginsRuntimeException(e);
        }
    }
}
