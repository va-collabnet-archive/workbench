/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.IdDirective;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;

/**
 *
 * @author kec
 */
public class UpdateTextDocumentListener implements DocumentListener, ActionListener, VetoableChangeListener {

    private static ThreadGroup updateTextThreadGroup =
            new ThreadGroup("updateTextThreadGroup");
    private static ExecutorService updateDocListenrService = Executors.newFixedThreadPool(1,
            new NamedThreadFactory(updateTextThreadGroup, "updateTextThread"));
    FixedWidthJEditorPane editorPane;
    DescriptionAnalogBI desc;
    Timer t;
    I_GetConceptData c;
    boolean update = false;
    I_ConfigAceFrame config;
    Collection<? extends RefexChronicleBI<?>> refexes;
    static ConceptChronicleBI gbConcept = null;
    static ConceptChronicleBI usConcept;
    static int prefNid;
    static int acceptNid;
    static int fsn;
    TerminologyBuilderBI builder;
    String text;

    public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane,
            DescriptionAnalogBI desc) throws TerminologyException, IOException {
        super();
        this.editorPane = editorPane;
        this.desc = desc;
        t = new Timer(5000, this);
        c = Terms.get().getConcept(desc.getConceptNid());
        Ts.get().addVetoablePropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, this);
        //get rf1/rf2 concept
        if (gbConcept == null) {
            gbConcept = Ts.get().getConcept(SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
            acceptNid = SnomedMetadataRfx.getDESC_ACCEPTABLE_NID();
            prefNid = SnomedMetadataRfx.getDESC_PREFERRED_NID();
            fsn = SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID();
            usConcept = Ts.get().getConcept(SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());
        }
    }
    long lastChange = Long.MIN_VALUE;

    @Override
    public void insertUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            desc.setText(text);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            desc.setText(text);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        lastChange++;
        text = editorPane.extractText();
        text = text.replaceAll("[\\s]", " ");
        text = text.replaceAll("   *", " ");
        try {
            desc.setText(text);
        } catch (PropertyVetoException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (t.isRunning()) {
            t.restart();
        } else {
            t.start();
        }
        update = true;
    }
    long lastAction = Long.MIN_VALUE;

    @Override
    public void actionPerformed(ActionEvent e) {
        if (lastAction < lastChange) {
            lastAction = lastChange;

            if (desc.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {
                updateDocListenrService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doAction();
                        } catch (IOException ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (PropertyVetoException ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvalidCAB ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (UnsupportedDialectOrLanguage ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TerminologyException ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ContradictionException ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

            } else {
                updateDocListenrService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            I_GetConceptData concept = Terms.get().getConceptForNid(desc.getNid());
                            Terms.get().addUncommitted(concept);
                        } catch (IOException ex) {
                            Logger.getLogger(UpdateTextDocumentListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        }

    }

    private void doAction() throws IOException, PropertyVetoException, InvalidCAB, UnsupportedDialectOrLanguage, TerminologyException, ContradictionException {
        config = Terms.get().getActiveAceFrameConfig();
        builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());
        ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
        if (update) { //create new
            if (desc.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {

                refexes = desc.getCurrentAnnotationMembers(config.getViewCoordinate());
                int type = desc.getTypeNid();


                if (refexes.isEmpty()) { //check for previous changes
                    if (type == fsn) {
                        doFsnUpdate();
                    } else {
                        doSynUpdate();
                    }

                } else { //TODO modify uncomitted version
                    RefexCnidAnalogBI gbRefex = null;
                    RefexCnidAnalogBI usRefex = null;
                    for (RefexChronicleBI<?> descRefex : refexes) {
                        if (descRefex.isUncommitted()) {
                            if (descRefex.getCollectionNid() == gbConcept.getNid()) {
                                gbRefex = (RefexCnidAnalogBI) descRefex;
                            } else if (descRefex.getCollectionNid() == usConcept.getNid()) {
                                usRefex = (RefexCnidAnalogBI) descRefex;
                            }
                        }
                    }
                    if (type == fsn) {
                        if (cv.getFsnDescsActive().size() > 1) {
                            doFsnUpdate(gbRefex, usRefex);
                        }
                    } else {
                        if (cv.getPrefDescsActive().size() > 1) {
                            doSynUpdate(gbRefex, usRefex);
                        }
                    }
                }
            }
            I_GetConceptData concept = Terms.get().getConceptForNid(desc.getNid());
            Terms.get().addUncommitted(concept);
        }
        update = false;
    }

    private void doFsnUpdate() throws PropertyVetoException, IOException, InvalidCAB, UnsupportedDialectOrLanguage,
            ContradictionException {

        desc.setText(text);
        RefexCAB refexSpecUs = new RefexCAB(
                TK_REFSET_TYPE.CID,
                desc.getNid(),
                usConcept.getNid(), IdDirective.GENERATE_HASH);
        refexSpecUs.put(RefexProperty.CNID1, prefNid);
        RefexChronicleBI<?> newRefexUs = builder.construct(refexSpecUs);

        RefexCAB refexSpecGb = new RefexCAB(
                TK_REFSET_TYPE.CID,
                desc.getNid(),
                gbConcept.getNid(), IdDirective.GENERATE_HASH);
        refexSpecGb.put(RefexProperty.CNID1, prefNid);
        RefexChronicleBI<?> newRefexGb = builder.construct(refexSpecGb);

        I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
        Ts.get().addUncommitted(refexGb);
        I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
        Ts.get().addUncommitted(refexUs);
    }

    private void doSynUpdate() throws PropertyVetoException, IOException, InvalidCAB, UnsupportedDialectOrLanguage, ContradictionException {
        desc.setText(text);

        if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //acceptable in both 
            RefexCAB refexSpecUs = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid(), IdDirective.GENERATE_HASH);
            refexSpecUs.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexUs = builder.construct(refexSpecUs);

            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid(), IdDirective.GENERATE_HASH);
            refexSpecGb.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexGb = builder.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
            Ts.get().addUncommitted(refexGb);
            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //acceptable in US
            RefexCAB refexSpecUs = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid(), IdDirective.GENERATE_HASH);
            refexSpecUs.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexUs = builder.construct(refexSpecUs);

            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())) { //acceptable in GB
            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFSET_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid(), IdDirective.GENERATE_HASH);
            refexSpecGb.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexGb = builder.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
            Ts.get().addUncommitted(refexGb);

        }
    }

    private void doFsnUpdate(RefexCnidAnalogBI gbRefex, RefexCnidAnalogBI usRefex)
            throws PropertyVetoException, IOException, InvalidCAB, UnsupportedDialectOrLanguage {
        TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());

        desc.setText(text);

        usRefex.setCnid1(prefNid);
        gbRefex.setCnid1(prefNid);
    }

    private void doSynUpdate(RefexCnidAnalogBI gbRefex, RefexCnidAnalogBI usRefex) throws PropertyVetoException,
            IOException, InvalidCAB, UnsupportedDialectOrLanguage, ContradictionException {
        TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());

        desc.setText(text);

        if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) {//acceptable in both
            if (usRefex == null) {
                //forget GB
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == gbConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
                doSynUpdate();
            } else {
                usRefex.setCnid1(acceptNid);
            }
            if (gbRefex == null) {
                //forget US
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == usConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
                doSynUpdate();
            } else {
                gbRefex.setCnid1(acceptNid);
            }

        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //acceptable in US
            if (usRefex == null) {
                doSynUpdate();
            } else {
                usRefex.setCnid1(acceptNid);
            }
            //forget GB
            List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
            for (I_ExtendByRef ext : extensions) {
                if (ext.getRefsetId() == gbConcept.getNid()) {
                    Terms.get().forget(ext);
                }
            }
        } else if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())) { //acceptable in GB
            if (gbRefex == null) {
                doSynUpdate();
            } else {
                gbRefex.setCnid1(acceptNid);
            }
            //forget US
            List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
            for (I_ExtendByRef ext : extensions) {
                if (ext.getRefsetId() == usConcept.getNid()) {
                    Terms.get().forget(ext);
                }
            }
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent pce) throws PropertyVetoException {
        try {
            if (desc.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {
                doAction();
            } else {
                I_GetConceptData concept = Terms.get().getConceptForNid(desc.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (IOException ex) {
            throw new PropertyVetoException(ex.toString(), pce);
        } catch (TerminologyException ex) {
            throw new PropertyVetoException(ex.toString(), pce);
        } catch (InvalidCAB ex) {
            throw new PropertyVetoException(ex.toString(), pce);
        } catch (UnsupportedDialectOrLanguage ex) {
            throw new PropertyVetoException(ex.toString(), pce);
        } catch (ContradictionException ex) {
            throw new PropertyVetoException(ex.toString(), pce);
        }
    }
}