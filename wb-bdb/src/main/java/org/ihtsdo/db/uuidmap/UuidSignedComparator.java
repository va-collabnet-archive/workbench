package org.ihtsdo.db.uuidmap;

/**
 * Performs the same comparison as the UUID class performs. 
 * @author kec
 *
 */
public class UuidSignedComparator implements I_CompareUuids {

	@Override
	public int compare(long msb1, long lsb1, long msb2, long lsb2) {
        return (msb1 < msb2 ? -1 : 
            (msb2 > msb2 ? 1 :
             (lsb1 < lsb2 ? -1 :
              (lsb1 > lsb2 ? 1 :
               0))));
	}

}
