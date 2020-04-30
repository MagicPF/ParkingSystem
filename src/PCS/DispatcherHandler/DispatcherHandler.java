package PCS.DispatcherHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import PCS.CollectorHandler.CollectorHandler;
import PCS.DispatcherHandler.Emulator.DispatcherEmulator;
import PCS.DispatcherHandler.DispatcherHandler;



/**
 * Ticket Dispatcher Handler Class.
 * The class will handle logic message from PCSCore, and it will also receive process logic commands from Dispatcher
 * @author Gong Yikai
 */
public class DispatcherHandler extends AppThread {
    /**
     * The PCSCore Box
     */
    protected final MBox pcsCore;
    /**
     * The Dispatcher Status. We have three Status:
     * idle,
     * waitPCSCoreReply,
     * waitForRemoval
     */
    private DispatcherStatus dispatcherStatus;
    protected int parkingFeeCoefficient;


    /**
     * DispatcherHandler Constructor
     *
     * @param id:A String ID of the Dispatcher. For example, DispatcherHandler
     * @param appKickstarter: An appKickstarter
     *
     * @author Gong Yikai
     */
    public DispatcherHandler(String id,AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        pcsCore = appKickstarter.getThread("PCSCore").getMBox();
        dispatcherStatus=DispatcherStatus.idle;
        this.parkingFeeCoefficient=Integer.parseInt(appKickstarter.getProperty("Ticket.calculateFeeCoefficient"));
    } // Dispatcher Handler Constructor



    /**
     * A method used to receive and judge Msg type
     * @author Gong Yikai
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


    /**
     * A method used to process message.
     * @param msg:a message received
     * @return if message type is terminated, return false. Else, return true
     * @author Gong Yikai
     */
    protected boolean processMsg(Msg msg) {
        boolean quit = false;
        DispatcherStatus oldStatus=dispatcherStatus;
        switch (msg.getType()) {
            case AddTicket:
                handleAddTicket(msg);
                break;

            case ReceiveTicketID:
                handleReceiveTicketID(msg);
                break;

            case RemoveTicket:
                handleRemoveTicket(msg);
                break;
            case Poll:		   handlePollReq();	     break;
            case PollAck:	   handlePollAck();	     break;

            case Terminate:	     quit = true;break;
        }
        return quit;
    }
    // processMsg



    /**
     * Handle AddTicket
     * @param msg Msg Received from processMsg when AddTicket Msg Type Receive
     * @author Gong Yikai
     */
    protected void handleAddTicket(Msg msg){
        log.info(id+": Handle Add Ticket");
        DispatcherStatus oldStatus=dispatcherStatus;
        switch (dispatcherStatus){
            case idle:
                SendAddTicket("Created new Ticket");
                pcsCore.send(new Msg(id, mbox, Msg.Type.AddTicket, ""));
                dispatcherStatus=DispatcherStatus.waitPCSCoreReply;
                break;
            case waitPCSCoreReply:
                log.warning(id+": Dispatcher is WaitPCSCoreReply! Ignore Add Ticket");
                break;
            case waitForRemoval:
                log.warning(id+": Dispatcher is WaitForRemoval! Ignore Add Ticket");
                break;
        }
        log.info(id+": Dispatcher status from ["+oldStatus+"] to ["+dispatcherStatus+"]");
    }

    /**
     * Handle ReceiveTicketID
     * @param msg Msg Received from processMsg when ReceiveTicketID Msg Type Receive
     * @author Gong Yikai
     */
    protected void handleReceiveTicketID(Msg msg){
        log.info(id+": Handle Receive Ticket ID");
        DispatcherStatus oldStatus=dispatcherStatus;
        switch (dispatcherStatus){
            case idle:
                log.warning(id+": Dispatcher is Idle. Ignore Receive Ticket ID");
                break;
            case waitPCSCoreReply:
                ReceiveTicketID(msg);
                dispatcherStatus=DispatcherStatus.waitForRemoval;
                break;
            case waitForRemoval:
                log.warning(id+": Dispatcher is WaitForRemoval! Ignore Reply");
                break;
        }
        log.info(id+": Dispatcher status from ["+oldStatus+"] to ["+dispatcherStatus+"]");
    }

    /**
     * Handle RemoveTicket
     * @param msg Msg Received from processMsg when RemoveTicket Msg Type Receive
     * @author Gong Yikai
     */
    protected void handleRemoveTicket(Msg msg){
        log.info(id+": Handle Remove");
        DispatcherStatus oldStatus=dispatcherStatus;
        switch (dispatcherStatus){
            case idle:
                log.warning(id+": Dispatcher is Idle. Ignore Remove Ticket");
                break;
            case waitPCSCoreReply:
                log.warning(id+": Dispatcher is Wait PCSCore Reply. Ignore Remove Ticket");
                break;
            case waitForRemoval:
                SendRemoveTicket(msg);
                log.fine("Remove Ticket");
                dispatcherStatus=DispatcherStatus.idle;
                break;
        }
        log.info(id+": Dispatcher status from ["+oldStatus+"] to ["+dispatcherStatus+"]");
    }

    /**
     * Inform PCSCore to add a ticket
     * @param mymsg information print
     * @author Gong Yikai
     */
    protected void SendAddTicket(String mymsg){
        log.info("Creating new Ticket");
    }

    /**
     * Get ticket ID to show on the emulator
     * @param  msg recieve
     * @author Gong Yikai
     */
    protected void ReceiveTicketID(Msg msg){
        log.info(msg.getDetails());
    }

    /**
     * Inform PCSCore the ticket is removed
     * @param msg receive
     * @author Gong Yikai
     */
    protected void SendRemoveTicket(Msg msg){
        pcsCore.send(new Msg(id,mbox,Msg.Type.RemoveTicket,"Remove Ticket Now"));
    }

    /**
     * Handle Pool Request
     * @author Gong Yikai
     */
    protected final void handlePollReq() {
        log.info(id + ": poll request received.  Send poll request to hardware.");
        sendPollReq();
    } // handlePollReq

    /**
     * handler Pool Acknowledgement
     * @author Gong Yikai
     */
    protected final void handlePollAck() {
        log.info(id + ": poll ack received.  Send poll ack to PCS Core.");
        pcsCore.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
    } // handlePollAck

    /**
     * Send Pool Request
     * @author Gong Yikai
     */
    protected void sendPollReq() {
        // fixme: send gate poll request to hardware
        log.info(id + ": poll request received");
    } // sendPollReq

    /**
     * Dispatcher status. Three in total: idle, waitPCSCoreReply, waitForRemoval
     * @author Gong Yikai
     */
    private enum DispatcherStatus {
        idle,
        waitPCSCoreReply,
        waitForRemoval
    }
} // DispatcherHandler
