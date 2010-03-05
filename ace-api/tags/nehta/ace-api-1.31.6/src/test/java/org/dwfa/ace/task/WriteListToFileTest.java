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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.dwfa.ace.task.util.FileSerializerDeserializer;
import org.dwfa.ace.task.util.TextFileDiffer;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.jmock.Expectations;
import org.jmock.Mockery;

/**
 * Unit Test for {@code WriteListToFile}
 * @author Matthew Edwards
 */
public class WriteListToFileTest extends TestCase {

    private static final String PROP_NAME = WriteListToFile.DEFAULT_PROPERTY_NAME;
    private static final String MSG_KEY = WriteListToFile.DEFAULT_MESSAGE_KEY;
    private static final String DFLT_WRITTEN_MSG = WriteListToFile.DEFAULT_FILE_WRITTEN_MESSAGE;
    private static final String DFLT_NOT_WRITTEN_MSG = WriteListToFile.DEFAULT_FILE_NOT_WRITTEN_MESSAGE;
    private static final String EXPECTED_FILE = "src/test/resources/org/dwfa/ace/task/expectedWriteListToFileTest.txt";
    private static final String LIST_OUTPUT_FILE = "target/outputFile.txt";
    private static final String SERIALIZED_OUTPUT_FILE = "target/outputFile.txt";
    private static final int EXPECTED_NUM_DECLARED_FIELDS = 12;
    private Mockery context;
    private I_EncodeBusinessProcess process;
    private I_Work worker;
    private TextFileDiffer textFileDiffer;

    @Override
    protected void setUp() throws Exception {
        context = new Mockery();
        process = context.mock(I_EncodeBusinessProcess.class);
        worker = context.mock(I_Work.class);
        textFileDiffer = new TextFileDiffer();
    }

    @Override
    protected void tearDown() throws Exception {
        new File(LIST_OUTPUT_FILE).delete();
    }

    /**
     * Test of evaluate method, of class WriteListToFile.
     */
    public void testEvaluate() throws Exception {
        final List<String> originalPropValue = new ArrayList<String>();
        originalPropValue.add("5adbb0d5-dbc7-5a0c-ab48-d69888889ae1");
        originalPropValue.add("7e807d2a-56c5-5b86-87da-0c0ee8a5d56f");
        originalPropValue.add("blah");
        originalPropValue.add("c6f8d026-c7a1-54e1-b85a-7907a2906516");
        originalPropValue.add("c6f8d026-c7a1-54e1-b85a-7907a2906511");

        WriteListToFile instance = new WriteListToFile(LIST_OUTPUT_FILE, PROP_NAME, MSG_KEY, DFLT_WRITTEN_MSG,
                DFLT_NOT_WRITTEN_MSG);
        context.checking(new Expectations() {

            {
                oneOf(process).readProperty(PROP_NAME);
                will(returnValue(originalPropValue));
                oneOf(process).readProperty(MSG_KEY);
                will(returnValue(""));
                oneOf(process).setProperty(MSG_KEY, String.format("%1$s%2$s", DFLT_WRITTEN_MSG, new File(
                        LIST_OUTPUT_FILE).getAbsolutePath()));
            }
        });
        instance.evaluate(process, worker);
        textFileDiffer.compare(new File(LIST_OUTPUT_FILE), new File(EXPECTED_FILE));
    }

    public void testSerializeAndDeserializeCorrect() throws Exception {
        FileSerializerDeserializer<WriteListToFile> serializer =
                new FileSerializerDeserializer<WriteListToFile>();

        WriteListToFile instance = new WriteListToFile(LIST_OUTPUT_FILE, PROP_NAME, MSG_KEY, DFLT_WRITTEN_MSG,
                DFLT_NOT_WRITTEN_MSG);

        serializer.setObject(instance);
        serializer.setOutputFile(new File(SERIALIZED_OUTPUT_FILE));

        serializer.serialize();

        serializer.setInputFile(new File(SERIALIZED_OUTPUT_FILE));

        WriteListToFile deserializedInstance = serializer.deserialize();

        assertEquals(deserializedInstance, instance);
    }

    public void testSerializeAndDeserializeWithDefaultsCorrect() throws Exception {
        FileSerializerDeserializer<WriteListToFile> serializer =
                new FileSerializerDeserializer<WriteListToFile>();

        WriteListToFile instance = new WriteListToFile();

        serializer.setObject(instance);
        serializer.setOutputFile(new File(SERIALIZED_OUTPUT_FILE));

        serializer.serialize();

        serializer.setInputFile(new File(SERIALIZED_OUTPUT_FILE));

        WriteListToFile deserializedInstance = serializer.deserialize();

        assertEquals(deserializedInstance, instance);

        assertEquals(deserializedInstance.getObjectListPropertyName(), PROP_NAME);

        assertEquals(deserializedInstance.getOutputFile(), WriteListToFile.DEFAULT_FILE_NAME);

        assertEquals(deserializedInstance.getMessageKey(), WriteListToFile.DEFAULT_MESSAGE_KEY);

        assertEquals(deserializedInstance.getFileWrittenOutputMessage(), WriteListToFile.DEFAULT_FILE_WRITTEN_MESSAGE);

        assertEquals(deserializedInstance.getFileNotWrittenOutputMessage(),
                WriteListToFile.DEFAULT_FILE_NOT_WRITTEN_MESSAGE);
    }

    /**
     * Ensures that the structure of the class has not changed, if it has the following methods need to be updated.
     * equals
     * hashCode
     * writeObject
     * readObject
     *
     * and the WriteListToFile.DATA_VERSION variable needs incrementing.
     */
    public void testCorrectNumberOfFieldsInClass() throws Exception {
        assertEquals(WriteListToFile.class.getDeclaredFields().length, EXPECTED_NUM_DECLARED_FIELDS);
    }

    public void testWithNullList() throws Exception {
        final List<String> originalPropValue = null;

        WriteListToFile instance = new WriteListToFile(LIST_OUTPUT_FILE, PROP_NAME, MSG_KEY, DFLT_WRITTEN_MSG,
                DFLT_NOT_WRITTEN_MSG);
        context.checking(new Expectations() {

            {
                oneOf(process).readProperty(PROP_NAME);
                will(returnValue(originalPropValue));
                oneOf(process).readProperty(MSG_KEY);
                will(returnValue(""));
                oneOf(process).setProperty(MSG_KEY, "Output file not was written. ");
            }
        });
        instance.evaluate(process, worker);
        assertTrue(!new File(LIST_OUTPUT_FILE).exists());
    }

    public void testWithEmptyList() throws Exception {
        final List<String> originalPropValue = new ArrayList<String>();

        WriteListToFile instance = new WriteListToFile(LIST_OUTPUT_FILE, PROP_NAME, MSG_KEY, DFLT_WRITTEN_MSG,
                DFLT_NOT_WRITTEN_MSG);
        context.checking(new Expectations() {

            {
                oneOf(process).readProperty(PROP_NAME);
                will(returnValue(originalPropValue));
                oneOf(process).readProperty(MSG_KEY);
                will(returnValue(""));
                oneOf(process).setProperty(MSG_KEY, "Output file not was written. ");
            }
        });
        instance.evaluate(process, worker);
        assertTrue(!new File(LIST_OUTPUT_FILE).exists());
    }

    public void testWithNonListValue() throws Exception {
        final String originalPropValue = "";

        WriteListToFile instance = new WriteListToFile(LIST_OUTPUT_FILE, PROP_NAME, MSG_KEY, DFLT_WRITTEN_MSG,
                DFLT_NOT_WRITTEN_MSG);
        context.checking(new Expectations() {

            {
                oneOf(process).readProperty(PROP_NAME);
                will(returnValue(originalPropValue));
                oneOf(process).readProperty(MSG_KEY);
                will(returnValue(""));
                oneOf(process).setProperty(MSG_KEY, "Output file not was written. ");
            }
        });
        instance.evaluate(process, worker);
        assertTrue(!new File(LIST_OUTPUT_FILE).exists());
    }
}
