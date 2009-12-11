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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.ToIoException;

import com.sleepycat.je.DatabaseException;

public class ExtensionByReferenceBean implements I_Transact {

   private static WeakHashMap<ExtensionByReferenceBean, WeakReference<ExtensionByReferenceBean>> ebrBeans = new WeakHashMap<ExtensionByReferenceBean, WeakReference<ExtensionByReferenceBean>>();

   private ExtensionByReferenceBean(int memberId) {
      super();
      this.memberId = memberId;
   }

   public static ExtensionByReferenceBean get(int memberId) {
      ExtensionByReferenceBean ebrBean = new ExtensionByReferenceBean(memberId);
      WeakReference<ExtensionByReferenceBean> ref = ebrBeans.get(ebrBean);
      if (ref != null) {
         ebrBean = ref.get();
      } else {
         synchronized (ebrBeans) {
            ref = ebrBeans.get(ebrBean);
            if (ref == null) {
               ebrBeans.put(ebrBean, new WeakReference<ExtensionByReferenceBean>(ebrBean));
            } else {
               ebrBean = ref.get();
            }
         }
      }
      return ebrBean;
   }

   public static ExtensionByReferenceBean make(UUID uid, ThinExtByRefVersioned extension) throws TerminologyException,
         IOException {
      return make(AceConfig.getVodb().uuidToNative(uid), extension);
   }

   public static ExtensionByReferenceBean makeNew(UUID uid, ThinExtByRefVersioned extension)
         throws TerminologyException, IOException {
      return makeNew(AceConfig.getVodb().uuidToNative(uid), extension);
   }

   private static HashSet<ExtensionByReferenceBean> newExtensions = new HashSet<ExtensionByReferenceBean>();

   public static ExtensionByReferenceBean makeNew(int memberId, ThinExtByRefVersioned extension) {
      ExtensionByReferenceBean ebrBean = make(memberId, extension);
      newExtensions.add(ebrBean);
      return ebrBean;
   }

   public static ExtensionByReferenceBean make(int memberId, ThinExtByRefVersioned extension) {
      ExtensionByReferenceBean ebrBean = new ExtensionByReferenceBean(memberId);
      WeakReference<ExtensionByReferenceBean> ref = ebrBeans.get(ebrBean);
      if (ref != null) {
         return ref.get();
      }
      ebrBean = get(memberId);
      ebrBean.extension = extension;
      return ebrBean;
   }

   public static ExtensionByReferenceBean get(UUID uid) throws TerminologyException, IOException {
      return get(AceConfig.getVodb().uuidToNative(uid));
   }

   public static ExtensionByReferenceBean get(Collection<UUID> uids) throws TerminologyException, IOException {
      return get(AceConfig.getVodb().uuidToNative(uids));
   }

   public static Collection<ExtensionByReferenceBean> getNewExtensions(int componentId) {
      List<ExtensionByReferenceBean> returnValues = new ArrayList<ExtensionByReferenceBean>();
      for (ExtensionByReferenceBean newEbr : newExtensions) {
         if (newEbr.extension.getComponentId() == componentId) {
            returnValues.add(newEbr);
         }
      }
      return returnValues;
   }

   private int memberId;

   private ThinExtByRefVersioned extension;

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
      extension = null;
      newExtensions.remove(this);
   }

   public void commit(int version, Set<TimePathId> values) throws IOException {
      if (AceLog.getEditLog().isLoggable(Level.FINE)) {
         AceLog.getEditLog().fine("Starting commit for ExtensionByReferenceBean: " + this);
      }
      StringBuffer buff = null;
      if (AceLog.getEditLog().isLoggable(Level.FINE)) {
         buff = new StringBuffer();
      }
      try {
         if (extension != null) {
            for (ThinExtByRefPart p : extension.getVersions()) {
               boolean changed = false;
               if (p.getVersion() == Integer.MAX_VALUE) {
                  p.setVersion(version);
                  values.add(new TimePathId(version, p.getPathId()));
                  changed = true;
                  if (buff != null) {
                     buff.append("\n  Committing: " + p);
                  }
               }
               if (changed) {
                  AceConfig.getVodb().writeExt(extension);
               }
            }
         }
      } catch (DatabaseException e) {
         throw new ToIoException(e);
      }
      if (AceLog.getAppLog().isLoggable(Level.FINE)) {
         AceLog.getAppLog().fine("Finished commit for ExtensionByReferenceBean: " + this);
      }
      if (AceLog.getEditLog().isLoggable(Level.FINE)) {
         AceLog.getEditLog().fine(buff.toString());
      }
      newExtensions.remove(this);
   }

   public ThinExtByRefVersioned getExtension() throws IOException {
      if (extension == null) {
         try {
            extension = AceConfig.getVodb().getExtension(memberId);
         } catch (DatabaseException e) {
            throw new ToIoException(e);
         }
      }
      return extension;
   }

   public UniversalAceExtByRefBean getUniversalAceBean() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefBean uEbrBean = new UniversalAceExtByRefBean(
            tf.getUids(extension.getRefsetId()), 
            tf.getUids(extension.getMemberId()), 
            tf.getUids(extension.getComponentId()), 
            tf.getUids(extension.getTypeId()));
      for (ThinExtByRefPart part: extension.getVersions()) {
         uEbrBean.getVersions().add(part.getUniversalPart());
      }
      return uEbrBean;
   }
}
