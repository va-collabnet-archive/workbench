package org.ihtsdo.tk.api.test.concept;

import org.ihtsdo.tk.api.test.DefaultProfileBuilder;
import org.intsdo.junit.bdb.BdbTestRunner;
import org.intsdo.junit.bdb.BdbTestRunnerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BdbTestRunner.class)
@BdbTestRunnerConfig(init = {DefaultProfileBuilder.class})
public class RetireConceptTest {

    @Test
    public void testSomething() {
        
    }
    
}
