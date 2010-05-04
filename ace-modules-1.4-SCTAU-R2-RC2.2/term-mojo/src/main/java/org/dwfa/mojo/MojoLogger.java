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
package org.dwfa.mojo;

import org.dwfa.ace.task.util.Logger;
import org.apache.maven.plugin.logging.Log;

public final class MojoLogger implements Logger {

    private final Log log;

    public MojoLogger(final Log log) {
        this.log = log;
    }

    public void logInfo(final String message) {
        if (log.isInfoEnabled()) {
            log.info(message);
        }
    }

    public void logWarn(final String message) {
        if (log.isWarnEnabled()) {
            log.warn(message);
        }
    }
}
