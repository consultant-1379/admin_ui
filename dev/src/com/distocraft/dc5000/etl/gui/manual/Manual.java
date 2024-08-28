
package com.distocraft.dc5000.etl.gui.manual;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.distocraft.dc5000.etl.gui.common.EtlguiServlet;
import com.distocraft.dc5000.etl.gui.dataflow.ViewRankBH;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Class is for redirecting manual page request to correct html-template.
 * 
 * @author Mark Stenback
 *
 */
public class Manual extends EtlguiServlet
{
	private static Log log = LogFactory.getLog(ViewRankBH.class);
	
	public Template doHandleRequest (HttpServletRequest request, HttpServletResponse response, Context ctx)
	{
		Template outty = null;
		
		String manPage = request.getParameter("page");
		
		if (manPage == null)
		{
			manPage = "index.html";
		}
		else
		{
			if (manPage.indexOf("-") == -1)
			{
				manPage += ".html";
			}
			else
			{
				String tmpStr = manPage.substring(0, manPage.indexOf("-")) + ".html#" + manPage.substring(manPage.indexOf("-")+1);
				
				manPage = tmpStr;
			}
		}
		
		try 
		{
			ctx.put("page", manPage);
			
			outty =  getTemplate("manual.vm");
		}
		
		catch (ParseErrorException pee)
		{
			log.error("Parse error for template " + pee);
	    }
		
		catch (ResourceNotFoundException rnfe)
		{
			log.error("Template not found " + rnfe);
	    }
		
		catch (Exception e)
		{
			log.error("Error " + e);
	    }
		
		return outty;
	}
}
