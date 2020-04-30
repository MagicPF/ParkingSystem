package AppKickstarter.misc;

import java.util.List;
import java.util.logging.Logger;
import java.util.ArrayList;


//======================================================================
// MBox
public class MBox {
    private String id;
    private Logger log;
    private List<Msg> mqueue = new ArrayList<>();


    //------------------------------------------------------------
    // MBox
    /**
     * Constructor of a mbox
     * @param id the name of the owner of this mbox
     * @param log logger to be used by this mbox
     */
    public MBox(String id, Logger log) {
	this.id = id;
	this.log = log;
    } // MBox


    //------------------------------------------------------------
    // send
    /**
     * Sends a message to this mbox.  Note that this is a non-blocking
     * send (that is, caller does not block when sending message to
     * the mbox).
     * @param msg the message to be sent
     */
    public final synchronized void send(Msg msg) {
	mqueue.add(msg);
	log.finest(id + ": send \"" + msg + "\"");
	notify();
    } // send


    //------------------------------------------------------------
    // receive
    /**
     * Receives a message from this mbox (should be called by mbox owner only!!).
     * Note that this is a blocking send (that is, if the mbox is empty, caller
     * blocks waiting until a message arrives).
     * @return the message received
     */
    public final synchronized Msg receive() {
	while (mqueue.isEmpty()) {
	    try {
		wait();
	    } catch (InterruptedException e) {}
	}
	Msg msg = mqueue.remove(0);
	log.finest(id + ": receiving \"" + msg + "\"");
	return msg;
    } // receive


    //------------------------------------------------------------
    // getMsgCnt
    /**
     * Gets the number of messages in the mbox
     * @return the number of messages in the mbox
     */
    public int getMsgCnt() {
	return mqueue.size();
    } // getMsgCnt
} // MBox

