
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.arena.task;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.InstructAndWait;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.wfpanel.PreviousNextOrCancel;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

import org.ihtsdo.arena.conceptview.FixedWidthJEditorPane;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.WizardBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.IdDirective;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.util.swing.GuiUtil;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.IOException;
import java.io.ObjectOutputStream;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author kec
 */
@BeanList(specs = { @Spec(
   directory = "tasks/ide/instruct",
   type      = BeanType.TASK_BEAN
) , @Spec(
   directory = "tasks/arena/wizard",
   type      = BeanType.TASK_BEAN
) })
public class NewConcept extends PreviousNextOrCancel {

   /*
    * -----------------------
    * Properties
    * -----------------------
    */

   // Serialization Properties

   /** Field description */
   private static final long serialVersionUID = 1;

   /** Field description */
   private static final int dataVersion = 1;

   // Task Attribute Properties

   /** Field description */
   private String instruction = "<html>Enter data for new concept:";

   /** Field description */
   private String relParentPropName = ProcessAttachmentKeys.REL_PARENT.getAttachmentKey();

   /** Field description */
   private boolean addUsDescFsn = false;

   /** Field description */
   private boolean addGbDescFsn = false;

   /** Field description */
   private boolean addUsDescPref = false;

   /** Field description */
   private boolean addGbDescPref = false;

   // Other Properties

   /** Field description */
   private I_ConfigAceFrame config;

   /** Field description */
   private I_HostConceptPlugins host;

   /** Field description */
   private transient WizardBI wizard;

   /** Field description */
   private TerminologyList tl;

   /** Field description */
   private FixedWidthJEditorPane fsn;

   /** Field description */
   private JLabel inputFsnLabel;

   /** Field description */
   private JLabel inputPrefLabel;

   /** Field description */
   private FixedWidthJEditorPane pref;

   /** Field description */
   private FixedWidthJEditorPane usFsn;

   /** Field description */
   private FixedWidthJEditorPane gbFsn;

   /** Field description */
   private JCheckBox gbBoxFsn;

   /** Field description */
   private JCheckBox usBoxFsn;

   /** Field description */
   private JLabel gbLabelFsn;

   /** Field description */
   private JLabel usLabelFsn;

   /** Field description */
   private FixedWidthJEditorPane usPref;

   /** Field description */
   private FixedWidthJEditorPane gbPref;

   /** Field description */
   private JCheckBox gbBoxPref;

   /** Field description */
   private JCheckBox usBoxPref;

   /** Field description */
   private JLabel gbLabelPref;

   /** Field description */
   private JLabel usLabelPref;

   /** Field description */
   private List<Integer> nidList;

   /** Field description */
   private ConceptCB conceptSpec;

   /** Field description */
   private DescriptionCAB descSpecGbFsn;

   /** Field description */
   private DescriptionCAB descSpecUsFsn;

   /** Field description */
   private RefexCAB refexSpecGbFsn;

   /** Field description */
   private RefexCAB refexSpecUsFsn;

   /** Field description */
   private DescriptionCAB descSpecGbPref;

   /** Field description */
   private DescriptionCAB descSpecUsPref;

   /** Field description */
   private RefexCAB refexSpecGbPref;

   /** Field description */
   private RefexCAB refexSpecUsPref;

   /** Field description */
   private RefexCAB refexSpecUsAcct;

   /** Field description */
   private RefexCAB refexSpecGbAcct;

   /** Field description */
   private TerminologyBuilderBI tc;

   /** Field description */
   private ConceptChronicleBI newConcept;

   /** Field description */
   private LANG_CODE lang;

   /** Field description */
   private ConceptChronicleBI gbRefexConcept;

   /** Field description */
   private ConceptChronicleBI usRefexConcept;

   /** Field description */
   private UUID gbUuid;

   /** Field description */
   private UUID usUuid;

   /** Field description */
   private ConceptChronicleBI acceptableConcept;

   /** Field description */
   private ConceptChronicleBI preferredConcept;

   /** Field description */
   private ConceptChronicleBI fsnConcept;

   /** Field description */
   private ConceptChronicleBI synConcept;

   /** Field description */
   private String fsnText;

   /** Field description */
   private String prefText;

   /**
    * Method description
    *
    *
    * @param prefText
    * @param fsnText
    */
   public void addSpellingVarients(String prefText, String fsnText) {
      String varient = "";
      String extra   = "";

      if ((fsnText.indexOf("(") != -1) && (fsnText.indexOf(")") != -1)) {
         extra = fsnText.substring(fsnText.indexOf("("), fsnText.indexOf(")") + 1);
      }

      try {
         if (DialectHelper.isTextForDialect(prefText, Language.EN_US.getLenient().getNid())
             && DialectHelper.isTextForDialect(prefText, Language.EN_UK.getLenient().getNid())) {
            lang = LANG_CODE.EN;
            this.inputFsnLabel.setText("fsn en-GB / en-US");
            this.inputFsnLabel.setVisible(true);
            this.inputPrefLabel.setText("pref en-GB / en-US");
            this.inputPrefLabel.setVisible(true);
            this.usBoxPref.setVisible(false);
            this.usBoxPref.setSelected(false);
            this.usPref.setVisible(false);
            this.usLabelPref.setVisible(false);
            this.gbBoxPref.setVisible(false);
            this.gbBoxPref.setSelected(false);
            this.gbPref.setVisible(false);
            this.gbLabelPref.setVisible(false);
            this.gbBoxFsn.setSelected(false);
            this.gbBoxFsn.setVisible(false);
            this.gbFsn.setVisible(false);
            this.gbLabelFsn.setVisible(false);
            this.usBoxFsn.setVisible(false);
            this.usBoxFsn.setSelected(false);
            this.usFsn.setVisible(false);
            this.usLabelFsn.setVisible(false);
         } else if (DialectHelper.isTextForDialect(prefText,
             Language.EN_UK.getLenient().getNid())) {    // check if lang is en-us
            lang = LANG_CODE.EN_US;
            this.inputFsnLabel.setText("fsn en-US");
            this.inputFsnLabel.setVisible(true);
            this.inputPrefLabel.setText("pref en-US");
            this.inputPrefLabel.setVisible(true);

            // get fsn
            varient = DialectHelper.makeTextForDialect(prefText, Language.EN_US.getLenient().getNid());
            this.gbFsn.setText(varient + " " + extra);
            this.gbBoxFsn.setSelected(true);
            this.gbBoxFsn.setVisible(false);
            this.gbFsn.setVisible(false);
            this.gbLabelFsn.setVisible(false);
            this.usBoxFsn.setVisible(false);
            this.usBoxFsn.setSelected(false);
            this.usFsn.setVisible(false);
            this.usLabelFsn.setVisible(false);

            // get pref
            varient = DialectHelper.makeTextForDialect(prefText, Language.EN_US.getLenient().getNid());
            this.gbPref.setText(varient);
            this.gbBoxPref.setSelected(true);
            this.gbBoxPref.setVisible(false);
            this.gbPref.setVisible(false);
            this.gbLabelPref.setVisible(false);
            this.usBoxPref.setVisible(false);
            this.usBoxPref.setSelected(false);
            this.usPref.setVisible(false);
            this.usLabelPref.setVisible(false);
         } else if (DialectHelper.isTextForDialect(prefText,
             Language.EN_US.getLenient().getNid())) {    // check if lang is en-gb
            lang = LANG_CODE.EN_GB;
            this.inputFsnLabel.setText("fsn en-GB");
            this.inputFsnLabel.setVisible(true);
            this.inputPrefLabel.setText("pref en-GB");
            this.inputPrefLabel.setVisible(true);

            // get fsn
            varient = DialectHelper.makeTextForDialect(prefText, Language.EN_UK.getLenient().getNid());
            this.usFsn.setText(varient + " " + extra);
            this.usBoxFsn.setSelected(true);
            this.usBoxFsn.setVisible(false);
            this.usFsn.setVisible(false);
            this.usLabelFsn.setVisible(false);
            this.gbBoxFsn.setVisible(false);
            this.gbBoxFsn.setSelected(false);
            this.gbFsn.setVisible(false);
            this.gbLabelFsn.setVisible(false);

            // get pref
            varient = DialectHelper.makeTextForDialect(prefText, Language.EN_UK.getLenient().getNid());
            this.usPref.setText(varient);
            this.usBoxPref.setSelected(true);
            this.usBoxPref.setVisible(false);
            this.usPref.setVisible(false);
            this.usLabelPref.setVisible(false);
            this.gbBoxPref.setVisible(false);
            this.gbBoxPref.setSelected(false);
            this.gbPref.setVisible(false);
            this.gbLabelPref.setVisible(false);
         }
      } catch (UnsupportedDialectOrLanguage | IOException ex) {
         Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   /**
    * Method description
    *
    *
    * @param process
    * @param worker
    *
    * @throws TaskFailedException
    */
   @Override
   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

      // Nothing to do
   }

   /**
    * Method description
    *
    *
    * @throws ContradictionException
    */
   private void createBlueprintConcept() throws ContradictionException {
      tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

      try {

         // get parents
         UUID   isa       = Snomed.IS_A.getLenient().getPrimUuid();
         UUID[] uuidArray = new UUID[nidList.size()];

         for (int index = 0; index < nidList.size(); index++) {
            uuidArray[index] = Terms.get().nidToUuid(nidList.get(index));
         }

         if (uuidArray.length > 0) {
            ConceptVersionBI firstParent = Ts.get().getConceptVersion(config.getViewCoordinate(),
                                              uuidArray[0]);
            Collection<? extends RelationshipVersionBI> outgoingIsa =
               firstParent.getRelationshipsOutgoingActiveIsa();

            if (!outgoingIsa.isEmpty()) {
               isa = Ts.get().getUuidPrimordialForNid(outgoingIsa.iterator().next().getTypeNid());
            } else {
               Collection<? extends RelationshipVersionBI> incomingIsa =
                  firstParent.getRelationshipsIncomingActiveIsa();

               if (!outgoingIsa.isEmpty()) {
                  isa = Ts.get().getUuidPrimordialForNid(incomingIsa.iterator().next().getTypeNid());
               }
            }
         }

         // create concept blue print
         if (lang.equals(LANG_CODE.EN_GB)) {
            conceptSpec = new ConceptCB(fsnText, prefText, LANG_CODE.EN, isa, IdDirective.GENERATE_HASH,
                                        uuidArray);
         } else {
            conceptSpec = new ConceptCB(fsnText, prefText, LANG_CODE.EN, isa, IdDirective.GENERATE_HASH,
                                        uuidArray);
         }

         conceptSpec.setComponentUuid(UUID.randomUUID());

         List<DescriptionCAB> fullySpecifiedNameCABs = conceptSpec.getFullySpecifiedNameCABs();

         for (DescriptionCAB fsnBp : fullySpecifiedNameCABs) {
            String   text  = fsnBp.getText();
            String[] parts = text.split(" ");

            fsnBp.initialCaseSignificant = CsWordsHelper.isIcTypeSignificant(parts[0],
                CaseSensitive.IC_SIGNIFICANT.getLenient().getNid());
         }

         List<DescriptionCAB> preferredNameCABs = conceptSpec.getPreferredNameCABs();

         for (DescriptionCAB prefBp : preferredNameCABs) {
            String   text  = prefBp.getText();
            String[] parts = text.split(" ");

            prefBp.initialCaseSignificant = CsWordsHelper.isIcTypeSignificant(parts[0],
                CaseSensitive.IC_SIGNIFICANT.getLenient().getNid());
         }

         newConcept = tc.constructIfNotCurrent(conceptSpec);
      } catch (IOException | InvalidCAB e) {
         AceLog.getAppLog().alertAndLogException(e);
      }
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintGbAcctRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecGbAcct = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(gbUuid),
                                        IdDirective.GENERATE_HASH);
         refexSpecGbAcct.put(
             RefexProperty.CNID1,
             Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

         RefexChronicleBI<?> annot = tc.construct(refexSpecGbAcct);

         if (!gbRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(gbRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @throws ContradictionException
    */
   private void createBlueprintGbFsnDesc() throws ContradictionException {
      String text = gbFsn.extractText();

      text = text.replaceAll("[\\s]", " ");
      text = text.replaceAll("   *", " ");

      try {
         String[] parts = text.split(" ");

         descSpecGbFsn = new DescriptionCAB(
             conceptSpec.getComponentUuid(), fsnConcept.getPrimUuid(), LANG_CODE.EN_GB, text,
             CsWordsHelper.isIcTypeSignificant(parts[0], CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()),
             IdDirective.GENERATE_HASH);
         tc.construct(descSpecGbFsn);
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintGbFsnRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecGbFsn = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(gbUuid),
                                       IdDirective.GENERATE_HASH);
         refexSpecGbFsn.put(RefexProperty.CNID1, preferredConcept.getNid());

         RefexChronicleBI<?> annot = tc.construct(refexSpecGbFsn);

         if (!gbRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(gbRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws ContradictionException
    */
   private ComponentVersionBI createBlueprintGbPrefDesc() throws ContradictionException {
      String text = gbPref.extractText();

      text = text.replaceAll("[\\s]", " ");
      text = text.replaceAll("   *", " ");

      ComponentVersionBI cv = null;

      try {
         String[] parts = text.split(" ");

         descSpecGbPref = new DescriptionCAB(conceptSpec.getComponentUuid(), synConcept.getPrimUuid(),
             LANG_CODE.EN_GB, text,
             CsWordsHelper.isIcTypeSignificant(parts[0], CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()),
             IdDirective.GENERATE_HASH);

         DescriptionChronicleBI dc = tc.construct(descSpecGbPref);

         cv = Ts.get().getComponentVersion(config.getViewCoordinate(), dc.getNid());
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }

      return cv;
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintGbPrefRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecGbPref = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(gbUuid),
                                        IdDirective.GENERATE_HASH);
         refexSpecGbPref.put(RefexProperty.CNID1, preferredConcept.getNid());

         RefexChronicleBI<?> annot = tc.construct(refexSpecGbPref);

         if (!gbRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(gbRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintUsAcctRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecUsAcct = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(usUuid),
                                        IdDirective.GENERATE_HASH);
         refexSpecUsAcct.put(
             RefexProperty.CNID1,
             Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

         RefexChronicleBI<?> annot = tc.construct(refexSpecUsAcct);

         if (!usRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(usRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @throws ContradictionException
    */
   private void createBlueprintUsFsnDesc() throws ContradictionException {
      String text = usFsn.extractText();

      text = text.replaceAll("[\\s]", " ");
      text = text.replaceAll("   *", " ");

      try {
         String[] parts = text.split(" ");

         descSpecUsFsn = new DescriptionCAB(
             conceptSpec.getComponentUuid(), fsnConcept.getPrimUuid(), LANG_CODE.EN_US, text,
             CsWordsHelper.isIcTypeSignificant(parts[0], CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()),
             IdDirective.GENERATE_HASH);
         tc.construct(descSpecUsFsn);
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintUsFsnRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecUsFsn = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(usUuid),
                                       IdDirective.GENERATE_HASH);
         refexSpecUsFsn.put(RefexProperty.CNID1, preferredConcept.getNid());

         RefexChronicleBI<?> annot = tc.construct(refexSpecUsFsn);

         if (!usRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(usRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @return
    *
    * @throws ContradictionException
    */
   private ComponentVersionBI createBlueprintUsPrefDesc() throws ContradictionException {
      String text = usPref.extractText();

      text = text.replaceAll("[\\s]", " ");
      text = text.replaceAll("   *", " ");

      ComponentVersionBI cv = null;

      try {
         String[] parts = text.split(" ");

         descSpecUsPref = new DescriptionCAB(conceptSpec.getComponentUuid(), synConcept.getPrimUuid(),
             LANG_CODE.EN_US, text,
             CsWordsHelper.isIcTypeSignificant(parts[0], CaseSensitive.IC_SIGNIFICANT.getLenient().getNid()),
             IdDirective.GENERATE_HASH);

         DescriptionChronicleBI dc = tc.construct(descSpecUsPref);

         cv = Ts.get().getComponentVersion(config.getViewCoordinate(), dc.getNid());
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }

      return cv;
   }

   /**
    * Method description
    *
    *
    * @param cv
    *
    * @throws ContradictionException
    */
   private void createBlueprintUsPrefRefex(ComponentVersionBI cv) throws ContradictionException {
      try {
         refexSpecUsPref = new RefexCAB(TK_REFEX_TYPE.CID, cv.getNid(), Ts.get().getNidForUuids(usUuid),
                                        IdDirective.GENERATE_HASH);
         refexSpecUsPref.put(RefexProperty.CNID1, preferredConcept.getNid());

         RefexChronicleBI<?> annot = tc.construct(refexSpecUsPref);

         if (!usRefexConcept.isAnnotationStyleRefex()) {
            Ts.get().addUncommitted(usRefexConcept);
         }
      } catch (IOException | InvalidCAB ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   /**
    * Method description
    *
    *
    * @param process
    * @param worker
    *
    * @return
    *
    * @throws TaskFailedException
    */
   @Override
   public Condition evaluate(final I_EncodeBusinessProcess process, final I_Work worker)
           throws TaskFailedException {
      try {

         // Present the user interface in the Workflow panel
         wizard = (WizardBI) worker.readAttachement(WorkerAttachmentKeys.WIZARD_PANEL.name());
         config = (I_ConfigAceFrame) worker.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
         host   =
            (I_HostConceptPlugins) worker.readAttachement(WorkerAttachmentKeys.I_HOST_CONCEPT_PLUGINS.name());
         host.unlink();

         DoSwing swinger = new DoSwing(process);

         swinger.execute();
         new Thread(new Runnable() {
            @Override
            public void run() {
               try {
                  CsWordsHelper.lazyInit();
               } catch (IOException ex) {
                  AceLog.getAppLog().alertAndLogException(ex);
               }
            }
         }).start();

         synchronized (this) {
            this.waitTillDone(worker.getLogger());
         }

         MakeNewConcept maker = new MakeNewConcept();

         maker.execute();
         restore();
         maker.getLatch().await();
      } catch (InterruptedException | InvocationTargetException | IllegalArgumentException e) {
         throw new TaskFailedException(e);
      }

      return returnCondition;
   }

   /**
    * Method description
    *
    *
    * @param in
    *
    * @throws ClassNotFoundException
    * @throws IOException
    */
   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();

      if (objDataVersion <= dataVersion) {
         if (objDataVersion >= 1) {
            instruction       = (String) in.readObject();
            relParentPropName = (String) in.readObject();
         } else {
            instruction = "<html>Select Parent for Concept Being Retired:";
         }
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   /**
    * This method overrides a method by the same name in the parent class. It
    * is used
    * to tell the parent class (PreviousNextOrCancel) whether to show
    * the previous button or not. Since we only want the Next and Cancel
    * buttons,
    * this method returns false .
    *
    * @return
    */
   @Override
   protected boolean showPrevious() {
      return false;
   }

   /*
    * -----------------------
    * Serialization Methods
    * -----------------------
    */

   /**
    * Method description
    *
    *
    * @param out
    *
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(instruction);
      out.writeObject(relParentPropName);
   }

   /**
    * Get the instructions for this task
    *
    * @return The instructions for this task.
    */
   public String getInstruction() {
      return instruction;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   public String getRelParentPropName() {
      return relParentPropName;
   }

   /**
    * Method description
    *
    *
    * @return
    */
   protected static String getToDoImage() {
      return "/24x24/plain/inbox_into.png";
   }

   /**
    * Set the instructions for this task
    *
    * @param instruction The instructions for this task.
    */
   public void setInstruction(String instruction) {
      this.instruction = instruction;
   }

   /**
    * Method description
    *
    *
    * @param newStatusPropName
    */
   public void setRelParentPropName(String newStatusPropName) {
      this.relParentPropName = newStatusPropName;
   }

   /**
    * Method description
    *
    *
    * @param wizardPanel
    * @param c
    */
   protected void setUpButtons(final JPanel wizardPanel, GridBagConstraints c) {
      c.gridx++;
      wizardPanel.add(new JLabel("  "), c);
      c.gridx++;
      c.anchor = GridBagConstraints.SOUTHWEST;

      JButton continueButton =
         new JButton(new ImageIcon(InstructAndWait.class.getResource(getContinueImage())));

      continueButton.setToolTipText("continue");
      wizardPanel.add(continueButton, c);
      continueButton.addActionListener(new BlueprintContinueActionListener());
      c.gridx++;

      JButton cancelButton = new JButton(new ImageIcon(InstructAndWait.class.getResource(getCancelImage())));

      cancelButton.setToolTipText("cancel");
      wizardPanel.add(cancelButton, c);
      cancelButton.addActionListener(new StopActionListener());
      c.gridx++;
      wizardPanel.add(new JLabel("     "), c);
      wizardPanel.validate();

      Container cont = wizardPanel;

      while (cont != null) {
         cont.validate();
         cont = cont.getParent();
      }

      wizardPanel.repaint();
   }

   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class BlueprintContinueActionListener implements ActionListener {

      /**
       * Method description
       *
       *
       * @param e
       */
      @Override
      public void actionPerformed(ActionEvent e) {
         TerminologyListModel model = (TerminologyListModel) tl.getModel();

         nidList = model.getNidsInList();

         if (fsn.extractText().length() == 0) {

            // please enter the fsn
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "please enter the fsn", "",
                                          JOptionPane.ERROR_MESSAGE);
         } else if ((fsn.extractText().length() != 0)
                    && ((fsn.extractText().indexOf("(") == -1) || (fsn.extractText().indexOf(")") == -1))) {

            // test for semantic tag
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                          "please enter the semantic tag", "", JOptionPane.ERROR_MESSAGE);
         } else if (fsn.extractText().split("\\s").length == 1) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                          "the fsn and semantic tag should be separated by a space", "",
                                          JOptionPane.ERROR_MESSAGE);
         } else if (fsn.extractText().indexOf("(") == fsn.extractText().lastIndexOf(")") - 1) {
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                          "the semantic tag should not be empty", "",
                                          JOptionPane.ERROR_MESSAGE);
         } else if (nidList.isEmpty()) {

            // please list parents for the new concept
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                          "please list parents for the new concept", "",
                                          JOptionPane.ERROR_MESSAGE);
         } else if ((fsn.extractText().length() != 0) && (fsn.extractText().indexOf("(") > 0)
                    && (fsn.extractText().indexOf(")") > fsn.extractText().indexOf("("))) {

            // get text parts and make query term
            String fullFsn = fsn.extractText();

            fullFsn = fullFsn.replaceAll("\n", "");
            fullFsn = fullFsn.replaceAll("   *", " ");

            String[]        fsnWords = fullFsn.split("\\s");
            HashSet<String> wordSet  = new HashSet<>();

            for (String word : fsnWords) {
               if (!wordSet.contains(word) && (word.length() > 1) &&!word.startsWith("(")
                   &&!word.endsWith(")")) {
                  word = QueryParser.escape(word);
                  wordSet.add(word);
               }
            }

            String queryTerm = null;

            for (String word : wordSet) {
               if (queryTerm == null) {
                  queryTerm = "+" + word;
               } else {
                  queryTerm = queryTerm + " " + "+" + word;
               }
            }

            try {
               SearchResult result = Terms.get().doLuceneSearch(queryTerm);

               if (result.topDocs.totalHits == 0) {
                  returnCondition = Condition.CONTINUE;
                  done            = true;
                  NewConcept.this.notifyTaskDone();
               }

               NidSetBI allowedStatusNids = config.getViewCoordinate().getAllowedStatusNids();
               Boolean  found             = false;

search:
               for (int i = 0; i < result.topDocs.totalHits; i++) {
                  Document                  doc           =
                     result.searcher.doc(result.topDocs.scoreDocs[i].doc);
                  int                       cnid          = Integer.parseInt(doc.get("cnid"));
                  int                       dnid          = Integer.parseInt(doc.get("dnid"));
                  I_DescriptionVersioned<?> potential_fsn = Terms.get().getDescription(dnid, cnid);

                  if (potential_fsn != null) {
                     for (I_DescriptionPart part_search : potential_fsn.getMutableParts()) {
                        String test = part_search.getText();

                        if (allowedStatusNids.contains(part_search.getStatusNid())
                            && part_search.getText().equals(fullFsn)) {
                           found = true;

                           break search;
                        } else {
                           found = false;
                        }
                     }
                  }
               }

               if (found) {
                  JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                                                "<html>FSN already used: " + fullFsn, "",
                                                JOptionPane.ERROR_MESSAGE);
               } else {
                  returnCondition = Condition.CONTINUE;
                  done            = true;
                  NewConcept.this.notifyTaskDone();
               }
            } catch (IOException | TerminologyException | ParseException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class CopyTextDocumentListener implements DocumentListener {

      /** Field description */
      int paren;

      /**
       * Method description
       *
       *
       * @param arg0
       */
      @Override
      public void changedUpdate(DocumentEvent arg0) {
         fsnText  = "";
         prefText = "";
         fsnText  = fsn.extractText();
         fsnText  = fsnText.replaceAll("[\\s]", " ");
         fsnText  = fsnText.replaceAll("   *", " ");
         paren    = fsnText.lastIndexOf("(");

         if (paren == -1) {
            prefText = fsnText;
            pref.setText(prefText);
         } else {
            prefText = fsnText.substring(0, paren).trim();
            pref.setText(prefText);
         }

         addSpellingVarients(prefText, fsnText);
      }

      /**
       * Method description
       *
       *
       * @param arg0
       */
      @Override
      public void insertUpdate(DocumentEvent arg0) {
         fsnText  = "";
         prefText = "";
         fsnText  = fsn.extractText();
         fsnText  = fsnText.replaceAll("[\\s]", " ");
         fsnText  = fsnText.replaceAll("   *", " ");
         paren    = fsnText.lastIndexOf("(");

         if (paren == -1) {
            prefText = fsnText;
            pref.setText(prefText);
         } else {
            prefText = fsnText.substring(0, paren).trim();
            pref.setText(prefText);
         }

         addSpellingVarients(prefText, fsnText);
      }

      /**
       * Method description
       *
       *
       * @param arg0
       */
      @Override
      public void removeUpdate(DocumentEvent arg0) {
         fsnText  = "";
         prefText = "";
         fsnText  = fsn.extractText();
         fsnText  = fsnText.replaceAll("[\\s]", " ");
         fsnText  = fsnText.replaceAll("   *", " ");
         paren    = fsnText.lastIndexOf("(");

         if (paren == -1) {
            prefText = fsnText;
            pref.setText(prefText);
         } else {
            prefText = fsnText.substring(0, paren).trim();
            pref.setText(prefText);
         }

         addSpellingVarients(prefText, fsnText);
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   private class DoSwing extends SwingWorker<Boolean, Boolean> {

      /** Field description */
      I_EncodeBusinessProcess process;

      /**
       * Constructs ...
       *
       *
       * @param process
       */
      public DoSwing(I_EncodeBusinessProcess process) {
         super();
         this.process = process;
         wizard.setWizardPanelVisible(true);
      }

      /**
       * Method description
       *
       *
       * @return
       *
       * @throws Exception
       */
      @Override
      protected Boolean doInBackground() throws Exception {
         setup(process);

         return true;
      }

      /**
       * Method description
       *
       */
      @Override
      protected void done() {
         try {
            get();

            JPanel      wizardPanel = wizard.getWizardPanel();
            Component[] components  = wizardPanel.getComponents();

            for (int i = 0; i < components.length; i++) {
               wizardPanel.remove(components[i]);
            }

            JPanel      workflowPanel   = config.getWorkflowPanel();
            Component[] componentsPanel = workflowPanel.getComponents();

            for (int i = 0; i < componentsPanel.length; i++) {
               workflowPanel.remove(componentsPanel[i]);
            }

            wizardPanel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();

            c.fill = GridBagConstraints.BOTH;

            // Add the Instructions
            c.gridx     = 0;
            c.gridy     = 0;
            c.weightx   = 1.0;
            c.weighty   = 0;
            c.gridwidth = 2;
            c.anchor    = GridBagConstraints.EAST;
            wizardPanel.add(new JLabel(instruction), c);

            // Add the processing buttons
            c.weightx   = 0.0;
            c.gridx     = 2;
            c.gridwidth = 1;
            setUpButtons(wizardPanel, c);
            c.gridx = 0;
            c.gridy++;
            c.weightx   = 0.0;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridwidth = 10;
            c.gridy++;
            wizardPanel.add(new JLabel("fully specified name:"), c);
            c.gridy++;
            c.weightx = 1.0;
            fsn       = new FixedWidthJEditorPane();
            fsn.setText("");
            fsn.setFixedWidth(300);
            fsn.setEditable(true);
            fsn.getDocument().addDocumentListener(new CopyTextDocumentListener());
            wizardPanel.add(fsn, c);
            c.gridwidth   = 4;
            c.gridx       += c.gridwidth;
            c.gridwidth   = 2;
            c.weightx     = 0.0;
            inputFsnLabel = new JLabel("fsn");
            inputFsnLabel.setVisible(false);
            wizardPanel.add(inputFsnLabel, c);
            c.gridy++;
            c.gridx   = 0;
            c.weightx = 0.0;
            c.gridy++;
            c.weightx = 0.0;
            wizardPanel.add(new JLabel("preferred name:"), c);
            c.gridy++;
            c.weightx = 1.0;
            pref      = new FixedWidthJEditorPane();
            pref.setText("");
            pref.setFixedWidth(300);
            pref.setEditable(true);
            wizardPanel.add(pref, c);
            c.gridwidth    = 4;
            c.gridx        += c.gridwidth;
            c.gridwidth    = 2;
            c.weightx      = 0.0;
            inputPrefLabel = new JLabel("pref");
            inputPrefLabel.setVisible(false);
            wizardPanel.add(inputPrefLabel, c);
            c.gridy++;
            c.gridx   = 0;
            c.weightx = 0.0;
            c.gridy++;
            c.weightx = 0.0;
            wizardPanel.add(new JLabel("parents:"), c);
            c.gridy++;
            c.weightx = 1.0;
            c.weighty = 1.0;
            tl        = new TerminologyList(config);
            wizardPanel.add(tl, c);
            c.weighty = 0.0;
            c.gridy++;
            c.gridx     = 0;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.weightx   = 1.0;
            c.gridwidth = 1;
            c.weightx   = 0.0;
            gbBoxFsn    = new JCheckBox();
            gbBoxFsn.addItemListener(new GbDialectFsnItemListener());
            gbBoxFsn.setVisible(false);
            wizardPanel.add(gbBoxFsn, c);
            c.gridx++;
            c.gridwidth = 3;
            c.weightx   = 1.0;
            gbFsn       = new FixedWidthJEditorPane();
            gbFsn.setFixedWidth(300);
            gbFsn.setEditable(true);
            gbFsn.setVisible(false);
            wizardPanel.add(gbFsn, c);
            c.gridx     += c.gridwidth;
            c.gridwidth = 2;
            c.weightx   = 0.0;
            gbLabelFsn  = new JLabel("fsn en-GB");
            gbLabelFsn.setVisible(false);
            wizardPanel.add(gbLabelFsn, c);
            c.gridy++;
            c.gridx     = 0;
            c.weightx   = 0.0;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.gridy++;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.weightx   = 1.0;
            c.gridwidth = 1;
            c.weightx   = 0.0;
            gbBoxPref   = new JCheckBox();
            gbBoxPref.addItemListener(new GbDialectPrefItemListener());
            gbBoxPref.setVisible(false);
            wizardPanel.add(gbBoxPref, c);
            c.gridx++;
            c.gridwidth = 3;
            c.weightx   = 1.0;
            gbPref      = new FixedWidthJEditorPane();
            gbPref.setFixedWidth(300);
            gbPref.setEditable(true);
            gbPref.setVisible(false);
            wizardPanel.add(gbPref, c);
            c.gridx     += c.gridwidth;
            c.gridwidth = 2;
            c.weightx   = 0.0;
            gbLabelPref = new JLabel("pref en-GB");
            gbLabelPref.setVisible(false);
            wizardPanel.add(gbLabelPref, c);
            c.gridy++;
            c.gridx     = 0;
            c.weightx   = 0.0;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.gridy++;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.weightx   = 1.0;
            c.gridwidth = 1;
            c.weightx   = 0.0;
            usBoxFsn    = new JCheckBox();
            usBoxFsn.addItemListener(new UsDialectFsnItemListener());
            usBoxFsn.setVisible(false);
            wizardPanel.add(usBoxFsn, c);
            c.gridx++;
            c.gridwidth = 3;
            c.weightx   = 1.0;
            usFsn       = new FixedWidthJEditorPane();
            usFsn.setFixedWidth(300);
            usFsn.setEditable(true);
            usFsn.setVisible(false);
            wizardPanel.add(usFsn, c);
            c.gridx     += c.gridwidth;
            c.gridwidth = 2;
            c.weightx   = 0.0;
            usLabelFsn  = new JLabel("fsn en-US");
            usLabelFsn.setVisible(false);
            wizardPanel.add(usLabelFsn, c);
            c.gridy++;
            c.gridx     = 0;
            c.weightx   = 0.0;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.gridy++;
            c.gridwidth = 10;
            wizardPanel.add(new JSeparator(), c);
            c.gridy++;
            c.weightx   = 1.0;
            c.gridwidth = 1;
            c.weightx   = 0.0;
            usBoxPref   = new JCheckBox();
            usBoxPref.addItemListener(new UsDialectPrefItemListener());
            usBoxPref.setVisible(false);
            wizardPanel.add(usBoxPref, c);
            c.gridx++;
            c.gridwidth = 3;
            c.weightx   = 1.0;
            usPref      = new FixedWidthJEditorPane();
            usPref.setFixedWidth(300);
            usPref.setEditable(true);
            usPref.setVisible(false);
            wizardPanel.add(usPref, c);
            c.gridx     += c.gridwidth;
            c.gridwidth = 2;
            c.weightx   = 0.0;
            usLabelPref = new JLabel("pref en-US");
            usLabelPref.setVisible(false);
            wizardPanel.add(usLabelPref, c);

            // empty thing
            c.gridx = 0;
            c.gridy++;
            c.weightx = 0;
            c.weighty = 1;
            wizardPanel.add(new JPanel(), c);
            fsn.requestFocusInWindow();
            wizardPanel.repaint();
            GuiUtil.tickle(wizardPanel);
         } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(NewConcept.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class GbDialectFsnItemListener implements ItemListener {

      /**
       * Method description
       *
       *
       * @param e
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
         AbstractButton abstractButton = (AbstractButton) e.getSource();
         boolean        selected       = abstractButton.getModel().isSelected();

         if (selected) {
            addGbDescFsn = true;
         } else {
            addGbDescFsn = false;
         }
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class GbDialectPrefItemListener implements ItemListener {

      /**
       * Method description
       *
       *
       * @param e
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
         AbstractButton abstractButton = (AbstractButton) e.getSource();
         boolean        selected       = abstractButton.getModel().isSelected();

         if (selected) {
            addGbDescPref = true;
         } else {
            addGbDescPref = false;
         }
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   private class MakeNewConcept extends SwingWorker<Object, Object> {

      /** Field description */
      CountDownLatch latch = new CountDownLatch(1);

      /**
       * Method description
       *
       *
       * @return
       *
       * @throws Exception
       */
      @Override
      protected Object doInBackground() throws Exception {

         // check return condition for CONTINUE or ITEM_CANCELLED
         if (returnCondition == Condition.CONTINUE) {
            createBlueprintConcept();
            gbRefexConcept    = Ts.get().getConcept(SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
            gbUuid            = gbRefexConcept.getPrimUuid();
            usRefexConcept    = Ts.get().getConcept(SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());
            usUuid            = usRefexConcept.getPrimUuid();
            acceptableConcept = Ts.get().getConcept(SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
            preferredConcept  = Ts.get().getConcept(SnomedMetadataRfx.getDESC_PREFERRED_NID());
            fsnConcept        = Ts.get().getConcept(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());
            synConcept        = Ts.get().getConcept(SnomedMetadataRfx.getDES_SYNONYM_NID());

            ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(),
                                     newConcept.getConceptNid());
            ComponentVersionBI fsn  = cv.getDescriptionFullySpecified();
            ComponentVersionBI pref = cv.getDescriptionPreferred();

            // create blueprints
            if (addUsDescFsn) {

//             createBlueprintUsFsnDesc();
               createBlueprintUsFsnRefex(fsn);
            }

            if (addGbDescFsn) {

//             createBlueprintGbFsnDesc();
//             createBlueprintGbFsnRefex(descSpecGbFsn.getComponentNid());
               createBlueprintGbFsnRefex(fsn);    // only using one fsn (US)
            }

            if (addUsDescPref) {
               ComponentVersionBI usPrefCv = createBlueprintUsPrefDesc();

               createBlueprintUsPrefRefex(usPrefCv);

//             createBlueprintGbAcctRefex(descSpecUsPref.getComponentNid()); //removed for rf2
            }

            if (addGbDescPref) {
               ComponentVersionBI gbPrefCv = createBlueprintGbPrefDesc();

               createBlueprintGbPrefRefex(gbPrefCv);

//             createBlueprintUsAcctRefex(descSpecGbPref.getComponentNid()); //removed for rf2
            }

            if (lang.equals(LANG_CODE.EN)) {
               createBlueprintUsFsnRefex(fsn);
               createBlueprintGbFsnRefex(fsn);
               createBlueprintUsPrefRefex(pref);
               createBlueprintGbPrefRefex(pref);
            }

            if (lang.equals(LANG_CODE.EN_US)) {
               createBlueprintUsFsnRefex(fsn);
               createBlueprintUsPrefRefex(pref);

//             createBlueprintGbAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
            }

            if (lang.equals(LANG_CODE.EN_GB)) {

//             createBlueprintGbFsnRefex(conceptSpec.getFsnCAB().getComponentNid());
               createBlueprintGbFsnRefex(fsn);    // only using one fsn
               createBlueprintGbPrefRefex(pref);

//             createBlueprintUsAcctRefex(conceptSpec.getPreferredCAB().getComponentNid()); //removed for rf2
            }
         }

         return null;
      }

      /**
       * Method description
       *
       */
      @Override
      protected void done() {
         try {
            get();

            I_AmTermComponent newTerm = (I_AmTermComponent) newConcept;

            wizard.setWizardPanelVisible(false);
            host.setTermComponent(newTerm);
            Ts.get().addUncommitted(newConcept);

            // wizard.setWizardPanelVisible(false);
         } catch (Exception ex) {
            ex.printStackTrace();
            returnCondition = Condition.ITEM_CANCELED;
            host.setTermComponent(null);
         } finally {
            latch.countDown();
         }
      }

      /**
       * Method description
       *
       *
       * @return
       */
      public CountDownLatch getLatch() {
         return latch;
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class UsDialectFsnItemListener implements ItemListener {

      /**
       * Method description
       *
       *
       * @param e
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
         AbstractButton abstractButton = (AbstractButton) e.getSource();
         boolean        selected       = abstractButton.getModel().isSelected();

         if (selected) {
            addUsDescFsn = true;
         } else {
            addUsDescFsn = false;
         }
      }
   }


   /**
    * Class description
    *
    *
    * @version        Enter version here..., 13/04/19
    * @author         Enter your name here...    
    */
   public class UsDialectPrefItemListener implements ItemListener {

      /**
       * Method description
       *
       *
       * @param e
       */
      @Override
      public void itemStateChanged(ItemEvent e) {
         AbstractButton abstractButton = (AbstractButton) e.getSource();
         boolean        selected       = abstractButton.getModel().isSelected();

         if (selected) {
            addUsDescPref = true;
         } else {
            addUsDescPref = false;
         }
      }
   }
}
