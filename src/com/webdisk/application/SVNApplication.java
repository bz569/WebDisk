package com.webdisk.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNConflictDescription;
import org.tmatesoft.svn.core.wc.SVNConflictReason;
import org.tmatesoft.svn.core.wc.SVNMergeFileSet;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.webdisk.model.Connection;

import com.webdisk.R;

public class SVNApplication extends Application
{
	/** 
	 * Activity constants
	 */
	public static final int PICK_CONFLICT_ACTION_REQUEST = 0;
	public static final String TAG = "SVNApplication";
	
	/**
	 * database
	 */
	public SQLiteDatabase database;
	
	/**
	 * Full path to main folder
	 */
	private String fullPathToMain = "";
	
	/**
	 * Maintains application perspective on whether or not there is external 
	 * storage available.
	 */
	private boolean mExternalStorageAvailable = false;
	
	/**
	 * Maintains applications perspective on whether or not the external storage
	 * is writable.
	 */
    private boolean mExternalStorageWriteable = false;
    
    /**
     * Path information
     */
    private File rootPath = null;
    
    /**
     * Current connection
     */
    private Connection currentConnection;
    
    /**
     * Current Revision
     */
    private SVNLogEntry currentRevision;
    
    /**
     * All connections
     */
    private ArrayList<Connection> allConnections;
    
    /**
     * BasicAithenticationManager sets up the svn authentication with the server.
     */
    private BasicAuthenticationManager myAuthManager;
    
    /**
     * The SVNClientManager class is used to manage SVN*Client objects
     */
    private SVNClientManager clientManager;
    
    /**
     * The SVNWCUtil is a utility class providing some common methods used by 
     * Working Copy API classes for such purposes as creating default run-time 
     * configuration and authentication drivers and some others.
     */
    private SVNWCUtil wcUtil;
    
    /**
     * Contains the Status of any interesting file status.
     */
    private ArrayList<SVNStatus> problemFiles = new ArrayList<SVNStatus>();
    
    /**
     * 
     */
    private SVNCommitInfo info;
    
    /**
     * Commit comments
     */
    private String commitComments = "";
    
    /**
     * Container for any conflicts that are found during update
     */
    private SVNConflictDescription currentConflict = null;
    
    /**
     * Holds users choice for resolving the current conflict under consideration.
     */
    SVNConflictChoice conflictDecision = SVNConflictChoice.MINE_FULL;
    
    /**
     * SVNConflictReason - contains the reason for conflict identified
     */
    SVNConflictReason conflictReason = null;
    
    /**
     * List of files that are in a state of conflict
     */
    SVNMergeFileSet conflictFiles = null;
    
    /**
     * Constructor
     */
    public SVNApplication() 
    {
    	
    	// initialize arraylists
    	this.allConnections = new ArrayList<Connection>();
    	
    	
    	// initialize the storage state
    	this.discoverStorageState();
    	
    	// make sure the app is initialized
		this.initAuthManager();
		
		// make sure the path is ready
		this.initializePath();
    	
    }
    
    /**
     * OnCreate
     */
    @Override
	public void onCreate() 
    {
		super.onCreate();
		
//		// retrieve the database
//		DatabaseHelper helper = new DatabaseHelper(this, this);
//		database = helper.getWritableDatabase();
		
		// try to retrieve the settings data
		this.initalizeSettings();
    }
    
    /**
	 * Try to retrieve the settings from the database if they exist.
	 * If they do not yet exist, create them.
	 */
	public void initalizeSettings() 
	{
		//����rootfolder
	}
	
	/**
	 * This method should be called anytime a new currentConnection is chosen
	 * before any action is attempted.
	 * 
	 */
    public void initAuthManager() {
    	try {
	    	// check to see that we have a current connection
	    	if(currentConnection != null) { 
	    		// initialize the Auth manager
	    		if (this.currentConnection.getKey() != null && this.currentConnection.getKey().length() > 0) {
	    			myAuthManager = new BasicAuthenticationManager(this.currentConnection.getUsername(), new File(this.currentConnection.getKey()), this.currentConnection.getPassword(), 22);
	    		} 
	    		else {
	    			myAuthManager = new BasicAuthenticationManager(this.currentConnection.getUsername(), this.currentConnection.getPassword());
	    		}
	    	}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	// initialize the clientManager
    	this.initClientManager();
    	
    	// initialize the clientManager children
    	this.initManagerChildren();
    }
    
    public void initClientManager() {
    	clientManager = SVNClientManager.newInstance();
    	clientManager.setAuthenticationManager(myAuthManager); 
    }
    
    public void initManagerChildren() {
    	
    	// working copy util
    	wcUtil = new SVNWCUtil();
    }
    
    /**
	 * Gets the current connection path
	 * 
	 * @return the path as a File.
	 */
	public File assignPath() {
		// get the sd card directory
		File file = null;

		file = new File(this.currentConnection.getFolder());
		
		return file;
	}

	public File assignPath(String subPath) {
		return assignPath("", subPath, true);
	}

	public File assignPath(String sdPath, String svnPath, boolean useWCroot) {
		// check to see that there is a path
		try {
			if (this.currentConnection != null
					&& this.currentConnection.getFolder().length() > 0)
			{
				// get the sd card directory
				File file = null;

				if (useWCroot)
					file = new File(this.getRootPath(), this.currentConnection.getFolder() + svnPath);
				else
					file = new File("/mnt/sdcard/", sdPath);

				return file;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	
	// TODO ��֪�������ʲô
	/**
	 * Creates path of File path given.  Will look backwards through the 
	 * path to create the entire folder structure needed, not just the 
	 * top level folder.
	 * @param path full directory path to be created. Can include part existing
	 * and new path.
	 */
	public void createPath(File path) {
		// folder does not yet exist, create it.
		System.out.println("Going to create : " + path.toString());
		// check to see if the parent exists and try to create
		int counter = 0;
		File parentFile = path.getParentFile();
		while (!parentFile.exists()) {
			counter++;
			parentFile = parentFile.getParentFile();
		}
		
		System.out.println("counter reached : " + counter);
		int counter2 = counter;
		for(int i=0; i<counter; i++) {
			File itFile = path;
			for(int j=0; j<counter2; j++) {
				itFile = itFile.getParentFile();
				
			}
			itFile.mkdir();
			System.out.println("Folder created : " + itFile);
			counter2--;
		}
		path.mkdir();
		System.out.println("Folder(s) created");
	}
    
    public void deleteRecursive(File tree) {
    	if (tree.isDirectory())
	        for (File child : tree.listFiles())
	            this.deleteRecursive(child);

		tree.delete();
    }
	
    public void discoverStorageState() {
    	// get the current state of external storage
    	String state = Environment.getExternalStorageState();

	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        // We can read and write the media
	        setmExternalStorageAvailable(setmExternalStorageWriteable(true));
	    } 
	    else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        // We can only read the media
	        setmExternalStorageAvailable(true);
	        setmExternalStorageWriteable(false);
	    } 
	    else {
	        // Something else is wrong. It may be one of many other states, but all we need
	        //  to know is we can neither read nor write
	        setmExternalStorageAvailable(setmExternalStorageWriteable(false));
	    }
    }
    
    public void initializePath() {
    	try {
    		String mainFolder = "";
    		
//    		// check to see if there is a default folder from the settings
//    		if(Settings.getInstance().getRootFolder().length() == 0) {
//    			mainFolder = "OASVN/";
//    		}
//    		else {
//    			mainFolder = Settings.getInstance().getRootFolder();
//    		}
    		
    		//����rootpath="Webdisk/"
    		mainFolder = "Webdisk/";
    		
    		// set the full path to main
    		this.setFullPathToMain(Environment.getExternalStorageDirectory() + "/" + mainFolder);
    		
	    	File folder = new File(this.getFullPathToMain());
	    	
		    if(!folder.exists()){
		    	// folder does not yet exist, create it.
		    	createPath(folder);
		    	this.setRootPath(folder);
		    	Log.i(getString(R.string.FILE), getString(R.string.directory_created)); 
		    }
		    else {
		    	// folder already exists
		    	this.setRootPath(folder);
		    	//Log.i(getString(R.string.FILE), getString(R.string.directory_exists)); 
		    }
		    
    	}
    	catch(Exception e) {
    		Log.e("FILE", "can't create folder");
    		e.printStackTrace();
    	}
    }
	
    /**
     * Created to be a central way to check the validity of paths across
     * multiple devices, sd-cards, etc.
     * 
     * see OASVN-67 for more details.
     */
    public void checkValidPath() {
    	
    }
    
    // SVNKit wrapper
    
    /**
     * Check to see if the folder exists as a local working copy, and is under version
     * control
     * @param directory to verify
     * @return true if the folder is a version controlled working copy
     */
    public Boolean verifyWorkingCopy(File file) {
    	Boolean state = true;
    	
    	try {
    		state = wcUtil.isWorkingCopyRoot(file);
    		state = wcUtil.isVersionedDirectory(file);
    	}
		catch(VerifyError ve) {
			String msg = ve.getMessage();
			
			ve.printStackTrace();
			
			// set the state to false
			state = false;
		}
		catch(Exception e) {
			String msg = e.getMessage();
			
			e.printStackTrace();
			
			// set the state to false
			state = false;
		}
    	
    	return state;
    }
    
    /**
	 * Retrieves all of the directories for the given repository
	 * 
	 * @param revision
	 *            as SVNRevision
	 * @param subpath
	 *            in the repository
	 * @return ArrayList<SVNDirEntry> - Contains all directories as objects
	 */
	@SuppressWarnings("unchecked")
	public Collection<SVNDirEntry> getAllDirectories(SVNRevision revision, String subPath)
	{
		// initialize the auth manager
		this.initAuthManager();

		SVNURL url = this.currentConnection.getRepositoryURL();

		Collection<SVNDirEntry> entriesList = null;
		try
		{
			SVNRepository repos = SVNRepositoryFactory.create(url);
			repos.setAuthenticationManager(getMyAuthManager());
			entriesList = repos.getDir(subPath, revision.getNumber(), null,
					(Collection<?>) null);
		}
		catch (SVNException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (VerifyError ve)
		{
			String msg = ve.getMessage();

			Log.i(TAG, msg);
			ve.printStackTrace();
		}
		catch (Exception e)
		{
			String msg = e.getMessage();

			// log this failure
			Log.i(TAG, msg);
			e.printStackTrace();
		}

		return entriesList;

	}
	
	// TODO why?
//	public File[] getLocalFilesSystemList() {
//
//		String folderDir = this.currentConnection.getFolder();
//
//		File[] localDirectory = null;
//		try {
//			File localFile = this.assignPath();
//			localDirectory = localFile.listFiles();
//		}
//		catch (VerifyError ve) {
//			String msg = ve.getMessage();
//			// log this failure
//			Log.i(TAG, msg);
//			ve.printStackTrace();
//		}
//		catch (Exception e) {
//			String msg = e.getMessage();
//			// log this failure
//			Log.i(TAG, msg);
//			e.printStackTrace();
//		}
//
//		return localDirectory;
//
//	}
    
	/**
     * Does full export of the Head revision
     * @return success or failure message
     */
    public String fullHeadExport() {

    		SVNRevision myRevision = SVNRevision.HEAD;
    		String rValue = doExport(myRevision);
    		return rValue;
    }
    
    /**
   	 * Does an export of version supplied does not create a working copy.
   	 * 
   	 * @param revision
   	 *            as long
   	 * @return success or failure message
   	 */
   	public String doExport(long revision)
   	{
   		// create the return holder
   		String rValue = "";

   		// convert the Long parameter value to an SVNRevision
   		try
   		{
   			SVNRevision thisRev = SVNRevision.create(revision);
   			rValue = doExport(thisRev);
   		}
   		catch (Exception e)
   		{
   			if (rValue.length() == 0)
   			{
   				rValue = "Invalid Revision";
   			}
   			e.printStackTrace();
   		}

   		return rValue;
   	}

   	/**
   	 * Does an export of the remote folder to the local. This does not create a
   	 * working copy and will not work if a working copy is already in the local
   	 * location.
   	 * 
   	 * @param revision
   	 *            of the remote repo to export
   	 * @return success or failure message
   	 */
   	public String doExport(SVNRevision revision)
   	{
   		return doExport(revision, "");
   	}

   	public String doExport(SVNRevision revision, String subPath)
   	{
   		SVNURL svnDir = this.currentConnection.getRepositoryURL();
   		return doExport(revision, new File(this.currentConnection.getFolder()), svnDir);
   	}
   	
   	/**
   	 * Does an export of the remote folder to the local. This does not create a
   	 * working copy and will not work if a working copy is already in the local
   	 * location.
   	 * 
   	 * @param revision
   	 *            of the remote repo to export
   	 * @param subpath
   	 *            of the object to export
   	 * @return success or failure message
   	 */
   	public String doExport(SVNRevision revision, File sdPath, SVNURL svnPath)
   	{
   		try
   		{
   			// initialize the auth manager
       		this.initAuthManager();
       		
       		// make sure the path is ready
       		initializePath();
       		
       		// get the parent folder
       		File parentFolder = sdPath.getParentFile();
       		// create the local path
       		if(!parentFolder.exists()){
   		    	// folder does not yet exist, create it.
       			createPath(parentFolder);
       		}
       		
       		System.out.println(svnPath.toString());
       		SVNURL myURL = svnPath;
       		File myFile = sdPath;
       		
   			SVNRevision pegRevision = SVNRevision.UNDEFINED;
   			SVNRevision myRevision = revision;
   			SVNDepth depth = SVNDepth.INFINITY;
   			
   			System.out.println("repository url : " + myURL.toString());
   			System.out.println("local path : " + myFile.toString());
   			try
   			{
   				// do the export
   				Long rev = clientManager.getUpdateClient().doExport(myURL, myFile, pegRevision,
   						myRevision, null, true, depth);

   				// log this success
   				Log.i(TAG, "export success");
   			}
   			catch (SVNException se)
   			{
   				String msg = se.getMessage();
   				
   				// log this failure
   				Log.i(TAG, msg);
   				
   				return msg;
   			}
   			catch (VerifyError ve)
   			{
   				String msg = ve.getMessage();

   				// log this failure
   				Log.i(TAG, msg);

   				ve.printStackTrace();
   				return getString(R.string.verify) + " " + msg;
   			}
   			catch (Exception e)
   			{
   				String msg = e.getMessage();

   				// log this failure
   				Log.i(TAG, msg);
   				e.printStackTrace();
   				return getString(R.string.exception) + " " + msg;
   			}
   		}
   		catch (Exception e)
   		{
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   			return e.getMessage();
   		}
   		return getString(R.string.success);
   	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	public void setmExternalStorageAvailable(boolean mExternalStorageAvailable) {
		this.mExternalStorageAvailable = mExternalStorageAvailable;
	}

	public boolean ismExternalStorageAvailable() {
		return mExternalStorageAvailable;
	}

	public boolean setmExternalStorageWriteable(boolean mExternalStorageWriteable) {
		this.mExternalStorageWriteable = mExternalStorageWriteable;
		return mExternalStorageWriteable;
	}

	public boolean ismExternalStorageWriteable() {
		return mExternalStorageWriteable;
	}

	public void setRootPath(File rootPath) {
		this.rootPath = rootPath;
	}

	public File getRootPath() {
		return rootPath;
	}

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public Connection getCurrentConnection() {
		return currentConnection;
	}

	public void setCurrentConnection(Connection currentConnection) {
		this.currentConnection = currentConnection;
	}

	public BasicAuthenticationManager getMyAuthManager() {
		return myAuthManager;
	}

	public void setMyAuthManager(BasicAuthenticationManager myAuthManager) {
		this.myAuthManager = myAuthManager;
	}

	public SVNClientManager getCm() {
		return clientManager;
	}

	public void setCm(SVNClientManager clientManager) {
		this.clientManager = clientManager;
	}

	public void setAllConnections(ArrayList<Connection> allConnections) {
		this.allConnections = allConnections;
	}
	
	public ArrayList<Connection> getAllConnections() {
		return this.allConnections;
	}

	public void setInfo(SVNCommitInfo info) {
		this.info = info;
	}

	public SVNCommitInfo getInfo() {
		return info;
	}

	public void setCommitComments(String commitComments) {
		this.commitComments = commitComments;
	}

	public String getCommitComments() {
		return commitComments;
	}

	public void setFullPathToMain(String fullPathToMain) {
		this.fullPathToMain = fullPathToMain;
	}

	public String getFullPathToMain() {
		return fullPathToMain;
	}

	public void setCurrentRevision(SVNLogEntry currentRevision) {
		this.currentRevision = currentRevision;
	}

	public SVNLogEntry getCurrentRevision() {
		return currentRevision;
	}

	public void setWcUtil(SVNWCUtil wcUtil) {
		this.wcUtil = wcUtil;
	}

	public SVNWCUtil getWcUtil() {
		return wcUtil;
	}

	public ArrayList<SVNStatus> getProblemFiles() {
		return problemFiles;
	}

	public void setProblemFiles(ArrayList<SVNStatus> problemFiles) {
		this.problemFiles = problemFiles;
	}

	public SVNConflictDescription getCurrentConflict() {
		return currentConflict;
	}

	public void setCurrentConflict(SVNConflictDescription currentConflict) {
		this.currentConflict = currentConflict;
	}
	
	public SVNConflictChoice getConflictDecision() {
		return conflictDecision;
	}

	public void setConflictDecision(SVNConflictChoice conflictDecision) {
		this.conflictDecision = conflictDecision;
	}

	public SVNConflictReason getConflictReason() {
		return conflictReason;
	}

	public void setConflictReason(SVNConflictReason conflictReason) {
		this.conflictReason = conflictReason;
	}

	public SVNMergeFileSet getConflictFiles() {
		return conflictFiles;
	}

	public void setConflictFiles(SVNMergeFileSet conflictFiles) {
		this.conflictFiles = conflictFiles;
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}