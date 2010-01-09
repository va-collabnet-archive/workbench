package org.ihtsdo.db.bdb;

/**
 * 
 * @author kec
 *
 */
public class NidCNidMap {
	
	private static final int NID_CNID_MAP_SIZE = 50000;
	private int[][] nidCNidMaps;
	int maxId = Integer.MIN_VALUE;
	
	
	public NidCNidMap(int nextId) {
		super();
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        nidCNidMaps = new int[nidCidMapCount][];
        for (int index = 0; index < nidCidMapCount; index++) {
        	nidCNidMaps[index] = new int[NID_CNID_MAP_SIZE];
        }
        maxId = (nidCNidMaps.length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
	}

	public void ensureCapacity(int nextId) {
        int nidCidMapCount = ((nextId - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE) + 1;
        if (nidCidMapCount > nidCNidMaps.length) {
            int[][] newNidCidMaps = new int[nidCidMapCount][];
        	for (int i = 0; i < nidCNidMaps.length; i++) {
        		newNidCidMaps[i] = nidCNidMaps[i];
        	}
        	newNidCidMaps[nidCNidMaps.length] = new int[NID_CNID_MAP_SIZE];
        	nidCNidMaps = newNidCidMaps;
        }
        maxId = (nidCNidMaps.length *  NID_CNID_MAP_SIZE) - Integer.MIN_VALUE; 
	}

	public int getCNid(int nid) {
		if (maxId < nid) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " nid: " + nid);
		}
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		return nidCNidMaps[mapIndex][indexInMap];
	}
	
	public void setCidForNid(int cNid, int nid) {
		if (maxId < cNid || maxId < nid) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " cNid: " + cNid + " nid: " + nid);
		}
		int mapIndex = (nid  - Integer.MIN_VALUE) / NID_CNID_MAP_SIZE;
		int indexInMap = (nid  - Integer.MIN_VALUE) % NID_CNID_MAP_SIZE;
		if (mapIndex < 0 || indexInMap < 0) {
			throw new ArrayIndexOutOfBoundsException(" maxId: " + maxId + " cNid: " + cNid + " nid: " + nid  + 
					" mapIndex: " + mapIndex + " indexInMap: " + indexInMap);
		}
		nidCNidMaps[mapIndex][indexInMap] = cNid;
	}

}
