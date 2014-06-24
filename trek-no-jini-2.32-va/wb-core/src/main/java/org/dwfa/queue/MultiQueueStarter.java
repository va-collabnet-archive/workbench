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
package org.dwfa.queue;

import org.ihtsdo.ttk.preferences.EnumBasedPreferences;
import org.ihtsdo.ttk.queue.QueueList;
import org.ihtsdo.ttk.queue.QueuePreferences;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiQueueStarter {

    /**
     * 
     * @param args the first argument of this array is the absolute node of the 
     *  queue list to be processed. 
     * @throws Exception 
     */
    public MultiQueueStarter(EnumBasedPreferences prefs) throws Exception {
        getLogger().log(Level.INFO,"\n*******************\n\n"
                + "Starting MultiQueueStarter with preferences "
                + "root of: {0}\n\n******************\n", prefs);

        QueueList queueList = new QueueList(prefs);
        for (QueuePreferences queuePreferences : queueList.getQueuePreferences()) {
            processQueue(queuePreferences);
        }
    }

    private void processQueue(QueuePreferences queuePreferences) throws Exception {
        File queueDirectory = queuePreferences.getQueueDirectory();
        if (!queueDirectory.exists()) {
            getLogger().info(" ** Creating queue directory: " + queueDirectory);
            queueDirectory.mkdirs();
        }
        
        if (!QueueServer.started(queuePreferences)) {
            new QueueServer(queuePreferences);
        }
    }

    protected final Logger getLogger() {
        return QueueServer.logger;
    }

}
