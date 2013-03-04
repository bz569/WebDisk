package com.webdisk.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;

import com.webdisk.model.Connection;
import com.webdisk.util.DateUtil;


public class Connection extends SVNModel
{
	/**
	 * Setup a the protocol type enum for the supported protocols
	 * @author brian.gormanly
	 *
	 */
	public enum PROTOCOL_TYPE  
	{  
	    HTTP("HTTP"),  
	    HTTPS("HTTPS"),  
	    SVN("SVN"), 
	    SVNSSH("SVN+SSH");
	  
	    private final String label;  
	  
	    private PROTOCOL_TYPE(String label) { this.label = label; }  
	  
	    @Override  
	    public String toString() { return label; }  
	}
	
		// members saved in the database
		private String name = "";
		private String textURL = "";
		private SVNURL repositoryURL;
		private PROTOCOL_TYPE type;
		private BasicAuthenticationManager authManager;
		private String username ="";
		private String password = "";
		private String key = "";
		private String folder = "";
		private Integer head = 0;
		
		private Collection<SVNDirEntry> directories = null;
		private List<SVNLogEntry> revisions;
		
		/**
		 * Default Constructor, connection is not ready to be used until url, username and password are provided
		 * and the AuthManager is initialized.
		 */
		public Connection() {
			
			
		}
		
		/**
		 * Creates Connection and prepares it with the provided information
		 * @param url
		 * @param type
		 * @param username
		 * @param password
		 * @param folder
		 */
		public Connection(String name, String url, PROTOCOL_TYPE type, String username, String password, String key, String folder) {
			
			// call the super, setting the table name
			
			this.setName(name);
			this.setUrl(url);
			this.setUsername(username);
			this.setPassword(password);
			this.setKey(key);
			this.setFolder(folder);
			this.initializeAuthManager();
		}
		
		/**
		 * Setup the BasicAuthManager with the supplied username and password
		 */
		public void initializeAuthManager(String username, String password, String key) {
			this.setUsername(username);
			this.setPassword(password);
			this.setKey(key);
			this.initializeAuthManager();
		}
		
		/**
		 * Setup the BasicAuthenticationManager with the user and password
		 * @return 
		 */
		public void initializeAuthManager() {
			try {
				// check the user name and password exist
				if(this.username.length() > 0 && this.password.length() > 0 && this.key.length() > 0) {
					this.authManager = new BasicAuthenticationManager(this.username, new File(this.key), this.password, 22);
				} 
				else if (this.username.length() > 0 && this.password.length() > 0) {
					this.authManager = new BasicAuthenticationManager(this.username, this.password);
				}
			}
			catch(Exception e) {
				 e.printStackTrace();
			}
		}
		
		// TODO OASVN_Connection.java 229 ¿ªÊ¼
		
		
		
		
		
		
		
		
		
		
		// getters and setters
		public void dateUpdated() {
			this.setDateModified(DateUtil.getGMTNow());
		}

		public String getTextURL() {
			return textURL;
		}

		public void setUrl(String url) {
			this.textURL = url;
			
			// set the type
			this.setType(url);
			
			this.setRepositoryUTL();
			
			dateUpdated();
		}
		
		public SVNURL getRepositoryURL() {
			return this.repositoryURL;
		}
		
		public void setRepositoryUTL() {
			try {
				this.repositoryURL = SVNURL.parseURIEncoded(this.getTextURL());
			} catch (SVNException e) {
				e.printStackTrace();
			}
		}

		public BasicAuthenticationManager getAuthManager() {
			return authManager;
		}

		public void setAuthManager(BasicAuthenticationManager authManager) {
			this.authManager = authManager;
			dateUpdated();
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
			dateUpdated();
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
			dateUpdated();
		}

		public String getKey() {
			return key;
		}
		
		public void setKey(String key) {
			this.key = key;
			dateUpdated();
		}
			
		public void setFolder(String folder) {
			this.folder = folder;
			dateUpdated();
		}

		public String getFolder() {
			return folder;
		}
		
		public PROTOCOL_TYPE getType() {
			return this.type;
		}
		
		public void setType(String url) {
			if(url.substring(0, 5).toLowerCase().equals("https")) {
				this.type = Connection.PROTOCOL_TYPE.HTTPS;
			}
			
			else if(url.substring(0, 5).toLowerCase().equals("http:")) {
				this.type = Connection.PROTOCOL_TYPE.HTTP;
			}
			
			else if(url.substring(0, 7).toLowerCase().equals("svn+ssh")) {
				this.type = Connection.PROTOCOL_TYPE.SVNSSH;
			}
			else if(url.substring(0, 4).toLowerCase().equals("svn:")) {
				this.type = Connection.PROTOCOL_TYPE.SVN;
			}
			// default to HTTP
			else {
				this.type = Connection.PROTOCOL_TYPE.HTTP;
			}
			
			dateUpdated();
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setHead(Integer head) {
			this.head = head;
		}

		public Integer getHead() {
			return head;
		}

		public void setDirectories(Collection<SVNDirEntry> directories) {
			this.directories = directories;
		}

		public Collection<SVNDirEntry> getDirectories() {
			return directories;
		}

		public void setRevisions(List<SVNLogEntry> revisions) {
			this.revisions = revisions;
		}

		public List<SVNLogEntry> getRevisions() {
			return revisions;
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
}
