package AppKickstarter.timer;

import AppKickstarter.misc.*;
import AppKickstarter.AppKickstarter;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


//======================================================================
// Timer
public class Timer extends AppThread {
    private static int simulationSpeed = 1000;
    private final int ticks;
    private static MBox timerMBox = null;
    private Ticker ticker = null;
    private ArrayList<ActiveTimer> timerList = null;
    private static long systemStartTime;

    //------------------------------------------------------------
    // Timer
    /**
     *  Constructor for the Timer (an appThread).
     * @param id the name of the timer (should be "Timer")
     * @param appKickstarter a reference to the AppKickstarter
     */
    public Timer(String id, AppKickstarter appKickstarter) {
	super(id, appKickstarter);
	ticker = new Ticker(getMBox());
	timerMBox = getMBox();
	timerList = new ArrayList<ActiveTimer>();
	ticks = Integer.parseInt(appKickstarter.getProperty("Timer.MSecPerTick"));
	simulationSpeed = Integer.parseInt(appKickstarter.getProperty("Timer.SimulationSpeed"));
    } // Timer


    //------------------------------------------------------------
    // run
    /**
     * Run method for the thread
     */
    public void run() {
	Thread.currentThread().setName(id);
	log.info(id + ": starting...");
	systemStartTime = System.currentTimeMillis();
	new Thread(ticker).start();

	boolean quit = false;
	while (!quit) {
	    Msg msg = mbox.receive();

	    switch (msg.getType()) {
		case Tick:
		    chkTimeout();
		    break;

		case SetTimer:
		    set(msg);
		    break;

		case CancelTimer:
		    cancel(msg);
		    break;

		case Terminate:
		    log.info(id + ": received terminate!");
		    ticker.stopTicker();
		    quit = true;
		    break;

		default:
		    String eMsg = "Invalid command for Timer: "+msg;
		    throw (new RuntimeException(eMsg));
	    }
	}

	// declaring our departure
	appKickstarter.unregThread(this);
	log.info(id + ": terminating...");
    } // run


    //------------------------------------------------------------
    // chkTimeout
    private void chkTimeout() {
	long currentTime = (new Date()).getTime();
	ArrayList<ActiveTimer> timeoutTimers = new ArrayList<ActiveTimer>();
	log.finest("Timer chk...");

	for (ActiveTimer timer : timerList) {
	    if (timer.timeout(currentTime)) {
		timeoutTimers.add(timer);
	    }
	}

	for (ActiveTimer timer : timeoutTimers) {
	    int timerID = timer.getTimerID();
	    String caller = timer.getCaller();
	    MBox mbox = timer.getMBox();
	    mbox.send(new Msg("Timer", null, Msg.Type.TimesUp, new TimerMsg(Msg.Type.TimesUp, timerID, 0).getMsg()));
	    timerList.remove(timer);
	}
    } // chkTimeout


    //------------------------------------------------------------
    // Ticker
    private class Ticker implements Runnable {
	private MBox timerMBox = null;
	private boolean quit = false;

	//----------------------------------------
	// ticker
	private Ticker(MBox timerMBox) {
	    this.timerMBox = timerMBox;
	} // Ticker


	//----------------------------------------
	// run
	public void run() {
	    Thread.currentThread().setName("Ticker");
	    while (!quit) {
		try {
		    Thread.sleep(ticks);
		} catch (Exception e) {};
		mbox.send(new Msg("Ticker", null, Msg.Type.Tick, "tick"));
	    }
	} // run


	//----------------------------------------
	// stopTicker
	private void stopTicker() {
	    quit = true;
	} // stopTicker
    } // Ticker


    //------------------------------------------------------------
    // ActiveTimer
    private static class ActiveTimer {
	private int  timerID;
	private long wakeupTime;
	private String caller;
	private MBox mbox;

	//----------------------------------------
	// ActiveTimer
	public ActiveTimer(int tid, long wakeupTime, String caller, MBox mbox) {
	    this.timerID = tid;
	    this.wakeupTime = wakeupTime;
	    this.caller = caller;
	    this.mbox = mbox;
	} // ActiveTimer

	//----------------------------------------
	// getters
	public int    getTimerID() { return this.timerID; }
	public String getCaller()  { return this.caller; }
	public MBox   getMBox()    { return this.mbox; }

	//----------------------------------------
	// timeout
	public boolean timeout(long currentTime) {
	    return currentTime > wakeupTime;
	} // timeout
    } // ActiveTimer


    //------------------------------------------------------------
    // setTimer (sleep time based on wall clock)
    /**
     * Static method for setting a timer based on the wall clock (timer ID is randomly generated for the caller).
     * @param id id of the caller
     * @param mbox mbox of the caller
     * @param sleepTime the sleep time (in millisecond)
     * @return the timerID
     */
    public static int setTimer(String id, MBox mbox, long sleepTime) {
	int timerID = (new Random()).nextInt(90000) + 10000;
	return setTimer(id, mbox, sleepTime, timerID);
    } // setTimer


    //------------------------------------------------------------
    // setTimer (sleep time based on wall clock)
    /**
     * Static method for setting a timer based on the wall clock.
     * @param id id of the caller
     * @param mbox mbox of the caller
     * @param sleepTime the sleep time (in millisecond)
     * @param timerID the timer Id
     * @return the timerID
     */
    public static int setTimer(String id, MBox mbox, long sleepTime, int timerID) {
	timerMBox.send(new Msg(id, mbox, Msg.Type.SetTimer, new TimerMsg(Msg.Type.SetTimer, timerID, sleepTime).getMsg()));
	return timerID;
    } // setTimer


    //------------------------------------------------------------
    // setSimulationTimer (sleep time based on simulation time)
    /**
     * Static method for setting a timer based on the simulation time (timer ID is randomly generated for the caller).
     * @param id id of the caller
     * @param mbox mbox of the caller
     * @param simulationSleepTimeInSeconds the sleep time (in seconds)
     * @return the timerID
     */
    public static int setSimulationTimer(String id, MBox mbox, long simulationSleepTimeInSeconds) {
	return setTimer(id, mbox, simulationSleepTimeInSeconds*simulationSpeed);
    } // setSimulationTimer


    //------------------------------------------------------------
    // setSimulationTimer (sleep time based on simulation time)
    /**
     * Static method for setting a timer based on the simulation time.
     * @param id id of the caller
     * @param mbox mbox of the caller
     * @param simulationSleepTimeInSeconds the sleep time (in seconds)
     * @param timerID the timer Id
     * @return the timerID
     */
    public static int setSimulationTimer(String id, MBox mbox, long simulationSleepTimeInSeconds, int timerID) {
	return setTimer(id, mbox, simulationSleepTimeInSeconds*simulationSpeed, timerID);
    } // setSimulationTimer


    //------------------------------------------------------------
    // getTimesUpMsgTimerId: returns timerID of a timeout msg (returns -1 on error)
    /**
     * Returns the timerID of the Timeout msg (based on the msg details).
     * @param msg the Timeout msg
     * @return the timerID of the Timeout msg
     */
    public static int getTimesUpMsgTimerId(Msg msg) {
        // get timerMsg
	TimerMsg timerMsg = new TimerMsg(msg.getDetails());

	// chk msg sender
	if (!msg.getSender().equals("Timer") || msg.getType() != Msg.Type.TimesUp || timerMsg.getType() != Msg.Type.TimesUp) {
	    return -1;
	}

	// return timerID
	return timerMsg.getTimerID();
    } // getTimesUpMsgTimerId


    //------------------------------------------------------------
    // getSimulationTime (in seconds)
    /**
     * Gets the simulation time
     * @return the simulation time
     */
    public static long getSimulationTime() {
	return (System.currentTimeMillis()-systemStartTime)/simulationSpeed;
    } // getSimulationTime


    //------------------------------------------------------------
    // set
    private void set(Msg msg) {
	// get timerMsg
	TimerMsg timerMsg = new TimerMsg(msg.getDetails());

	// get timerID
	int timerID = timerMsg.getTimerID();

	// get wakeup time
	long sleepTime = timerMsg.getSleepTime();
	long wakeupTime = (new Date()).getTime() + sleepTime;

	// get caller & mbox
	String caller = msg.getSender();
	MBox mbox = msg.getSenderMBox();

	// add this new timer to timer list
	timerList.add(new ActiveTimer(timerID, wakeupTime, caller, mbox));
	log.finest(id+": "+caller+" setting timer: "+ "["+sleepTime+"], ["+timerID+"]");
    } // set


    //------------------------------------------------------------
    // cancelTimer
    /**
     * Cancelling a timer.
     * @param id id of the caller
     * @param mbox mbox of the  caller
     * @param timerID timerID of the timer to be cancelled
     */
    public static void cancelTimer(String id, MBox mbox, int timerID) {
	timerMBox.send(new Msg(id, mbox, Msg.Type.CancelTimer, new TimerMsg(Msg.Type.CancelTimer, timerID, 0).getMsg()));
    } // cancelTimer


    //------------------------------------------------------------
    // cancel
    private void cancel(Msg msg) {
	// get timerMsg
        TimerMsg timerMsg = new TimerMsg(msg.getDetails());

	// get timerID
        int timerID = timerMsg.getTimerID();
        String caller = msg.getSender();

	ActiveTimer cancelTimer = null;

	for (ActiveTimer timer : timerList) {
	    if (timer.getTimerID() == timerID) {
		if (timer.getCaller().equals(caller)) {
		    cancelTimer = timer;
		    break;
		}
	    }
	}

	if (cancelTimer != null) {
	    timerList.remove(cancelTimer);
	    log.finest(id+": "+caller+" cancelling timer: "+"["+timerID+"]");
	} else {
	    log.warning(id+": "+caller+" cancelling timer: "+"["+timerID+"]"+ " TIMER NOT FOUND!!");
	}
    } // cancel


    //------------------------------------------------------------
    // TimerMsg
    /**
     * A class for handling the "details" field of a Timer Msg.
     */
    public static class TimerMsg {
	public final static String SetTmHdr = "SetTimer";
	public final static String CancelTmHdr = "CancelTimer";
	public final static String TimesUpHdr = "TimesUp";
	private final String msgHdr;
	private final Msg.Type type;
	private final int timerID;
	private final long sleepTime;


	//------------------------------------------------------------
	// TimerMsg
	/**
	 * Constructor of a TimerMsg (used for building the details
	 * field of a msg).
	 * @param type Msg type (must be SetTimer, CancelTimer or TimesUp)
	 * @param timerID the timer ID
	 * @param sleepTime the sleep time (in millisecond)
	 */
	public TimerMsg(Msg.Type type, int timerID, long sleepTime) {
	    this.type = type;
	    this.timerID = timerID;
	    this.sleepTime = sleepTime;
	    switch (this.type) {
		case SetTimer:	  msgHdr = SetTmHdr;	break;
		case CancelTimer: msgHdr = CancelTmHdr;	break;
		case TimesUp:	  msgHdr = TimesUpHdr;	break;
		default:
		    msgHdr = "Error";
		    ERROR("TimerMsg: unknown message type: " + this.type);
	    }
	} // TimerMsg


	//------------------------------------------------------------
	// TimerMsg (Format: msgHdr timerID sleepTime)
	/**
	 * Constructor of a TimerMsg (used for extracting data from
	 * the details field of a msg)
	 * @param msg the message
	 */
	public TimerMsg(String msg) {
	    String [] tokens = Lib.getTokens(msg);

	    // chk number of tokens
	    if (tokens.length != 3) {
		ERROR("TimerMsg: Unexpected number of tokens.  Msg: [" + msg + "] (" + tokens.length + " tokens)");
	    }

	    // get msgHdr
	    this.msgHdr = tokens[0];

	    // get type
	    switch (msgHdr) {
		case SetTmHdr:	  type = Msg.Type.SetTimer;	break;
		case CancelTmHdr: type = Msg.Type.CancelTimer;	break;
		case TimesUpHdr:  type = Msg.Type.TimesUp;	break;
		default:
		    type = Msg.Type.Error;
		    ERROR("TimerMsg: invalid msgHdr: " + msgHdr);
	    }

	    // get timerID
	    this.timerID = Integer.parseInt(tokens[1]);

	    // get sleepTime
	    this.sleepTime = Long.parseLong(tokens[2]);
	} // TimerMsg


	//------------------------------------------------------------
	// getMsgHdr
	/**
	 * Gets the header of the message
	 * @return the header of the message
	 */
	public String getMsgHdr() {
	    return msgHdr;
	} // getMsgHdr


	//------------------------------------------------------------
	// getType
	/**
	 * Gets the type of the message
	 * @return the type of the message
	 */
	public Msg.Type getType() {
	    return type;
	}


	//------------------------------------------------------------
	// getTimerID
	/**
	 * Gets the timer ID stored in the msg
	 * @return the timer ID stored in the msg
	 */
	public int getTimerID() {
	    return timerID;
	} // getTimerID


	//------------------------------------------------------------
	// getSleepTime
	/**
	 * Gets the sleep time stored in the msg
	 * @return the sleep time stored in the msg
	 */
	public long getSleepTime() {
	    return sleepTime;
	} // getSleepTime


	//------------------------------------------------------------
	// getMsg (Format: msgHdr timerID sleepTime)
	/**
	 * Gets the msg in String format (msgHdr timerID sleepTime)
	 * @return the msg in String format (msgHdr timerID sleepTime)
	 */
	public String getMsg() {
	    return String.format("%s %05d %d", msgHdr, timerID, sleepTime);
	} // getMsg


	//------------------------------------------------------------
	// toString
	/**
	 * Gets the msg in String format (essentially the same as getMsg)
	 * @return the msg in String format (essentially the same as getMsg)
	 */
	public String toString() {
	    return getMsg();
	} // toString


	//------------------------------------------------------------
	// ERROR
	/**
	 * For handling internal non-recoverable error: constructs the
	 * exception message, and throws a runtime exception.
	 * @param eMsg the exception message
	 */
	void ERROR(String eMsg) {
	    throw new RuntimeException(eMsg);
	} // ERROR
    } // TimerMsg
} // Timer
