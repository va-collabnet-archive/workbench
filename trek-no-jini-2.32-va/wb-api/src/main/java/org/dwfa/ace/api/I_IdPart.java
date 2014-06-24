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



package org.dwfa.ace.api;

//~--- non-JDK imports --------------------------------------------------------

import org.apache.commons.collections.primitives.ArrayIntList;

import org.ihtsdo.tk.api.id.IdBI;

public interface I_IdPart extends IdBI {
   @Deprecated
   public I_IdPart duplicateIdPart();

   /**
    * 1. Analog, an object, concept or situation which in some way resembles a different situation
    * 2. Analogy, in language, a comparison between concepts
    * @param statusNid
    * @param pathNid
    * @param time
    * @return
    */
   
   public I_IdPart makeIdAnalog(int statusNid, long time, int authorNid, int moduleNid, int pathNid);

   //~--- get methods ---------------------------------------------------------

   public int getAuthorNid();

   public int getAuthorityNid();

   /**
    * Denotation: the act of pointing out by name. Used as an
    * alternative to the repeated use of identifier with different
    * contextual meanings.
    * @return
    */
   public Object getDenotation();

   public ArrayIntList getPartComponentNids();

   @Deprecated
   public int getPathId();

   public int getPathNid();

   @Deprecated
   public int getStatusId();

   public int getStatusNid();
   
   public int getModuleNid(); //@afk no getAuthorNid?

   public long getTime();

   public int getVersion();

   //~--- set methods ---------------------------------------------------------

   public void setAuthorityNid(int sourceNid);

   public void setDenotation(Object sourceId);

   /**
    *
    * @param pathId
    * @deprecated use makeAnalog
    */
   @Deprecated
   public void setPathId(int pathId);

   @Deprecated
   public void setStatusId(int statusId);

   /**
    *
    * @param version
    * @deprecated use makeAnalog
    */
   @Deprecated
   public void setVersion(int version);
}
