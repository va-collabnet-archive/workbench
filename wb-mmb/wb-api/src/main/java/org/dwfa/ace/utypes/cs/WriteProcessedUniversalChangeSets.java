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
package org.dwfa.ace.utypes.cs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalIdList;

public class WriteProcessedUniversalChangeSets implements I_ProcessUniversalChangeSets {

    private static class NoHeaderObjectOutputStream extends ObjectOutputStream {

        public NoHeaderObjectOutputStream(OutputStream out) throws IOException {
            super(out);
        }

        @Override
        protected void writeStreamHeader() throws IOException {
            reset();
        }

    }

    private File changeSetFile;
    private boolean initialized = false;
    private ObjectOutputStream oos;

    private void lazyInit() throws IOException {
        if (initialized == false) {
            changeSetFile = new File(changeSetFile.getAbsolutePath());
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
            oos = new ObjectOutputStream(new FileOutputStream(changeSetFile));
            oos.writeObject(UniversalChangeSetReader.class);
            oos.flush();
            oos.close();
            FileOutputStream fos = new FileOutputStream(changeSetFile, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            oos = new NoHeaderObjectOutputStream(bos);
            initialized = true;
        }
    }

    public WriteProcessedUniversalChangeSets(File changeSetFile) {
        super();
        this.changeSetFile = changeSetFile;
    }

    public void processAceEbr(UniversalAceExtByRefBean bean, long commitTime) throws IOException {
        lazyInit();
        oos.writeLong(commitTime);
        oos.writeObject(bean);
    }

    public void processAcePath(UniversalAcePath path, long commitTime) throws IOException {
        lazyInit();
        oos.writeLong(commitTime);
        oos.writeObject(path);
    }

    public void processIdList(UniversalIdList list, long commitTime) throws IOException {
        lazyInit();
        oos.writeLong(commitTime);
        oos.writeObject(list);
    }

    public void processUniversalAceBean(UniversalAceBean bean, long commitTime) throws IOException {
        lazyInit();
        oos.writeLong(commitTime);
        oos.writeObject(bean);
    }

    public void close() throws IOException {
        oos.flush();
        oos.close();
    }

}
