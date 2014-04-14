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
package domainhealth.tests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import domainhealth.core.util.DateUtil;


import junit.framework.TestCase;

/**
 * Test-case class for: domainhealth.util.DateUtil
 * 
 * @see domainhealth.util.DateUtil
 */
public class DateUtilTest extends TestCase {
	/**
	 * Test method
	 */		
    public void testGetPreviousDay() {
    	try {
			Date date = DateUtil.getPreviousDay(format.parse("2008-01-01-01-01-01"));
			assertEquals(date, format.parse("2007-12-31-01-01-01"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	    
    public void testGetNextDay() {
    	try {
			Date date = DateUtil.getNextDay(format.parse("2008-01-01-01-01-01"));
			assertEquals(date, format.parse("2008-01-02-01-01-01"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	   
    public void testGetEarlierTimeInBounds() {
    	try {
			Date time = DateUtil.getEarlierTime(format.parse("2008-01-01-01-40-01"), 25);
			assertEquals(time, format.parse("2008-01-01-01-15-01"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	    
    public void testGetEarlierTimeOutBounds() {
    	try {
			Date time = DateUtil.getEarlierTime(format.parse("2008-01-01-00-00-01"), 25);
			assertEquals(time, format.parse("2008-01-01-00-00-00"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testGetLaterTimeInBounds() {
    	try {
			Date time = DateUtil.getLaterTime(format.parse("2008-01-01-01-40-01"), 25);
			assertEquals(time, format.parse("2008-01-01-02-05-01"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testGetLaterTimeOutBounds() {
    	try {
			Date time = DateUtil.getLaterTime(format.parse("2008-01-01-23-50-01"), 25);
			assertEquals(time, format.parse("2008-01-01-23-59-59"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }
    
	/**
	 * Test method
	 */	
    public void testGetStartTimeOfDay() {
    	try {
			Date time = DateUtil.getStartTimeOfDay(format.parse("2008-11-26-23-40-01"));
			assertEquals(time, format.parse("2008-11-26-00-00-00"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testGetEndTimeOfDayOrNowInBounds() {
    	try {
			Date time = DateUtil.getEndTimeOfDayOrNow(format.parse("2008-01-01-12-40-01"));
			assertEquals(time, format.parse("2008-01-01-23-59-59"));
    	} catch (ParseException e) {
			fail(e.getMessage());
		}
    }

	/**
	 * Test method
	 */	
    public void testGetEndTimeOfDayOrNowOutBounds() {
		Date now = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		Date time = DateUtil.getEndTimeOfDayOrNow(calendar.getTime());
		assertEquals(time, now);
    }

	/**
	 * Test method
	 */	
    public void testGetFullDurationTime() {
		Date now = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(now);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		Date time = DateUtil.getFullDurationTime(calendar.getTime(), 0);
		assertTrue(time.compareTo(now) < 0);
		time = DateUtil.getFullDurationTime(calendar.getTime(), 1439);
		assertTrue(time.compareTo(now) > 0);
    }

    // Constants
    private final static DateFormat format = new SimpleDateFormat(DateUtil.DATETIME_PARAM_FORMAT);
}