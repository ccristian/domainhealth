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
package domainhealth.frontend.tags;

import java.io.IOException;

import javax.servlet.jsp.JspContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import static domainhealth.frontend.display.GraphScopedAttributeUtils.*;

/**
 * DomainHealth JSP tag library tag to enable tags like 
 * "&lt;dh:link-ctx-params direction="firstdate"/&gt;" to be included in JSP page.
 * This generates the context parameters for URL links used in the DomainHealth 
 * web-application, to keep track of what date-time, scope, duration and 
 * navigation direction is currently being selected by the user of the web 
 * interface user.  Generates HTML output like: 
 *  "datetime=2011-10-04-12-27-12&duration=180&scope=ALL&direction=firstdate"
 */
public class LinkContextParamsTag extends SimpleTagSupport {
	/**
	 * Generates the HTML output text to replace the tag in the web page with
	 * output like: 
	 * datetime=2011-10-04-12-27-12&duration=180&scope=ALL&direction=firstdate
	 * Enables a tag attribute "direction" to be optionally defined to 
	 * override the direction of "current".
	 * 
	 * @throws IOException Indicates problem generated HTML output
	 * @throws JspException Indicates problem generated HTML output
	 */	
	public void doTag() throws JspException, IOException {
		JspContext ctx = getJspContext();
		ctx.getOut().print(String.format(PARAM_LINE_TEMPLATE, 
				ctx.findAttribute(DATETIME_PARAM), 
				ctx.findAttribute(DURATION_MINS_PARAM), 
				ctx.findAttribute(SCOPE_PARAM), 
				(direction == null) ? DIRECTION_VAL_DEFAULT : direction));	
	}
	
	/**
	 * Gets the overridden direction.
	 * 
	 * @return Null of not overridden or value of "previousdate", "firstdate", "current", "nextdate", "lastdate", "firsttime", "previoustime", "nexttime" or "lasttime" 
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * Overriddes the default direction of 'current'.
	 * 
	 * @param direction Value of "previousdate", "firstdate", "current", "nextdate", "lastdate", "firsttime", "previoustime", "nexttime" or "lasttime"
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
		
	// Members
	private static final String PARAM_LINE_TEMPLATE = "datetime=%s&duration=%s&scope=%s&direction=%s";
	private String direction = null;
}
