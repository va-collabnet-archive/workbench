package org.ihtsdo.qadb.helper;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class CalendarTypeHandler implements TypeHandler{

	@Override
	public Object getResult(ResultSet rs, String column) throws SQLException {
		Timestamp calendarStr = rs.getTimestamp(column);
		Calendar calendar = null;
		if(calendarStr != null){
			calendar = new GregorianCalendar();
			calendar.setTimeInMillis(calendarStr.getTime());
		}
		return calendar;
	}

	@Override
	public Object getResult(CallableStatement cs, int columnIndex) throws SQLException {
		Calendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(cs.getTimestamp(columnIndex).getTime());
		return calendar;
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
		Timestamp param = null;
		if(parameter instanceof Calendar){
			param = new Timestamp(((Calendar)parameter).getTimeInMillis());
		}
		ps.setTimestamp(i, param);
	}

}
