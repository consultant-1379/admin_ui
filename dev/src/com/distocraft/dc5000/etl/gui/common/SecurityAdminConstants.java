package com.distocraft.dc5000.etl.gui.common;

/**
 * Common constants for Security Administration (role/user/permission)
 * @author ekroklm
 *
 */
public class SecurityAdminConstants {

    /**
     * Action parameter in GET
     */
    public static final String ACTION_PARAM            = "action";

    
    /**
     * VM context variable: notification in footer
     */
    public static final String NOTIFICATION_VAR        = "notification";

    /**
     * VM context variable: error message for edit/add operations
     */
    public static final String ERROR_MESSAGE_VAR        = "errorMessage";
    
    
    /**
     * VM context variable: logged user name
     */
    public static final String LOGGED_USER_NAME_VAR    = "theuser";
    
    
    /**
     * VM context variable: set to 'yes' if adding an entity; false if 
     */
    public static final String ADD_MODE_VAR            = "addMode";

    
    /**
     * Contant for 'yes'; used by ADD_MODE_VAR
     * 
     * @see ADD_MODE_VAR
     */
    public static final String YES                     = "yes";
    
    
    /**
     * Zero (0) as String
     */
    public static final String ZERO                    = "0";    
    
    
    /**
     * Servlet session variable name for LoginForm bean
     */
    public static final String LOGIN_FORM_NAME         = "loginForm";


}
