package com.haier.common.utils.Calendar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {
	
	/**
	 * <p>Title: getTomorrowZero</p>  
	 * Description: <pre>当前时间后一天时间零点</pre>  
	 */
	public static Date getTomorrowZero() {
		Calendar calendar = Calendar. getInstance();
		calendar.setTime(new Date());
		calendar.set(Calendar. HOUR_OF_DAY, 0);
		calendar.set(Calendar. MINUTE, 0);
		calendar.set(Calendar. SECOND, 0);
		calendar.set(Calendar. MILLISECOND, 0);
		calendar.add(Calendar. DAY_OF_MONTH, 1);
		Date date = new Date();
        date = calendar.getTime();
        return date;
	}
	
	public static Long getdayDiff(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Calendar setCal = Calendar.getInstance();
		setCal.setTime(date);
		setCal.set(Calendar.HOUR_OF_DAY, 0);
		setCal.set(Calendar.MINUTE, 0);
		setCal.set(Calendar.SECOND, 0);
		setCal.set(Calendar.MILLISECOND, 0);
		  
		long dayDiff =(setCal.getTimeInMillis()-cal.getTimeInMillis())/(1000*60*60*24);
		System.out.println(dayDiff);
		return dayDiff;
	}
	
	public static void main(String[] args) throws ParseException {
		String endDate = "2018-05-20";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Long lll = getdayDiff(sdf.parse(endDate));
		System.out.println(lll);
	}

}
