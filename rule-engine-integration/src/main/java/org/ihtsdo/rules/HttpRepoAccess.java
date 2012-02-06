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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.drools.util.codec.Base64;
import org.dwfa.ace.log.AceLog;

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

/**
 * The Class HttpRepoAccess.
 */
public class HttpRepoAccess {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		//readEnum();
		//readEnumJr();
		//putEnumJr();
		updateEnumJr();
	}

	/**
	 * Read enum.
	 */
	public static void readEnum() {
		try {
			URL                url; 
			URLConnection      urlConn; 
			DataInputStream    dis;

			url = new URL("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/org.ihtsdo.qa3/dr%20enumerations.enumeration");

			urlConn = url.openConnection(); 
			urlConn.setDoInput(true); 
			urlConn.setUseCaches(false);
			
			String authString = "name:pass";
			AceLog.getAppLog().info("auth string: " + authString);
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			AceLog.getAppLog().info("Base64 encoded auth string: " + authStringEnc);
			urlConn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			dis = new DataInputStream(urlConn.getInputStream()); 
			String s; 
			while ((s = dis.readLine()) != null) { 
				AceLog.getAppLog().info(s); 
			} 
			dis.close(); 
		} catch (MalformedURLException mue) {
			AceLog.getAppLog().alertAndLogException(mue);
		} catch (IOException ioe) {
			AceLog.getAppLog().alertAndLogException(ioe);
		} 
	} 
	
	/**
	 * Read enum jr.
	 */
	public static void readEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/legacy%20enums.enumeration");
			for (DavResource res : resources)
			{
			     AceLog.getAppLog().info(res.toString()); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 AceLog.getAppLog().info(line);
			    	 }
			}
			
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
	
	/**
	 * Put enum jr.
	 */
	public static void putEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			sardine.put("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration", "prueba".getBytes());
			AceLog.getAppLog().info("Put finished...");
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			for (DavResource res : resources)
			{
			     AceLog.getAppLog().info(res.toString()); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 AceLog.getAppLog().info(line);
			    	 }
			}
			
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}
	
	/**
	 * Update enum jr.
	 */
	public static void updateEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			String current = "";
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			StringWriter writer = new StringWriter();
			PrintWriter pwriter = new PrintWriter(writer);
			for (DavResource res : resources)
			{
			     AceLog.getAppLog().info(res.toString()); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 pwriter.println(line);
			    	 AceLog.getAppLog().info(line);
			    	 current = line;
			    	 }
			}
			pwriter.println("Otra linea");
			//sardine.delete("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			//AceLog.getAppLog().info("Delete finished...");
			sardine.put("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration", writer.toString().getBytes());
			AceLog.getAppLog().info("Put finished...");
			resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			for (DavResource res : resources)
			{
			     AceLog.getAppLog().info(res.toString()); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 AceLog.getAppLog().info(line);
			    	 }
			}
			
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

}
