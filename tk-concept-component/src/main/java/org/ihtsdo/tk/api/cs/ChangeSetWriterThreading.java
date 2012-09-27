/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.cs;

import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

// TODO: Auto-generated Javadoc
/**
 * The Enum ChangeSetWriterThreading.
 */
public enum ChangeSetWriterThreading {
    
    /** The single thread. */
    SINGLE_THREAD("single threaded"), 
 /** The multi thread. */
 MULTI_THREAD("multi-threaded");

    /** The display string. */
    String displayString;

    /**
     * Instantiates a new change set writer threading.
     *
     * @param displayString the display string
     */
    private ChangeSetWriterThreading(String displayString) {
        this.displayString = displayString;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return displayString;
    }
  
   /**
    * Gets the.
    *
    * @param changeSetGenerationThreadingPolicy the change set generation threading policy
    * @return the change set writer threading
    */
   public static ChangeSetWriterThreading
           get(ChangeSetGenerationThreadingPolicy changeSetGenerationThreadingPolicy) {
    	switch (changeSetGenerationThreadingPolicy) {
		case SINGLE_THREAD:
			return SINGLE_THREAD;
		case MULTI_THREAD:
			return MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + changeSetGenerationThreadingPolicy);
		}
    }

    /**
     * Convert.
     *
     * @return the change set generation threading policy
     */
    public ChangeSetGenerationThreadingPolicy convert() {
    	switch (this) {
		case SINGLE_THREAD:
			return ChangeSetGenerationThreadingPolicy.SINGLE_THREAD;
		case MULTI_THREAD:
			return ChangeSetGenerationThreadingPolicy.MULTI_THREAD;
		default:
			throw new UnsupportedOperationException("Can't handle csgtp: " + this);
		}
    }

}
