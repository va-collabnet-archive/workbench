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
package org.dwfa.bpa;

import org.dwfa.bpa.process.Condition;

/**
 * @author kec
 * 
 */
public class Branch implements I_Branch {

    private static final long serialVersionUID = 8984276903055679737L

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        I_Branch another = (I_Branch) obj;
        return (this.condition.equals(another.getCondition()) && (this.destinationId == another.getDestinationId()));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.destinationId;
    }

    private Condition condition;

    private int destinationId;

    /**
     * @param condition
     * @param destinationId
     */
    public Branch(Condition condition, int destinationId) {
        super();
        this.condition = condition;
        this.destinationId = destinationId;
    }

    public Branch() {
        super();
    }

    /**
     * @see org.dwfa.bpa.I_Branch#getCondition()
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * @see org.dwfa.bpa.I_Branch#getDestinationId()
     */
    public int getDestinationId() {
        return destinationId;
    }

    public String toString() {
        return this.condition + " -> " + this.destinationId;
    }

    /**
     * @param condition The condition to set.
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * @param destinationId The destinationId to set.
     */
    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }
}
