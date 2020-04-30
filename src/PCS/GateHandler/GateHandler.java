package PCS.GateHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;


//======================================================================
// GateHandler

/**
 * Gate Handler
 * @author Joe
 */
public class GateHandler extends AppThread {

	/**
	 * PCSCore MBox
	 */
    protected final MBox pcsCore;

	/**
	 * Gate StatusGateOpened, GateClosed, GateOpening, GateClosing,
	 */
	private GateStatus gateStatus;

    //------------------------------------------------------------
    // GateHandler

	/**
	 * Gate Handler Constructor
	 * @param id The ID of Gate
	 * @param appKickstarter Appkickstarter
	 * @author Joe
	 */
    public GateHandler(String id, AppKickstarter appKickstarter) {
	super(id, appKickstarter);
	pcsCore = appKickstarter.getThread("PCSCore").getMBox();
	gateStatus = GateStatus.GateClosed;
    } // GateHandler


    //------------------------------------------------------------
    // run

	/**
	 * The run method of Gate Handler receives message and processes logic
	 * @author Joe
	 */
    public void run() {
	Thread.currentThread().setName(id);
	log.info(id + ": starting...");

	for (boolean quit = false; !quit;) {
	    Msg msg = mbox.receive();

	    log.fine(id + ": message received: [" + msg + "].");

	    quit = processMsg(msg);
	}

	// declaring our departure
	appKickstarter.unregThread(this);
	log.info(id + ": terminating...");
    } // run


    //------------------------------------------------------------
    // processMsg

	/**
	 * Process Logic
	 * @param msg Msg received from run() method
	 * @return If Msg type is Terminate, return true. ELse, return false
	 * @author Joe
	 */
    protected boolean processMsg(Msg msg) {
        boolean quit = false;

	switch (msg.getType()) {
	    case GateOpenRequest:  handleGateOpenRequest();  break;
	    case GateCloseRequest: handleGateCloseRequest(); break;
	    case GateOpenReply:	   handleGateOpenReply();    break;
	    case GateCloseReply:   handleGateCloseReply();   break;
	    case Poll:		   handlePollReq();	     break;
	    case PollAck:	   handlePollAck();	     break;
	    case Terminate:	   quit = true;		     break;
	    default:
		log.warning(id + ": unknown message type: [" + msg + "]");
	}
	return quit;
    } // processMsg


    //------------------------------------------------------------
    // handleGateOpenRequest

	/**
	 * Handle Gate Open Request
	 * @author Joe
	 */
    protected final void handleGateOpenRequest() {
	log.info(id + ": gate open request received");

	GateStatus oldGateStatus = gateStatus;
        switch (gateStatus) {
	    case GateOpening:
		log.warning(id + ": gate is already opening!!  Ignore request.");
		break;

	    case GateOpened:
		log.warning(id + ": gate is already opened!!  Ignore request.");
		break;

	    case GateClosing:
		log.info(id + ": gate is closing.  Change direction.");
		// falls through

	    case GateClosed:
		log.info(id + ": send signal to open the gate now.");
		sendGateOpenSignal();
	        gateStatus = GateStatus.GateOpening;
		break;
	}

	if (oldGateStatus != gateStatus) {
	    log.fine(id + ": gate status change: " + oldGateStatus + " --> " + gateStatus);
	}
    } // handleGateOpenRequest


    //------------------------------------------------------------
    // handleGateCloseRequest

	/**
	 * Handle Gate Close Request
	 * @author Joe
	 */
    protected final void handleGateCloseRequest() {
	log.info(id + ": gate close request received");

	GateStatus oldGateStatus = gateStatus;
	switch (gateStatus) {
	    case GateOpening:
		log.info(id + ": gate is opening.  Change direction.");
		// falls through

	    case GateOpened:
		log.info(id + ": send signal to close the gate now.");
		sendGateCloseSignal();
		gateStatus = GateStatus.GateClosing;
		break;

	    case GateClosing:
		log.warning(id + ": gate is already closing!!  Ignore request.");
		break;

	    case GateClosed:
		log.warning(id + ": gate is already closed!!  Ignore request.");
		break;
	}

	if (oldGateStatus != gateStatus) {
	    log.fine(id + ": gate status change: " + oldGateStatus + " --> " + gateStatus);
	}
    } // handleGateCloseRequest


    //------------------------------------------------------------
    // handleGateOpenReply


	/**
	 * Handle Gate Open reply
	 * @author Joe
	 */
	protected final void handleGateOpenReply() {
	log.info(id + ": gate open reply received");

	GateStatus oldGateStatus = gateStatus;
	switch (gateStatus) {
	    case GateOpening:
		log.info(id + ": inform PCS Core that gate has finished opening.");
		pcsCore.send(new Msg(id, mbox, Msg.Type.GateOpenReply, ""));
		gateStatus = GateStatus.GateOpened;
		break;

	    case GateOpened:
		log.warning(id + ": gate is already opened!!  Ignore reply.");
		break;

	    case GateClosing:
		log.warning(id + ": gate should be closing!!  *** CHK ***");
		break;

	    case GateClosed:
		log.warning(id + ": gate should be closed!!  *** CHK ***");
		break;
	}

	if (oldGateStatus != gateStatus) {
	    log.fine(id + ": gate status change: " + oldGateStatus + " --> " + gateStatus);
	}
    } // handleGateOpenReply


    //------------------------------------------------------------
    // handleGateCloseReply

	/**
	 * Handle Gate Close Reply
	 * @author Joe
	 */
    protected final void handleGateCloseReply() {
	log.info(id + ": gate close reply received");

	GateStatus oldGateStatus = gateStatus;
	switch (gateStatus) {
	    case GateOpening:
		log.warning(id + ": gate should be opening!!  *** CHK ***");
		break;

	    case GateOpened:
		log.warning(id + ": gate should be opened!!  *** CHK ***");
		break;

	    case GateClosing:
		log.info(id + ": inform PCS Core that gate has finished closing.");
		pcsCore.send(new Msg(id, mbox, Msg.Type.GateCloseReply, ""));
		gateStatus = GateStatus.GateClosed;
		break;

	    case GateClosed:
		log.warning(id + ": gate is already closed!!  Ignore reply.");
		break;
	}

	if (oldGateStatus != gateStatus) {
	    log.fine(id + ": gate status change: " + oldGateStatus + " --> " + gateStatus);
	}
    } // handleGateCloseReply


    //------------------------------------------------------------
    // handlePollReq

	/**
	 * Handle Pool Request
	 * @author Joe
	 */
    protected final void handlePollReq() {
	log.info(id + ": poll request received.  Send poll request to hardware.");
	sendPollReq();
    } // handlePollReq


    //------------------------------------------------------------
    // handlePollAck

	/**
	 * Handle Pool Acknowledgement
	 * @author Joe
	 */
    protected final void handlePollAck() {
	log.info(id + ": poll ack received.  Send poll ack to PCS Core.");
	pcsCore.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
    } // handlePollAck


    //------------------------------------------------------------
    // sendGateOpenSignal

	/**
	 * Send Gate Open Singal
	 * @author Joe
	 */
    protected void sendGateOpenSignal() {
	// fixme: send gate open signal to hardware
	log.info(id + ": sending gate open signal to hardware.");
    } // sendGateOpenSignal


    //------------------------------------------------------------
    // sendGateCloseSignal

	/**
	 * Send Gate Close Signal
	 * @author Joe
	 */
    protected void sendGateCloseSignal() {
	// fixme: send gate close signal to hardware
	log.info(id + ": sending gate close signal to hardware.");
    } // sendGateCloseSignal


    //------------------------------------------------------------
    // sendPollReq

	/**
	 * Send Pool Request
	 */
    protected void sendPollReq() {
	// fixme: send gate poll request to hardware
	log.info(id + ": poll request received");
    } // sendPollReq




    //------------------------------------------------------------
    // Gate Status

	/**
	 * Gate Status
	 * @author Joe
	 */
    private enum GateStatus {
	GateOpened,
	GateClosed,
	GateOpening,
	GateClosing,
    }
} // GateHandler
