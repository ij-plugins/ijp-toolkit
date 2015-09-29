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

package net.sf.ij_plugins.util;

import ij.gui.GenericDialog;
import net.sf.ij_plugins.IJPluginsRuntimeException;
import org.apache.commons.beanutils.PropertyUtils;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;


/**
 * Utilities for simplifying creation of property editor dialogs.
 *
 * @author Jarek Sacha
 * @deprecated scheduled for removal
 */
@Deprecated
public class DialogUtil {

    private DialogUtil() {
    }


    /**
     * Utility to automatically create ImageJ's GenericDialog for editing bean properties. It uses BeanInfo to extract
     * display names for each field. If a fields type is not supported its name will be displayed with a tag
     * "[Unsupported type: class_name]".
     *
     * @param bean  Java bean for which to create dialog.
     * @param title dialog title.
     * @return <code>true</code> if user closed bean dialog using OK button, <code>false</code> otherwise.
     */
    public static boolean showGenericDialog(final Object bean, final String title) {
        final BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(bean.getClass());
        } catch (final IntrospectionException e) {
            throw new IJPluginsRuntimeException("Error extracting bean info.", e);
        }

        final GenericDialog genericDialog = new GenericDialog(title);

        // Create generic dialog fields for each bean's property
        final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        try {
            for (final PropertyDescriptor pd : propertyDescriptors) {
                final Class<?> type = pd.getPropertyType();
                if (type.equals(Class.class) && "class".equals(pd.getName())) {
                    continue;
                }

                final Object o = PropertyUtils.getSimpleProperty(bean, pd.getName());
                if (type.equals(Boolean.TYPE)) {
                    final boolean value = (Boolean) o;
                    genericDialog.addCheckbox(pd.getDisplayName(), value);
                } else if (type.equals(Integer.TYPE)) {
                    final int value = (Integer) o;
                    genericDialog.addNumericField(pd.getDisplayName(), value, 0);
                } else if (type.equals(Float.TYPE)) {
                    final double value = ((Float) o).doubleValue();
                    genericDialog.addNumericField(pd.getDisplayName(), value, 6, 10, "");
                } else if (type.equals(Double.TYPE)) {
                    final double value = (Double) o;
                    genericDialog.addNumericField(pd.getDisplayName(), value, 6, 10, "");
                } else {
                    genericDialog.addMessage(pd.getDisplayName() + "[Unsupported type: " + type + "]");
                }

            }
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IJPluginsRuntimeException(e);
        }

//        final Panel helpPanel = new Panel();
//        helpPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
//        final Button helpButton = new Button("Help");
////                helpButton.addActionListener(this);
//        helpPanel.add(helpButton);
//        genericDialog.addPanel(helpPanel, GridBagConstraints.EAST, new Insets(15,0,0,0));

        // Show modal dialog
        genericDialog.showDialog();
        if (genericDialog.wasCanceled()) {
            return false;
        }

        // Read fields from generic dialog into bean's properties.
        try {
            for (final PropertyDescriptor pd : propertyDescriptors) {
                final Class<?> type = pd.getPropertyType();
                final Object propertyValue;
                if (type.equals(Boolean.TYPE)) {
                    propertyValue = genericDialog.getNextBoolean();
                } else if (type.equals(Integer.TYPE)) {
                    propertyValue = (int) Math.round(genericDialog.getNextNumber());
                } else if (type.equals(Float.TYPE)) {
                    final double value = genericDialog.getNextNumber();
                    propertyValue = new Float(value);
                } else if (type.equals(Double.TYPE)) {
                    propertyValue = genericDialog.getNextNumber();
                } else {
                    continue;
                }
                PropertyUtils.setProperty(bean, pd.getName(), propertyValue);
            }
        } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new IJPluginsRuntimeException(e);
        }

        return true;
    }
}
