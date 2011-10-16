package org.ihtsdo.arena.conceptview;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.path.PathExpander;
import org.ihtsdo.taxonomy.TaxonomyTree;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Dimension;

import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class FocusDrop extends JLabel implements I_AcceptConcept {

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   //~--- fields --------------------------------------------------------------

   private I_ConfigAceFrame config;
   private TaxonomyTree     navigatorTree;

   //~--- constructors --------------------------------------------------------

   public FocusDrop(ImageIcon imageIcon, TaxonomyTree navigatorTree, I_ConfigAceFrame config) {
      super(imageIcon);
      this.navigatorTree = navigatorTree;
      this.config        = config;
      setOpaque(true);
      setBackground(new Color(230, 230, 230));
      setMinimumSize(new Dimension(20, 20));
      setBorder(BorderFactory.createLoweredBevelBorder());
      setTransferHandler(new TerminologyTransferHandler(this));
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void sendConcept(I_GetConceptData c) {
      try {
         PathExpander epl = new PathExpander(navigatorTree, config, (ConceptChronicleBI) c);

         FutureHelper.addFuture(ACE.threadPool.submit(epl));
      } catch (IOException e1) {
         AceLog.getAppLog().alertAndLogException(e1);
      }
   }
}
