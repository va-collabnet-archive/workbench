/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.rules;

import org.drools.SystemEventListener;

/**
 * The listener interface for receiving consoleSystemEvent events.
 * The class that is interested in processing a consoleSystemEvent
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addConsoleSystemEventListener<code> method. When
 * the consoleSystemEvent event occurs, that object's appropriate
 * method is invoked.
 *
 * @see ConsoleSystemEventEvent
 */
public class ConsoleSystemEventListener implements SystemEventListener {

	
	/**
	 * Instantiates a new console system event listener.
	 */
	public ConsoleSystemEventListener() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#debug(java.lang.String)
	 */
	public void debug(String arg0) {
		System.out.println("DEBUG: " + arg0);
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#debug(java.lang.String, java.lang.Object)
	 */
	@Override
	public void debug(String arg0, Object arg1) {
		System.out.println("DEBUG M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("DEBUG EX: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#exception(java.lang.Throwable)
	 */
	@Override
	public void exception(Throwable arg0) {
		if (arg0 instanceof RuntimeException) {
			RuntimeException r = (RuntimeException) arg0;
			System.out.println("EXCEPTION Runtime: " + arg0.getMessage());
			System.out.println("EXCEPTION Cause: " + r.getCause().getMessage());
		}
		System.out.println("EXCEPTION: " + arg0.getMessage());
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#exception(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public void exception(String arg0, Throwable arg1) {
		System.out.println("EXCEPTION M: " + arg0);
		if (arg1 instanceof RuntimeException) {
			RuntimeException r = (RuntimeException) arg1;
			System.out.println("EXCEPTION Runtime: " + arg1.getMessage());
			System.out.println("EXCEPTION Cause: " + r.getCause().getMessage());
		}
		System.out.println("EXCEPTION: " + arg1.getMessage());
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#info(java.lang.String)
	 */
	@Override
	public void info(String arg0) {
		System.out.println("INFO: " + arg0);
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#info(java.lang.String, java.lang.Object)
	 */
	@Override
	public void info(String arg0, Object arg1) {
		System.out.println("INFO M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("INFO EX: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#warning(java.lang.String)
	 */
	@Override
	public void warning(String arg0) {
		System.out.println("WARNING: " + arg0);
	}

	/* (non-Javadoc)
	 * @see org.drools.SystemEventListener#warning(java.lang.String, java.lang.Object)
	 */
	@Override
	public void warning(String arg0, Object arg1) {
		System.out.println("WARNING M: " + arg0);
		if (arg1 instanceof Exception) {
			Exception e = (Exception) arg1;
			System.out.println("WARNING EX: " + e.getMessage());
		}
	}

}
