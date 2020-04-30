package PCS.DispatcherHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.Optional;
import java.util.logging.Logger;


/**
 * Dispatcher Controller is an Emulator of Real Dispatcher Hardware
 * @author Gong Yikai
 */
public class DispatcherController {
    /**
     * ID Get from Dispatcher Emulator
     */
    private String id;
    /**
     * AppKickstarter Object
     */
    private AppKickstarter appKickstarter;
    /**
     * Logger from Dispatcher Emulator
     */
    private Logger log;
    /**
     * Dispatcher Emulator Object
     */
    private DispatcherEmulator DispatcherEmulator;
    /**
     * Dispatcher Mox. Will be used to send messages
     */
    private MBox dispatcherMBox;
    /**
     * Dispatcher Text Area. Dispatcher Emulator will add message to here
     */
    public TextArea DispatcherTextArea;
    /**
     * Ticket ID Area. Show ticket ID here
     */
    public TextArea TicketIDField;
    /**
     * Entertime Area. Show enter time here
     */
    public TextArea EnterField;
    /**
     * Count How many Messages are added.
     */
    private int lineNo = 0;
    /**
     * Record drivers' enter time
     */
    private String ticket_enter;
    /**
     * Auto Poll Button
     */
    public Button autoPollButton;
    /**
     * Initialize Dispatcher Controller GUI
     * @param id:Handler ID
     * @param appKickstarter; AppKickstarter in PCSEmulator Starter
     * @param log: Logger in Dispatcher Emulator
     * @param DispatcherEmulator:DispatcherEmulator
     * @author Gong Yikai
     */
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, PCS.DispatcherHandler.Emulator.DispatcherEmulator DispatcherEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.DispatcherEmulator = DispatcherEmulator;
        this.dispatcherMBox = appKickstarter.getThread(id).getMBox();
    } // initialize


    /**
     * Button Information in DispatcherEmulator.fxm
     * @param actionEvent:Button Event
     * @author Gong Yikai
     */
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Print a ticket":

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reminder");
                alert.setHeaderText("Pick up your ticket please.");
                alert.setContentText("Ticket is your basis to pay and leave.");

                alert.showAndWait();

                dispatcherMBox.send(new Msg(id, null, Msg.Type.AddTicket,"" ));
                //dispatcherMBox.send(new Msg(id, null, Msg.Type.ReceiveID, id ));
                break;
            case "Remove ticket":


                dispatcherMBox.send(new Msg(id, null, Msg.Type.RemoveTicket, id ));
                //dispatcherMBox.send(new Msg(id, null, Msg.Type.ReceiveID, id ));
                break;
            case "Poll Request":
                appendTextArea("Send poll request.");
                dispatcherMBox.send(new Msg(id, null, Msg.Type.Poll, ""));
                break;

            case "Poll ACK":
                appendTextArea("Send poll ack.");
                dispatcherMBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
                break;
            case "Auto Poll: On":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
                dispatcherMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            case "Auto Poll: Off":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
                dispatcherMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    /**
     * Append Information to Controller
     * @param status: Information
     * @author Gong Yikai
     */
    public void appendTextArea(String status) {
        Platform.runLater(() -> DispatcherTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea

    /**
     * Show information on the GUI
     * @param tmpid : ticketID
     * @param tmpenter : enter time
     * @author Gong Yikai
     */
    public void showTicket(String tmpid, String tmpenter){
        ticket_enter = tmpenter;
        TicketIDField.setText(tmpid);
        EnterField.setText(ticket_enter);
    }
} // DispatcherEmulatorController
