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
package org.dwfa.ace.task.refset;

import org.dwfa.bpa.process.I_Work;
import org.dwfa.ace.task.util.Logger;

import java.util.logging.Level;

public final class TaskLogger implements Logger {

    private final I_Work i_work;

    public TaskLogger(final I_Work i_work) {
        this.i_work = i_work;
    }

    public void logInfo(final String message) {
        if (i_work.getLogger().isLoggable(Level.INFO)) {
            i_work.getLogger().info(message);
        }
    }

    public void logWarn(final String message) {
        if (i_work.getLogger().isLoggable(Level.WARNING)) {
            i_work.getLogger().warning(message);
        }
    }
}
