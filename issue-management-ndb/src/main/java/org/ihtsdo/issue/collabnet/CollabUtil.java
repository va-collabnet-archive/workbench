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
   private ICollabNetSoap m_sfSoap;

   /* the session id returned from a previous call to login() */
   private String m_sessionId;



   /**
    * a simple constructor built around the URL of the TeamForge server
    * <p>
    * @param serverUrl The fully qualified URL of the TeamForge server
    *                  instance
    * @param sessionId A session identifier returned from a prior call
    *                  to login()
    */
   public CollabUtil(String serverUrl, String sessionId)
   {

      m_sessionId = sessionId;

      m_sfSoap = (ICollabNetSoap) ClientSoapStubFactory.getSoapStub(
                                      ICollabNetSoap.class, serverUrl);
   }


   public CommentSoapList getCommentsList(String objectId) throws RemoteException{
	   return m_sfSoap.getCommentList(m_sessionId,objectId);
   }
}