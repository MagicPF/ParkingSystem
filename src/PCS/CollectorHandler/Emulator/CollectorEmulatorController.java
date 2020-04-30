package PCS.CollectorHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import PCS.PCSStarter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Collector Controller is an Emulator of Real Collector Hardware
 * @author Chuanyang Zheng
 */
public class CollectorEmulatorController {

    /**
     * ID Get from Collector Emulator
     */
    private String id;

    /**
     * AppKickstarter Object
     */
    private AppKickstarter appKickstarter;

    /**
     * Logger from Collector Emulator
     */
    private Logger log;

    /**
     * Collector Emulator Object
     */
    private CollectorEmulator collectorEmulator;

    /**
     * collector Mox. Will be used to send messages
     */
    private MBox collectorMBox;

    /**
     * Collector Text Area. Collector Emulator will add message to here
     */

    public TextArea collectorTextArea;

    /**
     * Input Area of Ticket ID
     */
    public TextArea collectorTextAreaInput;

    /**
     * Count How many Messages are added.
     */
    private int lineNo = 0;

    /**
     * Auto Poll Button
     */
    public Button autoPollButton;

    /**
     * Initialize Collector Controller GUI
     * @param id:Handler ID
     * @param appKickstarter; AppKickstarter in PCSEmulator Starter
     * @param log: Logger in Collector Emulator
     * @param collectorEmulator:CollectorEmulator
     * @author Chuanyang Zheng
     */
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, CollectorEmulator collectorEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.collectorEmulator =collectorEmulator ;
        this.collectorMBox = appKickstarter.getThread("CollectorHandler").getMBox();
    } // initialize

    /**
     * Button Information in CollectorEmulator.fxm
     * @param actionEvent:Button Event
     * @author Chuanyang Zheng
     */
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Collector Valid Request":
                collectorMBox.send(new Msg(id, null, Msg.Type.CollectorValidRequest,collectorTextAreaInput.getText()));
                appendTextArea("Collector Valid Ticket ["+collectorTextAreaInput.getText()+"]");
                log.info(id+"Collector Valid Ticket ["+collectorTextAreaInput.getText()+"]");
                collectorTextAreaInput.setText("");
                break;

            case "Collector Positive":
                collectorMBox.send(new Msg(id, null, Msg.Type.CollectorPositive, "Collector Positive"));
                break;

            case "Collector Negative":
                collectorMBox.send(new Msg(id, null, Msg.Type.CollectorNegative, "Collector Negative"));
                break;

            case "Collector Solve Problem":
                appendTextArea("Press Solve Problem Button");
                collectorMBox.send(new Msg(id, null, Msg.Type.CollectorSolveProblem, "Collector Solve Problem"));
                break;

            case "Poll Request":
                appendTextArea("Send poll request.");
                collectorMBox.send(new Msg(id, null, Msg.Type.Poll, ""));
                break;

            case "Poll ACK":
                appendTextArea("Send poll ack.");
                collectorMBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
                break;
            case "Auto Poll: On":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
                collectorMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            case "Auto Poll: Off":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
                collectorMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;


            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed

    //------------------------------------------------------------
    // appendTextArea

    /**
     * Append Information to Controller
     * @param status: Information
     * @author Chuanyang Zheng
     */
    public void appendTextArea(String status) {
        Platform.runLater(() -> collectorTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea

}
