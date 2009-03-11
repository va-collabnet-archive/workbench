package org.dwfa.util;

public class EqualUtil {
	public static boolean equals(Object thisValue, Object anotherValue) {
		if ((thisValue == anotherValue) == false) {
			if (thisValue == null) {
				return false;
			}
			if (thisValue.equals(anotherValue) == false) {
				return false;
			}
		}
		return true;
	}
	public static boolean unequal(Object thisValue, Object anotherValue) {
		return equals(thisValue, anotherValue) == false;
	}

}
