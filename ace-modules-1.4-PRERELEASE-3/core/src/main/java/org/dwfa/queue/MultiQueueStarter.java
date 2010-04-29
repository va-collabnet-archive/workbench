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

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import com.sun.jini.start.LifeCycle;

public class MultiQueueStarter {

    public MultiQueueStarter(String[] args, LifeCycle lc) throws Exception {
        getLogger().info(
            "\n*******************\n\n" + "Starting MultiQueueStarter with config file: " + Arrays.asList(args)
                + "\n\n******************\n");
        Configuration config = ConfigurationProvider.getInstance(args, getClass().getClassLoader());
        File[] directories = (File[]) config.getEntry(this.getClass().getName(), "directory", File[].class,
            new File[] { new File("queue") });

        for (File dir : directories) {
            processFile(dir, lc);
        }

    }

    private void processFile(File file, LifeCycle lc) throws Exception {
        if (file.isDirectory() == false) {
            if (file.getName().equalsIgnoreCase("queue.config") && (QueueServer.started(file) == false)) {
                getLogger().info("Found queue: " + file.getCanonicalPath());
                new QueueServer(new String[] { file.getCanonicalPath() }, lc);
            }
        } else {
            for (File f : file.listFiles()) {
                processFile(f, lc);
            }
        }
    }

    protected Logger getLogger() {
        return QueueServer.logger;
    }

}
