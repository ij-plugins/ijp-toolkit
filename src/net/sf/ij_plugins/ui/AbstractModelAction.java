/***
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
 */

package net.sf.ij_plugins.ui;

import net.sf.ij_plugins.beans.Model;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Helper for implementing actions that interact with a (presentation) model.
 * The action is disabled when {@code model} is {@code null}.
 * The action listens to model changes and and revalidates enabled state.
 *
 * @author Jarek Sacha
 */
public abstract class AbstractModelAction<M extends Model> extends AbstractAction {

    private M model;
    private final ModelChangeListener changeListener = new ModelChangeListener();
    private static final long serialVersionUID = 1L;


    protected AbstractModelAction(final String name) {
        super(name);
    }


    protected AbstractModelAction(final String name, final M model) {
        this(name);
        setModel(model);
    }


    @Override
    public boolean isEnabled() {
        return /* super.isEnabled()  && */ model != null;
    }


    public M getModel() {
        return model;
    }


    public void setModel(final M model) {
        if (model == this.model) {
            return;
        }

        if (this.model != null) {
            this.model.removePropertyChangeListener(changeListener);
        }

        firePropertyChange("model", this.model, this.model = model);

        if (this.model != null) {
            this.model.addPropertyChangeListener(changeListener);
        }
        setEnabled(isEnabled());
    }


    private class ModelChangeListener implements PropertyChangeListener {
        public void propertyChange(final PropertyChangeEvent evt) {
            setEnabled(isEnabled());
        }
    }
}
