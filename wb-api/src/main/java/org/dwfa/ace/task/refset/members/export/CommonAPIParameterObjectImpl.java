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
package org.dwfa.ace.task.refset.members.export;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.refset.members.RefsetUtil;
import org.dwfa.ace.task.util.Logger;

public class CommonAPIParameterObjectImpl implements CommonAPIParameterObject {

    private final RefsetUtil refsetUtil;
    private final I_TermFactory termFactory;
    private final Logger logger;

    public CommonAPIParameterObjectImpl(final RefsetUtil refsetUtil, final I_TermFactory termFactory,
            final Logger logger) {
        this.refsetUtil = refsetUtil;
        this.termFactory = termFactory;
        this.logger = logger;
    }

    public RefsetUtil getRefsetUtil() {
        return refsetUtil;
    }

    public I_TermFactory getTermFactory() {
        return termFactory;
    }

    public Logger getLogger() {
        return logger;
    }
}
