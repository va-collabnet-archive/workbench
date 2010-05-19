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
package org.dwfa.ace.task.refset.members;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.dwfa.bpa.process.I_DefineTask;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

public final class WriteSingleRefsetDescriptionSerializationTest {

    private static final String WORKING_REFSET_KEY = "working refset key";
    private static final String DIRECTORY_KEY = "somedirectory";

    private IMocksControl mockControl;
    private CleanableProcessExtByRefBuilder mockCleanableProcessBuilder;
    private PropertyValidator mockPropertyValidator;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;

    @Before
    public void setup() {
        mockControl = EasyMock.createStrictControl();
        mockCleanableProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);
        mockPropertyValidator = mockControl.createMock(PropertyValidator.class);
    }

    @Test
    public void shouldSerializeData() throws Exception {
        ObjectOutputStream mockOutputStream = mockControl.createMock(ObjectOutputStream.class);
        mockOutputStream.writeInt(1);
        mockOutputStream.writeUTF(DIRECTORY_KEY);
        mockOutputStream.writeUTF(WORKING_REFSET_KEY);
        mockControl.replay();

        I_DefineTask task = createTask();
        // As the Java API insists on calling a private handler method instead
        // of using an API,
        // we can assume that this method name will not change.
        Method method = WriteSingleRefsetDescription.class.getDeclaredMethod("writeObject",
            new Class[] { ObjectOutputStream.class });
        method.setAccessible(true);
        method.invoke(task, mockOutputStream);

        mockControl.verify();
    }

    @Test
    public void shouldDeserializeData() throws Exception {
        ObjectInputStream mockInputStream = mockControl.createMock(ObjectInputStream.class);
        EasyMock.expect(mockInputStream.readInt()).andReturn(1);
        EasyMock.expect(mockInputStream.readUTF()).andReturn(DIRECTORY_KEY);
        EasyMock.expect(mockInputStream.readUTF()).andReturn(WORKING_REFSET_KEY);
        mockControl.replay();

        I_DefineTask task = createDeserializedTask();
        Method method = WriteSingleRefsetDescription.class.getDeclaredMethod("readObject",
            new Class[] { ObjectInputStream.class });
        method.setAccessible(true);
        method.invoke(task, mockInputStream);

        assertThat(getFieldValue(task, "directoryKey").toString(), equalTo(DIRECTORY_KEY));
        assertThat(getFieldValue(task, "selectedRefsetKey").toString(), equalTo(WORKING_REFSET_KEY));
        assertThat(getFieldValue(task, "cleanableProcessBuilder"), notNullValue());
        assertThat(getFieldValue(task, "propertyValidator"), notNullValue());
        assertThat(getFieldValue(task, "terminologyWrapper"), notNullValue());

        mockControl.verify();
    }

    @Test
    public void shouldThrowAnExceptionIfTheObjectCantBeDeserialized() throws Exception {
        ObjectInputStream mockInputStream = mockControl.createMock(ObjectInputStream.class);
        EasyMock.expect(mockInputStream.readInt()).andReturn(2);
        mockControl.replay();

        I_DefineTask task = createTask();
        Method method = WriteSingleRefsetDescription.class.getDeclaredMethod("readObject",
            new Class[] { ObjectInputStream.class });
        method.setAccessible(true);

        try {
            method.invoke(task, mockInputStream);
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IOException, got " + e.getCause(), e.getCause().getClass() == IOException.class);
            mockControl.verify();
        }
    }

    private Object getFieldValue(final I_DefineTask task, final String fieldName) throws Exception {
        Field field = WriteSingleRefsetDescription.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(task);
    }

    private WriteSingleRefsetDescription createTask() {
        return new WriteSingleRefsetDescription(WORKING_REFSET_KEY, mockCleanableProcessBuilder, DIRECTORY_KEY,
            mockPropertyValidator, mockTerminologyWrapper);
    }

    private WriteSingleRefsetDescription createDeserializedTask() {
        return new WriteSingleRefsetDescription(WORKING_REFSET_KEY, null, DIRECTORY_KEY, mockPropertyValidator, null);
    }
}
