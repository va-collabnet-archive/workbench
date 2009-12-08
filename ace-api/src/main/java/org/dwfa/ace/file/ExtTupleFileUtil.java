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
package org.dwfa.ace.file;

import java.util.UUID;

import org.dwfa.ace.api.BeanPropertyMap;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.ThinExtByRefPartProperty;
import org.dwfa.ace.refset.RefsetHelper;
import org.dwfa.tapi.SuppressDataChecks;

public class ExtTupleFileUtil {

    protected static void newIntRefsetExtension(UUID refsetUUid, UUID componentUuid, int value, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveDate) throws Exception {

        newRefsetExtension(refsetUUid, componentUuid, memberUuid, pathUuid, statusUuid, effectiveDate,
            I_ThinExtByRefPartInteger.class, new BeanPropertyMap().with(ThinExtByRefPartProperty.INTEGER_VALUE, value));
    }

    protected static void newConceptRefsetExtension(UUID refsetUUid, UUID componentUuid, UUID c1Uuid, UUID memberUuid,
            UUID pathUuid, UUID statusUuid, int effectiveDate) throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        newRefsetExtension(refsetUUid, componentUuid, memberUuid, pathUuid, statusUuid, effectiveDate,
            I_ThinExtByRefPartConcept.class, new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE,
                termFactory.getId(c1Uuid).getNativeId()));
    }

    protected static void newConceptConceptRefsetExtension(UUID refsetUUid, UUID componentUuid, UUID c1Uuid,
            UUID c2Uuid, UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveDate) throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        newRefsetExtension(refsetUUid, componentUuid, memberUuid, pathUuid, statusUuid, effectiveDate,
            I_ThinExtByRefPartConceptConcept.class, new BeanPropertyMap().with(ThinExtByRefPartProperty.CONCEPT_ONE,
                termFactory.getId(c1Uuid).getNativeId()).with(ThinExtByRefPartProperty.CONCEPT_TWO,
                termFactory.getId(c2Uuid).getNativeId()));
    }

    protected static void newConceptConceptConceptRefsetExtension(UUID refsetUUid, UUID componentUuid, UUID c1Uuid,
            UUID c2Uuid, UUID c3Uuid, UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveDate)
            throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        newRefsetExtension(refsetUUid, componentUuid, memberUuid, pathUuid, statusUuid, effectiveDate,
            I_ThinExtByRefPartConceptConceptConcept.class, new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, termFactory.getId(c1Uuid).getNativeId()).with(
                ThinExtByRefPartProperty.CONCEPT_TWO, termFactory.getId(c2Uuid).getNativeId()).with(
                ThinExtByRefPartProperty.CONCEPT_THREE, termFactory.getId(c3Uuid).getNativeId()));
    }

    protected static void newConceptConceptStringRefsetExtension(UUID refsetUUid, UUID componentUuid, UUID c1Uuid,
            UUID c2Uuid, String strValue, UUID memberUuid, UUID pathUuid, UUID statusUuid, int effectiveDate)
            throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        newRefsetExtension(refsetUUid, componentUuid, memberUuid, pathUuid, statusUuid, effectiveDate,
            I_ThinExtByRefPartConceptConceptString.class, new BeanPropertyMap().with(
                ThinExtByRefPartProperty.CONCEPT_ONE, termFactory.getId(c1Uuid).getNativeId()).with(
                ThinExtByRefPartProperty.CONCEPT_TWO, termFactory.getId(c2Uuid).getNativeId()).with(
                ThinExtByRefPartProperty.STRING_VALUE, strValue));
    }

    @SuppressDataChecks
    private static void newRefsetExtension(UUID refsetUUid, UUID componentUuid, UUID memberUuid, UUID pathUuid,
            UUID statusUuid, int effectiveDate, Class<? extends I_ThinExtByRefPart> type, final BeanPropertyMap extProps)
            throws Exception {

        I_TermFactory termFactory = LocalVersionedTerminology.get();

        new RefsetHelper().newRefsetExtension(termFactory.getId(refsetUUid).getNativeId(), termFactory.getId(
            componentUuid).getNativeId(), type, extProps.with(ThinExtByRefPartProperty.STATUS, termFactory.getId(
            statusUuid).getNativeId()), memberUuid, pathUuid, effectiveDate);

        termFactory.commit();
    }
}
