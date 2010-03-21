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
package org.dwfa.bpa.dnd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;

import org.dwfa.bpa.process.I_ContainData;

/**
 * @author kec
 * 
 */
public class DataContainerInputStream extends InputStream {

    private InputStream inputStream;

    /*
     * public TerminologyInputStream(I_TerminologyComponent termComponent)
     * throws IOException {
     * ByteArrayOutputStream bytes = new ByteArrayOutputStream();
     * ObjectOutputStream oos = new ObjectOutputStream(bytes);
     * oos.writeObject(termComponent);
     * inputStream = new ByteArrayInputStream(bytes.toByteArray());
     * }
     */
    public DataContainerInputStream(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
    }

    public static DataContainerInputStream create(I_ContainData data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytes);
        oos.writeObject(data);
        return new DataContainerInputStream(new ByteArrayInputStream(bytes.toByteArray()));

    }

    /**
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        return inputStream.read();
    }

    /**
     * @see java.io.InputStream#available()
     */
    public int available() throws IOException {
        return inputStream.available();
    }

    /**
     * @see java.io.InputStream#close()
     */
    public void close() throws IOException {
        inputStream.close();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        return inputStream.equals(arg0);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return inputStream.hashCode();
    }

    /**
     * @see java.io.InputStream#mark(int)
     */
    public void mark(int arg0) {
        inputStream.mark(arg0);
    }

    /**
     * @see java.io.InputStream#markSupported()
     */
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    /**
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] arg0) throws IOException {
        return inputStream.read(arg0);
    }

    /**
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        return inputStream.read(arg0, arg1, arg2);
    }

    /**
     * @see java.io.InputStream#reset()
     */
    public void reset() throws IOException {
        inputStream.reset();
    }

    /**
     * @see java.io.InputStream#skip(long)
     */
    public long skip(long arg0) throws IOException {
        return inputStream.skip(arg0);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return inputStream.toString();
    }
}
