/*
 *  Copyright 2010 matt.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.dwfa.ace.task.util;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author matt
 */
public class ListUtilTest extends TestCase {

    public ListUtilTest(String testName) {
        super(testName);
    }

    /**
     * Test of concat method, of class ListUtil.
     */
    public void testConcat() {
        final List<String> listOfValues = new ArrayList<String>();
        listOfValues.add("5adbb0d5-dbc7-5a0c-ab48-d69888889ae1");
        listOfValues.add("7e807d2a-56c5-5b86-87da-0c0ee8a5d56f");
        listOfValues.add("blah");
        listOfValues.add("c6f8d026-c7a1-54e1-b85a-7907a2906516");
        listOfValues.add("c6f8d026-c7a1-54e1-b85a-7907a2906511");
        String seperator = ",";
        String expResult =
                "5adbb0d5-dbc7-5a0c-ab48-d69888889ae1,7e807d2a-56c5-5b86-87da-0c0ee8a5d56f,blah,c6f8d026-c7a1-54e1-b85a-7907a2906516,c6f8d026-c7a1-54e1-b85a-7907a2906511";
        String result = ListUtil.concat(listOfValues, seperator);
        assertEquals(expResult, result);
    }
}
