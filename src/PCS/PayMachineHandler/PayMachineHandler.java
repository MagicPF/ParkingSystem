package PCS.PayMachineHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.*;
import PCS.GateHandler.GateHandler;
import PCS.PCSStarter;
import PCS.PayMachineHandler.Emulator.PayMachineController;
import PCS.PayMachineHandler.Emulator.PayMachineEmulator;
import javafx.scene.control.Alert;


//======================================================================
// PayMachineHandler

/**
 * Pay Machine Handler
 * @author Pan Feng
 */
public class PayMachineHandler extends AppThread {
    protected final MBox pcsCore;
    protected PayMachineStatus PMS;
    protected int parkingFeeCoefficient;

    //------------------------------------------------------------
    // PayMachineHandler Constructor
    public PayMachineHandler(String id,AppKickstarter pcss) {
        super(id, pcss);
        pcsCore = appKickstarter.getThread("PCSCore").getMBox();
        PMS = PayMachineStatus.idle;
        parkingFeeCoefficient=Integer.parseInt(pcss.getProperty("Ticket.calculateFeeCoefficient"));
    } // Pay Machine Handler Constructor


    //------------------------------------------------------------
    // run
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
     * A Function to process the recieved Message
     * Switch the message type to different cases
     *  Handle it case by case
     * @param msg:The received message
     * @return Return true when Msg is not Terminated
     * @author Pan Feng
     */
    protected boolean processMsg(Msg msg) {
        boolean quit = false;
        boolean flag = false; // if processed successfully, true; Otherwise it is false
        String []tmp = msg.getDetails().split(",");
        switch (msg.getType()) {
            case TicketRequest:
                if(PMS == PayMachineStatus.idle) {
                    SendRequest(msg.getDetails()); // send fee calculation request to PCSCore
                    PMS = PayMachineStatus.WaitPaymentReply; // Update status
                    flag = true;
                }
            break;
            case TicketFee:
                if(PMS == PayMachineStatus.WaitPaymentReply) {
                    FeeReceive(msg.getDetails()); // Handle the message of Fee information for display
//                    PMS = PayMachineStatus.WaitDriver; // Update status
                    flag = true;
                }
            break;
            case PaymentACK:
                if(PMS == PayMachineStatus.WaitDriver) {
                    SendPaymentACK(msg.getDetails()); // Send Payment ACK to PCSCore
                    PMS = PayMachineStatus.WaitExitInfo; // Update
                    SendExitInfoRequest(msg.getDetails());// Send Exit Info Request to PCSCore
                    flag = true;
                }
            break;
            case ExitInfo:
                if(PMS == PayMachineStatus.WaitExitInfo) {
                    PMS = PayMachineStatus.WaitRemoval; // Update Status
                    ExitReceive(msg.getDetails()); // Update the panel display
                    flag = true;
                }
                break;
            case TicketRemoveACK:
                if(PMS == PayMachineStatus.WaitRemoval) {
                    PMS = PayMachineStatus.idle; // Update status
                    flag = true;
                    RemovalFinished();// Update the panel display
                }
            break;
            case Poll:		   handlePollReq();	flag = true;     break;
            case PollAck:	   handlePollAck();	flag = true;     break;
            case Terminate:	     quit = true;break;
        }
        if(flag) // if message proccessed successfully
            handleStatus(); // handle the status
        else
            log.warning(id + " is " + PMS + " cannot do it!!!"); // The current status cannot do that command
        return quit;
    }
    // processMsg

    //------------------------------------------------------------
    // SendRequest
    /**
     * A Function to process the received Message
     * @param mymsg:The received message
     * Send the request to PCSCore for fee.
     * @author Pan Feng
     */
    protected void SendRequest(String mymsg){
        pcsCore.send(new Msg(id, mbox, Msg.Type.TicketRequest, mymsg));
    }
    //SendRequest


    //------------------------------------------------------------
    // SendExitInfoRequest
    /**
     * A Function to send ExitInfo request
     *
     * @param mymsg:The received message
     * Send the request to PCSCore for ExitInfo.
     * @author Pan Feng
     */
    protected void SendExitInfoRequest(String mymsg){
        pcsCore.send(new Msg(id, mbox, Msg.Type.TicketExitInfoRequest, mymsg));
    }
    //SendExitInfoRequest

    //------------------------------------------------------------
    // RemovalFinished
    /**
     * A Function to display reminder
     * Display the Ticket is Removed
     * @author Pan Feng
     */
    protected void RemovalFinished(){
        log.info(id + "Ticket Removed");
    }
    //RemovalFinished

    //------------------------------------------------------------
    // FeeReceive
    /**
     * A Function to handle the Fee message
     *  Display the Fee is received
     *  Protocol message format: PayMachine ID,Ticket ID,TicketFee,EnterTime
     * @param mymsg Received Protocol Message
     * @author Pan Feng
     */
    protected void FeeReceive(String mymsg){
        String []str = mymsg.split(",");
        PMS = PayMachineStatus.WaitDriver; // Update status
    }
    //FeeReceive

    //------------------------------------------------------------
    // ExitReceive
    /**
     * A Function to handle the Exit Info
     *  Display the Exit time is received
     * @param mymsg Received protocol message
     * Protocol Message format is  PayMachine ID,Ticket ID,TicketFee,ExitTime
     * @author Pan Feng
     */
    protected void ExitReceive(String mymsg){
        log.info("Exit INfo received");
    }
    //ExitReceive

    //------------------------------------------------------------
    // SendPaymentACK
    /**
     * A Function to Display the PayMachine Status case by case
     * @param mymsg Received Protocol message
     * Protocol Format is Paymachine ID,Ticket ID
     * @author Pan Feng
     */
    protected void SendPaymentACK(String mymsg){
        log.fine(id+ ":ticket"+ mymsg + "Paid already.");
    }
    // SendPaymentACK


    //------------------------------------------------------------
    // handleStatus
    /**
     * A Function to Display the PayMachine Status case by case
     * @author Pan Feng
     */
    protected final void handleStatus() {

        PayMachineStatus oldStatus = PMS;
        switch (PMS) {
            case idle:
                log.info(id + ": I am idle please use me.");
                break;

            case WaitPaymentReply:
                log.info(id + ": I am waiting for Ticket Info");
                break;

            case WaitDriver:
                log.info(id + ": Waiting for driver's Payment");
                break;
            case WaitExitInfo:
                log.info(id + ": Waiting for ExitInfo");
                break;
            case WaitRemoval:
                log.info(id + ": Waiting for Removal");
                break;
        }

        if (oldStatus != PMS) {
            log.fine(id + ": gate status change: " + oldStatus + " --> " + PMS);
        }
    } // handleStatus

    //------------------------------------------------------------
    // handlePollReq

    /**
     * Handle Pool Request
     * @author Pan Feng
     */
    protected final void handlePollReq() {
        log.info(id + ": poll request received.  Send poll request to hardware.");
        sendPollReq();
    } // handlePollReq

    //------------------------------------------------------------
    // handlePollAck

    /**
     * Handle Pool Acknowledgement
     * @author Pan Feng
     */
    protected final void handlePollAck() {
        log.info(id + ": poll ack received.  Send poll ack to PCS Core.");
        pcsCore.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
    } // handlePollAck

    //------------------------------------------------------------
    // sendPollReq

    /**
     * Send Pool Request
     */
    protected void sendPollReq() {
        log.info(id + ": poll request received");
    } // sendPollReq




    //------------------------------------------------------------
    // PayMachineStatus
    protected enum PayMachineStatus {
        idle,
        WaitPaymentReply,
        WaitDriver,
        WaitExitInfo,
        WaitRemoval
    }// PayMachineStatus
} // PayMachineHandler
