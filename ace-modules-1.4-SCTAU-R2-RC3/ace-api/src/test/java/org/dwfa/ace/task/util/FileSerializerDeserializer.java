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
package org.dwfa.ace.task.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * Utility class to Serialize and Deserialize objects to and from a File.
 *
 * @author Matthew Edwards
 */
public class FileSerializerDeserializer<T> implements SerializerDeserializer {

    private T object;
    private File inputFile;
    private File outputFile;

    public FileSerializerDeserializer(File inputFile) {
        this.inputFile = inputFile;
    }

    public FileSerializerDeserializer(File inputFile, File outputFile) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public FileSerializerDeserializer() {
    }

    @Override
    public T deserialize() throws IOException, ClassNotFoundException {
        // Deserialize from a file
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
            return (T) in.readObject();
        } finally {
            in.close();
        }
    }

    @Override
    public void serialize() throws IOException {
        // Serialize to a file
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(outputFile));
            out.writeObject(object);
        } finally {
            out.close();
        }
    }

    /**
     * Gets the deserialized Object if the deserialize method has already been executed.
     * @return T the deserialized object.
     */
    public T getObject() {
        return object;
    }

    /**
     * Sets the object to Serialize.
     *@param object the object to Serialize
     */
    public void setObject(T object) {
        this.object = object;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
