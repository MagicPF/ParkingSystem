package PCS.GateHandler.Emulator;

import AppKickstarter.misc.*;
import AppKickstarter.timer.Timer;

import PCS.PCSStarter;
import PCS.GateHandler.GateHandler;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;


//======================================================================
// GateEmulator

/**
 * Gate Emulator
 * @author Joe
 */
public class GateEmulator extends GateHandler {

	/**
	 * State will be used in start() for creating GUI
	 */
    private Stage myStage;

	/**
	 * Gate Emulator Controller is an emulator of GUI
	 */
	private GateEmulatorController gateEmulatorController;

	/**
	 * PCSStarter
	 */
    private final PCSStarter pcsStarter;

	/**
	 * ID of the GateEmulator
	 */
	private final String id;

	/**
	 * How much time used to open gate
	 */
    private final int gateOpenTime;

	/**
	 * How much time used to close gate
	 */
	private final int gateCloseTime;

	/**
	 * Gate Open Timer ID for Timer wake up
	 */
    private final int GateOpenTimerID = 1;

	/**
	 * Gate Close Timer ID for Timer wake up
	 */
	private final int GateCloseTimerID = 2;

	/**
	 * Auto Open
	 */
    private boolean autoOpen;

	/**
	 * Auto Close
	 */
	private boolean autoClose;

	/**
	 * Auto Poll
	 */
    private boolean autoPoll;


    //------------------------------------------------------------
    // GateEmulator

	/**
	 * Gate Constructor
	 * @param id The ID of the Gate Emulator
	 * @param pcsStarter PCSStarter
	 * @author Joe
	 */
    public GateEmulator(String id, PCSStarter pcsStarter) {
	super(id, pcsStarter);
	this.pcsStarter = pcsStarter;
	this.id = id + "Emulator";
	this.gateOpenTime = Integer.parseInt(this.pcsStarter.getProperty("Gate.GateOpenTime"));
	this.gateCloseTime = Integer.parseInt(this.pcsStarter.getProperty("Gate.GateCloseTime"));
	this.autoOpen = true;
	this.autoClose = true;
	this.autoPoll = true;
    } // GateEmulator


    //------------------------------------------------------------
    // start

	/**
	 * Start Gate GUI
	 * @exception  Exception thorws Exception
	 * @author Joe
	 */
    public void start() throws Exception {
	Parent root;
	myStage = new Stage();
	FXMLLoader loader = new FXMLLoader();
	String fxmlName = "GateEmulator.fxml";
	loader.setLocation(GateEmulator.class.getResource(fxmlName));
	root = loader.load();
	gateEmulatorController = (GateEmulatorController) loader.getController();
	gateEmulatorController.initialize(super.getID(), pcsStarter, log, this);
	myStage.initStyle(StageStyle.DECORATED);
	myStage.setScene(new Scene(root, 420, 470));
	myStage.setTitle(id);
	myStage.setResizable(false);
	myStage.setOnCloseRequest((WindowEvent event) -> {
	    pcsStarter.stopApp();
	    Platform.exit();
	});
	myStage.show();
    } // GateEmulator


    //------------------------------------------------------------
    // processMsg

	/**
	 * Compared to GateHandler processMsg() method, add several new Msg cases
	 * @param msg Msg received from run() method
	 * @return If Msg type is Terminate, return true. ELse, return false
	 */
    protected final boolean processMsg(Msg msg) {
        boolean quit = false;

	switch (msg.getType()) {
	    case TimesUp:
	        handleTimesUp(msg);
	        break;

	    case GateEmulatorAutoOpenToggle:
		handleGateEmulatorAutoOpenToggle();
		break;

	    case GateEmulatorAutoCloseToggle:
		handleGateEmulatorAutoCloseToggle();
		break;

	    case EmulatorAutoPollToggle:
		handleGateEmulatorAutoPollToggle();
	        break;

	    default:
		quit = super.processMsg(msg);
	}
	return quit;
    } // processMsg


    //------------------------------------------------------------
    // sendGateOpenSignal
    @Override
    protected void sendGateOpenSignal() {
	logFine("Gate open signal received.  [autoOpen is " + (autoOpen ? "on]" : "off]"));
        if (autoOpen) {
	    logFine("Gate open timer started.");
	    Timer.setTimer(id, mbox, gateOpenTime, GateOpenTimerID);
	}
    } // sendGateOpenSignal


    //------------------------------------------------------------
    // sendGateCloseSignal
    @Override
    protected void sendGateCloseSignal() {
	logFine("Gate close signal received.  [autoClose is " + (autoClose ? "on]" : "off]"));
	if (autoClose) {
	    logFine("Gate close timer started.");
	    Timer.setTimer(id, mbox, gateCloseTime, GateCloseTimerID);
	}
    } // sendGateCloseSignal


    //------------------------------------------------------------
	// sendPollReq
	@Override
	protected void sendPollReq() {
		logFine("Poll request received.  [autoPoll is " + (autoPoll ? "on]" : "off]"));
		if (autoPoll) {
			logFine("Send poll ack.");
			mbox.send(new Msg(id, mbox, Msg.Type.PollAck, ""));
		}
	} // sendPollReq


    //------------------------------------------------------------
    // handleTimesUp

	/**
	 * Handle Timer Wake Up
	 * @param msg Msg receive from processMsg() method
	 * @author Joe
	 */
    public final void handleTimesUp(Msg msg) {
	logFine("Times up received.");

	switch (Timer.getTimesUpMsgTimerId(msg)) {
	    case GateOpenTimerID:
		logFine("Gate open timer is up.  [autoOpen is " + (autoOpen ? "on]" : "off]"));
	        if (autoOpen) {
		    logFine("Send gate open reply");
		    mbox.send(new Msg(id, mbox, Msg.Type.GateOpenReply, "Gate Open Reply"));
		} else {
	            // autoOpen is off.  just ignore timeout msg
		    logFine("Auto open is off.  Timer ignored.");
		}
		break;

	    case GateCloseTimerID:
		logFine("Gate close timer is up.  [autoClose is " + (autoClose ? "on]" : "off]"));
		if (autoClose) {
		    logFine("Send gate close reply");
		    mbox.send(new Msg(id, mbox, Msg.Type.GateCloseReply, "Gate Close Reply"));
		} else {
		    // autoClose is off.  just ignore timeout msg
		    logFine("Auto close is off.  Timer ignored.");
		}
		break;

	    default:
		logSevere("Unknown timer id!!");
	}
    } // handleTimesUp


    //------------------------------------------------------------
    // handleGateEmulatorAutoOpenToggle

	/**
	 * Handle Gate Emulator Auto Open Toggle
	 * @return return autoOpen
	 * @author Joe
	 */
    public final boolean handleGateEmulatorAutoOpenToggle() {
	autoOpen = !autoOpen;
	logFine("Auto open change: " + (autoOpen ? "off --> on" : "on --> off"));
	return autoOpen;
    } // handleGateEmulatorAutoOpenToggle


    //------------------------------------------------------------
    // handleGateEmulatorAutoCloseToggle

	/**
	 * Handle Gate Emulator Auto Close Toggle
	 * @return return autoClose
	 * @author Joe
	 */
    public final boolean handleGateEmulatorAutoCloseToggle() {
	autoClose = !autoClose;
	logFine("Auto close change: " + (autoClose ? "off --> on" : "on --> off"));
	return autoClose;
    } // handleGateEmulatorAutoCloseToggle


    //------------------------------------------------------------
    // handleGateEmulatorAutoPollToggle:

	/**
	 * Handle Gate Emulator Auto Poll Toggle
	 * @return return autoPoll
	 * @author Joe
	 */
    public final boolean handleGateEmulatorAutoPollToggle() {
        autoPoll = !autoPoll;
	logFine("Auto poll change: " + (autoPoll ? "off --> on" : "on --> off"));
        return autoPoll;
    } // handleGateEmulatorAutoPollToggle


    //------------------------------------------------------------
    // logFine

	/**
	 * Log Fine Type Information and add it to Controller
	 * @param logMsg Log Information
	 * @author Joe
	 */
    private final void logFine(String logMsg) {
	gateEmulatorController.appendTextArea("[FINE]: " + logMsg);
	log.fine(id + ": " + logMsg);
    } // logFine


    //------------------------------------------------------------
    // logInfo

	/**
	 * Log Infor Type Information and add it to Controller
	 * @param logMsg Log Information
	 * @author Joe
	 */
    private final void logInfo(String logMsg) {
	gateEmulatorController.appendTextArea("[INFO]: " + logMsg);
	log.info(id + ": " + logMsg);
    } // logInfo


    //------------------------------------------------------------
    // logWarning

	/**
	 * Log Warning Type Information and add it to Controller
	 * @param logMsg Log Information
	 * @author Joe
	 */
    private final void logWarning(String logMsg) {
	gateEmulatorController.appendTextArea("[WARNING]: " + logMsg);
	log.warning(id + ": " + logMsg);
    } // logWarning


    //------------------------------------------------------------
    // logSevere

	/**
	 * Log Severe Type Information and add it to Controller
	 * @param logMsg Log Information
	 * @author Joe
	 */
    private final void logSevere(String logMsg) {
	gateEmulatorController.appendTextArea("[SEVERE]: " + logMsg);
	log.severe(id + ": " + logMsg);
    } // logSevere
} // GateEmulator
