/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Mar 25, 2005
 */
package org.dwfa.bpa.htmlbrowser;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import net.jini.config.ConfigurationException;

import org.dwfa.bpa.util.ComponentFrame;
import org.dwfa.bpa.util.OpenFramesWindowListener;

public class JavaBrowser extends ComponentFrame implements HyperlinkListener, ActionListener {

    /**
     * http://today.java.net/pub/a/today/2004/05/24/html-pt1.html
     * http://today.java.net/pub/a/today/2004/06/14/html-pt2.html
     * Other options to consider:
     * http://jrex.mozdev.org/docs.html
     * https://jdic.dev.java.net/
     * http://sourceforge.net/projects/jxwb/
     * http://multivalent.sourceforge.net/
     */
    private static final long serialVersionUID = 1L;

    protected JMenu browserMenu;

    private JLabel addrLabel = new JLabel("Address:");

    private JTextField addrText = new JTextField("http://www.");

    private JButton goButton = new JButton("Go");

    private JEditorPane browser = new JEditorPane(); // The main HTML pane

    public JavaBrowser() throws Exception {
        super(new String[] {}, null);
        // Set the title for the frame
        setTitle(getNextFrameName());

        // Set the position and size of frame
        setBounds(10, 10, 500, 500);

        /*
         * Make the JEditoPane non-editable so that when user clicks on a link
         * then another page is loaded. By default, JEditorPane is editable and
         * works as a HTML editor not as a browser. Make make it work as a
         * browser we must make it non-editable
         */

        browser.setEditable(false);

        /*
         * Add a hyperlink listener so that when user clicks on a link then
         * hyperlinkUpdate ( ) method is called and we will load another page
         */

        browser.addHyperlinkListener(this);

        // Put the address text filed and button on the north of frame
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(addrLabel);
        panel.add(addrText);
        panel.add(goButton);

        // Add the action listener to button and address text field so that
        // when user hits enter in address text filed or presses the Go button
        // then new page is displayed
        addrText.addActionListener(this);
        goButton.addActionListener(this);

        // Add the panel and editor pane to the frame
        Container cp = getContentPane();

        // Add the panel to the north
        cp.add(panel, "North");
        cp.add(new JScrollPane(browser, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new OpenFramesWindowListener(this, this.cfb));
        this.setBounds(getDefaultFrameSize());

    }

    public void displayPage(String page) {

        // Check if user has specified any command line parameter
        if (page != null && page.trim().length() > 0) {

            // Set this address
            addrText.setText(page);

            /*
             * User may specify one of the following 1. A relative path for a
             * local file 2. An absolute path for a local file 3. A URL Check
             * for a valid user input
             */

            File localFile = new File(page);

            // Chgeck if the file exists on the dist
            if (localFile.exists() && localFile.isFile()) {
                /*
                 * Check if user specified the absolute path Add the file
                 * protocol in front of file name
                 */

                page = "file:///" + localFile.getAbsolutePath();
                try {
                    browser.setPage(page);
                } catch (Exception e1) {
                    // Not a valid URL
                    browser.setText("Could not load page:" + page + "\n" + "Error:" + e1.getMessage());
                }
            } else {
                // Maybe user specified a URL
                try {
                    URL url = new URL(page);
                    browser.setPage(url);
                } catch (Exception e) {
                    // Not a valid URL
                    browser.setText("Could not load page:" + page + "\n" + "Error:" + e.getMessage());
                }

            }

        } else {
            browser.setText("Could not load page:" + page);
        }

    }

    public void hyperlinkUpdate(HyperlinkEvent e) {
        /*
         * Get the event type for the link. There could be three types of event
         * generated by user actions on a link.When user's mouse enters a link
         * then ENTERED event is triggerd. When user clicks the link the
         * ACTIVATED is triggered and when user exits the link then EXITED is
         * triggerd.
         */

        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                // Loads the new page represented by link clicked
                URL url = e.getURL();
                browser.setPage(url);
                addrText.setText(url.toString());
            } catch (Exception exc) {
            }
        }
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#addAppMenus(javax.swing.JMenuBar)
     */
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(browserMenu = new JMenu("Browser"));
    }

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getQuitMenu()
     */
    public JMenu getQuitMenu() {
        return this.browserMenu;
    }

    /**
     * @see org.dwfa.bpa.util.I_InitComponentMenus#addInternalFrames(javax.swing.JMenu)
     */
    public void addInternalFrames(JMenu menu) {

    }

    public JMenuItem[] getNewWindowMenu() {
        return null;
    }

    public void actionPerformed(ActionEvent e) {
        // Load the new page
        String page = "";
        try {
            // Get the new url
            page = addrText.getText();

            // User may eneter a file name or URL. displayPage handles both of
            // them
            displayPage(page);
        } catch (Exception exc) {
            browser.setText("Page could not be loaded:" + page + "\n" + "Error:" + exc.getMessage());
        }
    }

    /**
     * @throws ConfigurationException
     * @see org.dwfa.bpa.util.ComponentFrame#getNextFrameName()
     */
    public String getNextFrameName() throws ConfigurationException {
        String title = "Java Browser";
        if (count > 0) {
            return title + " " + count++;
        }
        count++;
        return title;
    }

    private static int count = 0;

    /**
     * @see org.dwfa.bpa.util.ComponentFrame#getCount()
     */
    public int getCount() {
        return count;
    }

}
