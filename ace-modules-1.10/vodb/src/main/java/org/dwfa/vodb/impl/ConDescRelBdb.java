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
package org.dwfa.vodb.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.IdentifierSetReadOnly;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.CheckAndProcessLuceneMatch;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.I_StoreConceptAttributes;
import org.dwfa.vodb.I_StoreDescriptions;
import org.dwfa.vodb.I_StoreIdentifiers;
import org.dwfa.vodb.I_StoreRelationships;
import org.dwfa.vodb.ToIoException;
import org.dwfa.vodb.process.ProcessQueue;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessConceptAttributeEntries;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;
import org.dwfa.vodb.types.I_ProcessRelationshipEntries;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.ThinConVersioned;
import org.dwfa.vodb.types.ThinDescPartCore;
import org.dwfa.vodb.types.ThinDescPartWithCoreDelegate;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseStats;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.PreloadConfig;
import com.sleepycat.je.StatsConfig;

public class ConDescRelBdb implements I_StoreConceptAttributes, I_StoreDescriptions, I_StoreRelationships {

    private static final int THREAD_COUNT = 3;

    private class RelationshipIterator implements Iterator<I_RelVersioned> {

        boolean hasNext;

        private Iterator<I_GetConceptData> conItr = getConceptIterator();

        private Iterator<I_RelVersioned> relItr;

        private RelationshipIterator() throws IOException {
            super();
        }

        public boolean hasNext() {
            if (relItr != null) {
                if (relItr.hasNext()) {
                    return true;
                }
            }
            while (conItr.hasNext()) {
                try {
                    relItr = conItr.next().getSourceRels().iterator();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (relItr.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        public I_RelVersioned next() {
            if (hasNext()) {
                return relItr.next();
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public class ConDescRelBinding extends TupleBinding implements I_BindConDescRel {

        private ConCoreBdb conPartBdb;

        private DescCoreBdb descCoreBdb;

        private I_StoreRelParts<Integer> relPartBdb;

        public ConDescRelBinding(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
            super();
            conPartBdb = new ConCoreBdb(env, dbConfig);
            descCoreBdb = new DescCoreBdb(env, dbConfig);
            relPartBdb = new RelPartBdbEphMapIntKey(env, dbConfig);
        }

        public ConceptBean entryToObject(TupleInput ti) {
            throw new UnsupportedOperationException();
        }

        public ConceptBean populateBean(TupleInput ti, ConceptBean conceptBean) {
            try {
                synchronized (conceptBean) {

                    int conceptNid = conceptBean.getConceptId();
                    int attributeParts = ti.readShort();
                    if (attributeParts != 0) {
                        ThinConVersioned conceptAttributes = new ThinConVersioned(conceptNid, attributeParts);
                        for (int x = 0; x < attributeParts; x++) {
                            I_ConceptAttributePart conAttrPart;
                            try {
                                conAttrPart = conPartBdb.getConPart(ti.readInt());
                            } catch (IndexOutOfBoundsException e) {
                                throw new RuntimeException(e);
                            } catch (DatabaseException e) {
                                throw new RuntimeException(e);
                            }
                            conceptAttributes.addVersion(conAttrPart);
                        }
                        conceptBean.conceptAttributes = conceptAttributes;
                    }
                    int descCount = ti.readShort();
                    conceptBean.descriptions = new ArrayList<I_DescriptionVersioned>(descCount);
                    for (int x = 0; x < descCount; x++) {
                        int descId = ti.readInt();
                        int versionCount = ti.readShort();
                        ThinDescVersioned descV = new ThinDescVersioned(descId, conceptNid, versionCount);
                        conceptBean.descriptions.add(descV);
                        I_DescriptionPart lastPart = null;
                        for (int y = 0; y < versionCount; y++) {
                            ThinDescPartCore descCore = descCoreBdb.getDescPartCore(ti.readInt());
                            boolean newString = ti.readBoolean();
                            String text;
                            if (newString) {
                                text = ti.readString();
                            } else {
                                text = lastPart.getText();
                            }
                            lastPart = new ThinDescPartWithCoreDelegate(text, descCore);
                            descV.addVersion(lastPart);
                        }
                    }
                    int relCount = ti.readInt();
                    conceptBean.sourceRels = new ArrayList<I_RelVersioned>(relCount);
                    for (int x = 0; x < relCount; x++) {
                        int relId = ti.readInt();
                        int c2Id = ti.readInt();
                        int versionCount = ti.readShort();
                        ThinRelVersioned relv = new ThinRelVersioned(relId, conceptNid, c2Id, versionCount);
                        conceptBean.sourceRels.add(relv);
                        for (int y = 0; y < versionCount; y++) {
                            I_RelPart relPart = relPartBdb.getRelPart(ti.readInt());
                            assert relPart.getTypeId() != Integer.MAX_VALUE;
                            relv.addVersionNoRedundancyCheck(relPart);
                        }
                    }
                    int relOriginCount = ti.readInt();
                    if (relOriginCount > 0) {
                        int[] setElements = new int[relOriginCount];
                        for (int i = 0; i < relOriginCount; i++) {
                            setElements[i] = ti.readInt();
                        }
                        conceptBean.setRelOrigins(new IntSet(setElements));
                    } else {
                        conceptBean.setRelOrigins(new IntSet());
                    }
                    return conceptBean;
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            }
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            try {
                ConceptBean conceptBean = (ConceptBean) obj;
                synchronized (conceptBean) {
                    if (conceptBean.conceptAttributes == null) {
                        to.writeShort(0);
                    } else {
                        to.writeShort(conceptBean.conceptAttributes.versionCount());
                        for (I_ConceptAttributePart conAttrPart : conceptBean.conceptAttributes.getVersions()) {
                            to.writeInt(conPartBdb.getConPartId(conAttrPart));
                        }
                    }
                    if (conceptBean.descriptions == null) {
                        to.writeShort(0);
                    } else {
                        /*
                         * if (conceptBean.conceptAttributes == null) { throw
                         * new RuntimeException("Concept has descriptions, but
                         * no concept attributes: " + conceptBean); }
                         */
                        int descSize = conceptBean.getDescriptions().size();
                        to.writeShort(descSize);
                        for (I_DescriptionVersioned desc : conceptBean.descriptions) {
                            to.writeInt(desc.getDescId());
                            to.writeShort(desc.versionCount());
                            I_DescriptionPart lastPart = null;
                            for (I_DescriptionPart part : desc.getVersions()) {
                                try {
                                    to.writeInt(descCoreBdb.getDescPartCoreId(part));
                                    if (lastPart == null) {
                                        to.writeBoolean(true);
                                        to.writeString(part.getText());
                                    } else {
                                        if (lastPart.getText().equals(part.getText())) {
                                            to.writeBoolean(false);
                                        } else {
                                            to.writeBoolean(true);
                                            to.writeString(part.getText());
                                        }
                                    }
                                    lastPart = part;
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if ((conceptBean.sourceRels == null) || (conceptBean.sourceRels.size() == 0)) {
                        to.writeInt(0);
                    } else {
                        /*
                         * if (conceptBean.conceptAttributes == null) { throw
                         * new RuntimeException("Concept has relationships, but
                         * no concept attributes: " + conceptBean); } if
                         * (conceptBean.descriptions == null ||
                         * conceptBean.descriptions.size() == 0) { throw new
                         * RuntimeException("Concept has relationships, but no
                         * descriptions: " + conceptBean); }
                         */
                        to.writeInt(conceptBean.sourceRels.size());
                        for (I_RelVersioned rel : conceptBean.sourceRels) {
                            to.writeInt(rel.getRelId());
                            to.writeInt(rel.getC2Id());
                            to.writeShort(rel.versionCount());
                            for (I_RelPart part : rel.getVersions()) {
                                try {
                                    assert part.getTypeId() != Integer.MAX_VALUE;
                                    to.writeInt(relPartBdb.getRelPartId(part));
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if (conceptBean.getRelOrigins() == null) {
                        to.writeInt(0);
                    } else {
                        to.writeInt(conceptBean.getRelOrigins().getSetValues().length);
                        for (int i : conceptBean.getRelOrigins().getSetValues()) {
                            to.writeInt(i);
                        }
                    }
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.close();
                conPartBdb = null;
            }
            if (descCoreBdb != null) {
                descCoreBdb.close();
                descCoreBdb = null;
            }
            if (relPartBdb != null) {
                relPartBdb.close();
                relPartBdb = null;
            }
        }

        public void sync() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.sync();
            }
            if (descCoreBdb != null) {
                descCoreBdb.sync();
            }
            if (relPartBdb != null) {
                relPartBdb.sync();
            }
        }
    }

    protected static class DescriptionMap implements Callable<Boolean> {

        private short nextId = Short.MIN_VALUE;
        private HashMap<String, Short> textMap = new HashMap<String, Short>();
        private HashMap<Short, String> idMap = new HashMap<Short, String>();
        private byte[] inputBytes;

        public DescriptionMap(List<I_DescriptionVersioned> descriptions) throws UnsupportedEncodingException,
                DataFormatException {
            super();
            if (descriptions != null && descriptions.size() > 0) {
                for (I_DescriptionVersioned desc : descriptions) {
                    for (I_DescriptionPart part : desc.getVersions()) {
                        addToMap(part.getText());
                    }
                }
            }
        }

        public DescriptionMap(byte[] inputBytes) throws DataFormatException, UnsupportedEncodingException {
            super();
            this.inputBytes = inputBytes;

        }

        public Boolean call() throws DataFormatException, UnsupportedEncodingException {
            if (inputBytes == null) {
                return true;
            }
            int startIndex = 0;
            for (int i = 0; i < inputBytes.length; i++) {
                if (inputBytes[i] == '\0') {
                    int length = i - startIndex;
                    byte[] textBytes = new byte[length];
                    System.arraycopy(inputBytes, startIndex, textBytes, 0, length);
                    String text = new String(textBytes, "UTF-8");
                    addToMap(text);
                    startIndex = i + 1;
                }
            }
            inputBytes = null;
            return true;
        }

        public short getId(String text) throws UnsupportedEncodingException, DataFormatException {
            if (textMap.containsKey(text)) {
                return textMap.get(text);
            }
            return addToMap(text);
        }

        private short addToMap(String text) throws UnsupportedEncodingException, DataFormatException {
            if (text == null) {
                AceLog.getAppLog().info("Attempting to add null text to map. " + textMap);
            }
            short returnId = nextId;
            nextId++;
            textMap.put(text, returnId);
            idMap.put(returnId, text);
            return returnId;
        }

        public String getText(short id) throws UnsupportedEncodingException, DataFormatException {
            return idMap.get(id);
        }

        public byte[] getBytes() throws UnsupportedEncodingException, IOException {
            if (textMap.size() == 0) {
                return new byte[0];
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (short id = Short.MIN_VALUE; id < (Short.MIN_VALUE + idMap.size()); id++) {
                baos.write(idMap.get(id).getBytes("UTF-8"));
                baos.write('\0');
            }
            return baos.toByteArray();
        }
    }

    protected static class DescriptionCompressionMap implements Callable<Boolean> {

        private short nextId = Short.MIN_VALUE;
        private HashMap<String, Short> textMap = new HashMap<String, Short>();
        private HashMap<Short, String> idMap = new HashMap<Short, String>();
        private byte[] inputBytes;

        public DescriptionCompressionMap(List<I_DescriptionVersioned> descriptions)
                throws UnsupportedEncodingException, DataFormatException {
            super();
            if (descriptions != null && descriptions.size() > 0) {
                for (I_DescriptionVersioned desc : descriptions) {
                    for (I_DescriptionPart part : desc.getVersions()) {
                        addToMap(part.getText());
                    }
                }
            }
        }

        public DescriptionCompressionMap(byte[] inputBytes) throws DataFormatException, UnsupportedEncodingException {
            super();
            this.inputBytes = inputBytes;

        }

        public Boolean call() throws DataFormatException, UnsupportedEncodingException {
            if (inputBytes == null) {
                return true;
            }
            Inflater decompresser = new Inflater();
            decompresser.setInput(inputBytes);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buff = new byte[1024];
            int read = decompresser.inflate(buff);
            while (read > 0) {
                baos.write(buff, 0, read);
                read = decompresser.inflate(buff);
            }
            byte[] outputBytes = baos.toByteArray();
            int startIndex = 0;
            for (int i = 0; i < outputBytes.length; i++) {
                if (outputBytes[i] == '\0') {
                    int length = i - startIndex;
                    byte[] textBytes = new byte[length];
                    System.arraycopy(outputBytes, startIndex, textBytes, 0, length);
                    String text = new String(textBytes, "UTF-8");
                    addToMap(text);
                    startIndex = i + 1;
                }
            }
            inputBytes = null;
            return true;
        }

        public short getId(String text) throws UnsupportedEncodingException, DataFormatException {
            if (textMap.containsKey(text)) {
                return textMap.get(text);
            }
            return addToMap(text);
        }

        private short addToMap(String text) throws UnsupportedEncodingException, DataFormatException {
            if (text == null) {
                AceLog.getAppLog().info("Attempting to add null text to map. " + textMap);
            }
            short returnId = nextId;
            nextId++;
            textMap.put(text, returnId);
            idMap.put(returnId, text);
            return returnId;
        }

        public String getText(short id) throws UnsupportedEncodingException, DataFormatException {
            return idMap.get(id);
        }

        public byte[] getBytes() throws UnsupportedEncodingException, IOException {
            if (textMap.size() == 0) {
                return new byte[0];
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (short id = Short.MIN_VALUE; id < (Short.MIN_VALUE + idMap.size()); id++) {
                baos.write(idMap.get(id).getBytes("UTF-8"));
                baos.write('\0');
            }
            ByteArrayOutputStream compressedOut = new ByteArrayOutputStream();
            DeflaterOutputStream dout = new DeflaterOutputStream(compressedOut, new Deflater(Deflater.BEST_COMPRESSION));
            dout.write(baos.toByteArray());
            dout.close();
            return compressedOut.toByteArray();
        }
    }

    public class ConDescRelBindingWithDescCompression extends TupleBinding implements I_BindConDescRel {
        private ExecutorService exec = Executors.newFixedThreadPool(2);

        private ConCoreBdb conPartBdb;

        private DescCoreBdb descCoreBdb;

        private I_StoreRelParts<Short> relPartBdb;

        public ConDescRelBindingWithDescCompression(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
            super();
            conPartBdb = new ConCoreBdb(env, dbConfig);
            descCoreBdb = new DescCoreBdb(env, dbConfig);
            relPartBdb = new RelPartBdbEphMapShortKey(env, dbConfig);
        }

        public ConceptBean entryToObject(TupleInput ti) {
            throw new UnsupportedOperationException();
        }

        public ConceptBean populateBean(TupleInput ti, ConceptBean conceptBean) throws DataFormatException, IOException {
            try {
                synchronized (conceptBean) {
                    int descMapByteSize = ti.readInt();
                    byte[] descMapBytes = new byte[descMapByteSize];
                    ti.readFast(descMapBytes);
                    DescriptionCompressionMap descMap = new DescriptionCompressionMap(descMapBytes);
                    Future<Boolean> descMapFuture = exec.submit(descMap);
                    int conceptNid = conceptBean.getConceptId();
                    int attributeParts = ti.readShort();
                    if (attributeParts != 0) {
                        ThinConVersioned conceptAttributes = new ThinConVersioned(conceptNid, attributeParts);
                        for (int x = 0; x < attributeParts; x++) {
                            I_ConceptAttributePart conAttrPart;
                            try {
                                conAttrPart = conPartBdb.getConPart(ti.readInt());
                            } catch (IndexOutOfBoundsException e) {
                                throw new RuntimeException(e);
                            } catch (DatabaseException e) {
                                throw new RuntimeException(e);
                            }
                            conceptAttributes.addVersion(conAttrPart);
                        }
                        conceptBean.conceptAttributes = conceptAttributes;
                    }
                    int relCount = ti.readInt();
                    conceptBean.sourceRels = new ArrayList<I_RelVersioned>(relCount);
                    for (int x = 0; x < relCount; x++) {
                        int relId = ti.readInt();
                        int c2Id = ti.readInt();
                        int versionCount = ti.readShort();
                        ThinRelVersioned relv = new ThinRelVersioned(relId, conceptNid, c2Id, versionCount);
                        conceptBean.sourceRels.add(relv);
                        for (int y = 0; y < versionCount; y++) {
                            I_RelPart relPart = relPartBdb.getRelPart(ti.readShort());
                            assert relPart.getTypeId() != Integer.MAX_VALUE;
                            relv.addVersionNoRedundancyCheck(relPart);
                        }
                    }
                    int relOriginCount = ti.readInt();
                    if (relOriginCount > 0) {
                        int[] setElements = new int[relOriginCount];
                        for (int i = 0; i < relOriginCount; i++) {
                            setElements[i] = ti.readInt();
                        }
                        conceptBean.setRelOrigins(new IntSet(setElements));
                    } else {
                        conceptBean.setRelOrigins(new IntSet());
                    }

                    int descCount = ti.readShort();
                    conceptBean.descriptions = new ArrayList<I_DescriptionVersioned>(descCount);
                    descMapFuture.get();
                    for (int x = 0; x < descCount; x++) {
                        int descId = ti.readInt();
                        int versionCount = ti.readShort();
                        ThinDescVersioned descV = new ThinDescVersioned(descId, conceptNid, versionCount);
                        conceptBean.descriptions.add(descV);
                        for (int y = 0; y < versionCount; y++) {
                            ThinDescPartCore descCore = descCoreBdb.getDescPartCore(ti.readInt());
                            String text = descMap.getText(ti.readShort());
                            descV.addVersion(new ThinDescPartWithCoreDelegate(text, descCore));
                        }
                    }
                    return conceptBean;
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            try {
                ConceptBean conceptBean = (ConceptBean) obj;
                synchronized (conceptBean) {
                    DescriptionCompressionMap descMap = new DescriptionCompressionMap(conceptBean.descriptions);
                    byte[] descMapBytes = descMap.getBytes();
                    to.writeInt(descMapBytes.length);
                    to.writeFast(descMapBytes);
                    if (conceptBean.conceptAttributes == null) {
                        to.writeShort(0);
                    } else {
                        to.writeShort(conceptBean.conceptAttributes.versionCount());
                        for (I_ConceptAttributePart conAttrPart : conceptBean.conceptAttributes.getVersions()) {
                            to.writeInt(conPartBdb.getConPartId(conAttrPart));
                        }
                    }
                    if ((conceptBean.sourceRels == null) || (conceptBean.sourceRels.size() == 0)) {
                        to.writeInt(0);
                    } else {
                        to.writeInt(conceptBean.sourceRels.size());
                        for (I_RelVersioned rel : conceptBean.sourceRels) {
                            to.writeInt(rel.getRelId());
                            to.writeInt(rel.getC2Id());
                            to.writeShort(rel.versionCount());
                            for (I_RelPart part : rel.getVersions()) {
                                try {
                                    to.writeShort(relPartBdb.getRelPartId(part));
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if (conceptBean.getRelOrigins() == null) {
                        to.writeInt(0);
                    } else {
                        to.writeInt(conceptBean.getRelOrigins().getSetValues().length);
                        for (int i : conceptBean.getRelOrigins().getSetValues()) {
                            to.writeInt(i);
                        }
                    }
                    if (conceptBean.descriptions == null) {
                        to.writeShort(0);
                    } else {
                        int descSize = conceptBean.getDescriptions().size();
                        to.writeShort(descSize);
                        for (I_DescriptionVersioned desc : conceptBean.descriptions) {
                            to.writeInt(desc.getDescId());
                            to.writeShort(desc.versionCount());
                            for (I_DescriptionPart part : desc.getVersions()) {
                                try {
                                    to.writeInt(descCoreBdb.getDescPartCoreId(part));
                                    to.writeShort(descMap.getId(part.getText()));
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.close();
                conPartBdb = null;
            }
            if (descCoreBdb != null) {
                descCoreBdb.close();
                descCoreBdb = null;
            }
            if (relPartBdb != null) {
                relPartBdb.close();
                relPartBdb = null;
            }
        }

        public void sync() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.sync();
            }
            if (descCoreBdb != null) {
                descCoreBdb.sync();
            }
            if (relPartBdb != null) {
                relPartBdb.sync();
            }
        }
    }

    public class ConDescRelBindingWithDescMap extends TupleBinding implements I_BindConDescRel {
        private ExecutorService exec = Executors.newFixedThreadPool(2);

        private ConCoreBdb conPartBdb;

        private DescCoreBdb descCoreBdb;

        private I_StoreRelParts<Short> relPartBdb;

        public ConDescRelBindingWithDescMap(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
            super();
            conPartBdb = new ConCoreBdb(env, dbConfig);
            descCoreBdb = new DescCoreBdb(env, dbConfig);
            relPartBdb = new RelPartBdbEphMapShortKey(env, dbConfig);
        }

        public ConceptBean entryToObject(TupleInput ti) {
            throw new UnsupportedOperationException();
        }

        public ConceptBean populateBean(TupleInput ti, ConceptBean conceptBean) throws DataFormatException, IOException {
            try {
                synchronized (conceptBean) {
                    int descMapByteSize = ti.readInt();
                    byte[] descMapBytes = new byte[descMapByteSize];
                    ti.readFast(descMapBytes);
                    DescriptionMap descMap = new DescriptionMap(descMapBytes);
                    Future<Boolean> descMapFuture = exec.submit(descMap);
                    int conceptNid = conceptBean.getConceptId();
                    int attributeParts = ti.readShort();
                    if (attributeParts != 0) {
                        ThinConVersioned conceptAttributes = new ThinConVersioned(conceptNid, attributeParts);
                        for (int x = 0; x < attributeParts; x++) {
                            I_ConceptAttributePart conAttrPart;
                            try {
                                conAttrPart = conPartBdb.getConPart(ti.readInt());
                            } catch (IndexOutOfBoundsException e) {
                                throw new RuntimeException(e);
                            } catch (DatabaseException e) {
                                descMapFuture.get();
                                StringBuffer buff = new StringBuffer();
                                buff.append(e.getLocalizedMessage());
                                buff.append("desc map size: ");
                                buff.append(descMap.nextId);
                                for (short i = 0; i < descMap.nextId; i++) {
                                    buff.append("\n  desc[");
                                    buff.append(i);
                                    buff.append("]: ");
                                    buff.append(descMap.getText(i));
                                }
                                throw new RuntimeException(buff.toString(), e);
                            }
                            conceptAttributes.addVersion(conAttrPart);
                        }
                        conceptBean.conceptAttributes = conceptAttributes;
                    }
                    int relCount = ti.readInt();
                    conceptBean.sourceRels = new ArrayList<I_RelVersioned>(relCount);
                    for (int x = 0; x < relCount; x++) {
                        int relId = ti.readInt();
                        int c2Id = ti.readInt();
                        int versionCount = ti.readShort();
                        if (versionCount < 0) {
                            throw new IOException("Negative rel version count: " + versionCount + " for concept: \n\n"
                                + conceptBean);
                        }
                        ThinRelVersioned relv = new ThinRelVersioned(relId, conceptNid, c2Id, versionCount);
                        conceptBean.sourceRels.add(relv);
                        for (int y = 0; y < versionCount; y++) {
                            I_RelPart relPart = relPartBdb.getRelPart(ti.readShort());
                            assert relPart.getTypeId() != Integer.MAX_VALUE;
                            relv.addVersionNoRedundancyCheck(relPart);
                        }
                    }
                    int relOriginCount = ti.readInt();
                    if (relOriginCount > 0) {
                        int[] setElements = new int[relOriginCount];
                        for (int i = 0; i < relOriginCount; i++) {
                            setElements[i] = ti.readInt();
                        }
                        conceptBean.setRelOrigins(new IntSet(setElements));
                    } else {
                        conceptBean.setRelOrigins(new IntSet());
                    }

                    int descCount = ti.readShort();
                    conceptBean.descriptions = new ArrayList<I_DescriptionVersioned>(descCount);
                    descMapFuture.get();
                    for (int x = 0; x < descCount; x++) {
                        int descId = ti.readInt();
                        int versionCount = ti.readShort();
                        if (versionCount < 0) {
                            throw new IOException("Negative desc version count: " + versionCount + " for concept: \n\n"
                                + conceptBean);
                        }
                        ThinDescVersioned descV = new ThinDescVersioned(descId, conceptNid, versionCount);
                        conceptBean.descriptions.add(descV);
                        for (int y = 0; y < versionCount; y++) {
                            ThinDescPartCore descCore = descCoreBdb.getDescPartCore(ti.readInt());
                            String text = descMap.getText(ti.readShort());
                            descV.addVersion(new ThinDescPartWithCoreDelegate(text, descCore));
                        }
                    }
                    return conceptBean;
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        public void objectToEntry(Object obj, TupleOutput to) {
            try {
                ConceptBean conceptBean = (ConceptBean) obj;
                synchronized (conceptBean) {
                    DescriptionMap descMap = new DescriptionMap(conceptBean.descriptions);
                    byte[] descMapBytes = descMap.getBytes();
                    to.writeInt(descMapBytes.length);
                    to.writeFast(descMapBytes);
                    if (conceptBean.conceptAttributes == null) {
                        to.writeShort(0);
                    } else {
                        to.writeShort(conceptBean.conceptAttributes.versionCount());
                        for (I_ConceptAttributePart conAttrPart : conceptBean.conceptAttributes.getVersions()) {
                            to.writeInt(conPartBdb.getConPartId(conAttrPart));
                        }
                    }
                    if ((conceptBean.sourceRels == null) || (conceptBean.sourceRels.size() == 0)) {
                        to.writeInt(0);
                    } else {
                        to.writeInt(conceptBean.sourceRels.size());
                        for (I_RelVersioned rel : conceptBean.sourceRels) {
                            to.writeInt(rel.getRelId());
                            to.writeInt(rel.getC2Id());
                            if (rel.versionCount() >= Short.MAX_VALUE) {
                                AceLog.getAppLog().warning(
                                    "Relationship has " + rel.versionCount() + " versions: \n\n" + conceptBean);
                            }
                            to.writeShort(rel.versionCount());
                            for (I_RelPart part : rel.getVersions()) {
                                assert part.getTypeId() != Integer.MAX_VALUE;
                                try {
                                    to.writeShort(relPartBdb.getRelPartId(part));
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                    if (conceptBean.getRelOrigins() == null) {
                        to.writeInt(0);
                    } else {
                        to.writeInt(conceptBean.getRelOrigins().getSetValues().length);
                        for (int i : conceptBean.getRelOrigins().getSetValues()) {
                            to.writeInt(i);
                        }
                    }
                    if (conceptBean.descriptions == null) {
                        to.writeShort(0);
                    } else {
                        int descSize = conceptBean.getDescriptions().size();
                        if (descSize >= Short.MAX_VALUE) {
                            AceLog.getAppLog().warning("Concept has " + descSize + " descriptions: \n\n" + conceptBean);
                        }
                        to.writeShort(descSize);
                        for (I_DescriptionVersioned desc : conceptBean.descriptions) {
                            to.writeInt(desc.getDescId());
                            if (desc.versionCount() >= Short.MAX_VALUE) {
                                AceLog.getAppLog().warning(
                                    "Description has " + descSize + " versions: \n\n" + conceptBean);
                            }
                            to.writeShort(desc.versionCount());
                            for (I_DescriptionPart part : desc.getVersions()) {
                                try {
                                    to.writeInt(descCoreBdb.getDescPartCoreId(part));
                                    to.writeShort(descMap.getId(part.getText()));
                                } catch (DatabaseException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            } catch (DatabaseException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DataFormatException e) {
                throw new RuntimeException(e);
            }
        }

        public void close() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.close();
                conPartBdb = null;
            }
            if (descCoreBdb != null) {
                descCoreBdb.close();
                descCoreBdb = null;
            }
            if (relPartBdb != null) {
                relPartBdb.close();
                relPartBdb = null;
            }
        }

        public void sync() throws DatabaseException {
            if (conPartBdb != null) {
                conPartBdb.sync();
            }
            if (descCoreBdb != null) {
                descCoreBdb.sync();
            }
            if (relPartBdb != null) {
                relPartBdb.sync();
            }
        }
    }

    private I_BindConDescRel conDescRelBinding;

    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    private Database conDescRelDb;

    private File luceneDir;

    private I_StoreIdentifiers identifierDb;

    private IndexSearcher luceneSearcher = null;

    public ConDescRelBdb(Environment env, DatabaseConfig dbConfig, File luceneDir, I_StoreIdentifiers identifierDb,
            DatabaseSetupConfig.CORE_DB_TYPE type) throws DatabaseException {
        super();
        this.luceneDir = luceneDir;

        switch (type) {
        case CON_COMPDESC_REL:
            conDescRelBinding = new ConDescRelBindingWithDescCompression(env, dbConfig);
            break;
        case CON_DESC_REL:
            conDescRelBinding = new ConDescRelBinding(env, dbConfig);
            break;
        case CON_DESCMAP_REL:
            conDescRelBinding = new ConDescRelBindingWithDescMap(env, dbConfig);
            break;

        default:
            throw new DatabaseException("Unsupported CORE_DB_TYPE: " + type);
        }
        conDescRelDb = env.openDatabase(null, "conDescRelDb", dbConfig);
        PreloadConfig preloadConfig = new PreloadConfig();
        preloadConfig.setLoadLNs(false);
        conDescRelDb.preload(preloadConfig);

        this.identifierDb = identifierDb;
        logStats();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.impl.I_StoreConceptAttributes#writeConceptAttributes(org
     * .dwfa.ace.api.I_ConceptAttributeVersioned)
     */
    public void writeConceptAttributes(I_ConceptAttributeVersioned conceptAttributes) throws DatabaseException,
            IOException {

        if (conceptNids != null) {
            checkConceptNids(conceptAttributes.getConId());
        }
        ConceptBean bean = ConceptBean.get(conceptAttributes.getConId());
        bean.conceptAttributes = conceptAttributes;

        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(conceptAttributes.getConId(), key);
        conDescRelBinding.objectToEntry(bean, value);
        conDescRelDb.put(BdbEnv.transaction, key, value);
        conceptNids = null;
        // logStats();
    }

    private void checkConceptNids(int conceptId) throws DatabaseException {
        if (hasConcept(conceptId) == false) {
            conceptNids = null;
        }
    }

    public void logStats() throws DatabaseException {
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            StatsConfig config = new StatsConfig();
            config.setClear(true);
            config.setFast(false);
            DatabaseStats stats = conDescRelDb.getStats(config);
            AceLog.getAppLog().fine("conDescRelDb stats: " + stats.toString());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#hasConcept(int)
     */
    public boolean hasConcept(int conceptId) throws DatabaseException {
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(conceptId, key);
        if (conDescRelDb.get(BdbEnv.transaction, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.dwfa.vodb.impl.I_StoreConceptAttributes#getConceptAttributes(int)
     */
    public I_ConceptAttributeVersioned getConceptAttributes(int conceptId) throws IOException {
        ConceptBean bean = ConceptBean.get(conceptId);
        return bean.conceptAttributes;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.dwfa.vodb.impl.I_StoreConceptAttributes#getConceptIterator()
     */
    public Iterator<I_GetConceptData> getConceptIterator() throws IOException {
        return new ConceptIterator();

//        try {
//            return new ConceptIdArrayIterator();
//        } catch (DatabaseException e) {
//            throw new IOException(e);
//        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     *
     * org.dwfa.vodb.impl.I_StoreConceptAttributes#iterateConceptAttributeEntries
     * (org.dwfa.vodb.types.I_ProcessConceptAttributeEntries)
     */
    public void iterateConceptAttributeEntries(I_ProcessConceptAttributeEntries processor) throws Exception {
        Cursor concCursor = conDescRelDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processConceptAttributeEntry(foundKey, foundData);
            } catch (Exception e) {
                concCursor.close();
                throw e;
            }
        }
        concCursor.close();
    }

    public IdentifierSet getConceptIdSet() throws IOException {
        try {
            IdentifierSet cidSet = new IdentifierSet(identifierDb.getMaxId() - Integer.MIN_VALUE);
            refreshConceptNids();
            for (int nid : conceptNids) {
                cidSet.setMember(nid);
            }
            return cidSet;
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    public IdentifierSet getEmptyIdSet() throws IOException {
        try {
            return new IdentifierSet(identifierDb.getMaxId() - Integer.MIN_VALUE);
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    public I_RepresentIdSet getIdSetFromIntCollection(Collection<Integer> ids) throws IOException {
        IdentifierSet newSet = getEmptyIdSet();
        for (int nid : ids) {
            newSet.setMember(nid);
        }
        return newSet;
    }

    public I_RepresentIdSet getIdSetfromTermCollection(Collection<? extends I_AmTermComponent> components)
            throws IOException {
        IdentifierSet newSet = getEmptyIdSet();
        for (I_AmTermComponent component : components) {
            newSet.setMember(component.getNid());
        }
        return newSet;
    }

    public I_RepresentIdSet getReadOnlyConceptIdSet() throws IOException {
        refreshConceptNids();
        return idSet;
    }

    private static int[] conceptNids;
    private static IdentifierSetReadOnly idSet;
    private static ArrayList<Integer> relationshipNids;
    private static ArrayList<Integer> descriptionNids;

    private void refreshConceptNids() throws IOException {
        Cursor concCursor = null;
        try {
            if (conceptNids == null || getConceptCount() != conceptNids.length) {
                IdentifierSet tempIdSet = getEmptyIdSet();
                conceptNids = new int[getConceptCount()];
                concCursor = conDescRelDb.openCursor(null, null);
                DatabaseEntry foundKey = new DatabaseEntry();
                DatabaseEntry foundData = new DatabaseEntry();
                for (int i = 0; i < conceptNids.length; i++) {
                    if (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
                        AceLog.getAppLog().alertAndLogException(new Exception("premature end of concept cursor: " + i));
                    }
                    conceptNids[i] = (Integer) intBinder.entryToObject(foundKey);
                    tempIdSet.setMember(conceptNids[i]);
                }
                idSet = new IdentifierSetReadOnly(tempIdSet);
            }
        } catch (DatabaseException ex) {
            throw new IOException(ex);
        } finally {
            if (concCursor != null) {
                try {
                    concCursor.close();
                } catch (DatabaseException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private class ConceptIdArrayIterator implements Iterator<I_GetConceptData> {

        int index = 0;
        Cursor concCursor;
        int[] iteratorNids;

        private ConceptIdArrayIterator() throws DatabaseException, IOException {
            super();
            refreshConceptNids();
            iteratorNids = conceptNids.clone();
        }

        public boolean hasNext() {
            if (index >= iteratorNids.length) {
                if (concCursor != null) {
                    try {
                        concCursor.close();
                    } catch (DatabaseException e) {
                        e.printStackTrace();
                    }
                }
            }
            return index < iteratorNids.length;
        }

        public I_GetConceptData next() {
            index++;
            return ConceptBean.get(iteratorNids[index - 1]);
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported. ");
        }

    }

    private class ConceptIterator implements Iterator<I_GetConceptData> {

        DatabaseEntry foundKey = new DatabaseEntry();

        DatabaseEntry foundData = new DatabaseEntry();

        boolean hasNext;

        private Integer conceptId;

        private Cursor concCursor;

        private ConceptIterator() throws IOException {
            super();
            try {
                concCursor = conDescRelDb.openCursor(null, null);
                getNext();
            } catch (DatabaseException e) {
                throw new ToIoException(e);
            }
        }

        private void getNext() {
            try {
                hasNext = (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS);
                if (hasNext) {
                    conceptId = (Integer) intBinder.entryToObject(foundKey);
                } else {
                    conceptId = null;
                    concCursor.close();
                }
            } catch (Exception ex) {
                AceLog.getAppLog().alertAndLogException(ex);
                hasNext = false;
            }
        }

        public boolean hasNext() {
            return hasNext;
        }

        public I_GetConceptData next() {
            if (hasNext) {
                I_GetConceptData next = ConceptBean.get(conceptId);
                getNext();
                return next;
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected void finalize() throws Throwable {
            concCursor.close();
        }

    }

    private class DescriptionIterator implements Iterator<I_DescriptionVersioned> {

        boolean hasNext;

        private Iterator<I_GetConceptData> conItr = getConceptIterator();

        private Iterator<I_DescriptionVersioned> descItr;

        private DescriptionIterator() throws IOException {
            super();
        }

        public boolean hasNext() {
            if (descItr != null) {
                if (descItr.hasNext()) {
                    return true;
                }
            }
            while (conItr.hasNext()) {
                try {
                    descItr = conItr.next().getDescriptions().iterator();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (descItr.hasNext()) {
                    return true;
                }
            }
            return false;
        }

        public I_DescriptionVersioned next() {
            if (hasNext()) {
                return descItr.next();
            }
            return null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    public void close() throws DatabaseException {
        if (conDescRelDb != null) {
            conDescRelDb.close();
            conDescRelDb = null;
        }
        conDescRelBinding.close();
    }

    public void sync() throws DatabaseException {
        if (conDescRelDb != null) {
            if (!conDescRelDb.getConfig().getReadOnly()) {
                conDescRelDb.sync();
            }
        }
        conDescRelBinding.sync();
    }

    public I_ConceptAttributeVersioned conAttrEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        int conId = (Integer) intBinder.entryToObject(key);
        try {
            return getConceptAttributes(conId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws DatabaseException, IOException {
        boolean changed = false;

        if (bean.uncommittedIdVersioned != null) {
            for (I_IdVersioned idv : bean.uncommittedIdVersioned) {
                for (I_IdPart p : idv.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                        for (I_DescriptionVersioned desc : bean.getDescriptions()) {
                            if (desc.getDescId() == idv.getNativeId()) {
                                Document doc = new Document();
                                doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES,
                                    Field.Index.UN_TOKENIZED));
                                doc.add(new Field("cnid", Integer.toString(desc.getConceptId()), Field.Store.YES,
                                    Field.Index.UN_TOKENIZED));
                                addIdsToIndex(doc, identifierDb.getId(desc.getDescId()));
                                addIdsToIndex(doc, identifierDb.getId(desc.getConceptId()));
                                IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(), false);
                                writer.addDocument(doc);
                                writer.close();
                            }
                        }
                    }
                }
                identifierDb.writeId(idv);
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + idv);
                }
            }
            bean.uncommittedIdVersioned = null;
        }

        if (bean.conceptAttributes != null) {
            for (I_ConceptAttributePart p : bean.conceptAttributes.getVersions()) {
                if (p.getVersion() == Integer.MAX_VALUE) {
                    p.setVersion(version);
                    values.add(new TimePathId(version, p.getPathId()));
                    changed = true;
                    if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                        AceLog.getEditLog().fine("Committing: " + p);
                    }
                }
            }
        }
        if (bean.uncommittedConceptAttributes != null) {
            for (I_ConceptAttributePart p : bean.uncommittedConceptAttributes.getVersions()) {
                if (p.getVersion() == Integer.MAX_VALUE) {
                    changed = true;
                    p.setVersion(version);
                    values.add(new TimePathId(version, p.getPathId()));
                    if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                        AceLog.getEditLog().fine("Committing: " + p);
                    }
                }
            }
            bean.conceptAttributes = bean.uncommittedConceptAttributes;
            bean.uncommittedConceptAttributes = null;
        }
        if (bean.descriptions != null) {
            for (I_DescriptionVersioned desc : bean.descriptions) {
                for (I_DescriptionPart p : desc.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        writeToLucene(desc);
                        values.add(new TimePathId(version, p.getPathId()));
                        changed = true;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Committing: " + p);
                        }
                    }
                }
            }
        }
        if (bean.uncommittedDescriptions != null) {
            for (I_DescriptionVersioned desc : bean.uncommittedDescriptions) {
                for (I_DescriptionPart p : desc.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        changed = true;
                        p.setVersion(version);
                        writeToLucene(desc);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + desc);
                }
            }
            if (bean.descriptions == null) {
                bean.descriptions = new ArrayList<I_DescriptionVersioned>();
            }
            bean.descriptions.addAll(bean.uncommittedDescriptions);
            bean.uncommittedDescriptions = null;
        }
        if (bean.sourceRels != null) {
            for (I_RelVersioned srcRel : bean.sourceRels) {
                for (ListIterator<I_RelPart> partItr = srcRel.getVersions().listIterator(); partItr.hasNext();) {
                    I_RelPart part = partItr.next();
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        changed = true;
                        part.setVersion(version);
                        values.add(new TimePathId(version, part.getPathId()));
                    }
                }
            }
        }
        if (bean.uncommittedSourceRels != null) {
            for (I_RelVersioned rel : bean.uncommittedSourceRels) {
                changed = true;
                ConceptBean destBean = ConceptBean.get(rel.getC2Id());
                if (destBean.getRelOrigins() == null) {
                    destBean.setRelOrigins(new IntSet());
                }
                destBean.getRelOrigins().add(bean.getConceptId());
                destBean.flushDestRels();
                writeConceptToBdb(destBean);
                for (I_RelPart p : rel.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + rel);
                }
            }
            if (bean.sourceRels == null) {
                bean.sourceRels = new ArrayList<I_RelVersioned>();
            }
            bean.sourceRels.addAll(bean.uncommittedSourceRels);
            bean.uncommittedSourceRels = null;
            bean.destRels = null;
        }
        if (changed) {
            writeConceptToBdb(bean);
        }
    }

    private void writeConceptToBdb(ConceptBean bean) throws DatabaseException {
        if (conceptNids != null) {
            checkConceptNids(bean.getConceptId());
        }
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(bean.getConceptId(), key);
        conDescRelBinding.objectToEntry(bean, value);
        conDescRelDb.put(BdbEnv.transaction, key, value);
    }

    private void writeToLucene(I_DescriptionVersioned desc) throws DatabaseException {
        try {
            IndexReader reader = IndexReader.open(luceneDir);
            reader.deleteDocuments(new Term("dnid", Integer.toString(desc.getDescId())));
            reader.close();
            IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(), false);
            Document doc = new Document();
            doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            doc.add(new Field("cnid", Integer.toString(desc.getConceptId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
            addIdsToIndex(doc, identifierDb.getId(desc.getDescId()));
            addIdsToIndex(doc, identifierDb.getId(desc.getConceptId()));

            String lastDesc = null;
            for (I_DescriptionTuple tuple : desc.getTuples()) {
                if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine(
                            "Adding to index. dnid:  " + desc.getDescId() + " desc: " + tuple.getText());
                    }
                    doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.TOKENIZED));
                }

            }
            writer.addDocument(doc);
            writer.close();
            if (luceneSearcher != null) {
                luceneSearcher.close();
                AceLog.getAppLog().info("Closing lucene searcher");
            }
            luceneSearcher = null;
        } catch (CorruptIndexException e) {
            throw new DatabaseException(e);
        } catch (IOException e) {
            throw new DatabaseException(e);
        }
    }

    private void addIdsToIndex(Document doc, I_IdVersioned did) {
        for (I_IdPart p : did.getVersions()) {
            doc.add(new Field("desc", p.getSourceId().toString(), Field.Store.NO, Field.Index.UN_TOKENIZED));
        }
    }

    public Hits doLuceneSearch(String query) throws IOException, ParseException {
        if (luceneDir.exists() == false) {
            createLuceneDescriptionIndex();
        }
        if (luceneSearcher == null) {
            luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
            AceLog.getAppLog().info("Creating lucene searcher");
        }
        Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
        Hits saHits = luceneSearcher.search(q);
        if (saHits.length() > 0) {
            return saHits;
        }
        q = new QueryParser("desc", new WhitespaceAnalyzer()).parse(query);
        return luceneSearcher.search(q);
    }

    public I_DescriptionVersioned descEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        throw new UnsupportedOperationException("Iterate over concepts instead...");
    }

    public List<I_DescriptionVersioned> getDescriptions(int conceptId) throws DatabaseException, IOException {
        ConceptBean bean = ConceptBean.get(conceptId);
        if (bean.descriptions == null) {
            bean.descriptions = new ArrayList<I_DescriptionVersioned>();
        }
        return bean.descriptions;
    }

    public boolean hasDescription(int descId, int conceptId) throws DatabaseException, IOException {
        if (getDescriptions(conceptId) == null) {
            return false;
        }
        for (I_DescriptionVersioned desc : getDescriptions(conceptId)) {
            if (desc.getDescId() == descId) {
                return true;
            }
        }
        return false;
    }

    public Iterator<I_DescriptionVersioned> getDescriptionIterator() throws IOException {
        return new DescriptionIterator();
    }

    public void iterateDescriptionEntries(I_ProcessDescriptionEntries processor) throws Exception {
        throw new UnsupportedOperationException("Iterate concepts instead...");
    }

    public I_DescriptionVersioned getDescription(int descId, int concId) throws IOException, DatabaseException {
        for (I_DescriptionVersioned desc : getDescriptions(concId)) {
            if (desc.getDescId() == descId) {
                return desc;
            }
        }
        throw new IOException("No such description did: " + descId + " conid: " + concId);
    }

    public CountDownLatch searchLucene(I_TrackContinuation tracker, String query, Collection<LuceneMatch> matches,
            CountDownLatch latch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            LuceneProgressUpdator updater) throws DatabaseException, IOException, ParseException {
        Stopwatch timer = new Stopwatch();
        ;
        timer.start();
        try {
            Query q = new QueryParser("desc", new StandardAnalyzer()).parse(query);
            if (luceneDir.exists() == false) {
                updater.setProgressInfo("Making lucene index -- this may take a while...");
                createLuceneDescriptionIndex();
            }
            updater.setIndeterminate(true);
            if (luceneSearcher == null) {
                updater.setProgressInfo("Opening search index...");
                luceneSearcher = new IndexSearcher(luceneDir.getAbsolutePath());
            }
            updater.setProgressInfo("Starting StandardAnalyzer lucene query...");
            long startTime = System.currentTimeMillis();
            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                + " ms.");
            Hits hits = luceneSearcher.search(q);

            if (hits.length() > 0) {
                AceLog.getAppLog().info("StandardAnalyzer query returned " + hits.length() + " hits");
            } else {
                updater.setProgressInfo("Starting WhitespaceAnalyzer lucene query...");
                AceLog.getAppLog().info(
                    "StandardAnalyzer query returned no results. Now trying WhitespaceAnalyzer query");
                q = new QueryParser("desc", new WhitespaceAnalyzer()).parse(query);
                hits = luceneSearcher.search(q);
            }

            updater.setProgressInfo("Query complete in " + Long.toString(System.currentTimeMillis() - startTime)
                + " ms. Hits: " + hits.length());

            CountDownLatch hitLatch = new CountDownLatch(hits.length());
            updater.setHits(hits.length());
            updater.setIndeterminate(false);

            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                float score = hits.score(i);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine("Hit: " + doc + " Score: " + score);
                }

                ACE.threadPool.execute(new CheckAndProcessLuceneMatch(hitLatch, updater, doc, score, matches,
                    checkList, config, this));
            }
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                if (tracker.continueWork()) {
                    AceLog.getAppLog().info("Search time 1: " + timer.getElapsedTime());
                } else {
                    AceLog.getAppLog().info("Search 1 Canceled. Elapsed time: " + timer.getElapsedTime());
                }
            }
            timer.stop();
            return hitLatch;
        } catch (ParseException pe) {
            AceLog.getAppLog().alertAndLogException(pe);
            timer.stop();
            updater.setProgressInfo("Query malformed: " + query);
            updater.setIndeterminate(false);
            updater.setHits(0);
            return new CountDownLatch(0);
        }
    }

    public void searchConcepts(I_TrackContinuation tracker, IntList matches, CountDownLatch conceptLatch,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) throws IOException {

        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }
        Iterator<I_GetConceptData> conItr = getConceptIterator();
        Semaphore checkSemaphore = new Semaphore(15);
        Semaphore addSemaphore = new Semaphore(1);
        while (conItr.hasNext()) {
            I_GetConceptData concept = conItr.next();
            if (tracker.continueWork()) {
                try {
                    checkSemaphore.acquire();
                } catch (InterruptedException e) {
                    AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                // Semaphore checkSemaphore, IntList matches,
                ACE.threadPool.execute(new CheckAndProcessSearchTest(conceptLatch, checkSemaphore, addSemaphore,
                    matches, concept, checkList, config));
            } else {
                while (conceptLatch.getCount() > 0) {
                    conceptLatch.countDown();
                }
                try {
                    checkSemaphore.acquire(15);
                } catch (InterruptedException e) {
                    AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                break;
            }
        }
        try {
            conceptLatch.await();
        } catch (InterruptedException e) {
            AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            if (tracker.continueWork()) {
                AceLog.getAppLog().info("Search 2 time: " + timer.getElapsedTime());
            } else {
                AceLog.getAppLog().info("Canceled. Elapsed time: " + timer.getElapsedTime());
            }
            timer.stop();
        }
    }

    public void searchRegex(I_TrackContinuation tracker, Pattern p, Collection<I_DescriptionVersioned> matches,
            CountDownLatch conceptLatch, List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
            throws DatabaseException, IOException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            timer = new Stopwatch();
            timer.start();
        }
        Iterator<I_GetConceptData> conItr = getConceptIterator();
        Semaphore checkSemaphore = new Semaphore(15);
        while (conItr.hasNext()) {
            I_GetConceptData concept = conItr.next();
            if (tracker.continueWork()) {
                List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
                CountDownLatch descriptionLatch = new CountDownLatch(descriptions.size());
                for (I_DescriptionVersioned descV : descriptions) {
                    try {
                        checkSemaphore.acquire();
                    } catch (InterruptedException e) {
                        AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
                    ACE.threadPool.execute(new CheckAndProcessRegexMatch(descriptionLatch, checkSemaphore, p, matches,
                        descV, checkList, config));
                }
                try {
                    descriptionLatch.await();
                } catch (InterruptedException e) {
                    AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
                }
                conceptLatch.countDown();
            } else {
                while (conceptLatch.getCount() > 0) {
                    conceptLatch.countDown();
                }
                break;
            }
        }
        try {
            conceptLatch.await();
        } catch (InterruptedException e) {
            AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
        }
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            if (tracker.continueWork()) {
                AceLog.getAppLog().info("Search 2 time: " + timer.getElapsedTime());
            } else {
                AceLog.getAppLog().info("Canceled. Elapsed time: " + timer.getElapsedTime());
            }
            timer.stop();
        }
    }

    public void createLuceneDescriptionIndex() throws IOException {
        Stopwatch timer = new Stopwatch();
        timer.start();
        luceneDir.mkdirs();
        IndexWriter writer = new IndexWriter(luceneDir, new StandardAnalyzer(), true);
        writer.setUseCompoundFile(true);
        writer.setMergeFactor(10000);
        writer.setMaxMergeDocs(Integer.MAX_VALUE);
        writer.setMaxBufferedDocs(1000);
        Iterator<I_DescriptionVersioned> descItr = getDescriptionIterator();
        int counter = 0;
        int optimizeInterval = 10000;
        ProcessQueue processQueue = new ProcessQueue(THREAD_COUNT);
        LuceneDeescriptionProcessor processor = new LuceneDeescriptionProcessor(writer);
        while (descItr.hasNext()) {
            I_DescriptionVersioned descV = descItr.next();
            processor.add(descV);
            counter++;
            if (counter % optimizeInterval == 0) {
                AceLog.getAppLog().info("Lucene description index creation, " + counter + " queued");
                processQueue.execute(processor);
                processor = new LuceneDeescriptionProcessor(writer);
                synchronized (writer) {
                    writer.optimize();
                }
            }
        }
        if (processor.getBatchSize() > 0) {
            processQueue.execute(processor);
        }
        processQueue.awaitCompletion();
        AceLog.getAppLog().info("Optimizing index time: " + timer.getElapsedTime());
        writer.optimize();
        writer.close();
        if (AceLog.getAppLog().isLoggable(Level.INFO)) {
            AceLog.getAppLog().info("Index time: " + timer.getElapsedTime());
            timer.stop();
        }
    }

    class LuceneDeescriptionProcessor implements Runnable {
        List<I_DescriptionVersioned> batch = new ArrayList<I_DescriptionVersioned>();
        IndexWriter writer;

        public LuceneDeescriptionProcessor(IndexWriter writer) {
            this.writer = writer;
        }

        public int getBatchSize() {
            return batch.size();
        }

        public void add(I_DescriptionVersioned descV) {
            batch.add(descV);
        }

        @Override
        public void run() {
            try {
                for (I_DescriptionVersioned description : batch) {
                    indexDescription(writer, description);
                }
                synchronized (writer) {
                    writer.optimize();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private void indexDescription(IndexWriter writer, I_DescriptionVersioned descV) throws IOException,
            CorruptIndexException {
        Document doc = new Document();
        doc.add(new Field("dnid", Integer.toString(descV.getDescId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        doc.add(new Field("cnid", Integer.toString(descV.getConceptId()), Field.Store.YES, Field.Index.UN_TOKENIZED));
        try {
            addIdsToIndex(doc, identifierDb.getId(descV.getDescId()));
            addIdsToIndex(doc, identifierDb.getId(descV.getConceptId()));
        } catch (ToIoException e) {
            AceLog.getAppLog().severe("error indexing description: " + descV);
            AceLog.getAppLog().alertAndLogException(e);

        }

        String lastDesc = null;
        for (I_DescriptionTuple tuple : descV.getTuples()) {
            if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
                doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.TOKENIZED));
            }

        }
        synchronized (writer) {
            writer.addDocument(doc);
        }
    }

    public void writeDescription(I_DescriptionVersioned desc) throws DatabaseException, IOException {
        writeToLucene(desc);
        writeDescriptionNoLuceneUpdate(desc);

    }

    public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned newDesc) throws DatabaseException, IOException {
        ConceptBean bean = ConceptBean.get(newDesc.getConceptId());
        boolean newDescForConcept = true;
        for (I_DescriptionVersioned desc : bean.getDescriptions()) {
            if (desc.getDescId() == newDesc.getDescId()) {
                newDescForConcept = false;
                break;
            }
        }
        if (newDescForConcept) {
            bean.getDescriptions().add(newDesc);
        }
        writeConceptToBdb(bean);
    }

    public I_RelVersioned getRel(int relId, int conceptId) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        return concept.getSourceRel(relId);
    }

    public List<I_RelVersioned> getSrcRels(int conceptId) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        if (concept.sourceRels == null) {
            concept.sourceRels = new ArrayList<I_RelVersioned>();
        }
        return concept.getSourceRels();
    }

    public boolean hasRel(int relId, int conceptId) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        return concept.getSourceRel(relId) != null;
    }

    public boolean hasSrcRel(int conceptId, Set<Integer> srcRelTypeIds) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        for (I_RelVersioned rel : concept.getSourceRels()) {
            for (I_RelPart part : rel.getVersions()) {
                if (srcRelTypeIds.contains(part.getRelTypeId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSrcRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet sourceRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        return concept.getSourceRelTuples(allowedStatus, sourceRelTypes, positions, false).size() > 0;
    }

    public boolean hasSrcRels(int conceptId) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        return concept.getSourceRels().size() > 0;
    }

    public boolean hasDestRels(int conceptId) throws DatabaseException {
        ConceptBean concept = ConceptBean.get(conceptId);
        return concept.getRelOrigins().getSetValues().length > 0;
    }

    public I_RelVersioned relEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        throw new UnsupportedOperationException("Iterate over concepts instead...");
    }

    public void iterateRelationshipEntries(I_ProcessRelationshipEntries processor) throws Exception {
        throw new UnsupportedOperationException("Iterate over concepts instead...");
    }

    public boolean hasDestRel(int conceptId, Set<Integer> destRelTypeIds) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        if (concept.getRelOrigins() != null) {
            for (int originNid : concept.getRelOrigins().getSetValues()) {
                ConceptBean relOrigin = ConceptBean.get(originNid);
                for (I_RelVersioned rel : relOrigin.getSourceRels()) {
                    if (rel.getC2Id() == conceptId) {
                        for (I_RelPart part : rel.getVersions()) {
                            if (destRelTypeIds.contains(part.getRelTypeId())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public List<I_RelVersioned> getDestRels(int conceptId) throws DatabaseException, IOException {
        List<I_RelVersioned> destRels = new ArrayList<I_RelVersioned>();
        ConceptBean concept = ConceptBean.get(conceptId);
        if (concept.getRelOrigins() != null) {
            for (int originNid : concept.getRelOrigins().getSetValues()) {
                ConceptBean relOrigin = ConceptBean.get(originNid);
                for (I_RelVersioned rel : relOrigin.getSourceRels()) {
                    if (rel.getC2Id() == conceptId) {
                        destRels.add(rel);
                    }
                }
            }
        }
        return destRels;
    }

    public boolean hasDestRelTuple(int conceptId, I_IntSet allowedStatus, I_IntSet destRelTypes,
            Set<I_Position> positions) throws DatabaseException, IOException {
        ConceptBean concept = ConceptBean.get(conceptId);
        List<I_RelTuple> returnRels = new ArrayList<I_RelTuple>();
        if (concept.getRelOrigins() != null) {
            for (int originNid : concept.getRelOrigins().getSetValues()) {
                ConceptBean relOrigin = ConceptBean.get(originNid);
                for (I_RelVersioned rel : relOrigin.getSourceRels()) {
                    if (rel.getC2Id() == conceptId) {
                        rel.addTuples(allowedStatus, destRelTypes, positions, returnRels, false);
                        if (returnRels.size() > 0) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public Iterator<I_RelVersioned> getRelationshipIterator() throws IOException {
        return new RelationshipIterator();
    }

    public void writeRel(I_RelVersioned rel) throws IOException, DatabaseException {
        ConceptBean concept;
        try {
            concept = ConceptBean.get(rel.getC1Id());
            if (concept.sourceRels == null) {
                concept.sourceRels = new ArrayList<I_RelVersioned>();
            }
            if (concept.sourceRels.contains(rel) == false) {
                concept.sourceRels.add(rel);
            }
            writeConceptToBdb(concept);
        } catch (DatabaseException e) {
            AceLog.getAppLog().nonModalAlertAndLogException(new Exception(" Exception during add src rel"));
            throw e;
        }

        try {
            ConceptBean concept2 = ConceptBean.get(rel.getC2Id());
            if (concept2.getRelOrigins() == null) {
                concept2.setRelOrigins(new IntSet());
            }
            concept2.getRelOrigins().add(concept.getConceptId());
            writeConceptToBdb(concept2);
        } catch (DatabaseException e) {
            AceLog.getAppLog().nonModalAlertAndLogException(new Exception(" Exception during add dest rel origins"));
            throw e;
        }
    }

    public void cleanupSNOMED(I_IntSet relsToIgnore, I_IntSet releases) throws Exception {
        // Update the history records for the relationships...
        AceLog.getAppLog().info("Starting rel history update.");
        Iterator<I_GetConceptData> conItr = getConceptIterator();
        int compressedRels = 0;
        int retiredRels = 0;
        int currentRels = 0;
        int totalRels = 0;
        int retiredNid = LocalVersionedTerminology.get().uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
        while (conItr.hasNext()) {
            ConceptBean con = (ConceptBean) conItr.next();
            Iterator<I_RelVersioned> relItr = con.getSourceRels().iterator();
            boolean changed = false;
            while (relItr.hasNext()) {
                totalRels++;
                I_RelVersioned vrel = relItr.next();
                if (relsToIgnore.contains(vrel.getRelId()) == false) {
                    boolean addRetired = vrel.addRetiredRec(releases.getSetValues(), retiredNid);
                    boolean removeRedundant = vrel.removeRedundantRecs();
                    if (addRetired && removeRedundant) {
                        changed = true;
                        retiredRels++;
                        compressedRels++;
                    } else if (addRetired) {
                        changed = true;
                        retiredRels++;
                    } else if (removeRedundant) {
                        changed = true;
                        compressedRels++;
                        currentRels++;
                    } else {
                        currentRels++;
                    }
                }
            }
            if (changed) {
                writeConceptToBdb(con);
            }
        }
        AceLog.getAppLog().info("Total rels: " + totalRels);
        AceLog.getAppLog().info("Compressed rels: " + compressedRels);
        AceLog.getAppLog().info("Retired rels: " + retiredRels);
        AceLog.getAppLog().info("Current rels: " + currentRels);
    }

    public void setupBean(ConceptBean cb) throws IOException {
        /*
         * if (bean.conceptAttributes != null) { return bean; }
         */
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting concept : " + cb.getConceptId());
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        intBinder.objectToEntry(cb.getConceptId(), key);
        try {
            if (conDescRelDb.get(BdbEnv.transaction, key, value, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                TupleInput ti = new TupleInput(value.getData());
                conDescRelBinding.populateBean(ti, cb);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "Got concept: " + cb.getConceptId() + " elapsed time: " + timer.getElapsedTime() / 1000
                            + " secs");
                }
            }
        } catch (DatabaseException e) {
            throw new ToIoException(e);
        } catch (DataFormatException e) {
            throw new ToIoException(e);
        }
    }

    public int getConceptCount() throws DatabaseException {
        return (int) conDescRelDb.count();
    }

    public I_IntSet getConceptNids() throws IOException {
        if (conceptNids != null) {
            try {
                new ConceptIdArrayIterator();
            } catch (DatabaseException e) {
                throw new IOException(e);
            }
        }
        return new IntSet(conceptNids);
    }

    public IdentifierSet getRelationshipIdSet() throws IOException {
        try {
            IdentifierSet cidSet = new IdentifierSet(identifierDb.getMaxId() - Integer.MIN_VALUE);
            refreshRelationshipNids();
            for (int nid : relationshipNids) {
                cidSet.setMember(nid);
            }
            return cidSet;
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    private void refreshRelationshipNids() throws IOException {
        Cursor concCursor = null;
        try {
            refreshConceptNids();
            relationshipNids = new ArrayList<Integer>();
            concCursor = conDescRelDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            for (int i = 0; i < conceptNids.length; i++) {
                if (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
                    AceLog.getAppLog()
                        .alertAndLogException(new Exception("premature end of relationship cursor: " + i));
                }
                int conceptId = (Integer) intBinder.entryToObject(foundKey);
                List<I_RelVersioned> allRelationships = getSrcRels(conceptId);

                for (I_RelVersioned relationship : allRelationships) {
                    relationshipNids.add(relationship.getRelId());
                }
            }

        } catch (DatabaseException ex) {
            throw new IOException(ex);
        } finally {
            if (concCursor != null) {
                try {
                    concCursor.close();
                } catch (DatabaseException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    public IdentifierSet getDescriptionIdSet() throws IOException {
        try {
            IdentifierSet cidSet = new IdentifierSet(identifierDb.getMaxId() - Integer.MIN_VALUE);
            refreshDescriptionNids();
            for (int nid : descriptionNids) {
                cidSet.setMember(nid);
            }
            return cidSet;
        } catch (DatabaseException e) {
            throw new IOException(e);
        }
    }

    private void refreshDescriptionNids() throws IOException {
        Cursor concCursor = null;
        try {
            refreshConceptNids();
            descriptionNids = new ArrayList<Integer>();
            concCursor = conDescRelDb.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            for (int i = 0; i < conceptNids.length; i++) {
                if (concCursor.getNext(foundKey, foundData, LockMode.DEFAULT) != OperationStatus.SUCCESS) {
                    AceLog.getAppLog().alertAndLogException(new Exception("premature end of description cursor: " + i));
                }
                int conceptId = (Integer) intBinder.entryToObject(foundKey);
                List<I_DescriptionVersioned> allDescriptions = getDescriptions(conceptId);

                for (I_DescriptionVersioned description : allDescriptions) {
                    descriptionNids.add(description.getDescId());
                }
            }
        } catch (DatabaseException ex) {
            throw new IOException(ex);
        } finally {
            if (concCursor != null) {
                try {
                    concCursor.close();
                } catch (DatabaseException e) {
                    throw new IOException(e);
                }
            }
        }
    }

}
