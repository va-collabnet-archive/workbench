package org.dwfa.ace.url.tiuid;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

import com.sleepycat.je.DatabaseException;

public class BdbImageConnection extends URLConnection {

	I_ImageVersioned image;
	protected BdbImageConnection(URL url) {
		super(url);
	}

	  @Override
	  public void connect() throws IOException {
	    try {
	      String queryString = url.getQuery();
	      if (queryString.contains("$")) {
		    String[] parts = new String[2];
		    parts[0] = queryString.substring(0, queryString.indexOf('$') - 1);
		    parts[1] = queryString.substring(queryString.indexOf('$') + 1);;
	        String imageIdPart = parts[0];
	        Collection<UUID> conceptIdCollection = new ArrayList<UUID>();
	        if (parts.length > 1) {
		        String conceptIdPart = parts[1];
		        conceptIdCollection = collectionFromString(conceptIdPart);
	        } else {
	        	AceLog.getAppLog().warning("Query string with $ only has one part: " + queryString);
	        }
	        Collection<UUID> imageIdCollection = collectionFromString(imageIdPart);
	        //int conceptId = AceConfig.getVodb().getConceptNid(conceptIdCollection);
	        //int imageId = AceConfig.getVodb().getSubordinateUuidToNative(imageIdCollection, conceptId);
	        //image = AceConfig.getVodb().getImage(imageId, conceptId);
	        image = AceConfig.getVodb().getImage(AceConfig.getVodb().uuidToNative(imageIdCollection));
	        
	        if (image == null) {
	        	AceLog.getAppLog().warning("Image is null for queryString:" + queryString + 
	        			"\n imageIdCollection: " + imageIdCollection + 
	        			"\n conceptIdCollection: " + conceptIdCollection);
	        } else {
	        	AceLog.getAppLog().warning("Found image for queryString:" + queryString + 
	        			"\n imageIdCollection: " + imageIdCollection + 
	        			"\n conceptIdCollection: " + conceptIdCollection);
	        }
	      } else {
	        if (queryString.startsWith("[")) {
	          queryString = queryString.substring(1, queryString.length() - 2);
	          if (queryString.contains(",")) {
	            String[] ids = queryString.split(",");
	            for (String id : ids) {
	              try {
	                image = AceConfig.getVodb().getImage(UUID.fromString(id));
	                if (image != null) {
	                  return;
	                }
	              } catch (RuntimeException ex) {
	                AceLog.getAppLog().alertAndLogException(ex);
	              }
	            }
	          } else {
	            image = AceConfig.getVodb().getImage(UUID.fromString(queryString));
	          }
	        } else {
	          String id = url.getQuery();
	          if (id.length() == 36) {
	            image = AceConfig.getVodb().getImage(UUID.fromString(url.getQuery()));
	          } else {
	            image = AceConfig.getVodb().getImage(Integer.parseInt(id));
	          }
	        }
	      }
	    } catch (DatabaseException e) {
	      IOException ex = new IOException();
	      ex.initCause(e);
	      throw ex;
	    } catch (TerminologyException e) {
	      IOException ex = new IOException();
	      ex.initCause(e);
	      throw ex;
	    }
	  }


	  private Collection<UUID> collectionFromString(String idString) {
	    ArrayList<UUID> idList = new ArrayList<UUID>();
	    if (idString.startsWith("[")) {
	      idString = idString.substring(1, idString.length() - 2);
	      if (idString.contains(",")) {
	        String[] ids = idString.split(",");
	        for (String id : ids) {
	          idList.add(UUID.fromString(id));
	        }
	      } else {
	        idList.add(UUID.fromString(idString));
	      }
	    }
	    return idList;
	  }

	@Override
	public String getContentType() {
		if (image == null) {
			try {
				connect();
			} catch (IOException ex) {
				AceLog.getAppLog().alertAndLogException(ex);
				return null;
			}
		}
		
		return "image/" + image.getFormat(); //jpg png tiff ...
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (image == null) {
			connect();
		}
		ByteArrayInputStream stream = new ByteArrayInputStream(image.getImage());
		return stream;
	}

}
