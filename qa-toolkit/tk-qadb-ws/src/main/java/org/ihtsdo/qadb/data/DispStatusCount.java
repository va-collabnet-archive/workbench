package org.ihtsdo.qadb.data;

public class DispStatusCount {

	private String dispStatus;
	private Integer statusCount;

	public String getDispStatus() {
		return dispStatus;
	}

	public void setDispStatus(String dispStatus) {
		this.dispStatus = dispStatus;
	}

	public Integer getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(Integer statusCount) {
		this.statusCount = statusCount;
	}

	@Override
	public String toString() {
		return "DispStatusCount [dispStatus=" + dispStatus + ", statusCount=" + statusCount + "]";
	}
	
	

}
