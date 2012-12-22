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

import com.collabnet.ce.soap50.webservices.ClientSoapStubFactory;
import com.collabnet.ce.soap50.webservices.cemain.CommentSoapList;
import com.collabnet.ce.soap50.webservices.cemain.ICollabNetSoap;



/**
 * This example class works with  TeamForge documents including file
 * upload, etc.
 * <p>
 *
 */
public class CollabUtil
{

   /* the main teamforge interface */
   /** The m_sf soap. */
   private ICollabNetSoap m_sfSoap;

   /* the session id returned from a previous call to login() */
   /** The m_session id. */
   private String m_sessionId;



   /**
    * a simple constructor built around the URL of the TeamForge server
    * <p>.
    *
    * @param serverUrl The fully qualified URL of the TeamForge server
    * instance
    * @param sessionId A session identifier returned from a prior call
    * to login()
    */
   public CollabUtil(String serverUrl, String sessionId)
   {

      m_sessionId = sessionId;

      m_sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(
                                      ICollabNetSoap.class, serverUrl);
   }


   /**
    * Gets the comments list.
    *
    * @param objectId the object id
    * @return the comments list
    * @throws RemoteException the remote exception
    */
   public CommentSoapList getCommentsList(String objectId) throws RemoteException{
	   return m_sfSoap.getCommentList(m_sessionId,objectId);
   }
}