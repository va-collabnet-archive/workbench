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
package org.dwfa.tapi.impl;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.Expression;
import java.beans.XMLEncoder;
import java.io.IOException;

import org.dwfa.tapi.TerminologyException;

public class LocalFixedDescPersistenceDelegate extends DefaultPersistenceDelegate {

    protected Expression instantiate(Object oldInstance, Encoder out) {
        LocalFixedDesc d = (LocalFixedDesc) oldInstance;
        XMLEncoder encoder = (XMLEncoder) out;
        try {
            return new Expression(oldInstance, d.getClass(), "get", new Object[] { d.getUids(), encoder.getOwner() });
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        }
    }
}
