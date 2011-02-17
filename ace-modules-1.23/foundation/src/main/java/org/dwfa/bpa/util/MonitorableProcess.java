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
/*
 * Created on Feb 14, 2005
 */
package org.dwfa.bpa.util;

import java.io.IOException;

/**
 * @author kec
 * 
 */
public interface MonitorableProcess {

    /**
     * Called to find out how much work needs
     * to be done.
     */
    public int getLengthOfTask();

    /**
     * Called to find out how much has been done.
     * 
     * @throws IOException
     */
    public int getCurrent() throws IOException;

    public void stop() throws Exception;

    /**
     * Called to find out if the task has completed.
     */
    public boolean isDone();

    /**
     * A guarded wait mechanism. See section 3.2.3 in Concurrent Programming in
     * Java, 2nd edition.
     * 
     * @throws InterruptedException
     * 
     */

    public void waitTillDone() throws InterruptedException;

}
