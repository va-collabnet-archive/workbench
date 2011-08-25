
/**
 *
 */
package org.dwfa.ace.gui.concept;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import org.ihtsdo.helper.dto.DtoExtract;
import org.ihtsdo.helper.dto.DtoToText;
import org.ihtsdo.helper.io.FileIO;
import org.ihtsdo.rules.RulesLibrary;

//~--- JDK imports ------------------------------------------------------------

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

public class ProgrammersPopupListener extends MouseAdapter implements ActionListener, ClipboardOwner {
   private static Map<String, MENU_OPTIONS> optionMap = new HashMap<String,
                                                           MENU_OPTIONS>(MENU_OPTIONS.values().length);

   //~--- static initializers -------------------------------------------------

   static {
      for (MENU_OPTIONS option : MENU_OPTIONS.values()) {
         optionMap.put(option.menuText, option);
      }
   }

   //~--- fields --------------------------------------------------------------

   JPopupMenu popup = new JPopupMenu();

   /**
    *
    */
   private final ConceptPanel conceptPanel;

   //~--- constructors --------------------------------------------------------

   public ProgrammersPopupListener(ConceptPanel conceptPanel) {
      for (MENU_OPTIONS option : MENU_OPTIONS.values()) {
         option.addToMenu(popup, this);
      }

      this.conceptPanel = conceptPanel;
   }

   //~--- enums ---------------------------------------------------------------

   private enum MENU_OPTIONS {
      //J-
      WRITE_LONG_FORM_TO_CLIPBOARD("Write long form to clipboard"), 
      SET_FROM_NID("Set from nid"),
      ADD_TO_WATCH_LIST("Add to watch list"), 
      REMOVE_FROM_WATCH_LIST("Remove from watch list"), 
      GET_CONCEPT_ATTRIBUTES("Get concept attributes..."),
      SET_CACHE_SIZE("Set cache size"), 
      SET_CACHE_PERCENT("Set cache percent"),
      CHANGE_SET_TO_TEXT("Change set to text..."),
      ALL_CHANGE_SET_TO_TEXT("All Change sets in profiles to text"), 
      EXTRACT_CHANGE_SETS_FOR_CONCEPT("Extract change sets for concept..."),
      DTO_TO_TEXT("DTO to text..."),
      IMPORT_CHANGE_SET("Import change set..."), 
      TOGGLE_QA("Toggle QA");
      //J+

      String menuText;

      //~--- constructors -----------------------------------------------------

      private MENU_OPTIONS(String menuText) {
         this.menuText = menuText;
      }

      //~--- methods ----------------------------------------------------------

      public void addToMenu(JPopupMenu popup, ActionListener l) {
         JMenuItem menuItem = new JMenuItem(menuText);

         menuItem.addActionListener(l);
         popup.add(menuItem);
      }

      //~--- get methods ------------------------------------------------------

      public String getText() {
         return this.menuText;
      }

      //~--- set methods ------------------------------------------------------

      public void setText(String menuText) {
         this.menuText = menuText;
      }
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void actionPerformed(ActionEvent e) {
      switch (optionMap.get(e.getActionCommand())) {
      case ADD_TO_WATCH_LIST :
         addToWatch();

         break;

      case REMOVE_FROM_WATCH_LIST :
         removeFromWatch();

         break;

      case SET_FROM_NID :
         setFromNid();

         break;

      case WRITE_LONG_FORM_TO_CLIPBOARD :
         writeLongFormToClipboard();

         break;

      case GET_CONCEPT_ATTRIBUTES :
         getConceptAttributes();

         break;

      case SET_CACHE_PERCENT :
         setCachePercent();

         break;

      case SET_CACHE_SIZE :
         setCacheSize();

         break;

      case CHANGE_SET_TO_TEXT :
      case DTO_TO_TEXT :
      case ALL_CHANGE_SET_TO_TEXT :
         toText(optionMap.get(e.getActionCommand()));

         break;

      case IMPORT_CHANGE_SET :
         importEccs();

         break;

      case TOGGLE_QA :
         toggleQa();

         break;

      case EXTRACT_CHANGE_SETS_FOR_CONCEPT :
         extractChangeSets();

         break;

      default :
         AceLog.getAppLog().alertAndLogException(new Exception("No support for: "
                 + optionMap.get(e.getActionCommand())));
      }
   }

   private void addToWatch() {
      I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();

      Terms.get().addToWatchList(igcd);
   }

   public String askQuestion(String realm, String question, String defaultAnswer) {
      return (String) JOptionPane.showInputDialog(this.conceptPanel, question, realm,
              JOptionPane.PLAIN_MESSAGE, null, null, defaultAnswer);
   }

   private void extractChangeSets() {
      File             rootFile       = new File("profiles");
      String           prefix         = null;
      String           suffix         = ".eccs";
      List<File>       changeSetFiles = FileIO.recursiveGetFiles(rootFile, prefix, suffix, true);
      I_GetConceptData igcd           = (I_GetConceptData) this.conceptPanel.getTermComponent();
      FileDialog       dialog         = new FileDialog(new Frame(),
                                           "Select name and location for new directory...");

      dialog.setMode(FileDialog.SAVE);
      dialog.setDirectory(System.getProperty("user.dir"));
      dialog.setFile(igcd.toUserString() + " cs extract");

      Set<UUID> concepts = new TreeSet<UUID>();

      concepts.add(igcd.getPrimUuid());
      dialog.setVisible(true);

      if (dialog.getFile() != null) {
         File csfd = new File(dialog.getDirectory(), dialog.getFile());

         csfd.mkdir();

         for (File csf : changeSetFiles) {
            try {
               File extractFile = new File(csfd, "ex-" + csf.getName());

               DtoExtract.extract(csf, concepts, extractFile);
            } catch (IOException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            } catch (ClassNotFoundException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }
      }
   }

   private void importEccs() {
      try {
         FileDialog dialog = new FileDialog(new Frame(), "Select change set file...");

         dialog.setMode(FileDialog.LOAD);
         dialog.setDirectory(System.getProperty("user.dir"));
         dialog.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
               return name.endsWith(".eccs");
            }
         });
         dialog.setVisible(true);

         if (dialog.getFile() != null) {
            File csf = new File(dialog.getDirectory(), dialog.getFile());

            try {
               Terms.get().suspendChangeSetWriters();

               I_ReadChangeSet csr = Terms.get().newBinaryChangeSetReader(csf);

               csr.read();
            } finally {
               Terms.get().resumeChangeSetWriters();
            }
         }
      } catch (Exception ex) {
         Logger.getLogger(ProgrammersPopupListener.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   @Override
   public void lostOwnership(Clipboard clipboard, Transferable contents) {

      // nothing to do...
   }

   private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
         if (e.isAltDown() || e.isControlDown()) {
            popup.show(e.getComponent(), e.getX(), e.getY());
         }
      }
   }

   @Override
   public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
   }

   @Override
   public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
   }

   private void removeFromWatch() {
      I_GetConceptData igcd = (I_GetConceptData) this.conceptPanel.getTermComponent();

      Terms.get().removeFromWatchList(igcd);
   }

   private void toText(MENU_OPTIONS option) {
      try {
         switch (option) {
         case ALL_CHANGE_SET_TO_TEXT :
            File       rootFile       = new File("profiles");
            String     prefix         = null;
            String     suffix         = ".eccs";
            List<File> changeSetFiles = FileIO.recursiveGetFiles(rootFile, prefix, suffix, true);

            for (File csf : changeSetFiles) {
               DtoToText.convertChangeSet(csf);
            }

            break;

         default :
            JFileChooser fc        = new JFileChooser();
            int          returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
               switch (option) {
               case CHANGE_SET_TO_TEXT :
                  DtoToText.convertChangeSet(fc.getSelectedFile());

                  break;

               case DTO_TO_TEXT :
                  DtoToText.convertDto(fc.getSelectedFile());

                  break;
               }
            }
         }
      } catch (IOException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      } catch (ClassNotFoundException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   private void toggleQa() {
      if (RulesLibrary.rulesDisabled == false) {
         RulesLibrary.rulesDisabled = true;
         JOptionPane.showMessageDialog(null, "QA is Disabled!.", "Warning", JOptionPane.WARNING_MESSAGE);
      } else {
         RulesLibrary.rulesDisabled = false;
         JOptionPane.showMessageDialog(null, "QA is Enabled!.", "Warning", JOptionPane.WARNING_MESSAGE);
      }
   }

   private void writeLongFormToClipboard() {
      I_GetConceptData igcd     = (I_GetConceptData) this.conceptPanel.getTermComponent();
      Clipboard        clip     = Toolkit.getDefaultToolkit().getSystemClipboard();
      StringSelection  contents = new StringSelection(igcd.toLongString());

      clip.setContents(contents, this);
   }

   //~--- get methods ---------------------------------------------------------

   private void getConceptAttributes() {
      try {
         I_GetConceptData                        igcd    =
            (I_GetConceptData) this.conceptPanel.getTermComponent();
         I_ConceptAttributeVersioned             attr    = igcd.getConceptAttributes();
         List<? extends I_ConceptAttributeTuple> tuples  = attr.getTuples();
         List<? extends I_ConceptAttributeTuple> tuples2 =
            attr.getTuples(conceptPanel.getConfig().getAllowedStatus(),
                           conceptPanel.getConfig().getViewPositionSetReadOnly());
         List<? extends I_ConceptAttributeTuple> tuples3 =
            igcd.getConceptAttributeTuples(null, conceptPanel.getConfig().getViewPositionSetReadOnly(),
                                           conceptPanel.getConfig().getPrecedence(),
                                           conceptPanel.getConfig().getConflictResolutionStrategy());
         List<? extends I_ConceptAttributeTuple> tuples4 =
            igcd.getConceptAttributeTuples(null, conceptPanel.getConfig().getViewPositionSetReadOnly(),
                                           conceptPanel.getConfig().getPrecedence(),
                                           conceptPanel.getConfig().getConflictResolutionStrategy());

         AceLog.getAppLog().info("attr: " + attr);
         AceLog.getAppLog().info("tuples 1: " + tuples);
         AceLog.getAppLog().info("tuples 2: " + tuples2);
         AceLog.getAppLog().info("tuples 3: " + tuples3);
         AceLog.getAppLog().info("tuples 4: " + tuples4);
      } catch (IOException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      } catch (TerminologyException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   //~--- set methods ---------------------------------------------------------

   private void setCachePercent() {
      String percentString = askQuestion("Set bdb cache percent:", "Enter percent[1..99]:",
                                         "" + Terms.get().getCachePercent());

      if (percentString != null) {
         Terms.get().setCachePercent(percentString);
      }
   }

   private void setCacheSize() {
      String sizeString = askQuestion("Set bdb cache size:", "Enter size[XXXXm|XXg]:",
                                      "" + Terms.get().getCacheSize());

      if (sizeString != null) {
         Terms.get().setCacheSize(sizeString);
      }
   }

   private void setFromNid() {
      String nidString = askQuestion("Set panel to new concept:", "Enter nid:", "-2142075612");
      int    nid       = Integer.parseInt(nidString);

      try {
         I_GetConceptData concept = Terms.get().getConceptForNid(nid);

         this.conceptPanel.setTermComponent(concept);
      } catch (IOException ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }
}
