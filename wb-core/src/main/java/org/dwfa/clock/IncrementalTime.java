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
package org.dwfa.clock;

import java.rmi.RemoteException;

public class IncrementalTime implements I_KeepIncrementalTime {

    private long increment;
    private long count = 0;
    private I_KeepTime base;

    public IncrementalTime(int increment, I_KeepTime base) {
        this.increment = increment;
        this.base = base;
    }

    public IncrementalTime(int increment) {
        this(increment, new ConstantTime(System.currentTimeMillis()));
    }

    public long getTime() throws RemoteException {
        long elapsedTime = increment * count;
        return base.getTime() + elapsedTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.clock.I_KeepIncrementalTime#increment()
     */
    public void increment() {
        count++;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.clock.I_KeepIncrementalTime#reset()
     */
    public void reset() {
        count = 0;
    }
}
