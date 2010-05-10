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
package org.dwfa.ace.task;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;

public class AceTaskUtil {
    @SuppressWarnings("unchecked")
    public static I_GetConceptData getConceptFromObject(Object obj) throws TerminologyException, IOException {
        I_GetConceptData concept = null;
        if (I_GetConceptData.class.isAssignableFrom(obj.getClass())) {
            concept = (I_GetConceptData) obj;
        } else if (TermEntry.class.isAssignableFrom(obj.getClass())) {
            TermEntry conceptEntry = (TermEntry) obj;
            concept = LocalVersionedTerminology.get().getConcept(conceptEntry.ids);
        } else if (UUID.class.isAssignableFrom(obj.getClass())) {
            UUID editPathUuid = (UUID) obj;
            concept = LocalVersionedTerminology.get().getConcept(new UUID[] { editPathUuid });
        } else if (Collection.class.isAssignableFrom(obj.getClass())) {
            Collection<UUID> collection = (Collection<UUID>) obj;
            concept = LocalVersionedTerminology.get().getConcept(collection);
        } else if (Array.class.isAssignableFrom(obj.getClass())) {
            Array array = (Array) obj;
            if (Array.getLength(array) > 0) {
                Object arrayObj = Array.get(array, 0);
                if (UUID.class.isAssignableFrom(arrayObj.getClass())) {
                    List<UUID> uuidList = new ArrayList<UUID>();
                    for (int i = 0; i < Array.getLength(array); i++) {
                        uuidList.add((UUID) Array.get(array, i));
                    }

                    return LocalVersionedTerminology.get().getConcept(uuidList);
                }
            }
        }

        return concept;
    }

    public static I_GetConceptData getConceptFromProperty(I_EncodeBusinessProcess process, String propName)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException, TerminologyException,
            IOException {
        Object editPathobj = process.readProperty(propName);
        I_GetConceptData editPathConcept = AceTaskUtil.getConceptFromObject(editPathobj);
        return editPathConcept;
    }

}
