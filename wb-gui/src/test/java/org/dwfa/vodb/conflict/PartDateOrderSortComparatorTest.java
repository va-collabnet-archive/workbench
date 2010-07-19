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
/**
 * 
 */
package org.dwfa.vodb.conflict;

import junit.framework.Assert;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Dion
 * 
 */
public class PartDateOrderSortComparatorTest {

    /**
     * Implementation of a test I_AmPart - as the test case only
     * works on the version, calls to other methods get an
     * UnsupportedOperationException and will cause the test
     * to fail.
     * 
     * @author Dion
     */
    public class TestPart implements I_AmPart {
        int version;

        public I_AmPart duplicate() {
            throw new UnsupportedOperationException();
        }

        public int getPathId() {
            throw new UnsupportedOperationException();
        }

        public int getStatusId() {
            throw new UnsupportedOperationException();
        }

        public int getVersion() {
            return this.version;
        }

        public void setPathId(int pathId) {
            throw new UnsupportedOperationException();
        }

        public void setStatusId(int statusId) {
            throw new UnsupportedOperationException();
        }

        public void setVersion(int version) {
            this.version = version;

        }

        public ArrayIntList getPartComponentNids() {
            throw new UnsupportedOperationException();
        }

        public int getPositionId() {
            throw new UnsupportedOperationException();
        }

        public void setPositionId(int pid) {
            throw new UnsupportedOperationException();
        }
    	@Override
    	public long getTime() {
    		return ThinVersionHelper.convert(getVersion());
    	}

    	@Override
    	public I_AmPart makeAnalog(int statusNid, int pathNid, long time) {
    		I_AmPart newPart = duplicate();
    		newPart.setStatusId(statusNid);
    		newPart.setPathId(pathNid);
    		newPart.setTime(time);
    		return newPart;
    	}
    	@Override
    	public void setTime(long value) {
    	     throw new UnsupportedOperationException();
    	}

		@Override
		public int getAuthorNid() {
		     throw new UnsupportedOperationException();
		}

		@Override
		public void setAuthorNid(int authorNid) {
		     throw new UnsupportedOperationException();
		}

    }

    private TestPart newestTestPart;
    private TestPart oldestTestPart;
    private TestPart sameAsOldestTestPart;

    private PartDateOrderSortComparator normalComparator;
    private PartDateOrderSortComparator reverseComparator;

    @Before
    public void initialise() {
        newestTestPart = new TestPart();
        newestTestPart.setVersion(100);

        oldestTestPart = new TestPart();
        oldestTestPart.setVersion(10);

        sameAsOldestTestPart = new TestPart();
        sameAsOldestTestPart.setVersion(10);

        normalComparator = new PartDateOrderSortComparator(false);
        reverseComparator = new PartDateOrderSortComparator(true);
    }

    @Test
    public void testNormalCompare() throws Exception {
        Assert.assertTrue(normalComparator.compare(newestTestPart, oldestTestPart) > 0);
        Assert.assertTrue(normalComparator.compare(oldestTestPart, newestTestPart) < 0);
    }

    @Test
    public void testReverseOrderCompare() throws Exception {
        Assert.assertTrue(reverseComparator.compare(newestTestPart, oldestTestPart) < 0);
        Assert.assertTrue(reverseComparator.compare(oldestTestPart, newestTestPart) > 0);
    }

    @Test
    public void testEqualCompare() throws Exception {
        Assert.assertEquals(0, normalComparator.compare(sameAsOldestTestPart, oldestTestPart));
        Assert.assertEquals(0, normalComparator.compare(oldestTestPart, sameAsOldestTestPart));
        Assert.assertEquals(0, normalComparator.compare(sameAsOldestTestPart, sameAsOldestTestPart));

        Assert.assertEquals(0, reverseComparator.compare(sameAsOldestTestPart, oldestTestPart));
        Assert.assertEquals(0, reverseComparator.compare(oldestTestPart, sameAsOldestTestPart));
        Assert.assertEquals(0, reverseComparator.compare(sameAsOldestTestPart, sameAsOldestTestPart));
    }

}
