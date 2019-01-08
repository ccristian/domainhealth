//Copyright (C) 2008-2013 Paul Done . All rights reserved.
//This file is part of the DomainHealth software distribution. Refer to the  
//file LICENSE in the root of the DomainHealth distribution.
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
//IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
//ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE 
//LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
//CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//POSSIBILITY OF SUCH DAMAGE.
package domainhealth.core.util;

import static java.util.Calendar.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Provides Date related utility functions mainly to enable first, last, next 
 * and previous dates to be retrieved relevant to a given date
 */
public class DateUtil {
	/**
	 * System format for a date string (without time element) used in 
	 * directory and file path names : "yyyy-MM-dd"
	 */
	public static final String DATE_PATH_FORMAT = "yyyy-MM-dd";

	/**
	 * System format for a date-time string used in HTTP parameters: 
	 * "yyyy-MM-dd-HH-mm-ss"
	 */
	public static final String DATETIME_PARAM_FORMAT = "yyyy-MM-dd-HH-mm-ss";

	/**
	 * Display format for a date (without time): "dd-MMM-yyyy"
	 */
	public static final String DISPLAY_DATE_FORMAT = "dd-MMM-yyyy";

	/**
	 * System format for a full date-time: "yyyy/MM/dd HH:mm:ss"
	 */
	public static final String DISPLAY_DATETIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

	/**
	 * Display format for day of week: "EEEE"
	 */
	public static final String DISPLAY_DAY_FORMAT = "EEEE";
	
	/**
	 * Display format for time (without date): "HH:mm"
	 */
	public static final String DISPLAY_TIME_FORMAT = "HH:mm";

	/**
	 * Get the previous day date-time relative to the provided current 
	 * date-time.
	 * 
	 * @param date The current date-time
	 * @return The previous day's date-time
	 */
	public static Date getPreviousDay(Date date) {
		return getNthPreviousDay(date, 1);
	}
	
	/**
	 * Get the date-time of Nth day previous to the provided current date-time.
	 * 
	 * @param date The current date-time
	 * @param numDaysPrevious The number of days back to go
	 * @return The date-time of the Nth day previous to the provide date-time
	 */
	public static Date getNthPreviousDay(Date date, int numDaysPrevious) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_YEAR, (0 - numDaysPrevious));
		return calendar.getTime();
	}

	/**
	 * Get the next day date-time relative to the provided day
	 * 
	 * @param date The current date
	 * @return The next day date-time
	 */
	public static Date getNextDay(Date date) {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(DAY_OF_YEAR, 1);
		return calendar.getTime();
	}

	/**
	 * Get an earlier time date-time of the given day by the specified number 
	 * of minutes or just midnight if given time is within the specified 
	 * minutes from midnight
	 * 
	 * @param date The current date
	 * @param minusMins The number of minutes to subtract
	 * @return The earlier time date-time
	 */
	public static Date getEarlierTime(Date date, int minusMins) {
		Calendar minCalendar = new GregorianCalendar();
		minCalendar.setTime(date);
		minCalendar.set(HOUR_OF_DAY, 0);
		minCalendar.set(MINUTE, 0);
		minCalendar.set(SECOND, 0);
		Date minDate = minCalendar.getTime();
		Calendar newCalendar = new GregorianCalendar();
		newCalendar.setTime(date);
		newCalendar.add(MINUTE, 0 - minusMins);
		Date newDate = newCalendar.getTime();
		
		if (newDate.compareTo(minDate) < 0) {
			newDate = minDate;
		}			
				
		return newDate;
	}

	/**
	 * Get a later time date-time of the given day by the specified number 
	 * of minutes or just 1 minute before midnight if given time is within the
	 * specified minutes from the end of the day
	 * 
	 * @param date The current date
	 * @param addMins The number of minutes to add
	 * @return The later time date-time
	 */
	public static Date getLaterTime(Date date, int addMins) {
		Calendar maxCalendar = new GregorianCalendar();
		maxCalendar.setTime(date);
		maxCalendar.set(HOUR_OF_DAY, 23);
		maxCalendar.set(MINUTE, 59);
		maxCalendar.set(SECOND, 59);
		Date maxDate = maxCalendar.getTime();
		Calendar newCalendar = new GregorianCalendar();
		newCalendar.setTime(date);
		newCalendar.add(MINUTE, addMins);
		Date newDate = newCalendar.getTime();
		
		if (newDate.compareTo(maxDate) > 0) {
			newDate = maxDate;
		}			
				
		return newDate;
	}

	/**
	 * If the duration minutes from the start of the day is greater than the 
	 * current time, then return a new time which includes the full duration
	 * window, otherwise just return the current date.
	 * 
	 * @param date The current date
	 * @param durationMins The duration in minutes, required to be shown in the graph
	 * @return The max date-time window to allow for the requested duration minutes
	 */
	public static Date getFullDurationTime(Date date, int durationMins) {
		Calendar fullDurationDayCalendar = new GregorianCalendar();
		fullDurationDayCalendar.setTime(date);
		fullDurationDayCalendar.set(HOUR_OF_DAY, 0);
		fullDurationDayCalendar.set(MINUTE, 0);
		fullDurationDayCalendar.set(SECOND, 0);
		fullDurationDayCalendar.add(MINUTE, Math.min(durationMins, (MAX_MINS_IN_DAY -1)));
		Date maxDate = fullDurationDayCalendar.getTime();

		if (maxDate.compareTo(date) > 0) {
			return maxDate;
		} else {
			return date;
		}
	}
	
	/**
	 * Get the start time date-time of the given day (ie. midnight)
	 * 
	 * @param date The date-time
	 * @return The date-time of midnight of the day
	 */
	public static Date getStartTimeOfDay(Date date) {		
		Calendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(date);
		currentCalendar.set(HOUR_OF_DAY, 0);
		currentCalendar.set(MINUTE, 0);
		currentCalendar.set(SECOND, 0);
		return currentCalendar.getTime();
	}

	/**
	 * Get the end time date-time of the given day which is 1 minute before 
	 * midnight, unless the specified day is actually today, when the current 
	 * date-time is returned
	 * 
	 * @param date The date-time
	 * @return The date-time for the sooner of the current time and the latest time of the specified day
	 */
	public static Date getEndTimeOfDayOrNow(Date date) {
		Date nowDate = new Date();
		Calendar nowCalendar = new GregorianCalendar();
		nowCalendar.setTime(nowDate);
		Calendar currentCalendar = new GregorianCalendar();
		currentCalendar.setTime(date);
		currentCalendar.set(HOUR_OF_DAY, 23);
		currentCalendar.set(MINUTE, 59);
		currentCalendar.set(SECOND, 59);
		date = currentCalendar.getTime();
		
		if (date.compareTo(nowDate) > 0) {
			date = nowDate;
		}			
		
		return date;
	}
	
	// Constants
	private final static int MAX_MINS_IN_DAY = 60 * 24;
}
