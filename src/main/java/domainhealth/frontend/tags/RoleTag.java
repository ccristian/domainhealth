package domainhealth.frontend.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import domainhealth.core.env.AppLog;

public class RoleTag extends BodyTagSupport
{
    //private final Log log = LogFactory.getLog(RoleTag.class);
    private String name;

    //*********************************************************************
    // Tag method
    public int doStartTag() throws JspException
    {
        if(this.name != null)
        {
            try
            {
                HttpServletRequest httpRequest = (HttpServletRequest)pageContext.getRequest();
                
                // The user has the good role so we need to evaluate the body
                if(httpRequest.isUserInRole(name))
                {
                    AppLog.getLogger().info("The user belong the the good group ...");
                    return EVAL_BODY_INCLUDE;
                }
                
                // The user is not allowed we we don't need to evaluate the body
                else
                {
                    AppLog.getLogger().info("The user doesn't belong the the good group ...");
                    return SKIP_BODY;
                }
            }
            catch(Exception ex)
            {
                throw new JspException(ex);
            }
        }
        return SKIP_BODY;
    }

    /**
     * 
     */
    public int doEndTag() throws JspException
    {
        return EVAL_PAGE;
    }

    /**
     * @param name the role name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    //*********************************************************************
    // Constructor and initialization
    public RoleTag()
    {
        super();
        init();
    }

    private void init()
    {
        this.name = "";
    }
    
    public void release()
    {
        init();
        super.release();
    }
}