/*
 * Copyright 2007-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

package com.sun.tools.visualvm.modules.appui.about;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
public class TextBrowser {


    // --- Internal API --------------------------------------------------------

    synchronized static TextBrowser getInstance() {
        if (instance == null) {
            instance = new TextBrowser();
            instance.initComponents();
        }
        return instance;
    }
    
    void setCaption(String caption) {
        dialog.setTitle(caption);
    }
    
    void setPreferredBrowserSize(Dimension preferredSize) {
        this.preferredSize = preferredSize;
    }
    
    void showCodeText(String text) {
        textDisplayer.setText(text);
        displayerScrollPane.setViewportView(textDisplayer);
        try { textDisplayer.setCaretPosition(0); } catch (Exception e) {}
        displayerScrollPane.setPreferredSize(preferredSize);
        dialog.pack();
        closeButton.requestFocusInWindow();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    void showHTMLText(String text) {
        htmlTextDisplayer.setText(text);
        displayerScrollPane.setViewportView(htmlTextDisplayer);
        try { htmlTextDisplayer.setCaretPosition(0); } catch (Exception e) {}
        displayerScrollPane.setPreferredSize(preferredSize);
        dialog.pack();
        closeButton.requestFocusInWindow();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
    
    void close() {
        dialog.setVisible(false);
        dialog.dispose();
    }
    
    
    // --- Private implementation ----------------------------------------------
    
    private TextBrowser() {}
    
    private void initComponents() {
        dialog = new JDialog(AboutDialog.getInstance().getDialog(), "", true); // NOI18N
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) { cleanup(); }
        });

        contentPane = (JComponent)dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                 .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CLOSE_ACTION"); // NOI18N
        contentPane.getActionMap().put("CLOSE_ACTION", new AbstractAction() { // NOI18N
                public void actionPerformed(ActionEvent e) { close(); }});
        
        textDisplayer = new TextViewerComponent();
        htmlTextDisplayer = new HTMLTextArea();
        displayerScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        textDisplayer.setForeground(htmlTextDisplayer.getForeground());
        textDisplayer.setBackground(htmlTextDisplayer.getBackground());
        textDisplayer.setSelectionColor(htmlTextDisplayer.getSelectionColor());
        textDisplayer.setSelectedTextColor(htmlTextDisplayer.getSelectedTextColor());
        
        closeButton = new JButton(); // NOI18N
        Mnemonics.setLocalizedText(closeButton, NbBundle.getMessage(AboutDialogControls.class, "LBL_Close")); // NOI18N
        closeButton.setDefaultCapable(true);
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        close();
                    }
                });
            }
        });
        
        JPanel buttonsContainer = new JPanel(new BorderLayout());
        buttonsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 6, 10));
        buttonsContainer.add(closeButton, BorderLayout.EAST);
        
        contentPane.add(displayerScrollPane, BorderLayout.CENTER);
        contentPane.add(buttonsContainer, BorderLayout.SOUTH);
        
        dialog.getRootPane().setDefaultButton(closeButton);
        dialog.setResizable(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }
    
    private void cleanup() {
        displayerScrollPane.getViewport().removeAll();
        textDisplayer.setText(""); // NOI18N
        htmlTextDisplayer.setText(""); // NOI18N
    }
    
    private static TextBrowser instance;
    
    private JDialog dialog;
    private JComponent contentPane;
    private JButton closeButton;
    private TextViewerComponent textDisplayer;
    private HTMLTextArea htmlTextDisplayer;
    private JScrollPane displayerScrollPane;
    private Dimension preferredSize = new Dimension(400, 300);

}