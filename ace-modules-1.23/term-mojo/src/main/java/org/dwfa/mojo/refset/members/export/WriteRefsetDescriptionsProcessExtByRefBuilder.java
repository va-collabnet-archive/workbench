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
package org.dwfa.mojo.refset.members.export;

import java.io.File;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.select.DescriptionSelector;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRef;
import org.dwfa.ace.task.refset.members.CleanableProcessExtByRefBuilder;
import org.dwfa.ace.task.util.Logger;

public final class WriteRefsetDescriptionsProcessExtByRefBuilder implements CleanableProcessExtByRefBuilder {

    private Logger logger;
    private File selectedDirectory;
    private I_TermFactory termFactory;
    private DescriptionSelector descSelector;

    public CleanableProcessExtByRefBuilder withSelectedDir(final File selectedDirectory) {
        this.selectedDirectory = selectedDirectory;
        return this;
    }

    public CleanableProcessExtByRefBuilder withLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    public CleanableProcessExtByRefBuilder withTermFactory(final I_TermFactory termFactory) {
        this.termFactory = termFactory;
        return this;
    }

    public CleanableProcessExtByRefBuilder withLanguagePreference(DescriptionSelector descSelector) {
        this.descSelector = descSelector;
        return this;
    }

    public CleanableProcessExtByRef build() {
        RefsetUtil refsetUtil = new RefsetUtilImpl();
        return new WriteRefsetDescriptionsProcessExtByRef(new RefsetExportValidatorImpl(), new RefsetWriterImpl(
            new RefsetWriterParameterObjectImpl(new ProgressLoggerImpl(logger), new RefsetTextWriterImpl(refsetUtil,
                termFactory), new WriterFactoryImpl(selectedDirectory, logger, termFactory, refsetUtil)),
            new CommonAPIParameterObjectImpl(refsetUtil, termFactory, logger), descSelector), refsetUtil, termFactory,
            logger);
    }

}
