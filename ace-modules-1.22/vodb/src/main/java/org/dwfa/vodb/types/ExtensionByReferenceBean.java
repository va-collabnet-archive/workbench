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
package org.dwfa.vodb.types;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.ebr.I_GetExtensionData;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class ExtensionByReferenceBean implements I_Transact, I_GetExtensionData {

    public static enum REF_TYPE {
        SOFT, WEAK
    };

    private static REF_TYPE refType = REF_TYPE.SOFT;

    private static HashMap<Integer, Reference<ExtensionByReferenceBean>> ebrBeans = new HashMap<Integer, Reference<ExtensionByReferenceBean>>();

    private static HashSet<ExtensionByReferenceBean> newExtensions = new HashSet<ExtensionByReferenceBean>();

    public static void purge() {
        ebrBeans = new HashMap<Integer, Reference<ExtensionByReferenceBean>>();
        newExtensions = new HashSet<ExtensionByReferenceBean>();
    }

    private ExtensionByReferenceBean(int memberId) {
        super();
        this.memberId = memberId;
    }

    /**
     * Clears the <code>ebrBeans</code> map.
     * 
     * This is used when connecting to another DB as the native id will be
     * different
     */
    public static void resetBeanCache() {
        ebrBeans.clear();
    }

    public static ExtensionByReferenceBean get(int memberId) {
        if (ebrBeans.containsKey(memberId)) {
            Reference<ExtensionByReferenceBean> ref = ebrBeans.get(memberId);
            if (ref.isEnqueued() == false) {
                ExtensionByReferenceBean ebrBean = ref.get();
                if (ebrBean != null) {
                    return ebrBean;
                }
            }
        }

        synchronized (ebrBeans) {
            if (ebrBeans.containsKey(memberId)) {
                Reference<ExtensionByReferenceBean> ref = ebrBeans.get(memberId);
                if (ref.isEnqueued()) {
                    ebrBeans.remove(memberId);
                } else {
                    ExtensionByReferenceBean ebrBean = ref.get();
                    if (ebrBean != null) {
                        return ebrBean;
                    } else {
                        ebrBeans.remove(memberId);
                    }
                }
            }
            ExtensionByReferenceBean ebrBean = new ExtensionByReferenceBean(memberId);
            if (refType == REF_TYPE.SOFT) {
                ebrBeans.put(memberId, new SoftReference<ExtensionByReferenceBean>(ebrBean));
            } else {
                ebrBeans.put(memberId, new WeakReference<ExtensionByReferenceBean>(ebrBean));
            }
            return ebrBean;
        }
    }

    public static I_GetExtensionData make(UUID uid, I_ThinExtByRefVersioned extension) throws TerminologyException,
            IOException {
        return make(AceConfig.getVodb().uuidToNative(uid), extension);
    }

    public static I_GetExtensionData makeNew(UUID uid, I_ThinExtByRefVersioned extension) throws TerminologyException,
            IOException {
        return makeNew(AceConfig.getVodb().uuidToNative(uid), extension);
    }

    public static ExtensionByReferenceBean makeNew(int memberId, I_ThinExtByRefVersioned extension) {
        ExtensionByReferenceBean ebrBean = new ExtensionByReferenceBean(memberId);
        ebrBean.firstCommit = true;
        Reference<ExtensionByReferenceBean> ref = ebrBeans.get(memberId);
        if (ref != null) {
            throw new RuntimeException("ExtensionByReferenceBean already exists for: " + memberId);
        }
        ebrBean = make(memberId, extension);
        synchronized (newExtensions) {
            newExtensions.add(ebrBean);
        }
        return ebrBean;
    }

    public static ExtensionByReferenceBean make(int memberId, I_ThinExtByRefVersioned extension) {
        Reference<ExtensionByReferenceBean> ref = ebrBeans.get(memberId);
        if (ref != null && ref.isEnqueued()) {
            synchronized (ebrBeans) {
                ebrBeans.remove(memberId);
            }
        } else if (ref != null) {
            ExtensionByReferenceBean ebr = ref.get();
            if (ebr != null) {
                for (I_ThinExtByRefPart part : extension.getVersions()) {
                    if (ebr.extension.getVersions().contains(part) == false) {
                        ebr.extension.addVersion(part);
                    }
                }
                return ebr;
            } else {
                synchronized (ebrBeans) {
                    ebrBeans.remove(memberId);
                }
            }
        }
        ExtensionByReferenceBean ebrBean = get(memberId);
        ebrBean.extension = extension;
        return ebrBean;
    }

    public static I_GetExtensionData get(UUID uid) throws TerminologyException, IOException {
        return get(AceConfig.getVodb().uuidToNative(uid));
    }

    public static I_GetExtensionData get(Collection<UUID> uids) throws TerminologyException, IOException {
        return get(AceConfig.getVodb().uuidToNative(uids));
    }

    public static Collection<I_GetExtensionData> getNewExtensions(int componentId) throws IOException {
        List<I_GetExtensionData> returnValues = new ArrayList<I_GetExtensionData>();
        synchronized (newExtensions) {
            for (I_GetExtensionData newEbr : newExtensions) {
                if (newEbr.getExtension().getComponentId() == componentId) {
                    returnValues.add(newEbr);
                }
            }
        }
        return returnValues;
    }

    public static boolean hasNew(int memberId) {
        if (newExtensions.contains(memberId)) {
            return true;
        }
        return false;
    }

    public static I_ThinExtByRefVersioned getNewExtensionMember(int memberId) throws IOException {
        synchronized (newExtensions) {
            for (I_GetExtensionData newEbr : newExtensions) {
                if (newEbr.getExtension().getMemberId() == memberId) {
                    return newEbr.getExtension();
                }
            }
        }
        return null;
    }

    public static Collection<I_ThinExtByRefVersioned> getNewThinExtensionsForRefset(int refsetId) throws IOException {
        List<I_ThinExtByRefVersioned> returnValues = new ArrayList<I_ThinExtByRefVersioned>();
        synchronized (newExtensions) {
            for (I_GetExtensionData newEbr : newExtensions) {
                if (newEbr.getExtension().getRefsetId() == refsetId) {
                    returnValues.add(newEbr.getExtension());
                }
            }
        }
        return returnValues;
    }

    private int memberId;

    private boolean firstCommit = false;

    private I_ThinExtByRefVersioned extension;

    private boolean discard = false;

    private static int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        throw new IOException("This class is deliberately not serializable...");
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
        throw new IOException("This class is deliberately not serializable...");
    }

    public void abort() throws IOException {
        synchronized (newExtensions) {
            newExtensions.remove(this);
        }
        ebrBeans.remove(this.memberId);
    }

    public void commit(int version, Set<TimePathId> values) throws IOException {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("Starting commit for ExtensionByReferenceBean: " + this.memberId);
        }
        StringBuffer buff = null;
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            buff = new StringBuffer();
        }
        if (extension != null) {
            for (I_ThinExtByRefPart p : extension.getVersions()) {
                boolean changed = false;
                if (p.getVersion() == Integer.MAX_VALUE) {
                    p.setVersion(version);
                    values.add(new TimePathId(version, p.getPathId()));
                    changed = true;
                    if (buff != null) {
                        buff.append("\n  Committing member: " + extension.getMemberId() + " for component: "
                            + extension.getComponentId() + " part:" + p);
                    }
                }
                if (changed) {
                    AceConfig.getVodb().writeExt(extension);
                }
            }
        }
        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
            AceLog.getAppLog().fine("Finished commit for ExtensionByReferenceBean: " + this);
        }
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine(buff.toString());
        }
        synchronized (newExtensions) {
            newExtensions.remove(this);
        }
        firstCommit = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_GetExtensionData#getExtension()
     */
    public I_ThinExtByRefVersioned getExtension() throws IOException {
        if (extension == null) {
            extension = AceConfig.getVodb().getExtension(memberId);
        }
        return extension;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_GetExtensionData#getUniversalAceBean()
     */
    public UniversalAceExtByRefBean getUniversalAceBean() throws TerminologyException, IOException {
        I_TermFactory tf = LocalVersionedTerminology.get();
        UniversalAceExtByRefBean uEbrBean = new UniversalAceExtByRefBean(tf.getUids(getExtension().getRefsetId()),
            tf.getUids(getExtension().getMemberId()), tf.getUids(getExtension().getComponentId()),
            tf.getUids(getExtension().getTypeId()));
        for (I_ThinExtByRefPart part : getExtension().getVersions()) {
            uEbrBean.getVersions().add(part.getUniversalPart());
        }
        return uEbrBean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (ExtensionByReferenceBean.class.isAssignableFrom(obj.getClass())) {
            ExtensionByReferenceBean another = (ExtensionByReferenceBean) obj;
            return memberId == another.memberId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return memberId;
    }

    public boolean isFirstCommit() {
        return firstCommit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_GetExtensionData#getMemberId()
     */
    public int getMemberId() {
        return memberId;
    }

    public void removeFromCache() throws IOException {
        abort();
    }

    public void discard() {
        this.discard = true;
    }

    public boolean isUncommitted() throws IOException {
        if (discard) {
            return false;
        }
        if (extension != null) {
            if (extension.getVersions().size() > 0) {
                for (I_ThinExtByRefPart part : getExtension().getVersions()) {
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

}
