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

import java.util.Arrays;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public final class DescriptionPartSorterTest {

    private MocksControl mocksControl;
    private I_DescriptionPart mockDescriptionPart1;
    private I_DescriptionPart mockDescriptionPart2;
    private I_DescriptionPart mockDescriptionPart3;
    private I_DescriptionPart mockDescriptionPart4;
    private DescriptionPartSorter sorter;

    @Before public void setup() {
        sorter = new DescriptionPartSorter(new DescriptionPartComparator());
        mocksControl = new MocksControl(MocksControl.MockType.DEFAULT);
        mockDescriptionPart1 = mocksControl.createMock("part1", I_DescriptionPart.class);
        mockDescriptionPart2 = mocksControl.createMock("part2", I_DescriptionPart.class);
        mockDescriptionPart3 = mocksControl.createMock("part3", I_DescriptionPart.class);
        mockDescriptionPart4 = mocksControl.createMock("part4", I_DescriptionPart.class);
    }

    @Test public void shouldSortAscendingWhenUsed() {
        EasyMock.expect(mockDescriptionPart1.getVersion()).andReturn(35).atLeastOnce();
        EasyMock.expect(mockDescriptionPart2.getVersion()).andReturn(50).atLeastOnce();
        EasyMock.expect(mockDescriptionPart3.getVersion()).andReturn(100).atLeastOnce();
        EasyMock.expect(mockDescriptionPart4.getVersion()).andReturn(45).atLeastOnce();
        mocksControl.replay();

        I_DescriptionPart latestPart = sorter.sort(Arrays.asList(mockDescriptionPart1, mockDescriptionPart2,
                                                                 mockDescriptionPart3, mockDescriptionPart4));
        assertThat(latestPart, equalTo(mockDescriptionPart3));

        mocksControl.verify();
    }
}

