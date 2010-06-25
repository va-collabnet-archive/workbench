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
package org.dwfa.ace.cs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.util.TupleVersionPart;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ExtensionByReferenceBean;

public class BinaryChangeSetWriter implements I_WriteChangeSet {

    @Override
    public String toString() {
        return "BinaryChangeSetWriter: changeSetFile: " + changeSetFile + " tempFile: " + tempFile;
    }

    /**
    *
    */
    private static final long serialVersionUID = 1L;

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

    private File tempFile;

    private transient ObjectOutputStream tempOut;

    public BinaryChangeSetWriter(File changeSetFile, File tempFile) {
        super();
        this.changeSetFile = changeSetFile;
        this.tempFile = tempFile;
    }

    public void commit() throws IOException {

        if (tempOut != null) {
            tempOut.flush();
            tempOut.close();
            tempOut = null;
            String canonicalFileString = tempFile.getCanonicalPath();
            if (tempFile.exists()) {
                if (tempFile.renameTo(changeSetFile) == false) {
                    AceLog.getAppLog().warning("tempFile.renameTo failed. Attempting FileIO.copyFile...");
                    FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile.getCanonicalPath());
                }
                tempFile = new File(canonicalFileString);
                tempFile.delete();
            }
        }
    }

    public void open() throws IOException {
        if (changeSetFile.exists() == false) {
            changeSetFile.getParentFile().mkdirs();
            changeSetFile.createNewFile();
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(changeSetFile));
            oos.writeObject(BinaryChangeSetReader.class);
            oos.flush();
            oos.close();
        }
        FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile.getCanonicalPath());
        AceLog.getAppLog().info(
            "Copying from: " + changeSetFile.getCanonicalPath() + "\n        to: " + tempFile.getCanonicalPath());
        tempOut = new NoHeaderObjectOutputStream(new FileOutputStream(tempFile, true));
    }

    public void writeChanges(I_Transact change, long time) throws IOException {
        if (ConceptBean.class.isAssignableFrom(change.getClass())) {
            ConceptBean conceptChange = (ConceptBean) change;

            List<I_ConceptAttributeVersioned> conceptAttributes = new ArrayList<I_ConceptAttributeVersioned>();
            conceptAttributes.add(conceptChange.getUncommittedConceptAttributes());
            TupleVersionPart.removeDuplicateParts(conceptAttributes);
            TupleVersionPart.removeDuplicateParts(conceptChange.getUncommittedDescriptions());
            TupleVersionPart.removeDuplicateParts(conceptChange.getUncommittedSourceRels());

            if (conceptChange.isUncommitted()) {
                tempOut.writeLong(time);
                writeChanges(conceptChange, time);
            }
        } else if (I_Path.class.isAssignableFrom(change.getClass())) {
            I_Path pathChange = (I_Path) change;
            tempOut.writeLong(time);
            writeChanges(pathChange, time);
        } else if (UniversalIdList.class.isAssignableFrom(change.getClass())) {
            UniversalIdList idListChange = (UniversalIdList) change;
            tempOut.writeLong(time);
            writeIds(idListChange, time);
        } else if (ExtensionByReferenceBean.class.isAssignableFrom(change.getClass())) {
            ExtensionByReferenceBean extensionChange = (ExtensionByReferenceBean) change;
            if (extensionChange.isUncommitted()) {
                tempOut.writeLong(time);
                writeExtension(extensionChange, time);
            }
        } else {
            throw new IOException("Can't handle class: " + change.getClass().getName());
        }

    }

    private void writeChanges(ConceptBean cb, long time) throws IOException {
        try {
            UniversalAceBean bean = cb.getUniversalAceBean();
            tempOut.writeObject(bean);
            if (AceLog.getEditLog().isLoggable(Level.FINER)) {
                AceLog.getEditLog().finer("writeChanges time: " + time + " concept: " + bean);
            }
        } catch (TerminologyException e) {
            IOException ioe = new IOException(e.getLocalizedMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    private void writeExtension(ExtensionByReferenceBean ebrBean, long time) throws IOException {
        try {
            UniversalAceExtByRefBean bean = ebrBean.getUniversalAceBean();
            tempOut.writeObject(bean);
            if (AceLog.getEditLog().isLoggable(Level.FINER)) {
                AceLog.getEditLog().finer("writeChanges time: " + time + " extension: " + bean);
            }
        } catch (TerminologyException e) {
            IOException ioe = new IOException(e.getLocalizedMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }

    private void writeIds(UniversalIdList idList, long time) throws IOException {
        tempOut.writeObject(idList);
        if (AceLog.getEditLog().isLoggable(Level.FINER)) {
            AceLog.getEditLog().finer("writeChanges time: " + time + " idList: " + idList);
        }
    }

    private void writeChanges(I_Path path, long time) throws IOException {
        try {
            tempOut.writeObject(path.getUniversal());
            if (AceLog.getEditLog().isLoggable(Level.FINER)) {
                AceLog.getEditLog().finer("writeChanges time: " + time + " path: " + path);
            }
        } catch (TerminologyException e) {
            IOException ioe = new IOException(e.getLocalizedMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
}
