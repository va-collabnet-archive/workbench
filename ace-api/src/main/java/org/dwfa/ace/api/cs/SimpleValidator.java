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
package org.dwfa.ace.api.cs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.I_AmChangeSetObject;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.ace.utypes.UniversalAcePath;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.tapi.TerminologyException;

/**
 * Simple implementation of <code>I_ValidateChangeSetChanges</code> that always
 * returns true.
 * More selective implementation classes can override methods in this class that
 * validate individual change set objects.
 * 
 * @author kec
 * 
 */
public class SimpleValidator implements I_ValidateChangeSetChanges {

    Map<UUID, Integer> cache = new HashMap<UUID, Integer>();
    protected I_TermFactory termFactory;

    public boolean validateChange(I_AmChangeSetObject csObj, I_TermFactory tf) throws IOException, TerminologyException {
        if (UniversalAceBean.class.isAssignableFrom(csObj.getClass())) {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Read UniversalAceBean... " + csObj);
            }
            return validateAceBean((UniversalAceBean) csObj, tf);
        } else if (UniversalIdList.class.isAssignableFrom(csObj.getClass())) {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Read UniversalIdList... " + csObj);
            }
            return validateAceIdList((UniversalIdList) csObj, tf);
        } else if (UniversalAcePath.class.isAssignableFrom(csObj.getClass())) {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Read UniversalAcePath... " + csObj);
            }
            return validateAcePath((UniversalAcePath) csObj, tf);
        } else if (UniversalAceExtByRefBean.class.isAssignableFrom(csObj.getClass())) {
            if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                AceLog.getEditLog().fine("Read UniversalAceExtByRefBean... " + csObj);
            }
            return validateAceExtByRefBean((UniversalAceExtByRefBean) csObj, tf);
        } else {
            throw new IOException("Can't handle class: " + csObj.getClass().getName());
        }
    }

    protected boolean validateAceExtByRefBean(UniversalAceExtByRefBean bean, I_TermFactory tf) throws IOException,
            TerminologyException {
        return true;
    }

    protected boolean validateAcePath(UniversalAcePath path, I_TermFactory tf) throws IOException, TerminologyException {
        return true;
    }

    protected boolean validateAceIdList(UniversalIdList bean, I_TermFactory tf) throws IOException,
            TerminologyException {
        return true;
    }

    protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf) throws IOException, TerminologyException {
        return true;

    }

    public boolean validateFile(File csFile, I_TermFactory tf) throws IOException {
        return csFile.exists();
    }

    public String getFailureReport() {
        return "";
    }

    protected int getNativeId(Collection<UUID> uuids) throws TerminologyException, IOException {

        Integer cacheValue = null;
        Iterator<UUID> uuidsIterator = uuids.iterator();
        while (cacheValue == null && uuidsIterator.hasNext()) {
            cacheValue = cache.get(uuidsIterator.next());
        }

        if (cacheValue == null) {
            cacheValue = termFactory.uuidToNative(uuids);
            for (UUID uuid : uuids) {
                cache.put(uuid, cacheValue);
            }
        }

        return cacheValue;
    }

}
