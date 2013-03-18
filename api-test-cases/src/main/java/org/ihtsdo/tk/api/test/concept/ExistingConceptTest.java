package org.ihtsdo.tk.api.test.concept;

import java.io.IOException;
import java.util.UUID;

import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = {DefaultProfileBuilder.class})
public class ExistingConceptTest {

    @Test
    public void testDbAvailable() {
        Terms.get();
    }
    
    @Test
    public void testTopTaxonomy() throws IOException, TerminologyException {
        ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
        ConceptSpec testSpec = new ConceptSpec(
            "Substance (substance)", UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));
        testSpec.getStrict(vc);
    }
    
}
