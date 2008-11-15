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
package net.sf.ij_plugins.ui;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

/**
 * Demo of using {@link GlassPane}.
 * Based on example by Alexander Potochkin
 * http://weblogs.java.net/blog/alexfromsun/archive/2007/06/_enablingdisabl_1.html
 *
 * @author Jarek Sacha
 * @since Nov 15, 2008 8:09:11 AM
 */
public final class GlassPaneDemo extends JFrame {
    //    private JComponent initialGlassPane = new InitialGlassPane();
    private JComponent betterGlassPane = new GlassPane();

    public GlassPaneDemo() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar bar = new JMenuBar();
        JMenu menu = new JMenu("Options");
        final JCheckBoxMenuItem showItem = new JCheckBoxMenuItem("Show GlassPane");
        showItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        showItem.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    getGlassPane().setVisible(true);
                } else {
                    getGlassPane().setVisible(false);
                }

                // Request focus to the better GlassPane
                if (betterGlassPane.isShowing()) {
                    betterGlassPane.requestFocusInWindow();
                }
            }
        });

        menu.add(showItem);
        menu.addSeparator();

        ButtonGroup group = new ButtonGroup();

        final JMenuItem item1 = new JRadioButtonMenuItem("Initial GlassPane");
        item1.setSelected(true);
        group.add(item1);
        menu.add(item1);

        final JMenuItem item2 = new JRadioButtonMenuItem("Better GlassPane");
        group.add(item2);
        menu.add(item2);

        ItemListener menuListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (item1.isSelected()) {
                    setGlassPane(betterGlassPane);
                } else {
                    setGlassPane(betterGlassPane);
                }
            }
        };
        item1.addItemListener(menuListener);
        item2.addItemListener(menuListener);

        bar.add(menu);
        setJMenuBar(bar);

        setGlassPane(betterGlassPane);

        add(createPanel());
        setSize(250, 250);
        setLocationRelativeTo(null);
    }

    private JComponent createPanel() {
        JPanel panel = new JPanel();
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

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new GlassPaneDemo().setVisible(true);
            }
        });
    }

}
