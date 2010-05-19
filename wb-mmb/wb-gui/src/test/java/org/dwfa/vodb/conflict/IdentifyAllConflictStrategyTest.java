/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.vodb.conflict;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.dwfa.ace.api.I_AmPart;
import org.junit.Ignore;
import org.junit.Test;

public class IdentifyAllConflictStrategyTest {

    class TestDefaultAceConflictResolutionStrategy extends IdentifyAllConflictStrategy {

        private static final long serialVersionUID = 1L;

        public <T extends I_AmPart> boolean doesConflictExist(List<T> tuples) {
            return super.doesConflictExist(tuples);
        }
    }

    TestDefaultAceConflictResolutionStrategy conflictResolutionStrategy = new TestDefaultAceConflictResolutionStrategy();

    @Test
    @Ignore
    public void testDoesConflictExist() throws Exception {
        List<MockTuple> tuples = new ArrayList<MockTuple>();

        // start with a list of all the same entity
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        tuples.add(new MockTuple(1, 0, 1, "value 1"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        tuples.add(new MockTuple(1, 0, 2, "value 1"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        tuples.add(new MockTuple(1, 0, 3, "value 1"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        tuples.add(new MockTuple(1, 1, 4, "value 1"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        // now add a new version on a different path with a different value
        tuples.add(new MockTuple(1, 1, 5, "new value 1"));
        Assert.assertTrue(conflictResolutionStrategy.doesConflictExist(tuples));

        // now change the value back so the latest state on each path is the
        // same
        tuples.add(new MockTuple(1, 1, 6, "value 1"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        // now try with 2 entities, no conflict for either
        tuples.add(new MockTuple(2, 0, 1, "value 2"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        tuples.add(new MockTuple(2, 0, 2, "value 2"));
        Assert.assertFalse(conflictResolutionStrategy.doesConflictExist(tuples));

        // add conflict for second entity
        tuples.add(new MockTuple(2, 1, 3, "new value 2"));
        Assert.assertTrue(conflictResolutionStrategy.doesConflictExist(tuples));
    }

    @Test
    public void testResolveTuples() throws Exception {
        List<MockTuple> tuples = new ArrayList<MockTuple>();

        tuples.add(new MockTuple(1, 0, 1, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        tuples.add(new MockTuple(1, 0, 2, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        tuples.add(new MockTuple(1, 0, 3, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        tuples.add(new MockTuple(1, 1, 4, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        // now add a new version on a different path with a different value
        tuples.add(new MockTuple(1, 1, 5, "new value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        // now change the value back so the latest state on each path is the
        // same
        tuples.add(new MockTuple(1, 1, 6, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        // now try with 2 entities, no conflict for either
        tuples.add(new MockTuple(2, 0, 1, "value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        tuples.add(new MockTuple(2, 0, 2, "value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));

        // add conflict for second entity
        tuples.add(new MockTuple(2, 1, 3, "new value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveTuples(tuples));
    }

    @Test
    public void testResolveParts() throws Exception {
        List<MockTuple> tuples = new ArrayList<MockTuple>();

        tuples.add(new MockTuple(1, 0, 1, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        tuples.add(new MockTuple(1, 0, 2, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        tuples.add(new MockTuple(1, 0, 3, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        tuples.add(new MockTuple(1, 1, 4, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        // now add a new version on a different path with a different value
        tuples.add(new MockTuple(1, 1, 5, "new value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        // now change the value back so the latest state on each path is the
        // same
        tuples.add(new MockTuple(1, 1, 6, "value 1"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        // now try with 2 entities, no conflict for either
        tuples.add(new MockTuple(2, 0, 1, "value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        tuples.add(new MockTuple(2, 0, 2, "value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));

        // add conflict for second entity
        tuples.add(new MockTuple(2, 1, 3, "new value 2"));
        Assert.assertEquals(tuples, conflictResolutionStrategy.resolveParts(tuples));
    }
}
