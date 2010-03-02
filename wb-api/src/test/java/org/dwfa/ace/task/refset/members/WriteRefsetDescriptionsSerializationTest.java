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

import static org.hamcrest.CoreMatchers.equalTo;
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

public final class WriteRefsetDescriptionsSerializationTest {

    private static final String DIRECTORY_KEY = "somedirectory";
    private static final String DESERIALIZED_DIR_KEY = "deserialized dir key";

    private IMocksControl mockControl;
    private LocalVersionedTerminologyWrapper mockTerminologyWrapper;
    private CleanableProcessExtByRefBuilder mockProcessBuilder;

    @Before
    public void setup() {
        mockControl = EasyMock.createStrictControl();
        mockTerminologyWrapper = mockControl.createMock(LocalVersionedTerminologyWrapper.class);
        mockProcessBuilder = mockControl.createMock(CleanableProcessExtByRefBuilder.class);
    }

    @Test
    public void shouldSerializeData() throws Exception {
        ObjectOutputStream mockStream = mockControl.createMock(ObjectOutputStream.class);
        mockStream.writeInt(1);
        mockStream.writeUTF(DIRECTORY_KEY);
        mockControl.replay();

        I_DefineTask task = new WriteRefsetDescriptions(DIRECTORY_KEY, mockTerminologyWrapper, mockProcessBuilder);
        Method method = WriteRefsetDescriptions.class.getDeclaredMethod("writeObject",
            new Class[] { ObjectOutputStream.class });
        method.setAccessible(true);
        method.invoke(task, mockStream);

        mockControl.verify();
    }

    @Test
    public void shouldDeserializeData() throws Exception {
        ObjectInputStream mockObjectInputStream = mockControl.createMock(ObjectInputStream.class);
        EasyMock.expect(mockObjectInputStream.readInt()).andReturn(1);
        EasyMock.expect(mockObjectInputStream.readUTF()).andReturn(DESERIALIZED_DIR_KEY);
        mockControl.replay();

        I_DefineTask task = createEmptyTask();
        getReadObject().invoke(task, mockObjectInputStream);
        assertThat(getFieldValue(task, "directoryKey").toString(), equalTo(DESERIALIZED_DIR_KEY));
        assertThat(getFieldValue(task, "cleanableProcessExtByRefBuilder"), notNullValue());
        assertThat(getFieldValue(task, "terminologyWrapper"), notNullValue());

        mockControl.verify();
    }

    @Test
    public void shouldThrowAnExceptionIfTheObjectCantBeDeserialized() throws Exception {
        ObjectInputStream mockInputStream = mockControl.createMock(ObjectInputStream.class);
        EasyMock.expect(mockInputStream.readInt()).andReturn(2);
        mockControl.replay();

        I_DefineTask task = createEmptyTask();

        try {
            getReadObject().invoke(task, mockInputStream);
        } catch (InvocationTargetException e) {
            assertThat(e.getCause(), notNullValue());
            assertTrue("Expected IOException, got " + e.getCause(), e.getCause().getClass() == IOException.class);
            mockControl.verify();
        }
    }

    private Method getReadObject() throws NoSuchMethodException {
        Method method = WriteRefsetDescriptions.class.getDeclaredMethod("readObject",
            new Class[] { ObjectInputStream.class });
        method.setAccessible(true);
        return method;
    }

    private WriteRefsetDescriptions createEmptyTask() {
        return new WriteRefsetDescriptions(DIRECTORY_KEY, null, null);
    }

    private Object getFieldValue(final I_DefineTask task, final String fieldName) throws Exception {
        Field field = WriteRefsetDescriptions.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(task);
    }
}
