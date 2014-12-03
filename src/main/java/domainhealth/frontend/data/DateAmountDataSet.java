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
package domainhealth.frontend.data;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * Set to hold a unique collection of data-time/amount pairs (typically, but 
 * not necessarily exclusively, used to identify an x,y point on a graph).
 */

@XmlRootElement
public class DateAmountDataSet {
	/**
	 * Add a date-time/amount pair to the set. 
	 * 
	 * @param dateTime Date-time to add
	 * @param amount Amount number to add
	 */
	public void add(Date dateTime, double amount) {
		items.add(new DateAmountDataItem(dateTime, amount));
	}
	
	/**
	 * Add a date-time/amount pair to the set
	 * 
	 * @param dateAmountItem Date-time/amount pair
	 */
	public void add(DateAmountDataItem dateAmountItem) {
		items.add(dateAmountItem);
	}

	/**
	 * Get an iterator for the set which is ordered by increasing date-time
	 * 
	 * @return Iterator of date-time/amount pairs
	 */
	public Iterator<DateAmountDataItem> getByIncreasingDateTime() {
		return items.iterator();
	}
	
	// Members
	private TreeSet<DateAmountDataItem> items = new TreeSet<DateAmountDataItem>(new Comparator<DateAmountDataItem>() {
		public int compare(DateAmountDataItem o1, DateAmountDataItem o2) {
			if (o1.getDateTime().before(o2.getDateTime())) {
				return -1;
			} else if (o1.getDateTime().after(o2.getDateTime())) {
				return +1;
			} else {
				return 0;				
			}
		}
	});

	@Override
	public String toString() {
		return "DateAmountDataSet{" +
				"items=" + items +
				'}';
	}
}
