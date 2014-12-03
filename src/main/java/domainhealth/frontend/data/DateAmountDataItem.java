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
import java.util.Date;

/**
 * Structure for holding a simple Date-Number pair (typically, but not 
 * necessarily exclusively, used to identify an x,y point on a graph).
 */
@XmlRootElement
public class DateAmountDataItem {
	/**
	 * Create date-number pair.
	 * 
	 * @param dateTime Date-time value
	 * @param amount Number value
	 */
    public DateAmountDataItem(Date dateTime, double amount) {
		this.dateTime = dateTime;
		this.amount = amount;
    }

    /**
     * Get stored date-time
     * 
     * @return Date-time
     */
	public Date getDateTime() {
		return this.dateTime;
	}

	/**
	 * Get stored number
	 * 
	 * @return Number amount
	 */
	public double getAmount() {
		return this.amount;
	}

	// Members
	private Date dateTime = null;
	private double amount = 0;
}
