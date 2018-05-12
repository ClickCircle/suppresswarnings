/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.alone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <br/> Now
 * <br/>--|-----------------|------>
 * <br/>Time    Left    TenYears
 * <br/>
 * <br/><code>Now  = System.currentTimeMillis()</code>
 * <br/><code>Left = TenYears - Now</code>
 * <br/><code>Time = TenYears - Left</code>
 * 
 * @author lijiaming
 *
 */
@SuppressWarnings({"all", "2028/04/07 00:00:00.000", "1838649600000"})
public class TimeUtil {
	public static final String TimeFormat = "yyyy/MM/dd HH:mm:ss";
	public static final String TenYears = "2028/04/07 00:00:00";
	public static final long CountDown = 1838649600000L;
	
	public static void main(String[] args) throws ParseException {
		long now = getNowLeft();
		System.out.println(System.currentTimeMillis());
		System.out.println(now);
		System.out.println(getTimeString(now));
	}
	public static long getNowLeft() {
		return getTimeLeft(System.currentTimeMillis());
	}
	public static long getTimeLeft(long timeMillis) {
		return CountDown - timeMillis;
	}
	public static long getRealTime(long leftMillis) {
		return CountDown - leftMillis;
	}
	public static String getTimeString(long timeMillis) {
		return new SimpleDateFormat(TimeFormat).format(new Date(timeMillis));
	}
	public static String getTimeStringByLeft(long leftMillis) {
		return getTimeString(getRealTime(leftMillis));
	}
}