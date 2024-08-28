package com.distocraft.dc5000.etl.gui.common;

import javax.servlet.http.HttpServletRequest;


/**
 * Security helper class for add/edit/delete role/user/perm. group.
 * 
 * @author ekroklm
 *
 */
public class SecurityAdminHelper {

    /**
     * Helper: returns value of 'paramName' or empty string if param is null.
     * 
     * @param request           request containing `paramName`
     * @param paramName         parameter to get
     * @return                  parameter value or empty string if paramName is NULL
     */
    public static final String getReqParam ( final HttpServletRequest request, final String paramName ) {
        String value = request.getParameter(paramName);
        if ( value == null ){
            value = "";
        }
        return value;
    }

    
//    public static final boolean isSuccessfulHandlerResult (final String result) {
//        return ( result != null && result.charAt(0) == '0' );
//    }
    
} //class
