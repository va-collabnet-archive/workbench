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
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.helper.cswords.CsWordsHelper;
import org.ihtsdo.helper.dialect.DialectHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidAnalogBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.CaseSensitive;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
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
    ConceptChronicleBI gbConcept;
    ConceptChronicleBI usConcept;
    int prefNid;
    int acceptNid;
    TerminologyBuilderBI tc;
    String text;

    public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane,
            DescriptionAnalogBI desc) throws TerminologyException, IOException {
        super();
        this.editorPane = editorPane;
        this.desc = desc;
        t = new Timer(5000, this);
        c = Terms.get().getConcept(desc.getConceptNid());
        Ts.get().addVetoablePropertyChangeListener(TerminologyStoreDI.CONCEPT_EVENT.PRE_COMMIT, this);
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
        tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());
        if (update) { //create new
            if (desc.getLang().equals(LANG_CODE.EN.getFormatedLanguageCode())) {

                refexes = desc.getAnnotationsActive(config.getViewCoordinate());
                int type = desc.getTypeNid();
                int fsn = SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID();
                int definition = SnomedMetadataRf2.DEFINITION_RF2.getLenient().getConceptNid();

                //get rf1/rf2 concept
                gbConcept = Ts.get().getConcept(SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
                usConcept = Ts.get().getConcept(SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());
                acceptNid = SnomedMetadataRfx.getDESC_ACCEPTABLE_NID();
                prefNid = SnomedMetadataRfx.getDESC_PREFERRED_NID();

                if (refexes.isEmpty()) { //check for previous changes
                    if (type == fsn) {
                        doFsnUpdate();
                    }else if(type == definition){
                        doDefUpdate();
                    }else if(type == SnomedMetadataRfx.getDES_SYNONYM_NID()) {
                        doSynUpdate();
                    }

                } else { //TODO modify uncomitted version
                    RefexNidAnalogBI gbRefex = null;
                    RefexNidAnalogBI usRefex = null;
                    for (RefexChronicleBI<?> descRefex : refexes) {
                        if (descRefex.isUncommitted()) {
                            if (descRefex.getRefexNid() == gbConcept.getNid()) {
                                gbRefex = (RefexNidAnalogBI) descRefex;;
                            } else if (descRefex.getRefexNid() == usConcept.getNid()) {
                                usRefex = (RefexNidAnalogBI) descRefex;
                            }
                        }
                    }
                    if (type == fsn) {
                        doFsnUpdate(gbRefex, usRefex);
                    }else if(type == definition){
                        doDefUpdate(gbRefex, usRefex);
                    }else if(type == SnomedMetadataRfx.getDES_SYNONYM_NID()) {
                        doSynUpdate(gbRefex, usRefex);
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
                TK_REFEX_TYPE.CID,
                desc.getNid(),
                usConcept.getNid());
        refexSpecUs.put(RefexProperty.CNID1, prefNid);
        RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

        RefexCAB refexSpecGb = new RefexCAB(
                TK_REFEX_TYPE.CID,
                desc.getNid(),
                gbConcept.getNid());
        refexSpecGb.put(RefexProperty.CNID1, prefNid);
        RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

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
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid());
            refexSpecUs.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid());
            refexSpecGb.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
            Ts.get().addUncommitted(refexGb);
            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //acceptable in US
            RefexCAB refexSpecUs = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid());
            refexSpecUs.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())) { //acceptable in GB
            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid());
            refexSpecGb.put(RefexProperty.CNID1, acceptNid);
            RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
            Ts.get().addUncommitted(refexGb);

        }
    }
    
    private void doDefUpdate() throws PropertyVetoException, IOException, InvalidCAB, UnsupportedDialectOrLanguage, ContradictionException {
        desc.setText(text);

        if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //preferred in both 
            RefexCAB refexSpecUs = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid());
            refexSpecUs.put(RefexProperty.CNID1, prefNid);
            RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid());
            refexSpecGb.put(RefexProperty.CNID1, prefNid);
            RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
            Ts.get().addUncommitted(refexGb);
            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //preferred in US
            RefexCAB refexSpecUs = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    usConcept.getNid());
            refexSpecUs.put(RefexProperty.CNID1, prefNid);
            RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);

            I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
            Ts.get().addUncommitted(refexUs);
        } else if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())) { //preferred in GB
            RefexCAB refexSpecGb = new RefexCAB(
                    TK_REFEX_TYPE.CID,
                    desc.getNid(),
                    gbConcept.getNid());
            refexSpecGb.put(RefexProperty.CNID1, prefNid);
            RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);

            I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
            Ts.get().addUncommitted(refexGb);

        }
    }

    private void doFsnUpdate(RefexNidAnalogBI gbRefex, RefexNidAnalogBI usRefex)
            throws PropertyVetoException, IOException, InvalidCAB, UnsupportedDialectOrLanguage {
        TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());

        desc.setText(text);

        usRefex.setNid1(prefNid);
        gbRefex.setNid1(prefNid);
    }

    private void doSynUpdate(RefexNidAnalogBI gbRefex, RefexNidAnalogBI usRefex) throws PropertyVetoException,
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
                usRefex.setNid1(acceptNid);
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
                gbRefex.setNid1(acceptNid);
            }

        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //acceptable in US
            if (usRefex == null) {
                doSynUpdate();
            } else {
                usRefex.setNid1(acceptNid);
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
                gbRefex.setNid1(acceptNid);
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
    
    private void doDefUpdate(RefexNidAnalogBI gbRefex, RefexNidAnalogBI usRefex) throws PropertyVetoException,
            IOException, InvalidCAB, UnsupportedDialectOrLanguage, ContradictionException {
        TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                config.getViewCoordinate());

        desc.setText(text);

        if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())
                && DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) {//preferred in both
            if (usRefex == null) {
                //forget GB
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == gbConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
                doDefUpdate();
            } else {
                usRefex.setNid1(prefNid);
            }
            if (gbRefex == null) {
                //forget US
                List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
                for (I_ExtendByRef ext : extensions) {
                    if (ext.getRefsetId() == usConcept.getNid()) {
                        Terms.get().forget(ext);
                    }
                }
                doDefUpdate();
            } else {
                gbRefex.setNid1(prefNid);
            }

        } else if (DialectHelper.isTextForDialect(text, Language.EN_UK.getLenient().getNid())) { //preferred in US
            if (usRefex == null) {
                doDefUpdate();
            } else {
                usRefex.setNid1(prefNid);
            }
            //forget GB
            List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
            for (I_ExtendByRef ext : extensions) {
                if (ext.getRefsetId() == gbConcept.getNid()) {
                    Terms.get().forget(ext);
                }
            }
        } else if (DialectHelper.isTextForDialect(text, Language.EN_US.getLenient().getNid())) { //preferred in GB
            if (gbRefex == null) {
                doDefUpdate();
            } else {
                gbRefex.setNid1(prefNid);
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