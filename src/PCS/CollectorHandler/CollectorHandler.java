package PCS.CollectorHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;

/**
 * Ticket Collector Handler Class.
 * The class will handle logic message from PCSCore, and it will also receive process logic commands from Collector
 * @author Chuanyang Zheng
 */
public class CollectorHandler extends AppThread {


    /**
     * The PCSCore Box
     */
    protected final MBox pcsCore;

    /**
     * The Collector Status. We have three Status:
     * CollectorAvailable,
     * CollectorWaitValidation,
     * CollectorWarning,
     */
    private CollectorStatus collectorStatus;

    /**
     * CollectorHandler Constructor
     *
     * @param id:A String ID of the Collector. For example, CollecotrHandler
     * @param appKickstarter: An appKickstarter
     *
     * @author Chuanyang Zheng
     */
    public CollectorHandler(String id, AppKickstarter appKickstarter){
        super(id, appKickstarter);
        pcsCore = appKickstarter.getThread("PCSCore").getMBox();
        collectorStatus = CollectorStatus.CollectorAvailable;
    }


    /**
     * A method used to receive and judge Msg type
     * @author Chuanyang Zheng
     */
    public void run() {
        Thread.currentThread().setName(id);
        log.info(id + ": starting...");

        for (boolean quit = false; !quit; ) {
            Msg msg = mbox.receive();

            log.fine(id + ": message received: [" + msg + "].");

            quit = processMsg(msg);
        }
    }


    /**
     * A method used to process message.
     * @param msg:a message received
     * @return if message type is terminated, return false. Else, return true
     * @author Chuanyang Zheng
     */
    protected boolean processMsg(Msg msg){
        boolean quit = false;

        switch (msg.getType()) {
            case CollectorValidRequest:  handleCollectorValidRequest(msg);  break;
            case CollectorPositive:   handleCollectorPositive();   break;
            case CollectorNegative: handleCollectorNegative(); break;
            case CollectorSolveProblem:	   handleCollctorSolveProblem();    break;
            case Poll:		   handlePollReq();	     break;
            case PollAck:	   handlePollAck();	     break;
            case Terminate:	   quit = true;		     break;
            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
        return quit;
    }

    /**
     * Handle Collector Valid Request. First Check Collector Status. If Collector is available, send valid request to PCSCore
     * @param msg:A msg received from processMsg(Msg msg) method
     * @author Chuanyang Zheng
     */
    protected void handleCollectorValidRequest(Msg msg){
        log.info(id + ": collector valid request received");
        CollectorStatus oldStatus=collectorStatus;

        switch (collectorStatus){
            case CollectorAvailable:
                int ticketID = -1;
                try {
                    try {
                        ticketID = Integer.parseInt(msg.getDetails());
                    } catch (Exception e) {
                        throw new Exception(id + ": collector receive invalid message: [" + msg.getDetails() + "]");
                    }
                    pcsCore.send(new Msg(id, mbox, Msg.Type.CollectorValidRequest, msg.getDetails()));
                    collectorStatus=CollectorStatus.CollectorWaitValidation;
                } catch (Exception e) {
                    sendParseIntFailSignal(e.getMessage());
                    e.printStackTrace();
                }
                break;

            case CollectorWaitValidation:
                log.warning(id+" Collector is waiting validation result. Ignore Validation Request");
                break;
            case CollectorWarning:
                log.warning(id+" Collector is warning. Ignore Validation Request");
        }
        log.fine(id+": Collector Status from "+oldStatus+"-> "+collectorStatus);
    }

    //** Input:Nothing*/
    //** Return:Nothing*/
    //A simple function. The ticket is valid. Therefore, we log received message.

    /**
     * PCSCore Tell Handler That The ticket is valid.
     * Therefore, we log received message. For append the mssage to Controller area, we will finish it in ConllectorEmulator Class.
     * @author Chuanyang Zheng
     */
    protected void handleCollectorPositive(){
        log.info(id + ": collector receive positive validation");
        CollectorStatus oldStatus=collectorStatus;
        switch (collectorStatus){
            case CollectorAvailable:
                log.warning(id+": Collector is Available Now. Wrong State!");
                break;
            case CollectorWaitValidation:
                collectorStatus=CollectorStatus.CollectorAvailable;
                sendPositiveSignal();
                break;
            case CollectorWarning:
                log.warning(id+": Collector is Warning Now. Wrong State!");
                break;
        }
        log.fine(id+": Collector Status from "+oldStatus+"-> "+collectorStatus);
    }



    /**
     *  PCS believe that the ticket is invalid. Therefore, in the method, ring alrams, ask staff to solve problem
     * @author Chuanyang Zheng
     */
    protected void handleCollectorNegative(){
        log.info(id+": collector receive negative validation");
        CollectorStatus oldStatus=collectorStatus;
        switch (collectorStatus){
            case CollectorAvailable:
                log.warning(id+": Collector is Available Now. Wrong State!");
                break;
            case CollectorWaitValidation:
                sendStartAlarmSignal();
                collectorStatus=CollectorStatus.CollectorWarning;
                break;
            case CollectorWarning:
                log.warning(id+": Collector is Warning Now. Wrong State");
        }
        log.fine(id+": Collector Status from "+oldStatus+"-> "+collectorStatus);
    }

    //**Input:No thing
    //**Return: Nothing
    //**After solve problem, we use the function to tell PCS Core.

    /**
     * After solve problem, we use the function to tell PCS Core.
     *
     * @author Chuanyang Zheng
     */
    protected void handleCollctorSolveProblem(){
        log.info(id+": collector receive Problem-Solve Message");
        CollectorStatus oldStatus=collectorStatus;
        switch (collectorStatus){
            case CollectorAvailable:
                log.warning(id+": collector is Now "+collectorStatus+" Wrong State!");
                break;
            case CollectorWaitValidation:
                log.warning(id+": collector is Now "+collectorStatus+" Wrong State!");
                break;
            case CollectorWarning:
                sendStopAlarmSignal();
                pcsCore.send(new Msg(id,mbox, Msg.Type.CollectorSolveProblem,""));
                log.info(id+" Inform PCSCore than already solve problem ");
                collectorStatus=CollectorStatus.CollectorAvailable;
                break;
        }
        log.fine(id+": Collector Status from "+oldStatus+"-> "+collectorStatus);

    }

    /**
     * Send Ring Alarm Signal To Collector Controller.
     * @author Chuanyang Zheng
     */
    protected void sendStartAlarmSignal(){
        log.fine(id+" Ring Alarm!");
    }

    /**
     * Solve Problem, Send Stop Alarm Signal To Collector
     * @author Chuanyang Zheng
     */
    protected void sendStopAlarmSignal(){
        log.fine(id+": Stop Alarm!");
    }

    /**
     * Handle Pool Request
     * @author Chuanyang Zheng
     */
    protected final void handlePollReq() {
        log.info(id + ": poll request received.  Send poll request to hardware.");
        sendPollReq();
    } // handlePollReq

    /**
     * Send Pool Request
     * @author Chuanyang Zheng
     */
    protected void sendPollReq() {
        // fixme: send gate poll request to hardware
        log.info(id + ": poll request received");
    } // sendPollReq

    /**
     * PCSCore Valid A true Ticket. CollectorHandler Sends Signal To Collector
     * @author Chuanyang Zheng
     */
    protected void sendPositiveSignal(){
        log.info(id+": Ticket is Valid!");
    }


    //------------------------------------------------------------
    // handlePollAck

    /**
     * handler Pool Acknowledgement
     * @author Chuanyang Zheng
     */
    protected final void handlePollAck() {
        log.info(id + ": poll ack received.  Send poll ack to PCS Core.");
        pcsCore.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
    } // handlePollAck

    /**
     * When Parse Int fail, log the information
     * @param information information will be logged
     * @author Chuanyang Zheng
     */
    protected void sendParseIntFailSignal(String information){
        log.warning(id+": "+information);
    }

    /**
     * Collector Status. Totally Three:CollectorAvailable,CollectorWaitValidation,CollectorWarning
     * @author Chuanyang Zheng
     */
    private enum CollectorStatus {
        CollectorAvailable,
        CollectorWaitValidation,
        CollectorWarning,
    }
}
