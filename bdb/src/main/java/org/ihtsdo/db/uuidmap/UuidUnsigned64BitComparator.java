package org.ihtsdo.db.uuidmap;

public class UuidUnsigned64BitComparator implements I_CompareUuids {

	/**
	 * This algorithm performs unsigned 64 bit comparison of the 
	 * msb and lsb of 2 uuids. This method is based on the following routine:
	 * <code>
	 * public static boolean isLessThanUnsigned(long n1, long n2) {
  			return (n1 < n2) ^ ((n1 < 0) != (n2 < 0));
		}
		</code>
		see: http://www.javamex.com/java_equivalents/unsigned_arithmetic.shtml
	 */
	@Override
	public int compare(long msb1, long lsb1, long msb2, long lsb2) {
	
		if (msb1 == msb2) {
			if (lsb1 == lsb2) {
				return 0;
			}
			if ((lsb1 < lsb2) ^ ((lsb1 < 0) != (lsb2 < 0))) {
				return -1;
			}
			return 1;
		}
		if ((msb1 < msb2) ^ ((msb1 < 0) != (msb2 < 0))) {
			return -1;
		}
		return 1;
	}
}
