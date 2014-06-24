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
package org.ihtsdo.mojo.mojo.memrefset.mojo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.io.util.FileUtilImpl;

import com.thoughtworks.xstream.XStream;

public final class CmrscsXMLReaderImpl implements CmrscsXMLReader {

    private XStream xStream;
    private FileUtilImpl fileUtil;

    public CmrscsXMLReaderImpl() {
        fileUtil = new FileUtilImpl();
        xStream = new CmrscsResultXStreamConfigImpl().configure();
    }

    public CmrscsResult read(final String fileName) {
        Reader reader = open(fileName);
        CmrscsResult cmrscsResult = (CmrscsResult) xStream.fromXML(reader);
        fileUtil.closeSilently(reader);
        return cmrscsResult;
    }

    private Reader open(final String fileName) {
        try {
            return new BufferedReader(new FileReader(fileName));
        } catch (Exception e) {
            throw new CmrscsXMLReaderException(e);
        }
    }
}
