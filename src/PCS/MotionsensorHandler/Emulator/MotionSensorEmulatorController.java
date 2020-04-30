package PCS.MotionsensorHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.util.logging.Logger;

/**
 * Motion Sensor Emulator Controller is an emulator of Motion Sensor
 * @author Chuanyang Zheng, ZHANGYIJIA
 */
public class MotionSensorEmulatorController {

    public Button autoPollButton;
    /**
     * The ID of Motion Sensor Emulator
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
     * Motion Sensor Emulator
     */
    private MotionSensorEmulator motionSensorEmulator;

    /**
     * Motion Sensor Emulator Mbox
     */
    private MBox motionSensorBox;

    /**
     * Motion Sensor Text Area will store received message of Controller
     */
    public TextArea motionSensorTextArea;

    /**
     * Count Message
     */
    private int lineNo = 0;

    /**
     *Initialize Controller
     * @param id The ID of Motion Sensor Controller
     * @param appKickstarter Appkiskstarter
     * @param log The logger
     * @param motionSensorEmulator Motion Sensor Emulator
     */
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, MotionSensorEmulator motionSensorEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.motionSensorEmulator = motionSensorEmulator;
        this.motionSensorBox = appKickstarter.getThread(id).getMBox();
    } // initialize

    /**
     * Receive Button Event from MotionSensorEmulator.fxml and process the messages
     * @param actionEvent ActionEvent from MotionSensorEmulator.fxml
     * @author Chuanyang Zheng,ZHANG YIJIA
     */
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Detect":
                motionSensorBox.send(new Msg(id, null, Msg.Type.MotionSensorDetect, "Motion Sensor Detect"));
                appendTextArea("Motion Sensor Detect");
                break;
            case "Poll Request":
                appendTextArea("Send poll request.");
                motionSensorBox.send(new Msg(id, null, Msg.Type.Poll, ""));
                break;
            case "Poll ACK":
                appendTextArea("Send poll ack.");
                motionSensorBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
                break;
            case "Auto Poll: On":
               Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
               motionSensorBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            case "Auto Poll: Off":
               Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
                motionSensorBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;



            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    //------------------------------------------------------------
    // appendTextArea

    /**
     * Apppend Text Area
     * @param status The meesage to be added to Motion Sensor Text Area
     * @author Chuanyang Zheng
     */
    public void appendTextArea(String status) {
        Platform.runLater(() -> motionSensorTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea

}
