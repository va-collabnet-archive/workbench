package org.ihtsdo.arena.drools;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.conceptview.ConceptTemplates;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.drools.facts.FactFactory;
import org.ihtsdo.tk.drools.facts.View;
import org.ihtsdo.tk.spec.SpecBI;
import org.intsdo.tk.drools.manager.DroolsExecutionManager;

public class EditPanelKb {

    private I_ConfigAceFrame config;
    private CountDownLatch kbLatch = new CountDownLatch(1);
    private String kbKey = EditPanelKb.class.getCanonicalName();
    private Set<File> kbFiles = new HashSet<File>();

    public EditPanelKb(I_ConfigAceFrame config) {
        super();
        this.config = config;
        kbFiles.add(new File("drools-rules/TkApiRules.drl"));
        if(new File("drools-rules/extras/TkApiRulesXtra.drl").exists()){
            kbFiles.add(new File("drools-rules/extras/TkApiRulesXtra.drl"));
        }
        ACE.threadPool.execute(new KbSetkupRunner());
    }

    private class KbSetkupRunner implements Runnable {

        @Override
        public void run() {
            try {
                DroolsExecutionManager.setup(kbKey, kbFiles);
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            kbLatch.countDown();
        }
    }

    public Map<SpecBI, Integer> setConcept(I_GetConceptData c, ConceptViewSettings settings) {
        Map<SpecBI, Integer> templates = new TreeMap<SpecBI, Integer>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        if (c == null) {
            return templates;
        }
        
        try {
            kbLatch.await();
            Map<String, Object> globals = new HashMap<String, Object>();
            globals.put("templates", templates);
            Collection<Object> facts = new ArrayList<Object>();
            ViewCoordinate coordinate = config.getViewCoordinate();
            
            View viewType = null;

            if (settings.getRelAssertionType() == RelAssertionType.STATED) {
                viewType = View.STATED;
            } else if (settings.getRelAssertionType() == RelAssertionType.INFERRED) {
                viewType = View.INFERRED;
            } else if (settings.getRelAssertionType() == RelAssertionType.INFERRED_THEN_STATED) {
                viewType = View.STATED_AND_INFERRED;
            } else if (settings.getRelAssertionType() == RelAssertionType.LONG_NORMAL_FORM) {
                viewType = View.LONG_NORMAL_FORM;
            } else if (settings.getRelAssertionType() == RelAssertionType.SHORT_NORMAL_FORM) {
                viewType = View.SHORT_NORMAL_FORM;
            }
            facts.add(FactFactory.get(viewType));
            facts.add(Ts.get().getConceptVersion(coordinate, c.getNid()));
            boolean executed = DroolsExecutionManager.fireAllRules(kbKey, kbFiles,
                    globals, facts, false);
            
            Map<Integer, Boolean> oldTemplates = ConceptTemplates.templates;
            if(templates.isEmpty()){
                ConceptTemplates.templates.put(c.getConceptNid(), false);
            }else{
                ConceptTemplates.templates.put(c.getConceptNid(), true);
            }
            Map<Integer, Boolean> newTemplates = ConceptTemplates.templates;
            
            if(oldTemplates.containsKey(c.getConceptNid())){
                if(oldTemplates.get(c.getConceptNid()) !=
                        ConceptTemplates.templates.get(c.getConceptNid())){
                    Ts.get().touchComponentTemplate(c.getConceptNid());
                }
            }else{
                Ts.get().touchComponentTemplate(c.getConceptNid());
            }
            
            
            
            if (!executed) {
                AceLog.getAppLog().warning("### " + kbKey
                        + " not executed secondary to prior failure.");
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

//        try {
//            I_GetConceptData context = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CONTEXT.getUids());
//            ResultsCollectorWorkBench results = RulesLibrary.checkConcept(c, context, false, config);
//            Map<SpecBI, Integer> guvnorTemplates = results.getWbTemplates();
//            templates.putAll(guvnorTemplates);
//        } catch (Exception e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }

        return templates;
    }
    
        public Map<SpecBI, Integer> setConcept(I_GetConceptData c) {
        Map<SpecBI, Integer> templates = new TreeMap<SpecBI, Integer>(new Comparator<Object>() {

            @Override
            public int compare(Object o1, Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        if (c == null) {
            return templates;
        }
        
        try {
            kbLatch.await();
            Map<String, Object> globals = new HashMap<String, Object>();
            globals.put("templates", templates);
            Collection<Object> facts = new ArrayList<Object>();
            ViewCoordinate coordinate = config.getViewCoordinate();
            
            View viewType = View.STATED_AND_INFERRED;
            facts.add(FactFactory.get(viewType));
            facts.add(Ts.get().getConceptVersion(coordinate, c.getNid()));
            boolean executed = DroolsExecutionManager.fireAllRules(kbKey, kbFiles,
                    globals, facts, false);
            if(templates.isEmpty()){
                ConceptTemplates.templates.put(c.getConceptNid(), false);
                Ts.get().touchComponentTemplate(c.getConceptNid());
            }else{
                ConceptTemplates.templates.put(c.getConceptNid(), true);
                Ts.get().touchComponentTemplate(c.getConceptNid());
            }
            if (!executed) {
                AceLog.getAppLog().warning("### " + kbKey
                        + " not executed secondary to prior failure.");
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

//        try {
//            I_GetConceptData context = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CONTEXT.getUids());
//            ResultsCollectorWorkBench results = RulesLibrary.checkConcept(c, context, false, config);
//            Map<SpecBI, Integer> guvnorTemplates = results.getWbTemplates();
//            templates.putAll(guvnorTemplates);
//        } catch (Exception e) {
//            AceLog.getAppLog().alertAndLogException(e);
//        }

        return templates;
    }
}
