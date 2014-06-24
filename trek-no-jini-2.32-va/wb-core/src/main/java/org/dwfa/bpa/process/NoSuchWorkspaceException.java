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
 * Created on Mar 21, 2005
 */
package org.dwfa.bpa.process;

/**
 * Thrown if a specified workspace cannot be found.
 * 
 * @author kec
 * 
 */
public class NoSuchWorkspaceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public NoSuchWorkspaceException() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     */
    public NoSuchWorkspaceException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param message
     * @param cause
     */
    public NoSuchWorkspaceException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param cause
     */
    public NoSuchWorkspaceException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
    }
}
