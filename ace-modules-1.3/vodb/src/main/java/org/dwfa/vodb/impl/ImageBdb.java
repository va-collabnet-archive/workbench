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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.dwfa.vodb.ConceptKeyForImageCreator;
import org.dwfa.vodb.I_StoreImages;
import org.dwfa.vodb.I_StoreInBdb;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.bind.ThinImageBinder;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_ProcessImageEntries;
import org.dwfa.vodb.types.ThinImageVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryCursor;
import com.sleepycat.je.SecondaryDatabase;

public class ImageBdb implements I_StoreInBdb, I_StoreImages {

    private Database imageDb;
    private SecondaryDatabase conceptImageMap;
    private ThinImageBinder imageBinder = new ThinImageBinder();
    private TupleBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);

    public ImageBdb(Environment env, DatabaseConfig dbConfig) throws DatabaseException {
        super();
        imageDb = env.openDatabase(null, "imageDb", dbConfig);
        ConceptKeyForImageCreator concToImageKeyCreator = new ConceptKeyForImageCreator();

        SecondaryConfig imageByConConfig = new SecondaryConfig();
        imageByConConfig.setReadOnly(VodbEnv.isReadOnly());
        imageByConConfig.setDeferredWrite(VodbEnv.isDeferredWrite());
        imageByConConfig.setAllowCreate(!VodbEnv.isReadOnly());
        imageByConConfig.setSortedDuplicates(true);
        imageByConConfig.setKeyCreator(concToImageKeyCreator);
        imageByConConfig.setAllowPopulate(true);

        imageByConConfig.setTransactional(VodbEnv.isTransactional());
        conceptImageMap = env.openSecondaryDatabase(null, "conceptImageMap", imageDb, imageByConConfig);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.impl.I_StoreImages#writeImage(org.dwfa.ace.api.I_ImageVersioned
     * )
     */
    public void writeImage(I_ImageVersioned image) throws DatabaseException {
        DatabaseEntry imageKey = new DatabaseEntry();
        DatabaseEntry imageValue = new DatabaseEntry();
        intBinder.objectToEntry(image.getImageId(), imageKey);
        imageBinder.objectToEntry(image, imageValue);
        imageDb.put(BdbEnv.transaction, imageKey, imageValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreImages#hasImage(int)
     */
    public boolean hasImage(int imageId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting image : " + imageId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry imageKey = new DatabaseEntry();
        DatabaseEntry imageValue = new DatabaseEntry();
        intBinder.objectToEntry(imageId, imageKey);
        if (imageDb.get(BdbEnv.transaction, imageKey, imageValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Got image: " + imageId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreImages#getImage(int)
     */
    public I_ImageVersioned getImage(int nativeId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting image : " + nativeId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry imageKey = new DatabaseEntry();
        DatabaseEntry imageValue = new DatabaseEntry();
        intBinder.objectToEntry(nativeId, imageKey);
        if (imageDb.get(BdbEnv.transaction, imageKey, imageValue, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            I_ImageVersioned image = (I_ImageVersioned) imageBinder.entryToObject(imageValue);
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine(
                    "Got image: " + nativeId + " for concept: " + ConceptBean.get(image.getConceptId())
                        + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
            }
            return image;
        }
        throw new DatabaseException("Image for: " + nativeId + " not found.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.impl.I_StoreImages#getImages(int)
     */
    public List<I_ImageVersioned> getImages(int conceptId) throws DatabaseException {
        Stopwatch timer = null;
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Getting images for: " + conceptId);
            timer = new Stopwatch();
            timer.start();
        }
        DatabaseEntry secondaryKey = new DatabaseEntry();

        intBinder.objectToEntry(conceptId, secondaryKey);
        DatabaseEntry foundData = new DatabaseEntry();

        SecondaryCursor mySecCursor = conceptImageMap.openSecondaryCursor(null, null);
        OperationStatus retVal = mySecCursor.getSearchKey(secondaryKey, foundData, LockMode.DEFAULT);
        List<I_ImageVersioned> matches = new ArrayList<I_ImageVersioned>();
        while (retVal == OperationStatus.SUCCESS) {
            ThinImageVersioned imageFromConceptId = (ThinImageVersioned) imageBinder.entryToObject(foundData);
            if (imageFromConceptId.getConceptId() == conceptId) {
                matches.add(imageFromConceptId);
            } else {
                break;
            }
            retVal = mySecCursor.getNextDup(secondaryKey, foundData, LockMode.DEFAULT);
        }
        mySecCursor.close();
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine(
                "Images fetched for: " + conceptId + " elapsed time: " + timer.getElapsedTime() / 1000 + " secs");
        }
        return matches;
    }

    public void close() throws DatabaseException {
        if (conceptImageMap != null) {
            conceptImageMap.close();
        }
        if (imageDb != null) {
            imageDb.close();
        }
    }

    public void sync() throws DatabaseException {
        if (conceptImageMap != null) {
            if (!conceptImageMap.getConfig().getReadOnly()) {
                conceptImageMap.sync();
            }
        }
        if (imageDb != null) {
            if (!imageDb.getConfig().getReadOnly()) {
                imageDb.sync();
            }
        }
    }

    public void iterateImages(I_ProcessImageEntries processor) throws Exception {
        Cursor imageCursor = imageDb.openCursor(null, null);
        DatabaseEntry foundKey = processor.getKeyEntry();
        DatabaseEntry foundData = processor.getDataEntry();
        while (imageCursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            try {
                processor.processImages(foundKey, foundData);
            } catch (Exception e) {
                imageCursor.close();
                throw e;
            }
        }
        imageCursor.close();
    }

    public I_ImageVersioned imageEntryToObject(DatabaseEntry key, DatabaseEntry value) {
        return (I_ImageVersioned) imageBinder.entryToObject(value);
    }

    public void commit(ConceptBean bean, int version, Set<TimePathId> values) throws IOException, DatabaseException {
        if (bean.images != null) {
            for (I_ImageVersioned image : bean.images) {
                boolean changed = false;
                for (I_ImagePart p : image.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                        changed = true;
                        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                            AceLog.getEditLog().fine("Committing: " + p);
                        }
                    }
                }
                if (changed) {
                    AceConfig.getVodb().writeImage(image);
                }
            }
        }
        if (bean.uncommittedImages != null) {
            for (I_ImageVersioned image : bean.uncommittedImages) {
                for (I_ImagePart p : image.getVersions()) {
                    if (p.getVersion() == Integer.MAX_VALUE) {
                        p.setVersion(version);
                        values.add(new TimePathId(version, p.getPathId()));
                    }
                }
                this.writeImage(image);
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("Committing: " + image);
                }
            }
            if (bean.images == null) {
                bean.images = new ArrayList<I_ImageVersioned>();
            }
            bean.images.addAll(bean.uncommittedImages);
            bean.uncommittedImages = null;
        }
    }

    public void setupBean(ConceptBean cb) throws IOException {
        // nothing to do
    }

}
