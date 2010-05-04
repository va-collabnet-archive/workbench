/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.bpa.tasks.prop;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link PrependStringToProperty} Task
 * @author Matthew Edwards
 */
public class PrependStringToPropertyTest {

    private Mockery context;
    private I_EncodeBusinessProcess process;
    private I_Work worker;

    @Before
    public void setup() {
        context = new Mockery();
        process = context.mock(I_EncodeBusinessProcess.class);
        worker = context.mock(I_Work.class);
    }

    /**
     * Test of not unescaping Java characters in the setValueText method, of class PrependStringToProperty.
     */
    @Test
    public void testNotUnescapingJavaCharacters() throws Exception {
        final String stringPropName = "DIALOG_MSG";
        final String originalPropValue = "This is a testValue";
        final String expectedResult = "PrependText\\nThis is a testValue";
        PrependStringToProperty instance = new PrependStringToProperty();
        instance.setStringPropName(stringPropName);
        instance.setValueText("PrependText\\n");
        context.checking(new Expectations() {

            {
                oneOf(process).getProperty(stringPropName);
                will(returnValue(originalPropValue));
                oneOf(process).setProperty(stringPropName, expectedResult);
            }
        });
        instance.evaluate(process, worker);
        assertEquals(expectedResult, instance.getValueText() + originalPropValue);
    }
}
