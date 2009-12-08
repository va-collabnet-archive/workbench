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
import java.util.zip.DataFormatException;

import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_BindConDescRel {

    public ConceptBean populateBean(TupleInput ti, ConceptBean conceptBean) throws DataFormatException, IOException;

    public ConceptBean entryToObject(TupleInput ti);

    public void objectToEntry(Object obj, TupleOutput to);

    public void objectToEntry(Object obj, DatabaseEntry value);

    public void close() throws DatabaseException;

    public void sync() throws DatabaseException;

}
