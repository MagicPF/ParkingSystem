package PCS.PayMachineHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;

import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import javax.swing.*;


//======================================================================
// PayMachineController

/**
 * Pay Machine Handler
 * @author Pan Feng
 */
public class PayMachineController {
    /**
     * ID of the Controller
     */
    private String id;

    /**
     * Appkicsstarter
     */
    private AppKickstarter appKickstarter;

    /**
     * Logger
     */
    private Logger log;

    /**
     * Pay Machine Emulator
     */
    private PayMachineEmulator PayMachineEmulator;

    /**
     * Pay Machine Box
     */
    private MBox payMBox;

    /**
     * Text Area
     */
    public TextArea payTextArea;

    /**
     * For Input Ticket ID
     */
    public TextArea TicketIDField;

    /**
     * Show Fee
     */
    public TextArea FeeField;

    /**
     * Enter Timer
     */
    public TextArea EnterField;

    /**
     * Exit Tiime
     */
    public TextArea ExitField;
    public Button autoPollButton;
    private int lineNo = 0;

    /**
     * Store ticker_id,ticker_fee,ticket_time
     */
    private String ticket_id,ticket_fee,ticket_time;

    //------------------------------------------------------------
    // initialize
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, PayMachineEmulator PayMachineEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.PayMachineEmulator = PayMachineEmulator;
        this.payMBox = appKickstarter.getThread(id).getMBox();
    } // initialize


    //------------------------------------------------------------
    // buttonPressed
    /**
     *  Controller to handle whether there is a button pressed.
     *  @param actionEvent actionEvent received from panel
     *  Handle the process case by case based on the content of "pressed button"
     * @author Pan Feng
     */
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Insert ticket": // the insert ticket button pressed
                TextInputDialog dialog = new TextInputDialog("0"); // Dialog for input the ticket ID
                dialog.setTitle("Inserting Ticket....");
                dialog.setContentText("Please Input Ticket ID:");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) // if Inputed
                    ticket_id = result.get(); // get the inputs
                if(ticket_id == null || ticket_id.isEmpty()) // Check Validation
                    new Alert(Alert.AlertType.ERROR, "Please input valid ID :(", new ButtonType[]{ButtonType.OK}).show();
                else
                    payMBox.send(new Msg(id, null, Msg.Type.TicketRequest, id + "," + ticket_id)); // send the Fee request with Paymachine ID,Ticket ID
                break;
            case "Pay by Oct": // payment button pressed
                if(ticket_id == null || ticket_id.isEmpty())
                    new Alert(Alert.AlertType.ERROR, "Please insert first :)", new ButtonType[]{ButtonType.OK}).show(); // Check is ticket here
                else
                    payMBox.send(new Msg(id, null, Msg.Type.PaymentACK, id + "," + ticket_id)); // send payment request
                break;
            case "Remove ticket":
                payMBox.send(new Msg(id, null, Msg.Type.TicketRemoveACK, id + "," + ticket_id)); // send removal ACK
                break;
            case "Poll Request":
                appendTextArea("Send poll request.");
                payMBox.send(new Msg(id, null, Msg.Type.Poll, ""));
                break;
            case "Poll ACK":
                appendTextArea("Send poll ack.");
                payMBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
                break;
            case "Auto Poll: On":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
                payMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            case "Auto Poll: Off":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
                payMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            default:
                log.warning(id + ": unknown button: [" + btn.getText() + "]");
                break;
        }
    } // buttonPressed


    //------------------------------------------------------------
    // appendTextArea
    public void appendTextArea(String status) {
        Platform.runLater(() -> payTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea
    //------------------------------------------------------------
    // updateTicket
    /**
     * Function to update the Ticket Info in the upper textArea()
     *  @param tmpid the Current TicketID
     *  @param tmpfee the Current TicketFee
     *  @param tmptime the Received Time of the Ticket; if fee is 0 it is exit time, if fee is not 0 it is the enter time.
     *  Update the Display in GUI.
     * @author Pan Feng
     */

    //------------------------------------------------------------
    // updateTicket
    /**
     *
     * @param tmpid Ticket ID
     * @param tmpfee Ticket Fee
     * @param tmptime Ticket Time
     */
    public void updateTicket(String tmpid,String tmpfee, String tmptime){
        ticket_fee = tmpfee; ticket_time = tmptime;
        TicketIDField.setText(tmpid);
        FeeField.setText(ticket_fee);
        if(Float.parseFloat(tmpfee) == 0) // if the ticket is paid, the tmptime is exit time
            ExitField.setText(ticket_time);
        else {
            EnterField.setText(ticket_time); // update the enter time
            ExitField.setText("");
        }
    }//updateTicket

} // PayMachineController
