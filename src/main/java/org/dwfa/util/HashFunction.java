package org.dwfa.util;

public class HashFunction {
	public static int hashCode(int[] parts) {
		int hash = 0;
		int len = parts.length;
		for (int i = 0; i < len; i++) {
			hash <<= 1;
			if (hash < 0) {
				hash |= 1;
			}
			hash ^= parts[i];
		}
		return hash;
	}

}
