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

package org.dwfa.bpa.process;

/**
 * Exit conditions for tasks.
 * 
 * @author kec
 * 
 */
public enum Condition {

    CONTINUE("Continue", true), ITEM_CANCELED("Canceled", true), ITEM_SKIPPED("Skipped", true), ITEM_COMPLETE("Completed", true), TRUE("True", true), FALSE("False", true), STOP("Stop", true), PROCESS_COMPLETE("The End", false), STOP_THEN_REPEAT("Stop then Repeat", false), WAIT_FOR_WEB_FORM("Web Post", true), PREVIOUS("Previous", true);

    public String toString() {
        return description;
    }

    public boolean isBranchCondition() {
        return branchCondition;
    }

    /**
	 *  
	 */
    private final String description;
    private final boolean branchCondition;

    private Condition(String description, boolean setBranch) {
        this.description = description;
        this.branchCondition = setBranch;
    }

    public static Condition getFromString(String desc) {
        for (Condition c : Condition.values()) {
            if (c.description.equals(desc)) {
                return c;
            }
        }
        throw new IllegalArgumentException(desc);
    }
}
