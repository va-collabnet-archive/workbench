/*
 * Created on Mar 17, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.beans.DefaultPersistenceDelegate;
import java.beans.ExceptionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.BusinessProcessPersistenceDelegate;
import org.dwfa.bpa.ExecutionRecord;
import org.dwfa.bpa.TaskInfo;
import org.dwfa.bpa.TaskInfoPersistenceDelegate;
import org.dwfa.bpa.data.DataContainer;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.ConditionPersistenceDelegate;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;

/**
 * @author kec
 * 
 */
public class ProcessBuilderPanel extends JPanel implements ActionListener {

   public class SaveProcessForXmlActionListener implements ActionListener {

      public void actionPerformed(ActionEvent e) {
         try {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) ProcessBuilderPanel.this.getTopLevelAncestor(),
                  "Save Process (.xml)", FileDialog.SAVE);
            f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "processes");
            f.setVisible(true); // Display dialog and wait for response
            if (f.getFile() != null) {
               String fileName = f.getFile();
               if (fileName.endsWith(".xml") == false) {
                  fileName = fileName + ".xml";
               }

               File processXmlFile = new File(f.getDirectory(), fileName);

               XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(processXmlFile)));
               encoder.setPersistenceDelegate(BusinessProcess.class, new BusinessProcessPersistenceDelegate());
               encoder.setPersistenceDelegate(Condition.class, new ConditionPersistenceDelegate());
               encoder.setPersistenceDelegate(TaskInfo.class, new TaskInfoPersistenceDelegate());
               encoder.setPersistenceDelegate(Date.class, new DefaultPersistenceDelegate(new String[] { "time" }));
               encoder.setExceptionListener(new ExceptionListener() {

                  public void exceptionThrown(Exception e) {
                     logger.log(Level.WARNING, e.getMessage(), e);

                  }
               });
               encoder.writeObject(processPanel.getProcess());
               encoder.close();

               if (logger.isLoggable(Level.FINER)) {
                  logger.finer("Wrote to disk:\n" + processPanel.getProcess());

               }
            }
            f.dispose(); // Get rid of the dialog box
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
         treeModel.fireTreeStructureChanged();
         if (logger.isLoggable(Level.INFO)) {
            logger.info("Tree structure changed.");
         }
      }

   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static Logger logger = Logger.getLogger(ProcessBuilderPanel.class.getName());

   private class ChangeToPropActionListener implements ActionListener {

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            setupPropertiesPane();
         } catch (Exception e1) {
            logger.log(Level.SEVERE, e1.getMessage(), e1);
         }
      }

   }

   private class ChangeToDataActionListener implements ActionListener {

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            int dividerLocation = splitPane.getDividerLocation();
            splitPane.setLeftComponent(setupDataPanel(worker));
            splitPane.setDividerLocation(dividerLocation);
         } catch (Exception e1) {
            logger.log(Level.SEVERE, e1.getMessage(), e1);
         }
      }

   }

   private class ChangeToTaskActionListener implements ActionListener {

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            updateTaskPanel();
            int dividerLocation = splitPane.getDividerLocation();
            splitPane.setLeftComponent(taskSplitPane);
            splitPane.setDividerLocation(dividerLocation);
         } catch (Exception e1) {
            logger.log(Level.SEVERE, e1.getMessage(), e1);
         }

      }

   }

   private class NewProcessActionListener implements ActionListener {

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         newProcess();
      }

   }

   private class SaveProcessActionListener implements ActionListener {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) ProcessBuilderPanel.this.getTopLevelAncestor(), "Save Process (.bp)",
                  FileDialog.SAVE);
            f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "processes");
            f.setVisible(true); // Display dialog and wait for response
            if (f.getFile() != null) {
               String fileName = f.getFile();
               if (fileName.endsWith(".bp") == false) {
                  fileName = fileName + ".bp";
               }
               File processBinaryFile = new File(f.getDirectory(), fileName);
               FileOutputStream fos = new FileOutputStream(processBinaryFile);
               BufferedOutputStream bos = new BufferedOutputStream(fos);
               ObjectOutputStream oos = new ObjectOutputStream(bos);
               oos.writeObject(processPanel.getProcess());
               oos.close();
               if (logger.isLoggable(Level.FINER)) {
                  logger.finer("Wrote to disk:\n" + processPanel.getProcess());

               }
            }
            f.dispose(); // Get rid of the dialog box
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
         treeModel.fireTreeStructureChanged();
         if (logger.isLoggable(Level.INFO)) {
            logger.info("Tree structure changed.");
         }
      }

   }

   private class SaveProcessForLauncherActionListener implements ActionListener {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) ProcessBuilderPanel.this.getTopLevelAncestor(),
                  "Save Process for Launcher Queue", FileDialog.SAVE);
            f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "processes");
            f.setFile(processPanel.getProcess().getProcessID() + "." + UUID.randomUUID() + ".bp");
            f.setVisible(true); // Display dialog and wait for response
            if (f.getFile() != null) {
               File processBinaryFile = new File(f.getDirectory(), f.getFile());
               FileOutputStream fos = new FileOutputStream(processBinaryFile);
               BufferedOutputStream bos = new BufferedOutputStream(fos);
               ObjectOutputStream oos = new ObjectOutputStream(bos);
               oos.writeObject(processPanel.getProcess());
               oos.close();
               if (logger.isLoggable(Level.FINER)) {
                  logger.finer("Wrote to disk:\n" + processPanel.getProcess());

               }
            }
            f.dispose(); // Get rid of the dialog box
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
         treeModel.fireTreeStructureChanged();
         if (logger.isLoggable(Level.INFO)) {
            logger.info("Tree structure changed.");
         }
      }

   }

   private class ReadProcessActionListener implements ActionListener {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         // Create a file dialog box to prompt for a new file to display
         FileDialog f = new FileDialog((Frame) ProcessBuilderPanel.this.getTopLevelAncestor(), "Open Process",
               FileDialog.LOAD);
         f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "processes");
         f.setFilenameFilter(new FilenameFilter() {

            public boolean accept(File dir, String name) {

               return name.endsWith(".bp") || name.endsWith(".xml");
            }
         });
         f.setVisible(true); // Display dialog and wait for response
         try {
            if (f.getFile() != null) {
               File processFile = new File(f.getDirectory(), f.getFile());
               if (f.getFile().endsWith(".xml")) {
                  XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream(processFile)));
                  I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) d.readObject();
                  d.close();
                  setProcess(process);
               } else {
                  FileInputStream fis = new FileInputStream(processFile);
                  BufferedInputStream bis = new BufferedInputStream(fis);
                  ObjectInputStream ois = new ObjectInputStream(bis);
                  Object obj = ois.readObject();
                  logger.info("Read object: " + obj.getClass().toString());
                  I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) obj;
                  ois.close();
                  setProcess(process);

               }
            }
            f.dispose(); // Get rid of the dialog box
         } catch (Exception ex) {
            logger.log(Level.WARNING, "Exception reading object: " + f.getFile(), ex);
         }
      }

   }

   private class TakeProcessNoTranActionListener implements ActionListener {
      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
         try {
            // Create a file dialog box to prompt for a new file to display
            FileDialog f = new FileDialog((Frame) ProcessBuilderPanel.this.getTopLevelAncestor(), "Open Process",
                  FileDialog.LOAD);
            f.setDirectory(System.getProperty("user.dir") + System.getProperty("file.separator") + "processes");
            f.setFilenameFilter(new FilenameFilter() {

               public boolean accept(File dir, String name) {

                  return name.endsWith(".bp");
               }
            });
            f.setVisible(true); // Display dialog and wait for response
            if (f.getFile() != null) {
               File processFile = new File(f.getDirectory(), f.getFile());
               if (f.getFile().endsWith(".xml")) {
                  XMLDecoder d = new XMLDecoder(new BufferedInputStream(new FileInputStream("Test.xml")));
                  I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) d.readObject();
                  d.close();
                  setProcess(process);
               } else {
                  FileInputStream fis = new FileInputStream(processFile);
                  BufferedInputStream bis = new BufferedInputStream(fis);
                  ObjectInputStream ois = new ObjectInputStream(bis);
                  I_EncodeBusinessProcess process = (I_EncodeBusinessProcess) ois.readObject();
                  ois.close();
                  setProcess(process);
               }
               processFile.delete();
            }
            f.dispose(); // Get rid of the dialog box
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
      }

   }

   private class ExecuteProcessActionListener implements ActionListener {
      Configuration config;

      UUID id;

      I_Work worker;

      String exceptionMessage = "";

      /**
       * @param config
       * @param id
       * @param worker
       * @param frames
       */
      public ExecuteProcessActionListener(Configuration config, UUID id, I_Work worker) {
         super();
         this.config = config;
         this.id = id;
         this.worker = worker;
      }

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {

         execute.setText("execute");
         execute.setEnabled(false);
         statusMessage.setText("<html><font color='red'>Executing process: " + "<font color='blue'>"
               + processPanel.getProcess().getName());
         Runnable r = new Runnable() {
            public void run() {
               I_EncodeBusinessProcess process = processPanel.getProcess();
               try {
                  logger.info("Worker: " + worker.getWorkerDesc() + " (" + worker.getId() + ") executing process: "
                        + process.getName());
                  worker.execute(process);
                  SortedSet<ExecutionRecord> sortedRecords = new TreeSet<ExecutionRecord>(process.getExecutionRecords());
                  Iterator<ExecutionRecord> recordItr = sortedRecords.iterator();
                  if (logger.isLoggable(Level.FINE)) {
                     StringBuffer buff = new StringBuffer();
                     while (recordItr.hasNext()) {
                        ExecutionRecord rec = recordItr.next();
                        buff.append("\n");
                        buff.append(rec.toString());
                     }
                     logger.fine(buff.toString());
                  }
                  exceptionMessage = "";
               } catch (Throwable e1) {
                  logger.log(Level.WARNING, e1.toString(), e1);
                  exceptionMessage = e1.toString();
               }
               SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                     execute.setText("<html><font color='#006400'>execute");
                     execute.setEnabled(true);
                     if (exceptionMessage.equals("")) {
                        statusMessage.setText("<html><font color='blue'>Process complete");
                     } else {
                        statusMessage.setText("<html><font color='blue'>Process complete: <font color='red'>"
                              + exceptionMessage);
                     }
                  }
               });
            }

         };
         new Thread(r).start();

      }

   }

   private class TaskDirectoryListener implements TreeSelectionListener {

      /**
       * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
       */
      public void valueChanged(TreeSelectionEvent e) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

         if (node == null) {
            taskDirectory = null;
         } else {
            // createNodes(node);
            FileWrapper wrapper = (FileWrapper) node.getUserObject();
            taskDirectory = wrapper.getFile();
         }

         try {
            updateTaskPanel();
         } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
         }
      }
   }

   private NewProcessActionListener newProcessActionListener = new NewProcessActionListener();

   private SaveProcessActionListener saveProcessActionListener = new SaveProcessActionListener();

   private ReadProcessActionListener openProcessActionListener = new ReadProcessActionListener();

   private TakeProcessNoTranActionListener takeProcessNoTranActionListener = new TakeProcessNoTranActionListener();

   private SaveProcessForLauncherActionListener saveProcessForLauncherQueueActionListener = new SaveProcessForLauncherActionListener();

   private SaveProcessForXmlActionListener saveProcessForXmlActionListener = new SaveProcessForXmlActionListener();

   private ExecuteProcessActionListener executeProcessActionListener;

   private JSplitPane splitPane;

   private ProcessPanel processPanel;

   private JPanel statusPanel;

   private JButton execute = new JButton("<html><font color='#006400'>execute");

   private JLabel statusMessage = new JLabel("<HTML><font color='blue'>Process Builder ready...");

   private I_Work worker;

   private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yy HH:mm");

   private JTree tree;

   private FileTreeModel treeModel = new FileTreeModel();

   private File taskDirectory;

   private JPanel tasksPanel;

   private JSplitPane taskSplitPane;

   private JTable propertiesPane;

   private String defaultOrigin;

   /**
    * 
    * @param jiniConfig
    * @param worker
    * @throws SecurityException
    * @throws IntrospectionException
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    * @throws PropertyVetoException
    * @throws ClassNotFoundException
    * @throws NoSuchMethodException
    * @throws IOException
    * @throws ConfigurationException
    */
    public ProcessBuilderPanel(Configuration jiniConfig, I_Work worker) throws SecurityException,
         IntrospectionException, InvocationTargetException, IllegalAccessException, PropertyVetoException,
         ClassNotFoundException, NoSuchMethodException, IOException, ConfigurationException {
      super(new GridBagLayout());
      defaultOrigin = (String) jiniConfig.getEntry(this.getClass().getName(), "defaultOrigin", String.class);

      this.worker = worker;

      JScrollPane taskScroller = setupTaskPanel();

      taskSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, createTaskTree(), taskScroller);
      taskSplitPane.setDividerLocation(200);

      splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, taskSplitPane, processPanel);
      splitPane.setOneTouchExpandable(true);
      splitPane.setDividerLocation(200);
      // splitPane.setDividerLocation(0);
      newProcess();

      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.NORTHWEST;
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 1;
      c.weighty = 1;
      c.gridx = 0;
      c.gridy = 0;
      this.add(this.splitPane, c);
      this.statusPanel = makeStatusPanel(this.statusMessage, execute);
      c.weighty = 0;
      c.gridy = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      this.add(this.statusPanel, c);
      executeProcessActionListener = new ExecuteProcessActionListener(jiniConfig, UUID.randomUUID(), worker);
      execute.addActionListener(executeProcessActionListener);
      tree.setSelectionRow(0);
   }

   /**
    * 
    */
   private void newProcess() {
      try {
         String processName = "Process " + dateFormatter.format(new Date());
         BusinessProcess newProcess = new BusinessProcess(processName, Condition.CONTINUE);
         newProcess.setOriginator(defaultOrigin);
         setProcess(newProcess);
      } catch (Exception ex) {
         logger.log(Level.SEVERE, ex.getMessage(), ex);
      }
   }

   /**
    * @throws RemoteException
    * @throws IOException
    * @throws IntrospectionException
    */
   private void setupPropertiesPane() throws RemoteException, IOException, IntrospectionException {
      int dividerLocation = splitPane.getDividerLocation();
      propertiesPane = new JTable(new PropertyTableModel(processPanel.getProcess()));
      propertiesPane.setCellSelectionEnabled(true);
      propertiesPane.setDragEnabled(true);
      TableColumn column = null;
      for (int i = 0; i < 2; i++) {
         column = propertiesPane.getColumnModel().getColumn(i);
         switch (i) {
         case 0:
            column.setPreferredWidth(100);
            break;
         default:
            column.setPreferredWidth(25);
         }

      }
      splitPane.setLeftComponent(new JScrollPane(propertiesPane));
      splitPane.setDividerLocation(dividerLocation);
   }

   private void updatePropertiesPane() throws RemoteException, IOException, IntrospectionException {
      if (this.propertiesPane != null) {
         this.propertiesPane.setModel(new PropertyTableModel(processPanel.getProcess()));
      }

   }

   private void updateTaskPanel() throws ClassNotFoundException,
            IntrospectionException, InvocationTargetException,
            IllegalAccessException, IOException {
        int dividerLocation = taskSplitPane.getDividerLocation();
        tasksPanel.removeAll();
        tasksPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.001;
        c.weighty = 0.001;
        c.gridx = 0;
        c.gridy = 0;
        if (this.taskDirectory != null) {
            File[] tasksAndProcesses = this.taskDirectory
                    .listFiles(new FilenameFilter() {

                        public boolean accept(File dir, String name) {
                            return name.endsWith(".bp")
                                    || name.endsWith(".task");
                        }
                    });
            if (tasksAndProcesses != null) {
                List<I_DefineTask> tasks = new ArrayList<I_DefineTask>();
                Map<I_DefineTask, File> taskFileMap = new HashMap<I_DefineTask, File>();
                
                /*
                 * 
                 */
                  
                for (int i = 0; i < tasksAndProcesses.length; i++) {
                   try {
                  FileInputStream fis = new FileInputStream(
                          tasksAndProcesses[i]);
                  BufferedInputStream bis = new BufferedInputStream(fis);
                  ObjectInputStream ois = new ObjectInputStream(bis);
                  I_DefineTask task = (I_DefineTask) ois.readObject();
                  ois.close();
                  tasks.add(task);
                  taskFileMap.put(task, tasksAndProcesses[i]);
                   } catch (Exception ex) {
                      logger.log(Level.WARNING, "Exception processing " + 
                            tasksAndProcesses[i] + " (" + ex.toString() + ")", ex);
                } catch (Error ex) {
                    logger.log(Level.SEVERE, "Error processing " + 
                            tasksAndProcesses[i] + " (" + ex.toString() + ")", ex);
                } 
                }
                Collections.sort(tasks, new Comparator<I_DefineTask>() {

                   public int compare(I_DefineTask t1, I_DefineTask t2) {
                      try {
                      BeanInfo ti1 = t1.getBeanInfo();
                      String nameForT1 = ti1.getBeanDescriptor().getDisplayName();
                      BeanInfo ti2 = t2.getBeanInfo();
                      String nameForT2 = ti2.getBeanDescriptor().getDisplayName();
                      return nameForT1.compareTo(nameForT2);
                      } catch (IntrospectionException e) {
                         logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
                         return t1.getClass().getName().compareTo(t2.getClass().getName());
                      }
                   }});

                for (I_DefineTask task: tasks) {
                   try {
                   createTaskPanelAndAdd(tasksPanel, c, task,
                         taskFileMap.get(task));
                  } catch (Exception ex) {
                      logger.log(Level.WARNING, "Exception processing " + 
                            task + " (" + ex.toString() + ")", ex);
                  } catch (Error ex) {
                      logger.log(Level.SEVERE, "Error processing " + 
                            task + " (" + ex.toString() + ")", ex);
                  } 
                }
            }

            c.gridy++;
            c.weighty = 1;
            tasksPanel.add(new JPanel(), c);

            
        }
        JScrollPane scroller = new JScrollPane(this.tasksPanel);
        this.taskSplitPane.setBottomComponent(scroller);
        this.taskSplitPane.setDividerLocation(dividerLocation);
    }

   private JScrollPane createTaskTree() {
      // Create the nodes.
      DefaultMutableTreeNode top = new DefaultMutableTreeNode(new FileWrapper(new File(System.getProperty("user.dir"),
            "tasks")));
      createNodes(top);
      DefaultMutableTreeNode processesNode = new DefaultMutableTreeNode(new FileWrapper(new File(System
            .getProperty("user.dir"), "processes")));
      top.add(processesNode);
      createNodes(processesNode);

      // Create a tree that allows one selection at a time.
      // tree = new JTree(top);
      tree = new JTree(treeModel);
      tree.addTreeSelectionListener(new TaskDirectoryListener());
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
         private static final long serialVersionUID = 1L;

         private int maxHeight = 20;

         /**
          * @see javax.swing.tree.DefaultTreeCellRenderer#getPreferredSize()
          */
         public Dimension getPreferredSize() {
            Dimension pref = super.getPreferredSize();
            if (pref.height > maxHeight) {
               pref.height = maxHeight;
            }

            return pref;
         }

         /**
          * @see javax.swing.JComponent#getMaximumSize()
          */
         public Dimension getMaximumSize() {
            Dimension max = super.getMaximumSize();
            if (max.height > maxHeight) {
               max.height = maxHeight;
            }

            return max;
         }
      };
      renderer.setLeafIcon(null);
      renderer.setClosedIcon(null);
      renderer.setOpenIcon(null);
      renderer.setIcon(null);
      tree.setCellRenderer(renderer);
      return new JScrollPane(tree);
   }

   /**
    * @param top
    */
   private void createNodes(DefaultMutableTreeNode top) {
      top.removeAllChildren();
      File root = ((FileWrapper) top.getUserObject()).getFile();
      File[] children = root.listFiles(new FileFilter() {

         public boolean accept(File pathname) {
            return pathname.isDirectory();
         }
      });
      if (children != null) {
         for (int i = 0; i < children.length; i++) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileWrapper(children[i]));
            top.add(childNode);
            createNodes(childNode);
         }
      }

   }

   private void setProcess(I_EncodeBusinessProcess process) throws PropertyVetoException, Exception {
      this.processPanel = new ProcessPanel(process, this.worker);
      int dividerLocation = this.splitPane.getDividerLocation();
      this.splitPane.setRightComponent(this.processPanel);
      this.splitPane.setDividerLocation(dividerLocation);
      // this.statusMessage.setText("<html><font color='blue'>Set process to:
      // " + process.getName());
      this.statusMessage.setText("<html><font color='blue'>Process Builder ready...");
      updatePropertiesPane();
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Set process to: \n" + process.toString());
      }

   }

   private JPanel makeStatusPanel(JLabel statusMessage, JButton execute) {
      JPanel panel = new JPanel(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.WEST;
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0;
      c.weighty = 0;
      c.gridy = 0;
      c.gridx = 7;
      panel.add(new JLabel("    "), c); // filler for grow box.

      c.gridx = 6;
      panel.add(execute, c);
      c.gridx = 5;
      // panel.add(cancel, c);
      c.gridx = 4;
      c.weightx = 1;
      c.fill = GridBagConstraints.HORIZONTAL;
      panel.add(statusMessage, c);
      c.weightx = 0;
      c.gridx = 3;
      c.weightx = 0;
      c.fill = GridBagConstraints.NONE;
      panel.add(new JLabel("   "), c);

      c.gridx = 2;
      String[] panelTypes = { "tasks", "data", "properties" };

      JComboBox panelTypeComboBox = new JComboBox(panelTypes);
      panelTypeComboBox.setSelectedIndex(0);
      panelTypeComboBox.addActionListener(new ActionListenerAdaptor());
      panel.add(panelTypeComboBox, c);
      return panel;
   }

   private class ActionListenerAdaptor implements ActionListener {
      ChangeToTaskActionListener changeToTask = new ChangeToTaskActionListener();

      ChangeToDataActionListener changeToData = new ChangeToDataActionListener();

      ChangeToPropActionListener changeToProp = new ChangeToPropActionListener();

      public void actionPerformed(ActionEvent evt) {
         JComboBox cb = (JComboBox) evt.getSource();
         String action = (String) cb.getSelectedItem();
         if (action.equals("tasks")) {
            this.changeToTask.actionPerformed(evt);
         } else if (action.equals("data")) {
            this.changeToData.actionPerformed(evt);
         } else if (action.equals("properties")) {
            this.changeToProp.actionPerformed(evt);
         }

      }
   }

   /**
    * @param configSide
    * @throws ClassNotFoundException
    * @throws IntrospectionException
    * @throws IllegalAccessException
    * @throws InvocationTargetException
    */
   private JScrollPane setupTaskPanel() throws ClassNotFoundException, IntrospectionException,
         InvocationTargetException, IllegalAccessException {
      tasksPanel = new JPanel();
      tasksPanel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.NORTHWEST;
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 0.001;
      c.weighty = 0.001;
      c.gridx = 0;
      c.gridy = 0;
      JScrollPane taskScroller = new JScrollPane(tasksPanel);
      c.gridy++;
      c.weighty = 1;
      tasksPanel.add(new JPanel(), c);

      return taskScroller;
   }

   /**
    * @param tasksPanel
    * @param c
    * @param task
    * @return
    * @throws ClassNotFoundException
    * @throws IntrospectionException
    * @throws InvocationTargetException
    * @throws IllegalAccessException
    */
   private void createTaskPanelAndAdd(JPanel tasksPanel, GridBagConstraints c, I_DefineTask task, File source)
         throws ClassNotFoundException, IntrospectionException, InvocationTargetException, IllegalAccessException {
      TaskPanel tp = new TaskPanel(task, true, true, null, source, worker);
      tp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
      tasksPanel.add(tp, c);
      c.gridy++;
   }

   private JScrollPane setupDataPanel(I_Work worker) throws RemoteException, PropertyVetoException,
         ClassNotFoundException, SecurityException, NoSuchMethodException {
      JPanel dataPanel = new JPanel();
      dataPanel.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.anchor = GridBagConstraints.NORTHWEST;
      c.fill = GridBagConstraints.BOTH;
      c.weightx = 0.001;
      c.weighty = 0.001;
      c.gridx = 0;
      c.gridy = 0;
      JScrollPane dataScroller = new JScrollPane(dataPanel);
      File[] dataBeanDirFile = new File("data").listFiles();
      if (dataBeanDirFile != null) {
         for (File f : dataBeanDirFile) {
            if (f.getName().endsWith(".data")) {
               try {
                  FileInputStream fis = new FileInputStream(f);
                  BufferedInputStream bis = new BufferedInputStream(fis);
                  ObjectInputStream ois = new ObjectInputStream(bis);
                  DataContainer data = (DataContainer) ois.readObject();
                  ois.close();
                  createDataPanelAndAdd(worker, dataPanel, c, data);
               } catch (Exception ex) {
                  logger.log(Level.WARNING, "Exception processing " + f + " (" + ex.toString() + ")", ex);
               }
            }
         }
      }
      c.weighty = 1;
      dataPanel.add(new JPanel(), c);

      return dataScroller;
   }

   /**
    * 
    * @param worker
    * @param dataPanel
    * @param c
    * @param container
    * @throws PropertyVetoException
    * @throws Exception
    */
 
   private void createDataPanelAndAdd(I_Work worker, JPanel dataPanel, GridBagConstraints c, DataContainer container)
         throws PropertyVetoException, Exception {
      DataContainerPanel dp;
      try {
         dp = new DataContainerPanel(container, true, true, null, worker);
         dp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
         dataPanel.add(dp, c);
      } catch (Exception e) {
         logger.log(Level.SEVERE, e.getMessage(), e);
         dataPanel.add(new JLabel("<html><font color='red'>Error: see log"));
      }
      c.gridy++;
   }

   /**
    * @return Returns the newProcessActionListener.
    */
   public NewProcessActionListener getNewProcessActionListener() {
      return newProcessActionListener;
   }

   /**
    * @return Returns the openProcessActionListener.
    */
   public ReadProcessActionListener getReadProcessActionListener() {
      return openProcessActionListener;
   }

   /**
    * @return Returns the saveProcessActionListener.
    */
   public SaveProcessActionListener getSaveProcessActionListener() {
      return saveProcessActionListener;
   }

   /**
    * @return
    */
   public ActionListener getTakeNoTranProcessActionListener() {
      return this.takeProcessNoTranActionListener;
   }

   public void actionPerformed(ActionEvent evt) {
      if (evt.getActionCommand().equals("taskAdded")) {
         try {
            this.updatePropertiesPane();
         } catch (Exception e) {
            logger.log(Level.SEVERE, e.toString(), e);
         }
      }

   }

   public ActionListener getSaveForLauncherQueueActionListener() {
      return saveProcessForLauncherQueueActionListener;
   }

   public ActionListener getSaveAsXmlActionListener() {
      return saveProcessForXmlActionListener;
   }
}