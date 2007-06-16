package org.dwfa.mojo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.dwfa.vodb.bind.ThinVersionHelper;

public class SimpleUniversalAcePath {
	
	private static SimpleDateFormat dateParser = new SimpleDateFormat();
	private String uuidStr;
	private String timeStr;
	
	public SimpleUniversalAcePath(String uuidStr, String timeStr) {
		super();
		this.uuidStr = uuidStr;
		this.timeStr = timeStr;
	}
	
	public SimpleUniversalAcePath() {
		super();
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	public String getUuidStr() {
		return uuidStr;
	}
	public void setUuidStr(String uuidStr) {
		this.uuidStr = uuidStr;
	}
	
	public int getTime() throws ParseException {
		if (timeStr == null) {
			return Integer.MAX_VALUE;
		}
		Date date = dateParser.parse(timeStr);
		return ThinVersionHelper.convert(date.getTime());
	}

	public Collection<UUID> getPathId() {
		Collection<UUID> list = new ArrayList<UUID>();
		list.add(UUID.fromString(uuidStr));
		return list;
	}

}
