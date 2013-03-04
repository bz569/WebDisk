package com.webdisk.model;

import java.util.Date;

import com.webdisk.util.DateUtil;

public abstract class SVNModel
{
	/**
	 * Last modified date.  Updated with any change to record
	 */
	private Date dateModified;
	
	/**
	 * Date record was initially created.
	 */
	private Date dateCreated;
	
	/**
	 * Active flag
	 */
	private Boolean active; 
	
	/**
	 * Constructor
	 */
	public SVNModel() {
		// set the create time 
		this.setDateCreated(DateUtil.getGMTNow());
		this.setDateModified(DateUtil.getGMTNow());

	}

	// gettors and settors

	public Date getDateModified() {
		return this.dateModified;
	}

	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Boolean getActive() {
		return active;
	}

}
