/*
 * Image/J Plugins
 * Copyright (C) 2002-2010 Jarek Sacha
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

package net.sf.ij_plugins.ui;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
 * Demo of using {@link GlassPane}.
 * Based on example by Alexander Potochkin
 * http://weblogs.java.net/blog/alexfromsun/archive/2006/09/a_wellbehaved_g.html
 *
 * @author Jarek Sacha
 * @since Nov 15, 2008 8:09:11 AM
 */
public final class GlassPaneDemo extends JFrame {

    private static final long serialVersionUID = 1L;


    public GlassPaneDemo() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JMenuBar bar = new JMenuBar();
        final JMenu menu = new JMenu("Options");
        final JCheckBoxMenuItem showItem = new JCheckBoxMenuItem("Show GlassPane");
        showItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        showItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getGlassPane().setVisible(true);
                } else {
                    getGlassPane().setVisible(false);
                }
            }
        });

        menu.add(showItem);
        bar.add(menu);
        setJMenuBar(bar);

        setGlassPane(new GlassPane());

        add(createPanel());
        setSize(250, 250);
        setLocationRelativeTo(null);
    }


    private JComponent createPanel() {
        final JPanel panel = new JPanel();
        panel.add(new JLabel("<html><em>Press Ctrl-D to toggle glass pane</em></html>"));
        panel.add(new JCheckBox("JCheckBox"));
        panel.add(new JRadioButton("JRadioButton"));
        panel.add(new JTextField(15));
        panel.add(new JSlider());
        panel.add(new JCheckBox("JCheckBox"));
        panel.add(new JRadioButton("JRadioButton"));
        panel.add(new JTextField(15));
        panel.add(new JButton("JButton"));
        return panel;
    }


    public static void main(final String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                final GlassPaneDemo frame = new GlassPaneDemo();
                frame.setVisible(true);
            }
        });
    }

}
