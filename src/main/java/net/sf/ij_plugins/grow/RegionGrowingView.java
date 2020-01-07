/*
 * IJ-Plugins
 * Copyright (C) 2002-2020 Jarek Sacha
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
package net.sf.ij_plugins.grow;

import net.sf.ij_plugins.ui.multiregion.MultiRegionManagerView;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * @author Jarek Sacha
 * @since February 18, 2008
 */
public class RegionGrowingView extends JPanel {

    private final RunAction runAction;
    private final SeedImageAction seedImageAction;
    private final HelpAction helpAction;
    private static final long serialVersionUID = 4217582431710992600L;

    /**
     * Creates new form RegionGrowingView
     */
    public RegionGrowingView() {
        final RegionGrowingModel model = new RegionGrowingModel(multiRegionManagerView.getModel());
        runAction = new RunAction(model, this);
        seedImageAction = new SeedImageAction(model, this);
        helpAction = new HelpAction(model, this);

        initComponents();
        numberOfAnimationFramesSpinner.setModel(model.getNumberOfAnimationFramesSM());
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        final JSeparator seedSelectionSeparator = new JSeparator();
        final JLabel seedSelectionLabel = new JLabel();
        final JSeparator bottomSeparator = new JSeparator();
        final JButton runButton = new JButton();
        final JLabel optionsLabel = new JLabel();
        final JSeparator optionsSeparator = new JSeparator();
        final JLabel numberOfAnimationFramesLabel = new JLabel();
        numberOfAnimationFramesSpinner = new JSpinner();
        final JButton seedImageButton = new JButton();
        final JButton helpButton = new JButton();

        seedSelectionLabel.setText("Seed Selection");

        runButton.setAction(runAction);
        runButton.setIcon(new ImageIcon(getClass().getResource("/net/sf/ij_plugins/grow/run.png"))); // NOI18N
        runButton.setToolTipText("Run Seeded Region Growing");

        optionsLabel.setText("Options");

        numberOfAnimationFramesLabel.setText("Number of animation frames");

        seedImageButton.setAction(seedImageAction);
        seedImageButton.setText("Seed Image");
        seedImageButton.setToolTipText("Create image of current seeds.");

        helpButton.setAction(helpAction);
        helpButton.setText("Help");
        helpButton.setToolTipText("Open help page link in the browser.");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                .addComponent(multiRegionManagerView, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(numberOfAnimationFramesLabel)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(numberOfAnimationFramesSpinner, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(seedSelectionLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(seedSelectionSeparator, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(optionsLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(optionsSeparator, GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
                                .addComponent(bottomSeparator, GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                                .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(runButton)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(seedImageButton)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(helpButton)
                                )
                        )
                        .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, runButton, seedImageButton, helpButton);

        layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                .addComponent(seedSelectionLabel)
                                .addComponent(seedSelectionSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addComponent(multiRegionManagerView, GroupLayout.PREFERRED_SIZE, 223, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                .addComponent(optionsLabel)
                                .addComponent(optionsSeparator, GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                .addComponent(numberOfAnimationFramesLabel)
                                                .addComponent(numberOfAnimationFramesSpinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGap(18, 18, 18)
                                        .addComponent(bottomSeparator, GroupLayout.PREFERRED_SIZE, 9, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(runButton))
                                .addComponent(seedImageButton)
                                .addComponent(helpButton)
                        )
                        .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private final MultiRegionManagerView multiRegionManagerView = new MultiRegionManagerView();
    private JSpinner numberOfAnimationFramesSpinner;
    // End of variables declaration//GEN-END:variables

}
