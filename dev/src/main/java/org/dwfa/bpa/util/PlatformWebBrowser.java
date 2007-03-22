/*
 * Created on Jan 17, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

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

	private static boolean tryNativeBrowser = false;

	private static class OpenMacWebBrowser implements I_OpenURL {
		Class fileManagerClass;

		Object fileManagerObj;

		Class[] defArgs = { String.class };

		Method openUrlMethod;

		public OpenMacWebBrowser() throws Exception {
			fileManagerClass = Class.forName("com.apple.eio.FileManager");
			fileManagerObj = fileManagerClass.getConstructor(new Class[] {})
					.newInstance();
			openUrlMethod = fileManagerClass.getDeclaredMethod("openURL",
					defArgs);

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
	 * @author kec
	 *
	 */
	private static class JnlpOpener implements I_OpenURL {

		Object javaxJnlpBasicService;

		Method showDocumentMethod;

		public JnlpOpener() throws ClassNotFoundException, SecurityException,
				NoSuchMethodException, IllegalArgumentException,
				IllegalAccessException, InvocationTargetException {
			Class jnlpServiceManagerClass = Class
					.forName("javax.jnlp.ServiceManager");
			Method lookupMethod = jnlpServiceManagerClass.getMethod("lookup",
					new Class[] { String.class });
			javaxJnlpBasicService = lookupMethod.invoke(null,
					new Object[] { "javax.jnlp.BasicService" });
			showDocumentMethod = javaxJnlpBasicService.getClass().getMethod(
					"showDocument", new Class[] { URL.class });
		}

		public boolean openURL(URL url) {

			try {
				return (Boolean) showDocumentMethod.invoke(
						javaxJnlpBasicService, new Object[] { url });
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

	public static boolean MAC_OS_X = (System.getProperty("os.name")
			.toLowerCase().startsWith("mac os x"));

	private static I_OpenURL opener;

	public static boolean openURL(URL url) throws Exception {
		if (tryNativeBrowser) {
			try {
				if (opener == null) {
					if (MAC_OS_X) {
						opener = new OpenMacWebBrowser();
					} else {
						opener = new JnlpOpener();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				opener = new GenericOpener();
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
