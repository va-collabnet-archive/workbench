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

import com.googlecode.sardine.DavResource;
import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

public class HttpRepoAccess {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//readEnum();
		//readEnumJr();
		//putEnumJr();
		updateEnumJr();
	}

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
			System.out.println("auth string: " + authString);
			byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
			String authStringEnc = new String(authEncBytes);
			System.out.println("Base64 encoded auth string: " + authStringEnc);
			urlConn.setRequestProperty("Authorization", "Basic " + authStringEnc);
			dis = new DataInputStream(urlConn.getInputStream()); 
			String s; 
			while ((s = dis.readLine()) != null) { 
				System.out.println(s); 
			} 
			dis.close(); 
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} 
	} 
	
	public static void readEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/legacy%20enums.enumeration");
			for (DavResource res : resources)
			{
			     System.out.println(res); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 System.out.println(line);
			    	 }
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void putEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			sardine.put("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration", "prueba".getBytes());
			System.out.println("Put finished...");
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			for (DavResource res : resources)
			{
			     System.out.println(res); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 System.out.println(line);
			    	 }
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void updateEnumJr() {
		try {
			Sardine sardine = SardineFactory.begin("username", "password");
			String current = "";
			List<DavResource> resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			StringWriter writer = new StringWriter();
			PrintWriter pwriter = new PrintWriter(writer);
			for (DavResource res : resources)
			{
			     System.out.println(res); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 pwriter.println(line);
			    	 System.out.println(line);
			    	 current = line;
			    	 }
			}
			pwriter.println("Otra linea");
			//sardine.delete("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			//System.out.println("Delete finished...");
			sardine.put("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration", writer.toString().getBytes());
			System.out.println("Put finished...");
			resources = sardine.getResources("http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/webdav/packages/qa4/insertedEnum.enumeration");
			for (DavResource res : resources)
			{
			     System.out.println(res); // calls the .toString() method.
			     BufferedReader reader = new BufferedReader(new InputStreamReader(sardine.getInputStream(res.getAbsoluteUrl()), "UTF-8"));
			     String line;
			     while ((line = reader.readLine()) != null) {
			    	 System.out.println(line);
			    	 }
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
