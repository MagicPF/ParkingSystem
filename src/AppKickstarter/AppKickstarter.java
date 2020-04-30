package AppKickstarter;

import AppKickstarter.timer.Timer;
import AppKickstarter.misc.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.Hashtable;


//======================================================================
// AppKickstarter
public abstract class AppKickstarter {
    private String cfgFName = null;
    private Properties cfgProps = null;
    private Hashtable<String, AppThread> appThreads = null;
    protected String id = null;
    protected Logger log = null;
    private ConsoleHandler logConHd = null;
    private FileHandler logFileHd = null;
    private Timer timer = null;
    protected abstract void startApp();
    protected abstract void stopApp();


    //------------------------------------------------------------
    // AppKickstarter
    /**
     * Constructor for AppKickstarter (default log file NOT appending)
     * @param id name of the application
     * @param cfgFName file name of the configuration file
     */
    public AppKickstarter(String id, String cfgFName) {
	this(id, cfgFName, false);
    } // AppKickstarter


    //------------------------------------------------------------
    // AppKickstarter
    /**
     * Constructor for AppKickstarter
     * @param id name of the application
     * @param cfgFName file name of the configuration file
     * @param append appending or overwriting log file?
     */
    private AppKickstarter(String id, String cfgFName, boolean append) {
	this.id = id;
	this.cfgFName = cfgFName;
	logConHd = null;
	logFileHd = null;
	id = getClass().getName();

	// set my thread name
	Thread.currentThread().setName(this.id);

	// read system config from property file
	try {
	    cfgProps = new Properties();
	    FileInputStream in = new FileInputStream(cfgFName);
	    cfgProps.load(in);
	    in.close();
	    logConHd = new ConsoleHandler();
	    logConHd.setFormatter(new LogFormatter());
	    logFileHd = new FileHandler("etc/" + id + ".log", append);
	    logFileHd.setFormatter(new LogFormatter());
	} catch (FileNotFoundException e) {
	    System.out.println("Failed to open config file ("+cfgFName+").");
	    System.exit(-1);
	} catch (IOException e) {
	    System.out.println("Error reading config file ("+cfgFName+").");
	    System.exit(-1);
	}

	// get and configure logger
	log = Logger.getLogger(id);
	log.addHandler(logConHd);
	log.addHandler(logFileHd);
	log.setUseParentHandlers(false);
	log.setLevel(Level.FINER);
	logConHd.setLevel(Level.parse(getProperty("AppKickstarter.ConsoleLogLevel", "INFO")));
	logFileHd.setLevel(Level.parse(getProperty("AppKickstarter.FileLogLevel", "INFO")));
	appThreads = new Hashtable<String, AppThread>();
    } // AppKickstarter


    //------------------------------------------------------------
    // regThread
    /**
     * Register an appThread to the AppKickstarter framework
     * @param appThread the appThread to register
     */
    public void regThread(AppThread appThread) {
	log.fine(id + ": registering " + appThread.getID());
	synchronized (appThreads) { appThreads.put(appThread.getID(), appThread); }
    } // regThread


    //------------------------------------------------------------
    // unregThread
    /**
     * Unregister an appThread to the AppKickstarter framework
     * @param appThread the appThread to unregister
     */
    public void unregThread(AppThread appThread) {
	log.fine(id + ": unregistering " + appThread.getID());
	synchronized (appThreads) { appThreads.remove(appThread.getID()); }
    } // unregThread


    //------------------------------------------------------------
    // getThread
    /**
     * Get an appThread that has previously registered with AppKickstarter
     * @param id the name of to appThread to get
     * @return the corresponding appThread (null if not found)
     */
    public AppThread getThread(String id) {
	synchronized (appThreads) { return appThreads.get(id); }
    } // getThread


    //------------------------------------------------------------
    // getLogger
    /**
     * Gets the logger created by the AppKickstarter
     * @return the logger created by the AppKickstarter
     */
    public Logger getLogger() {
	return log;
    } // getLogger


    //------------------------------------------------------------
    // getLogConHd
    /**
     * Gets the console handler of the logger
     * @return the console handler of the logger
     */
    public ConsoleHandler getLogConHd() {
	return logConHd;
    }
    // getLogConHd


    //------------------------------------------------------------
    // getLogFileHd
    /**
     * Gets the file handler of the logger
     * @return the file handler of the logger
     */
    public FileHandler getLogFileHd() {
	return logFileHd;
    } // getLogFileHd


    //------------------------------------------------------------
    // getProperty
    /**
     * Get the specified property that was read from system config
     * file during system startup (no default value)
     * @param property the name of the property
     * @return the value of the property (null if not found)
     */
    public String getProperty(String property) {
	String s = cfgProps.getProperty(property);

	if (s == null) {
	    log.severe(id + ": getProperty(" + property + ") failed.  Check the config file (" + cfgFName + ")!");
	}
	return s;
    } // getProperty


    //------------------------------------------------------------
    // getProperty
    /**
     * Get the specified property that was read from system config
     * file during system startup (no default value)
     * @param property the name of the property
     * @param defaultValue the default value for the property
     * @return the value of the property; if property not found, return defaultValue
     */
    public String getProperty(String property, String defaultValue) {
	String s = cfgProps.getProperty(property);

	if (s == null) {
	    return defaultValue;
	}
	return s;
    } // getProperty


    //------------------------------------------------------------
    // getSimulationTime (in seconds)
    /**
     * Gets the simulation time.
     * @return the simulation time
     */
    public long getSimulationTime() {
	return timer.getSimulationTime();
    } // getSimulationTime


    //------------------------------------------------------------
    // getSimulationTimeStr
    /**
     * Gets the simulation time as a String.
     * @return the simulation time as a formatted String
     */
    public String getSimulationTimeStr() {
	long t = timer.getSimulationTime();
	int s = (int) t % 60;
	int m = (int) (t/60) % 60;
	int h = (int) (t/3600) % 60;

	return String.format("%02d:%02d:%02d", h, m, s);
    } // getSimulationTimeStr
} // AppKickstarter
