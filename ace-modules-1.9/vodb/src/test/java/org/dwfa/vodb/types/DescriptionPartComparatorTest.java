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
package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_DescriptionPart;
import org.easymock.EasyMock;
import org.easymock.internal.MocksControl;
import org.junit.Before;
import org.junit.Test;

import java.util.Comparator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public final class DescriptionPartComparatorTest {

    private Comparator<I_DescriptionPart> descPartComparator;
    private MocksControl mocksControl;
    private I_DescriptionPart mockDescriptionPart1;
    private I_DescriptionPart mockDescriptionPart2;

    @Before public void setup() {
        mocksControl = new MocksControl(MocksControl.MockType.DEFAULT);
        descPartComparator = new DescriptionPartComparator();
        mockDescriptionPart1 = mocksControl.createMock("part1", I_DescriptionPart.class);
        mockDescriptionPart2 = mocksControl.createMock("part2", I_DescriptionPart.class);
    }

    @Test public void shouldSortSmallerPartsBeforeBiggerParts() {
        EasyMock.expect(mockDescriptionPart1.getVersion()).andReturn(10);
        EasyMock.expect(mockDescriptionPart2.getVersion()).andReturn(20);
        mocksControl.replay();

        int result = descPartComparator.compare(mockDescriptionPart1, mockDescriptionPart2);

        assertThat(result, equalTo(-1));

        mocksControl.verify();
    }

    @Test public void shouldSortSmallerBiggerPartsBeforeSmallerParts() {
        EasyMock.expect(mockDescriptionPart1.getVersion()).andReturn(100);
        EasyMock.expect(mockDescriptionPart2.getVersion()).andReturn(50);
        mocksControl.replay();

        int result = descPartComparator.compare(mockDescriptionPart1, mockDescriptionPart2);

        assertThat(result, equalTo(1));

        mocksControl.verify();
    }

    @Test public void shouldSortEqualParts() {
        EasyMock.expect(mockDescriptionPart1.getVersion()).andReturn(35);
        EasyMock.expect(mockDescriptionPart2.getVersion()).andReturn(35);
        mocksControl.replay();

        int result = descPartComparator.compare(mockDescriptionPart1, mockDescriptionPart2);

        assertThat(result, equalTo(0));

        mocksControl.verify();
    }
}
