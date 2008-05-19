package org.dwfa.ace.refset;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.dwfa.ace.api.I_TermFactory;

public class MemberRefsetChangesetWriter {

	private UUID editPathUUID; 

	private HashMap<Integer, DataOutputStream> refsetStreams = new HashMap<Integer, DataOutputStream>();    
	
	private String baseDir;
	
	private I_TermFactory termFactory;
	
	public MemberRefsetChangesetWriter(String baseDir, I_TermFactory termFactory, UUID editPathUUID) {
		this.baseDir = baseDir;
		this.termFactory = termFactory;
		this.editPathUUID = editPathUUID;
	}
	
	public void addToRefset(int conceptId, int membershipTypeId, int refsetId, int statusId) throws Exception {
		DataOutputStream outputStream;
		if (!refsetStreams.containsKey(refsetId)) {
			String postfix = "";
			int count = 1;
			File newRefsetFile = new File(baseDir, refsetId + postfix + ".cmrscs");
			while (newRefsetFile.exists()) {
				postfix = "." + count;
				newRefsetFile = new File(baseDir, refsetId + postfix + ".cmrscs");
			}
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(newRefsetFile)));
			
			// commit time
			outputStream.writeLong(System.currentTimeMillis());
			// path 
			writeUuid(outputStream, editPathUUID);
			// refset 
			writeUuid(outputStream, termFactory.getUids(refsetId));
			
			refsetStreams.put(refsetId, outputStream);
		} else {
			outputStream = refsetStreams.get(refsetId);
		}
		
		writeUuid(outputStream, UUID.randomUUID());
		writeUuid(outputStream, termFactory.getUids(conceptId));
		writeUuid(outputStream, termFactory.getUids(statusId));
		writeUuid(outputStream, termFactory.getUids(membershipTypeId));
		
	}
	
	public static void writeUuid(DataOutputStream dos, Collection<UUID> uuids) throws IOException {
		writeUuid(dos, uuids.iterator().next());
	}
	
    public static void writeUuid(DataOutputStream dos, UUID uuid) throws IOException {
    	dos.writeLong(uuid.getMostSignificantBits());
    	dos.writeLong(uuid.getLeastSignificantBits());
    }
    
    public void close() throws Exception {
		UUID endUid = new UUID(0, 0);

		for (DataOutputStream dos : refsetStreams.values()) {
			writeUuid(dos, endUid);
			dos.close();
		}
    }
	
}
