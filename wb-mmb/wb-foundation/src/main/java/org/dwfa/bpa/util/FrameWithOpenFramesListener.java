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
 * Created on May 5, 2005
 */
package org.dwfa.bpa.util;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.app.I_ManageStandardAppFunctions;
import org.dwfa.bpa.gui.action.BringWindowToFront;

/**
 * @author kec
 * 
 */
public class FrameWithOpenFramesListener extends JFrame implements I_ManageStandardAppFunctions, ListDataListener,
        ActionListener {
    protected static Logger logger = Logger.getLogger(FrameWithOpenFramesListener.class.getName());
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public class ShowApiListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {
                String userDir = System.getProperty("user.dir");
                PlatformWebBrowser.openURL(new URL("file://" + userDir + "/site/index.html"));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    protected JDialog aboutBox, prefs;

    protected JMenuBar mainMenuBar = new JMenuBar();

    protected JMenu bundleMenu, windowMenu;

    protected JMenuItem addQueueMI;

    protected JMenuItem quitMI;

    protected JMenuItem docsMI, supportMI, optionsMI, aboutMI;
    // Ask AWT which menu modifier we should be using.
    final static int MENU_MASK = ComponentFrame.getMenuMask();

    // Check that we are on Mac OS X. This is crucial to loading and using the
    // OSXAdapter class.
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    public FrameWithOpenFramesListener(String title, String menuName, JComponent content) throws Exception {
        super();
        this.setTitle(title);
        OpenFrames.addFrame(this);
        OpenFrames.addFrameListener(this);
        // ;
        this.getContentPane().setLayout(new GridLayout(1, 1));
        this.getContentPane().add(content);
        addMenus(menuName);

        setupAbout();

        // Set up our application to respond to the Mac OS X application menu
        macOSXRegistration();

        this.setBounds(ComponentFrame.getDefaultFrameSize());
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.addWindowListener(new OpenFramesWindowListener(this, this));
    }

    public void macOSXRegistration() {
        if (MAC_OS_X) {
            try {
                Class<?> osxAdapterClass = ClassLoader.getSystemClassLoader().loadClass("org.dwfa.app.OSXAdapter");
                Constructor<?> c = osxAdapterClass.getConstructor(new Class[] { I_ManageStandardAppFunctions.class });
                c.newInstance(new Object[] { this });
            } catch (NoClassDefFoundError e) {
                // This will be thrown first if the OSXAdapter is loaded on a
                // system without the EAWT
                // because OSXAdapter extends ApplicationAdapter in its def
                logger.log(
                    Level.SEVERE,
                    "This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled.",
                    e);
            } catch (ClassNotFoundException e) {
                // This shouldn't be reached; if there's a problem with the
                // OSXAdapter we should get the
                // above NoClassDefFoundError first.
                logger.log(
                    Level.SEVERE,
                    "This version of Mac OS X does not support the Apple EAWT.  Application Menu handling has been disabled.",
                    e);
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    /**
     *  
     */
    private void setupAbout() {
        aboutBox = AboutBox.getAboutBox(this);
        setupPrefs();
    }

    /**
     *  
     */
    private void setupPrefs() {
        prefs = new JDialog(this, "Architectonic Preferences");

        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();

        // Create the combo box, select item at index 4.
        // Indices start at 0, so 4 specifies the pig.
        JComboBox lookAndFeelList = new JComboBox(lookAndFeels);
        lookAndFeelList.setRenderer(new DefaultListCellRenderer() {

            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                UIManager.LookAndFeelInfo lafValue = (UIManager.LookAndFeelInfo) value;
                super.getListCellRendererComponent(list, lafValue.getName(), index, isSelected, cellHasFocus);
                return this;
            }

        });

        lookAndFeelList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                try {
                    JComboBox comboBox = (JComboBox) e.getSource();
                    UIManager.LookAndFeelInfo lafInfo = (LookAndFeelInfo) comboBox.getSelectedItem();
                    UIManager.setLookAndFeel(lafInfo.getClassName());

                    SwingUtilities.updateComponentTreeUI(FrameWithOpenFramesListener.this);

                } catch (ClassNotFoundException e1) {
                    logger.log(Level.SEVERE, e1.getMessage(), e1);
                } catch (InstantiationException e1) {
                    logger.log(Level.SEVERE, e1.getMessage(), e1);
                } catch (IllegalAccessException e1) {
                    logger.log(Level.SEVERE, e1.getMessage(), e1);
                } catch (UnsupportedLookAndFeelException e1) {
                    logger.log(Level.SEVERE, e1.getMessage(), e1);
                }
            }
        });
        lookAndFeelList.setSelectedItem(UIManager.getLookAndFeel());
        // petList.addActionListener(this);

        prefs.getContentPane().setLayout(new GridLayout(2, 1));
        prefs.getContentPane().add(lookAndFeelList);
    }

    public void addMenus(String menuName) {
        // Quit menu item is provided on Mac OS X.. only make it on other
        // platforms.
        if (!MAC_OS_X) {
            mainMenuBar.add(bundleMenu = new JMenu(menuName));
            bundleMenu.add(aboutMI = new JMenuItem("About..."));
            aboutMI.addActionListener(this);
            bundleMenu.add(optionsMI = new JMenuItem("Options"));
            optionsMI.addActionListener(this);
            bundleMenu.addSeparator();
            bundleMenu.addSeparator();
            bundleMenu.add(quitMI = new JMenuItem("Quit"));
            quitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MASK));
            quitMI.addActionListener(this);
        }

        mainMenuBar.add(windowMenu = new JMenu("Window"));
        Iterator<JFrame> windowItr = OpenFrames.getFrames().iterator();
        while (windowItr.hasNext()) {
            JFrame frame = windowItr.next();
            JMenuItem menuItem = new JMenuItem(frame.getTitle());
            menuItem.addActionListener(new BringWindowToFront(frame));
            windowMenu.add(menuItem);
        }

        setJMenuBar(mainMenuBar);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == quitMI) {
            quit();
        } else if (e.getSource() == optionsMI) {
            preferences();
        } else if (e.getSource() == aboutMI) {
            about();
        }
    }

    // General info dialog. The OSXAdapter calls this method when "About
    // OSXAdapter"
    // is selected from the application menu.
    public void about() {
        aboutBox = AboutBox.getAboutBox(this);
        aboutBox.pack();
        aboutBox.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        aboutBox.setResizable(false);
        aboutBox.setVisible(true);
        aboutBox.toFront();
    }

    // General preferences dialog. The OSXAdapter calls this method when
    // "Preferences..."
    // is selected from the application menu.
    public void preferences() {
        prefs.setSize(320, 240);
        prefs.setLocation((int) this.getLocation().getX() + 22, (int) this.getLocation().getY() + 22);
        prefs.setResizable(false);
        prefs.setVisible(true);
    }

    // General info dialog. The OSXAdapter calls this method when "Quit
    // OSXAdapter"
    // is selected from the application menu, Cmd-Q is pressed, or "Quit" is
    // selected from the Dock.
    public boolean quit() {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "[b] Quit?",
            JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            System.out.println("[b] Quit option selected...");
            if (MAC_OS_X) {
                return true;
            }
            System.exit(0);
        }
        return false;
    }

    /**
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public final void intervalAdded(ListDataEvent e) {
        this.contentsChanged(e);
    }

    /**
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public final void intervalRemoved(ListDataEvent e) {
        this.contentsChanged(e);
    }

    private void addFramesToWindowMenu() {
        JMenu submenu = new JMenu("New");
        windowMenu.add(submenu);
        windowMenu.addSeparator();
        Collection<JFrame> openFrames = OpenFrames.getFrames();
        synchronized (openFrames) {
            for (JFrame frame : openFrames) {
                JMenuItem menuItem = new JMenuItem(frame.getTitle());
                menuItem.addActionListener(new BringWindowToFront(frame));
                windowMenu.add(menuItem);
            }
        }
        Collection<JMenuItem> newWindowMenuItems = OpenFrames.getNewWindowMenuItems();
        synchronized (newWindowMenuItems) {
            for (JMenuItem newWindowMenu : newWindowMenuItems) {
                submenu.add(newWindowMenu);
            }
        }

    }

    /**
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public final void contentsChanged(ListDataEvent e) {
        this.windowMenu.removeAll();
        addFramesToWindowMenu();
    }

    public void openApplication() {
        throw new UnsupportedOperationException();
    }

    public void openFile() {
        throw new UnsupportedOperationException();
    }

    public void printFile() {
        throw new UnsupportedOperationException();
    }

    public void reOpenApplication() {
        throw new UnsupportedOperationException();
    }

}
