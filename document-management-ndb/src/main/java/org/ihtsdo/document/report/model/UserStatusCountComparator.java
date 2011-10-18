package org.ihtsdo.document.report.model;

import java.util.Comparator;

public class UserStatusCountComparator implements Comparator<UserStatusCount> {

	@Override
	public int compare(UserStatusCount e1, UserStatusCount e2) {
		int result = 0;
		result = e1.getDate().compareTo(e2.getDate());
		if (result != 0) {
			return result;
		}else{
			result = e1.getUserName().compareTo(e2.getUserName());
			if(result != 0){
				return result;
			}else{
				result = e1.getStatus().compareTo(e2.getStatus());
			}
		}
		return result;
	}

}
