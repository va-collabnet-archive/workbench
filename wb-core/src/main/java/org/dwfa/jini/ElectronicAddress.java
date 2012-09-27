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
 * Created on Mar 24, 2005
 */
package org.dwfa.jini;

import net.jini.entry.AbstractEntry;

/**
 * Simple Jini entry class that allows an in-box to advertise its address.
 * 
 * @author kec
 * 
 */
public class ElectronicAddress extends AbstractEntry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String address;

    public ElectronicAddress() {
        super();
    }

    /**
     * 
     */
    public ElectronicAddress(String address) {
        super();
        this.address = address;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (ElectronicAddress.class.isAssignableFrom(obj.getClass())) {
            ElectronicAddress other = (ElectronicAddress) obj;
            if ((this.address == null) || (other.address == null)) {
                if ((this.address == null) && (other.address == null)) {
                    return true;
                } else {
                    return false;
                }
            }
            return this.address.equals(other.address);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.address.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Electronic address: " + this.address;
    }
}
