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
package org.ihtsdo.issue.collabnet;


import java.rmi.RemoteException;
import java.util.logging.Level;

import org.dwfa.ace.log.AceLog;

import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory;
import com.collabnet.ce.soap50.webservices.cemain.ICollabNetSoap;



/**
 * Example class encapsulating a TeamForge connection.
 * This class covers up the details of logging in and out
 * of a TeamForge server.
 *
 */
public class TeamForgeConnection
{

   /* the TeamForge interface */
   /** The m_sf soap. */
   private ICollabNetSoap m_sfSoap;

   /* the username used to login to TeamForge */
   /** The m_user name. */
   private String m_userName;



   /**
    * a simple constructor built around the URL of the TeamForge server
    * <p>.
    *
    * @param serverUrl The fully qualified URL of the TeamForge server
    * instance
    */
   public TeamForgeConnection(String serverUrl)
	{

		m_sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(
					                       ICollabNetSoap.class, serverUrl);
	}



   /**
    * login to the TeamForge instance using the supplied username and password.
    * Upon successful login, the session ID is returned to the calling object.
    * <p>
    *
    * @param userName the user name
    * @param password the password
    * @return the session id established by the login() call
    * <p>
    * @throws RemoteException if any errors are returned by the login call, the
    * exception is returned to the calling program
    */
   public String login(String userName, String password)
		throws RemoteException
	{
      String sessionId;

      m_userName = userName;

      try
      {
         /*
            the login call takes a username and password and returns a session
            identifer to be passed into subsequent API calls.

            The userName and password are the same values used to login to
            TeamForge via the web interface
          */
         sessionId = m_sfSoap.login(userName, password);
      }
      catch (RemoteException e)
      {	
    	  if(AceLog.getAppLog().isLoggable(Level.FINE))
			AceLog.getAppLog().info((new StringBuilder()).append("unable to connect to TeamForge " ).append( e.getMessage()).toString());     
         throw new RemoteException("unable to connect to TeamForge",e);
      }
 	  if(AceLog.getAppLog().isLoggable(Level.FINE))
			AceLog.getAppLog().info((new StringBuilder()).append("successfully connected to TeamForge" ).toString());     
 
      return (sessionId);
   }


   /**
    * logoff closes the TeamForge connection specified by sessionId and
    * releases any resources held by the client API and the server
    * <p>.
    *
    * @param sessionId the identifier for the TeamForge session.  This ID
    * should have been previously returned by the login()
    * TeamForge call.
    * @throws RemoteException any errors returned by the logoff() call are
    * packaged and thrown to the calling object
    */
   public void logoff(String sessionId)
		throws RemoteException
	{
      try
      {
         m_sfSoap.logoff(m_userName, sessionId);
      }
      catch (RemoteException e)
      {
    	  if(AceLog.getAppLog().isLoggable(Level.FINE))
  			AceLog.getAppLog().info((new StringBuilder()).append("unable to disconnect from TeamForge " ).append( e.getMessage()).toString());     
          throw new RemoteException("unable to disconnect from TeamForge",e);
      }
      if(AceLog.getAppLog().isLoggable(Level.FINE))
			AceLog.getAppLog().info((new StringBuilder()).append("successfully disconnect to TeamForge " ).toString());     
    }

}
