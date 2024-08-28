package com.distocraft.dc5000.etl.gui.systemmonitor;

import java.util.List;
import java.util.Map;

public class ConfirmFeatures {
	
    private List<String> features;
    
    private String dcuser_pwd;
    
    private String default_path;
    
    private String configured_path;
    
    private String root_pwd;
   
    private Map<String, Map<String, Map<String, String>>> installed_feat_selected_add_size;
    
    private Map<String, Map<String, Map<String, String>>>  new_feat_list;
    
    private List<String> final_backlist;
    
    private Map<String, Map<String, Map<String, String>>> installed_features;
    
    
    /**
	 * Private constructor
	 */
	private ConfirmFeatures(){
	}
	  
	
    /**
     *  Instance creation
     */
    private static ConfirmFeatures confirmfeatures_instance = null;
    
    
    /**
     * Gets the singleton instance instance of this class
     * 
     * @return The unique instance of this class
     */
    public static ConfirmFeatures getInst()
    {
        if (confirmfeatures_instance == null)
        	confirmfeatures_instance = new ConfirmFeatures();
 
        return confirmfeatures_instance;
    }
	
    public List<String> getFeatures() {
        return features;
    }

    public List<String> setFeatures(List<String> features) {
        return this.features = features;
    }
    
    public String getDcuser_pwd() {
		return dcuser_pwd;
	}

	public String setDcuser_pwd(String dcuser_pwd) {
		return this.dcuser_pwd = dcuser_pwd;
	}

	public String getDefault_path() {
		return default_path;
	}

	public String setDefault_path(String default_path) {
		return this.default_path = default_path;
	}

	public String getConfigured_path() {
		return configured_path;
	}

	public String setConfigured_path(String configured_path) {
		return this.configured_path = configured_path;
	}

	public String getRoot_pwd() {
		return root_pwd;
	}

	public String setRoot_pwd(String root_pwd) {
		return this.root_pwd = root_pwd;
	}

	public Map<String, Map<String, Map<String, String>>> getInstalled_features() {
		return installed_features;
	}

	public Map<String, Map<String, Map<String, String>>> setInstalled_features(
			Map<String, Map<String, Map<String, String>>> installed_features) {
		return this.installed_features = installed_features;
	}

	public List<String> getFinal_backlist() {
		return final_backlist;
	}

	public List<String> setFinal_backlist(List<String> final_backlist) {
		return this.final_backlist = final_backlist;
	}

	public Map<String, Map<String, Map<String, String>>> getNew_feat_list() {
		return new_feat_list;
	}

	public Map<String, Map<String, Map<String, String>>> setNew_feat_list(
			Map<String, Map<String, Map<String, String>>> new_feat_list) {
		return this.new_feat_list = new_feat_list;
	}

	public Map<String, Map<String, Map<String, String>>> getInstalled_feat_selected_add_size() {
		return installed_feat_selected_add_size;
	}

	public Map<String, Map<String, Map<String, String>>> setInstalled_feat_selected_add_size(
			Map<String, Map<String, Map<String, String>>> installed_feat_selected_add_size) {
		return this.installed_feat_selected_add_size = installed_feat_selected_add_size;
	}
	
}
