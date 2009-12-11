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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartString;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartString extends ThinExtByRefPart {
    private String stringValue;

    
    @Override
    public boolean equals(Object obj) {
       if (super.equals(obj)) {
          if (ThinExtByRefPartString.class.isAssignableFrom(obj.getClass())) {
              ThinExtByRefPartString another = (ThinExtByRefPartString) obj;
             return stringValue.equals(another.stringValue);
          }
       }
       return false;
    }

    @Override
    public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
       I_TermFactory tf = LocalVersionedTerminology.get();
       UniversalAceExtByRefPartString stringPart = new UniversalAceExtByRefPartString();
       stringPart.setStringValue(stringValue);
       stringPart.setPathUid(tf.getUids(getPathId()));
       stringPart.setStatusUid(tf.getUids(getStatus()));
       stringPart.setTime(ThinVersionHelper.convert(getVersion()));
       return stringPart;
    }

    @Override
    public ThinExtByRefPartString duplicatePart() {
       return new ThinExtByRefPartString(this);
    }

    public ThinExtByRefPartString() {
       super();
    }

    public ThinExtByRefPartString(ThinExtByRefPartString another) {
       super(another);
       this.stringValue = another.stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

 }
