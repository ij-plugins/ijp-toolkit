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
package ij_plugins.toolkit.util;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * @author Jarek Sacha
 */
abstract public class IJPluginsSimpleBeanInfo extends SimpleBeanInfo {

    protected final Class<?> beanClass;

    protected IJPluginsSimpleBeanInfo(final Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    protected PropertyDescriptor create(final String propertyName) throws IntrospectionException {
        return create(propertyName, null, null);
    }

    protected PropertyDescriptor create(final String propertyName, final String displayName)
            throws IntrospectionException {

        return create(propertyName, displayName, null);
    }

    protected PropertyDescriptor create(final String propertyName,
                                        final String displayName,
                                        final String shortDescription) throws IntrospectionException {

        final PropertyDescriptor r = new PropertyDescriptor(propertyName, beanClass);

        if (displayName != null) {
            r.setDisplayName(displayName);
        }

        if (shortDescription != null) {
            r.setShortDescription(shortDescription);
        }

        return r;
    }
}
