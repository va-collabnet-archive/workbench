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
 * Created on May 21, 2005
 */
package org.dwfa.bpa.util;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
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

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.dwfa.app.I_ManageStandardAppFunctions;
import org.dwfa.bpa.gui.action.BringWindowToFront;

import com.sun.jini.start.LifeCycle;

/**
 * @author kec
 * 
 */
public class ComponentFrameBean implements ActionListener, I_ManageStandardAppFunctions, ListDataListener,
        WindowListener {
    protected static Logger logger = Logger.getLogger(ComponentFrameBean.class.getName());

    public class ShowApiListener implements ActionListener {

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            try {

                String userDir = System.getProperty("user.dir");
                File startFile = new File(userDir + "/site/index.html");

                PlatformWebBrowser.openURL(startFile.toURI().toURL());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    List<I_DoQuitActions> quitList = new ArrayList<I_DoQuitActions>();

    private JDialog aboutBox, prefs;

    private JMenuBar mainMenuBar;

    private JMenu editMenu, windowMenu, bundleMenu;

    private JMenuItem cutMI, copyMI, pasteMI, quitMI;

    private JMenuItem optionsMI, aboutMI;

    /**
     * Cache of our <code>LifeCycle</code> object TODO implement the lifeCycle
     * destroy methods. See TxnManagerImpl for an example.
     */
    @SuppressWarnings("unused")
    private LifeCycle lifeCycle = null;

    /** The jini configuration file for this object */
    protected Configuration config;

    // Ask AWT which menu modifier we should be using.
    protected static int MENU_MASK;

    // Check that we are on Mac OS X. This is crucial to loading and using the
    // OSXAdapter class.
    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    private ComponentFrame frame;

    private I_InitComponentMenus compMenuIniter;

    private boolean hiddenFrame;

    public ComponentFrameBean(String[] args, LifeCycle lc, ComponentFrame frame, I_InitComponentMenus compMenuIniter,
            boolean hiddenFrame) throws Exception {
        this(args, lc, frame, compMenuIniter, new JMenuBar(), hiddenFrame);
    }

    public ComponentFrameBean(String[] args, LifeCycle lc, ComponentFrame frame, I_InitComponentMenus compMenuIniter)
            throws Exception {
        this(args, lc, frame, compMenuIniter, new JMenuBar());
    }

    public ComponentFrameBean(String[] args, LifeCycle lc, ComponentFrame frame, I_InitComponentMenus compMenuIniter,
            JMenuBar mainMenuBar) throws Exception {
        this(args, lc, frame, compMenuIniter, mainMenuBar, false);
    }

    /**
     * @param title
     * @throws Exception
     */
    public ComponentFrameBean(String[] args, LifeCycle lc, ComponentFrame frame, I_InitComponentMenus compMenuIniter,
            JMenuBar mainMenuBar, boolean hiddenFrame) throws Exception {
        super();
        // Ask AWT which menu modifier we should be using.
        try {
            MENU_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        } catch (HeadlessException e) {
            MENU_MASK = 0;
            e.printStackTrace();
        }

        this.mainMenuBar = mainMenuBar;
        this.frame = frame;
        this.frame.addWindowListener(this);
        this.compMenuIniter = compMenuIniter;
        this.config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        this.lifeCycle = lc;
        this.hiddenFrame = hiddenFrame;
        this.quitList.add(new StandardQuitter(frame));

    }

    public void setup() throws Exception {
        if (hiddenFrame == false) {
            OpenFrames.addFrame(this.frame);
        }
        OpenFrames.addNewWindowMenuItemGenerator(compMenuIniter);
        addMenus();

        setupAbout();
        OpenFrames.addFrameListener(this);

        // Set up our application to respond to the Mac OS X application menu
        macOSXRegistration();

        this.frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    // Generic registration with the Mac OS X application menu. Checks the
    // platform, then attempts
    // to register with the Apple EAWT.
    // This method calls OSXAdapter.registerMacOSXApplication() and
    // OSXAdapter.enablePrefs().
    // See OSXAdapter.java for the signatures of these methods.
    public final void macOSXRegistration() {
        if (MAC_OS_X) {
            try {
                Class<?> osxAdapterClass = Thread.currentThread().getContextClassLoader().loadClass(
                    "org.dwfa.app.OSXAdapter");
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
        aboutBox = AboutBox.getAboutBox(this.frame);
        setupPrefs();
    }

    /**
   * 
   */
    private void setupPrefs() {
        prefs = new JDialog(this.frame, "Preferences");

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

                    SwingUtilities.updateComponentTreeUI(ComponentFrameBean.this.frame);

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

    public final void addMenus() throws Exception {
        mainMenuBar.add(editMenu = new JMenu("Edit"));
        TransferActionListener actionListener = new TransferActionListener();

        cutMI = new JMenuItem(new javax.swing.text.DefaultEditorKit.CutAction());
        cutMI.setText("Cut");
        cutMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        cutMI.addActionListener(actionListener);
        editMenu.add(cutMI);
        copyMI = new JMenuItem(new javax.swing.text.DefaultEditorKit.CopyAction());
        copyMI.setText("Copy");
        copyMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit()
            .getMenuShortcutKeyMask()));
        copyMI.addActionListener(actionListener);
        editMenu.add(copyMI);
        pasteMI = new JMenuItem(new javax.swing.text.DefaultEditorKit.PasteAction());
        pasteMI.setText("Paste");
        pasteMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit()
            .getMenuShortcutKeyMask()));
        pasteMI.addActionListener(actionListener);
        editMenu.add(pasteMI);
        this.compMenuIniter.addAppMenus(mainMenuBar);
        // Quit menu item is provided on Mac OS X.. only make it on other
        // platforms.
        if (!MAC_OS_X) {
            JMenu quitMenu = this.compMenuIniter.getQuitMenu();
            if (quitMenu != null) {
                quitMenu.addSeparator();
                quitMenu.add(quitMI = new JMenuItem("Quit"));
                quitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, MENU_MASK));
                quitMI.addActionListener(this);
            }
        }

        int helpIndex = -1;
        for (int i = 0; i < mainMenuBar.getMenuCount(); i++) {
            JMenu menu = mainMenuBar.getMenu(i);
            if (menu.getText().toLowerCase().equals("help")) {
                helpIndex = i;
                break;
            }
        }
        // About menu item is provided on Mac OS X.. only make it on other
        // platforms.
        // Options/Prefs menu item is provided on Mac OS X.. only make it on
        // other platforms.
        String menuName = null;
        if (System.getProperty("org.dwfa.AppMenuName") != null
            && System.getProperty("org.dwfa.AppMenuName").length() > 3) {
            menuName = AboutBox.removeQuotes(System.getProperty("org.dwfa.AppMenuName"));
        }
        if (!MAC_OS_X) {
            if (helpIndex == -1) {
                mainMenuBar.add(bundleMenu = new JMenu(menuName));
            } else {
                bundleMenu = mainMenuBar.getMenu(helpIndex);
            }
            bundleMenu.addSeparator();
            if (menuName == null) {
                bundleMenu.add(aboutMI = new JMenuItem("About..."));
            } else {
                bundleMenu.add(aboutMI = new JMenuItem("About " + menuName + "..."));
            }
            aboutMI.addActionListener(this);
        }
        windowMenu = new JMenu("Window");
        if (helpIndex == -1) {
            mainMenuBar.add(windowMenu);
        } else {
            mainMenuBar.add(windowMenu, helpIndex);
        }
        addFramesToWindowMenu();

        this.frame.setJMenuBar(mainMenuBar);
    }

    /**
   * 
   */
    private void addFramesToWindowMenu() {
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "Changing window menu for: " + this.frame.getTitle());
        }
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
            logger.log(Level.FINEST, "Trying to add new items.");
            for (JMenuItem newWindowMenu : newWindowMenuItems) {
                if (newWindowMenu != null) {
                    submenu.add(newWindowMenu);
                    logger.log(Level.FINEST, "Adding new window item: " + newWindowMenu.getText());
                }
            }
        }

    }

    public final void actionPerformed(ActionEvent e) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer(e.toString());
        }
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
    public final void about() {
        for (JFrame frame : OpenFrames.getFrames()) {
            if (frame.isActive()) {
                aboutBox = AboutBox.getAboutBox(frame);
                aboutBox.pack();
                aboutBox.setLocation((int) frame.getLocation().getX() + 22, (int) frame.getLocation().getY() + 22);
                aboutBox.setResizable(false);
                aboutBox.setVisible(true);
                aboutBox.toFront();
                break;
            }
        }
    }

    // General preferences dialog. The OSXAdapter calls this method when
    // "Preferences..."
    // is selected from the application menu.
    public final void preferences() {
        prefs.setSize(320, 240);
        prefs.setLocation((int) this.frame.getLocation().getX() + 22, (int) this.frame.getLocation().getY() + 22);
        prefs.setResizable(false);
        prefs.setVisible(true);
    }

    // General info dialog. The OSXAdapter calls this method when "Quit
    // OSXAdapter"
    // is selected from the application menu, Cmd-Q is pressed, or "Quit" is
    // selected from the Dock.
    public final boolean quit() {
        boolean quit = false;
        for (I_DoQuitActions quitter : quitList) {
            quit = quitter.quit();
            if (quit == false) {
                return false;
            }
        }
        if (quit) {
            System.out.println("[a] Quit option selected...");
            if (MAC_OS_X) {
                return true;
            }
            System.exit(0);
        }
        return false;
    }

    public static class StandardQuitter implements I_DoQuitActions {
        Component component;

        public StandardQuitter(Component component) {
            super();
            this.component = component;
        }

        public boolean quit() {
            int option = JOptionPane.showConfirmDialog(component, "Are you sure you want to quit?", "Quit?",
                JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                return true;
            }

            return false;
        }

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

    /**
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public final void contentsChanged(ListDataEvent e) {
        this.windowMenu.removeAll();
        addFramesToWindowMenu();
        this.compMenuIniter.addInternalFrames(this.windowMenu);
    }

    public void windowOpened(WindowEvent e) {

    }

    public void windowClosing(WindowEvent e) {
    	if (frame.sendToBackInsteadOfClose()) {
            if (SwingUtilities.isEventDispatchThread()) {
            	frame.toBack();
           } else {
               SwingUtilities.invokeLater(new Runnable() {
                    @Override
                   public void run() {
                    	frame.toBack();
                   }
               });
           }
    	} else {
            boolean okToClose = false;
            if (ComponentFrame.class.isAssignableFrom(this.frame.getClass())) {
                ComponentFrame cf = (ComponentFrame) this.frame;
                okToClose = cf.okToClose();
            } else {
                okToClose = (this.frame.getDefaultCloseOperation() == WindowConstants.DISPOSE_ON_CLOSE);
            }

            if (okToClose) {
                OpenFrames.removeFrameListener(this);
                OpenFrames.removeFrame(this.frame);
                this.frame.setVisible(false);
                this.frame.dispose();
            }
    	}
    }

    public void windowClosed(WindowEvent e) {
        OpenFrames.removeFrame(null);
    }

    public void windowIconified(WindowEvent e) {
         OpenFrames.removeFrame(null);
    }

    public void windowDeiconified(WindowEvent e) {
        if (PhantomFrame.class.isAssignableFrom(frame.getClass()) == false) {
            OpenFrames.addFrame(frame);
        }
    }

    public void windowActivated(WindowEvent e) {
        if (PhantomFrame.class.isAssignableFrom(frame.getClass()) == false) {
            OpenFrames.addFrame(frame);
        }
    }

    public void windowDeactivated(WindowEvent e) {
        OpenFrames.removeFrame(null);
    }

    public void openApplication() {
        if (PhantomFrame.class.isAssignableFrom(frame.getClass()) == false) {
            OpenFrames.addFrame(frame);
        }
    }

    public void openFile() {
        throw new UnsupportedOperationException();
    }

    public void printFile() {
        throw new UnsupportedOperationException();
    }

    public void reOpenApplication() {
        if (PhantomFrame.class.isAssignableFrom(frame.getClass()) == false) {
            OpenFrames.addFrame(frame);
        }
    }

    public JMenu getBundleMenu() {
        return bundleMenu;
    }

    public void setBundleMenu(JMenu bundleMenu) {
        this.bundleMenu = bundleMenu;
    }

    public JMenu getEditMenu() {
        return editMenu;
    }

    public void setEditMenu(JMenu editMenu) {
        this.editMenu = editMenu;
    }

    public JMenuBar getMainMenuBar() {
        return mainMenuBar;
    }

    public void setMainMenuBar(JMenuBar mainMenuBar) {
        this.mainMenuBar = mainMenuBar;
    }

    public JMenu getWindowMenu() {
        return windowMenu;
    }

    public void setWindowMenu(JMenu windowMenu) {
        this.windowMenu = windowMenu;
    }

    public List<I_DoQuitActions> getQuitList() {
        return quitList;
    }

    public JMenuItem getQuitMI() {
        return quitMI;
    }
}
