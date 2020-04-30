package PCS.GateHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;


//======================================================================
// GateEmulatorController

/**
 * Gate Emulator Controller is a GateEmulator
 * @author Joe
 */
public class GateEmulatorController {

	/**
	 * The ID of Gate Emulator Controller
	 */
    private String id;

	/**
	 * Appkickstarter
	 */
	private AppKickstarter appKickstarter;

	/**
	 * Logger
	 */
    private Logger log;

	/**
	 * Gate Emulator
	 */
	private GateEmulator gateEmulator;

	/**
	 * Gate Box
	 */
    private MBox gateMBox;

	/**
	 * Gate Text Area contains button events
	 */
	public TextArea gateTextArea;

	/**
	 * Count line
	 */

	private int lineNo = 0;

	public Button autoOpenButton;
	public Button autoCloseButton;
	public Button autoPollButton;


    //------------------------------------------------------------
    // initialize

	/**
	 * Initialize Gate Emulator Controller
	 * @param id GateEmulatorController ID
	 * @param appKickstarter Appkickstarter
	 * @param log Logger
	 * @param gateEmulator Gate Emulator
	 * @author Joe
	 */
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, GateEmulator gateEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
	this.log = log;
	this.gateEmulator = gateEmulator;
	this.gateMBox = appKickstarter.getThread(id).getMBox();
    } // initialize


    //------------------------------------------------------------
    // buttonPressed

	/**
	 * Process Button Event
	 * @param actionEvent Action Event from GateEmulator.fxml
	 * @author Joe
	 */
    public void buttonPressed(ActionEvent actionEvent) {
	Button btn = (Button) actionEvent.getSource();

	switch (btn.getText()) {
	    case "Gate Open Request":
		gateMBox.send(new Msg(id, null, Msg.Type.GateOpenRequest, "GateOpenReq"));
		break;

	    case "Gate Open Reply":
		gateMBox.send(new Msg(id, null, Msg.Type.GateOpenReply, "GateOpenReply"));
		break;

	    case "Gate Close Request":
		gateMBox.send(new Msg(id, null, Msg.Type.GateCloseRequest, "GateCloseReq"));
		break;

	    case "Gate Close Reply":
		gateMBox.send(new Msg(id, null, Msg.Type.GateCloseReply, "GateCloseReply"));
		break;

	    case "Poll Request":
		appendTextArea("Send poll request.");
		gateMBox.send(new Msg(id, null, Msg.Type.Poll, ""));
		break;

	    case "Poll ACK":
		appendTextArea("Send poll ack.");
		gateMBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
		break;

		case "Auto Open: On":
			Platform.runLater(() -> autoOpenButton.setText("Auto Open: Off"));
			gateMBox.send(new Msg(id, null, Msg.Type.GateEmulatorAutoOpenToggle, "ToggleAutoOpen"));
			break;

		case "Auto Open: Off":
			Platform.runLater(() -> autoOpenButton.setText("Auto Open: On"));
			gateMBox.send(new Msg(id, null, Msg.Type.GateEmulatorAutoOpenToggle, "ToggleAutoOpen"));
			break;

		case "Auto Close: On":
			Platform.runLater(() -> autoCloseButton.setText("Auto Close: Off"));
			gateMBox.send(new Msg(id, null, Msg.Type.GateEmulatorAutoCloseToggle, "ToggleAutoClose"));
			break;

		case "Auto Close: Off":
			Platform.runLater(() -> autoCloseButton.setText("Auto Close: On"));
			gateMBox.send(new Msg(id, null, Msg.Type.GateEmulatorAutoCloseToggle, "ToggleAutoClose"));
			break;

		case "Auto Poll: On":
			Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
			gateMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
			break;

		case "Auto Poll: Off":
			Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
			gateMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
			break;

	    default:
	        log.warning(id + ": unknown button: [" + btn.getText() + "]");
		break;
	}
    } // buttonPressed


    //------------------------------------------------------------
    // appendTextArea

	/**
	 * Store Controller messages
	 * @param status Messages to be stored
	 * @author Joe
	 */
    public void appendTextArea(String status) {
	Platform.runLater(() -> gateTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea
} // GateEmulatorController
