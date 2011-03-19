package org.ihtsdo.arena.drools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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
            facts.add(Ts.get().getConceptVersion(coordinate, c.getNid()));
            boolean executed = DroolsExecutionManager.fireAllRules(kbKey, kbFiles,
                    globals, facts, false);
            if (!executed) {
                AceLog.getAppLog().warning("### " + kbKey + 
                        " not executed secondary to prior failure.");
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        try {
            I_GetConceptData context = Terms.get().getConcept(RefsetAuxiliary.Concept.TEMPLATE_CONTEXT.getUids());
            ResultsCollectorWorkBench results = RulesLibrary.checkConcept(c, context, false, config);
            Map<SpecBI, Integer> guvnorTemplates = results.getWbTemplates();
            templates.putAll(guvnorTemplates);
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }

        return templates;
    }
}
