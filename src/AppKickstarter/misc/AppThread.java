package AppKickstarter.misc;

import AppKickstarter.AppKickstarter;
import java.util.logging.Logger;


//======================================================================
// AppThread
public abstract class AppThread implements Runnable {
    protected String id;
    protected AppKickstarter appKickstarter;
    protected MBox mbox = null;
    protected Logger log = null;

    //------------------------------------------------------------
    // AppThread
    /**
     * Constructor for an appThread
     * @param id name of the appThread
     * @param appKickstarter a reference to our AppKickstarter
     */
    public AppThread(String id, AppKickstarter appKickstarter) {
	this.id = id;
	this.appKickstarter = appKickstarter;
	log = appKickstarter.getLogger();
	mbox = new MBox(id, log);
	appKickstarter.regThread(this);
	log.fine(id + ": created!");
    } // AppThread


    //------------------------------------------------------------
    // getMBox
    /**
     * Get the mbox of this appThread
     * @return the mbox of this appThread
     */
    public MBox getMBox() { return mbox; }


    //------------------------------------------------------------
    // getID
    /**
     * Get the id of this appThread
     * @return the id of this appThread
     */
    public String getID() { return id; }
} // AppThread
