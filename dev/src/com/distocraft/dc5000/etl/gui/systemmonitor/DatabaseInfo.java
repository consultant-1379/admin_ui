package com.distocraft.dc5000.etl.gui.systemmonitor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Copyright &copy; Distocraft ltd. All rights reserved.<br>
 * Subclass com.distocraft.dc5000.etl.gui.systemmonitor.MonitorInformation . Adds few more actions based on database information.
 * Value class for database info.
 *  
 * @author Jani Vesterinen
 */
public class DatabaseInfo extends MonitorInformation{
  private static final String ENIQ_REP = "ENIQ REP";
  private static final String SQL_ANYWHERE = "SQL Anywhere";
  private static final String IQ = "IQ";
  private final String name;
  private StringBuffer info = new StringBuffer();
  private String size = "";
  private String freeSpace = "";
  private String totalsize = "";
  private double totalSizeMb = 0;
  private double usedSpaceMb = 0;
  private float otherVersionSize = 0;
  private int otherVersionMultip = 0;
  private String backuptime = "";  
  private boolean isDBFull = false;
  
  private final Log log = LogFactory.getLog(this.getClass());
  
  /**
   * Constructs object with wanted name.
   * @param name
   */
  public DatabaseInfo(final String name){
    this.name = name;
  }
  
  /**
   * Gets database name.
   * @return dbname
   */
  public String getName(){
    return name;
  }
  
  public String getProduct() {
    if (name.equals(ENIQ_REP)) {
      return SQL_ANYWHERE;
    }
    return IQ;
  }
  
  /**
   * Gets free space as String.
   * @return free db space
   */  
  public String getFreeSpace(){
    return freeSpace;
  }
  
  /**
   * Sets free space as String.
   * @param s
   */
  public void setFreeSpace(final String s){
    freeSpace = s;
  }
  
  /**
   * Gets all the information considering certain database.
   * @return all info
   */
  public String getAllInfo(){
    return info.toString();
  }

  /**
   * Sets info text. Text is appended, so it is possible to add one line at a time.
   * @param string info text
   * @param override if this is true, all previous texts are overriden.
   */
  public void setAllInfo(final String string, final boolean override){
    if (override){
      info = new StringBuffer();
      info.append(string);
    }else{
      info.append(string);
      info.append("<br />");
    }
  }
  
  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.systemmonitor.MonitorInformation#getSize()
   */
  public String getSize() {
    return size;
  }

  /* (non-Javadoc)
   * @see com.distocraft.dc5000.etl.gui.systemmonitor.MonitorInformation#setSize(java.lang.String)
   */
  public void setSize(final String size) {
    this.size = size;
  }
  
/*  *//**
   * Sets detail flag.
   * @param b
   *//*
  public void setDetail(boolean b) {
    detail  = b;
  }

  *//**
   * Checks if details exist.
   * @return
   *//*
  public boolean getDetail(){
    return detail;
  }
  */
  /**
   * 
   * Parses all the size information that is needed for: Pagesize, Version number, Blocksize, All blocks and used/total hard disk space.
   * @param mainiqrow
   * @param pagesize
   * @param version
   * @param otherVersion
   */
  public void parseSize(final String mainiqrow, final String pagesize, final String version, final String otherVersion, final String backuptime, final String iqMain) {
    try {
      log.debug("blockSize = " + mainiqrow.substring(mainiqrow.indexOf(":")+1,mainiqrow.indexOf(" of")));
      log.debug("allBlocks = " + mainiqrow.substring(mainiqrow.indexOf("of ")+3, mainiqrow.indexOf(",")));
      final long blockSize = Long.parseLong(mainiqrow.substring(mainiqrow.indexOf(":")+1,mainiqrow.indexOf(" of")));
      final long allBlocks = Long.parseLong(mainiqrow.substring(mainiqrow.indexOf("of ")+3, mainiqrow.indexOf(",")));
      
      String[] splitted = mainiqrow.split(",");
      splitted = splitted[1].split("%");
      final int percentage = Integer.parseInt(splitted[0].trim());
      isDBFull = percentage >= 90;
      
      //Mainiq row
      /*
      StringTokenizer st = new StringTokenizer(mainiqrow, ":");
      st.nextToken();
      StringTokenizer stNext = new StringTokenizer(st.nextToken().toString(), "of");
      int blocksize = Integer.parseInt(stNext.nextToken().toString().trim());
      
      //All blocks
      StringTokenizer stAllBlocks = new StringTokenizer(stNext.nextToken().toString(), ",");
      int allBlocks = Integer.parseInt(stAllBlocks.nextToken().trim().toString());
      */
      log.debug("page = " + pagesize.substring(pagesize.indexOf("/")+1, pagesize.indexOf("blk")) );
      final int page = Integer.parseInt(pagesize.substring(pagesize.indexOf("/")+1, pagesize.indexOf("blk")));
      /*
      StringTokenizer stt = new StringTokenizer(pagesize, "/");
      stt.nextToken();
      
      //Page size
      StringTokenizer sttNext = new StringTokenizer(stt.nextToken().toString(), "bl");
      int page = Integer.parseInt(sttNext.nextToken().toString().trim());
      */
      
      //Other versions
      final String[] otherVersionSplitResult = otherVersion.split(" ");
      
      if(otherVersionSplitResult.length >= 3) {
        otherVersionMultip = Integer.parseInt(otherVersionSplitResult[0].trim());
        
        final String otherVersionSizeString = otherVersionSplitResult[2];
        
        if(otherVersionSizeString.indexOf("Mb") != -1) {
          otherVersionSize = new Float(otherVersionSizeString.substring(0, otherVersionSizeString.indexOf("Mb"))).floatValue();
        } else if (otherVersionSizeString.indexOf("Gb") != -1) {
          otherVersionSize = new Float(otherVersionSizeString.substring(0, otherVersionSizeString.indexOf("Gb"))).floatValue() * 1024;
        } else {
          otherVersionSize = 0;
          log.info("Unknown otherVersionSize. Setting otherVersionSize to 0.");
        }
        
        log.debug("otherVersionSize = " + otherVersionSize);
        
      } else {
        log.debug("Failed to split otherVersion. Setting 0 for otherVersionMultip and otherVersionSize.");
        otherVersionMultip = 0;
        otherVersionSize = 0;
      }
      
      /*
      StringTokenizer ost = new StringTokenizer(otherVersion, " ");
      otherVersionMultip = Integer.parseInt(ost.nextToken().toString().trim());
      ost.nextToken();
      StringTokenizer ostS = new StringTokenizer(ost.nextToken().toString().trim(), "M");
      otherVersionSize = Float.parseFloat(ostS.nextToken().toString().trim());
      */
      
      //Backup delay
      this.setBackuptime(backuptime);
      
      final StringBuffer sb = new StringBuffer();
      final double usedSpaceMb = (double)((((float)page * (float)blockSize) / 1024) / ((float)1024));
      final double totalSpaceMb = (double)((((float)page * (float)allBlocks) / 1024) / ((float)1024));
      
      setTotalSize(totalSpaceMb);
      setUsedSpace(usedSpaceMb);
      
      int iqMainTotalspaceM = 0;
      double iqMainusedSpaceM = 0;
      
      String[] iqMainUsagespilt=iqMain.split(" ");
      String iqMainUsage=iqMainUsagespilt[0];
      String iqMainSize=iqMainUsagespilt[1];
      
      if(iqMainSize.endsWith("T")) {
    	  iqMainTotalspaceM = (int)(Double.parseDouble(iqMainSize.substring(0, iqMainSize.length()-1)) * 1024 * 1024);	  
      }
      if(iqMainSize.endsWith("G")) {
    	  iqMainTotalspaceM  = (int)(Double.parseDouble(iqMainSize.substring(0, iqMainSize.length()-1)) * 1024);	  
      }
      if(iqMainSize.endsWith("M")) {
    	  iqMainTotalspaceM = (int)(Double.parseDouble(iqMainSize.substring(0, iqMainSize.length()-1)));	  
      }
      
      
      iqMainusedSpaceM = (double) (Integer.parseInt(iqMainUsage)*iqMainTotalspaceM)/100;
      final int available= (int)((double) iqMainTotalspaceM - iqMainusedSpaceM );
      
      
      sb.append(getProduct() + " Version: "); 
      sb.append(version);
      sb.append(".<br />");
      sb.append("Total database size: " + iqMainTotalspaceM + " MB.");
      sb.append("<br />");
      sb.append("Used space: ");
      sb.append((int)iqMainusedSpaceM  + " MB");
      sb.append(" (" + iqMainUsage + "%).");
      sb.append(" Available " + available + " MB.");

      
      setAllInfo(sb.toString(), false);
      totalsize = String.valueOf(totalSpaceMb);
    } catch (NumberFormatException e) {
      log.error("NumberFormatException: ",e);
    } catch (Exception exc){
      log.error("Exception: ",exc);
    }
     
   }
  
  public void parseSQLAnywhereSize(final String version, final String total_space, final String free_space,
      final String backuptime) {
    try {
      final long totalSpaceInBytes = Long.parseLong(total_space); 
      final long freeSpaceInBytes = Long.parseLong(free_space); 
      
      final StringBuffer sb = new StringBuffer();
      final long totalSpaceMB = Math.round((totalSpaceInBytes / 1024f) / 1024f);
      final long freeSpaceMB = Math.round((freeSpaceInBytes / 1024f) / 1024f);
      final long usedSpaceMB = totalSpaceMB - freeSpaceMB; 

      final long percentage = Math.round(usedSpaceMB  * 100d / totalSpaceMB);
      isDBFull = percentage >= 90;
      
      setBackuptime(backuptime);
      setTotalSize(totalSpaceMB);
      setUsedSpace(usedSpaceMB);
      
      sb.append(getProduct() + " Version: ");
      sb.append(version);
      sb.append(".<br />");
      sb.append("Total database size: " + totalSpaceMB + " MB.");
      sb.append("<br />");
      sb.append("Used space: ");
      sb.append(usedSpaceMB + " MB");
      sb.append(" (" + percentage + "%).");
      sb.append(" Available " + freeSpaceMB + " MB.");

      setAllInfo(sb.toString(), false);
      totalsize = String.valueOf(totalSpaceMB);
    } catch (NumberFormatException e) {
      log.error("NumberFormatException: ",e);
    } catch (Exception exc){
      log.error("Exception: ",exc);
    }
  }
  
  public void new_parseSQLAnywhereSize(final String version, String free_space, String space_used,
	      final String backuptime) {
	    
	    float freeSpaceM = 0f, usedSpaceM = 0f, totalSpaceM = 0f;
	    try {
	    	if (free_space.endsWith("T")) {
	    		freeSpaceM = (float)(Double.parseDouble(free_space.substring(0, free_space.length()-1)) * 1024 * 1024);
	    	}
	    	
	    	if (space_used.endsWith("T")) {
	    		usedSpaceM = (float)(Double.parseDouble(space_used.substring(0, space_used.length()-1)) * 1024 * 1024);
	    	}
	    	
	    	if (free_space.endsWith("G")) {
	    		freeSpaceM = (float)(Double.parseDouble(free_space.substring(0, free_space.length()-1)) * 1024);
	    	}
	    	
	    	if (space_used.endsWith("G")) {
	    		usedSpaceM = (float)(Double.parseDouble(space_used.substring(0, space_used.length()-1)) * 1024);
	    	}
	    	
	    	if (free_space.endsWith("M")) {
	    		freeSpaceM = (float)(Double.parseDouble(free_space.substring(0, free_space.length()-1)));
	    	}
	    	
	    	if (space_used.endsWith("M")) {
	    		usedSpaceM = (float)(Double.parseDouble(space_used.substring(0, space_used.length()-1)));
	    	}
	    	
			   final StringBuffer sb = new StringBuffer();
		  
		      log.info("Free REPDB space :: "+freeSpaceM+"MB");
		      log.info("Used REPDB space :: "+usedSpaceM+"MB");
		      
		      totalSpaceM = freeSpaceM + usedSpaceM;
		      log.info("Total REPDB size :: "+totalSpaceM+"MB");
		      
		      final float percent = (float) ((usedSpaceM/totalSpaceM)  * 100.00);
		      
		      int percentage = (int) percent; 
		      int totalSpaceMB = (int)totalSpaceM;
		      int freeSpaceMB = (int)freeSpaceM;
		      int usedSpaceMB = (int)usedSpaceM;
		      
	      isDBFull = percentage >= 90;
	      
	      setBackuptime(backuptime);
	      setTotalSize(totalSpaceMB);
	      setUsedSpace(usedSpaceMB);
	      
	      sb.append(getProduct() + " Version: ");
	      sb.append(version);
	      sb.append(".<br />");
	      sb.append("Total database size: " + totalSpaceMB + " MB.");
	      sb.append("<br />");
	      sb.append("Used space: ");
	      sb.append(usedSpaceMB + " MB");
	      sb.append(" (" + percentage + "%).");
	      sb.append(" Available " + freeSpaceMB + " MB.");

	      setAllInfo(sb.toString(), false);
	      totalsize = String.valueOf(totalSpaceMB);
	    } catch (NumberFormatException e) {
	      log.error("NumberFormatException: ",e);
	    } catch (Exception exc){
	      log.error("Exception: ",exc);
	    }
	  }

  private void setUsedSpace(final double usedSpaceMb) {
    this.usedSpaceMb  = usedSpaceMb;
  }

  /**
   * Gets used space.
   * @return used disk space
   */
  public double getUsedSpace(){
    return usedSpaceMb;
  }
  
  /**
   * Total space of database size.
   * 
   * @param totalSpaceMb
   */
  private void setTotalSize(final double totalSpaceMb) {
    totalSizeMb = totalSpaceMb;
  }

  /**
   * Gets totalsize in megabytes as string.
   * @return total size
   */
  public String getTotalSizeStringMB() {
    return totalsize ;
  }

  /**
   * Gets host harddidsk totalsize in megabytes as float.
   * @return totalsize
   */
  public double getTotalSizeIntMB() {
    return totalSizeMb;
  }
  
  /**
   * This is a multiplier that is used for counting other versions.
   * @return multiplier
   */
  public int getOtherVersionMultip() {
    return otherVersionMultip;
  }
/**
 * Gets the total of other versions size.
 * @return otherversion size
 */
  public float getOtherVersionSize() {
    return otherVersionSize;
  }

  private void setBackuptime(final String backuptime) {
    this.backuptime = backuptime;
  }
  
  public String getBackuptime() {
    return backuptime;
  }
  
  public boolean isDBFull() {
    return isDBFull;
  }

  
}
