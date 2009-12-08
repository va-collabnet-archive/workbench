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
package org.dwfa.app;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * An adaptor that links the application to the standard Apple ApplicationEvents
 * that may
 * be generated by the finder, applescript, or other applications. This class
 * uses reflection
 * and a dynamic proxy class so that it will compile on any platform.
 * 
 * @author kec
 * 
 */
public class OSXAdapter implements InvocationHandler {
    private static Object appShell;
    private static Class<?> appClass;
    private Object listener;
    private Class<?> appListenerInterfaceClass;
    private I_ManageStandardAppFunctions appGuts;

    /**
     * 
     * @param appGuts
     * @throws Exception if this class is instantiated on a platform other than
     *             OS X. The calling method
     *             should verify that the OS by testing the system property as
     *             follows: <code><pre>
 		String lcOSName = System.getProperty("os.name").toLowerCase();
		if (lcOSName.startsWith("mac os x") == true) {
		    ...
		}
     * </pre></code>
     */
    public OSXAdapter(I_ManageStandardAppFunctions appGuts) throws Exception {
        String lcOSName = System.getProperty("os.name").toLowerCase();
        if (lcOSName.startsWith("mac os x") == false) {
            throw new Exception("Unsupported platform: " + lcOSName);
        }

        this.appGuts = appGuts;
        if (appShell == null) {
            appClass = Class.forName("com.apple.eawt.Application");
            appShell = appClass.newInstance();
        }
        appListenerInterfaceClass = Class.forName("com.apple.eawt.ApplicationListener");
        listener = Proxy.newProxyInstance(appClass.getClassLoader(), new Class[] { appListenerInterfaceClass }, this);

        Method addApplicationListenerMethod = appClass.getMethod("addApplicationListener", appListenerInterfaceClass);
        addApplicationListenerMethod.invoke(appShell, listener);

        for (Method m : appClass.getMethods()) {
            if (m.getName().equals("setEnabledPreferencesMenu")) {
                m.invoke(appShell, new Object[] { new Boolean(true) });
            }
        }
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object applicationEvent = args[0];
        Method setHandledMethod = null;
        for (Method m : applicationEvent.getClass().getMethods()) {
            if (m.getName().equals("setHandled")) {
                setHandledMethod = m;
            }
        }
        if (method.getName().equals("handleAbout")) {
            appGuts.about();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        if (method.getName().equals("handleOpenApplication")) {
            appGuts.openApplication();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        if (method.getName().equals("handleOpenFile")) {
            appGuts.openFile();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        if (method.getName().equals("handlePreferences")) {
            appGuts.preferences();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        if (method.getName().equals("handlePrintFile")) {
            appGuts.printFile();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        if (method.getName().equals("handleQuit")) {
            if (appGuts.quit()) {
                setHandledMethod.invoke(applicationEvent, new Object[] { true });
            } else {
                setHandledMethod.invoke(applicationEvent, new Object[] { false });
            }
        }
        if (method.getName().equals("handleReOpenApplication")) {
            appGuts.reOpenApplication();
            setHandledMethod.invoke(applicationEvent, new Object[] { true });
        }
        return null;
    }

}
