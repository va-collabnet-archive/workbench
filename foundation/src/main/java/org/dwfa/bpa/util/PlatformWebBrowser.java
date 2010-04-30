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
 * Created on Jan 17, 2006
 */
package org.dwfa.bpa.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.dwfa.bpa.htmlbrowser.JavaBrowser;

/**
 * This class provides a method to access a "native" web browser on the host
 * platform. If it cannot access a native web browser, it will create a
 * swing-based web browser that is cross platform, but very poor at rendering
 * content.
 * 
 * @author kec
 * 
 */
public class PlatformWebBrowser {
    private interface I_OpenURL {
        public boolean openURL(URL url);
    }

    private static boolean tryNativeBrowser = true;

    private static class OpenMacWebBrowser implements I_OpenURL {
        Class<?> fileManagerClass;

        Object fileManagerObj;

        Class<?>[] defArgs = { String.class };

        Method openUrlMethod;

        public OpenMacWebBrowser() throws Exception {
            fileManagerClass = Class.forName("com.apple.eio.FileManager");
            fileManagerObj = fileManagerClass.getConstructor(new Class[] {}).newInstance();
            openUrlMethod = fileManagerClass.getDeclaredMethod("openURL", defArgs);

        }

        public boolean openURL(URL url) {
            Object[] args = { url.toString() };
            try {
                openUrlMethod.invoke(fileManagerObj, args);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Another option is to consider exec functions. "start file.html"
     * opens the file in the default web browser on Windows XP.
     * 
     * @author kec
     * 
     */
    private static class DesktopOpener implements I_OpenURL {

        Object desktopObject;

        Method browseMethod;

        Method openMethod;

        public DesktopOpener() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
                IllegalArgumentException, IllegalAccessException, InvocationTargetException {

            Class<?> desktopClass = Class.forName("java.awt.Desktop");

            Method getDesktopMethod = desktopClass.getMethod("getDesktop", new Class[] {});
            desktopObject = getDesktopMethod.invoke(null);

            browseMethod = desktopObject.getClass().getMethod("browse", new Class[] { Class.forName("java.net.URI") });

            openMethod = desktopObject.getClass().getMethod("open", new Class[] { Class.forName("java.io.File") });
        }

        public boolean openURL(URL url) {

            try {
                String externalForm = url.toExternalForm();
                if (url.getProtocol().toLowerCase().equals("file") && url.getFile().toLowerCase().endsWith(".pdf")) {
                    openMethod.invoke(desktopObject, new Object[] { new File(url.getFile().replace("%20", " ")) });
                } else {
                    externalForm = externalForm.replace(" ", "%20");
                    URL urlForUri = new URL(externalForm);
                    browseMethod.invoke(desktopObject, new Object[] { urlForUri.toURI() });
                }
                return true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return false;
        }
    }

    private static class GenericOpener implements I_OpenURL {
        JavaBrowser browser;

        public GenericOpener() throws Exception {
            super();
            browser = new JavaBrowser();
        }

        public boolean openURL(URL url) {
            try {
                browser.displayPage(url.toString());
                browser.setVisible(true);
                browser.toFront();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

    }

    public static boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));

    private static I_OpenURL opener;

    public static boolean openURL(URL url) throws Exception {
        if (tryNativeBrowser) {
            try {
                if (opener == null) {
                    opener = new DesktopOpener();
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    if (MAC_OS_X) {
                        opener = new OpenMacWebBrowser();
                    } else {
                        opener = new GenericOpener();
                    }
                } catch (Exception e2) {
                    opener = new GenericOpener();
                }
            }

        } else {
            if (opener == null) {
                opener = new GenericOpener();
            }
        }
        return opener.openURL(url);
    }

    public static void main(String[] args) {
        try {
            PlatformWebBrowser.openURL(new URL("http://www.informatics.com"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
