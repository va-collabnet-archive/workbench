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
package org.dwfa.vodb.jar;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import com.sleepycat.je.DatabaseEntry;

public abstract class TermWriter implements Callable<Object> {
    DataOutputStream dos;
    int count = 0;
    protected boolean canceled = false;

    public TermWriter(OutputStream outStream) {
        dos = new DataOutputStream(new BufferedOutputStream(outStream));
    }

    public DatabaseEntry getDataEntry() {
        return new DatabaseEntry();
    }

    public DatabaseEntry getKeyEntry() {
        return new DatabaseEntry();
    }

    public void close() throws IOException {
        dos.close();
    }

    public int getCount() {
        return count;
    }

    public void cancel() {
        canceled = true;
    }

}
