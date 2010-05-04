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
package org.dwfa.ace.task;

import java.beans.PropertyDescriptor;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link WriteListToFileBeanInfo}
 * @author Matt Edwards
 */
public class WriteListToFileBeanInfoTest {

    private static final int EXPECTED_NUM_OF_PROP_DESRIPTORS = 5;

    /**
     * Test of getPropertyDescriptors method, of class WriteListToFileBeanInfo.
     */
    @Test
    public void testGetPropertyDescriptors() {
        WriteListToFileBeanInfo instance = new WriteListToFileBeanInfo();
        PropertyDescriptor[] result = instance.getPropertyDescriptors();
        assertEquals(result.length, EXPECTED_NUM_OF_PROP_DESRIPTORS);
    }
}
