package org.ihtsdo.arena.taxonomyview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.tree.JTreeWithDragImage;

import org.ihtsdo.arena.ArenaComponentSettings;
import org.ihtsdo.taxonomy.TaxonomyHelper;
import org.ihtsdo.taxonomy.TaxonomyMouseListener;
import org.ihtsdo.taxonomy.TaxonomyTree;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class TaxonomyViewSettings extends ArenaComponentSettings {
   private static final int dataVersion = 1;

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   // transient
   private JScrollPane view;

   //~--- methods -------------------------------------------------------------

   @Override
   public JComponent makeComponent(I_ConfigAceFrame config) {
      if (view == null) {
         TaxonomyHelper hierarchicalTreeHelper = new TaxonomyHelper(config, "Arena taxonomy", null);

         try {
            view = hierarchicalTreeHelper.getHierarchyPanel();
            hierarchicalTreeHelper.addMouseListener(new TaxonomyMouseListener(hierarchicalTreeHelper));

            TaxonomyTree tree = (TaxonomyTree) view.getViewport().getView();

            tree.setFont(tree.getFont().deriveFont(getFontSize()));
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

      return view;
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion == dataVersion) {

         //
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   @Override
   protected void setupSubtypes() {

      // TODO Auto-generated method stub
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public I_GetConceptData getConcept() {
      return null;
   }

   @Override
   public I_HostConceptPlugins getHost() {
      return null;
   }

   @Override
   public JComponent getLinkComponent() {
      return new JLabel("   ");
   }

   @Override
   public List<AbstractButton> getSpecializedButtons() {
      return new ArrayList<AbstractButton>();
   }

   @Override
   public String getTitle() {
      return "Taxonomy Viewer";
   }
}
