package PCS.VacancyHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
/**
 * Ticket Vacancy Handler Class.
 * The class will handle logic message from PCSCore, and it will also receive process logic commands from Vacancy
 * @author Gong Yikai
 */
public class VacancyHandler extends AppThread {
    /**
     * The PCSCore Box
     */
    protected final MBox pcsCore;
    /**
     * The Vacancy Status. We have one Status:
     * VacancyDisRunning
     */
    private  VacancyDisStatus vacancyDisStatus;

    /**
     * VacancyHandler Constructor
     *
     * @param id:A String ID of the Vacancy. For example, CollecotrHandler
     * @param appKickstarter: An appKickstarter
     *
     * @author Gong Yikai
     */
    public VacancyHandler(String id, AppKickstarter appKickstarter) {
        super(id, appKickstarter);
        pcsCore = appKickstarter.getThread("PCSCore").getMBox();
        vacancyDisStatus = VacancyDisStatus.VacancyDisRunning;
    } // VacancyDisHandler

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
    protected boolean processMsg(Msg msg){
        boolean quit = false;

        switch (msg.getType()) {
            case VacancyDisUpdateRequest:  handleVacancyDisUpdateRequest(msg);  break;
            case Poll:		   handlePollReq();	     break;
            case PollAck:	   handlePollAck();	     break;

            case Terminate:	     quit = true;break;


            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
        return quit;
    }

    /**
     * Handle Vacancy Display update request. 
     * @param msg:A msg received from processMsg(Msg msg) method
     * @author Gong Yikai
     */
    public void handleVacancyDisUpdateRequest(Msg msg){
        log.info(id + ": vacancy display update request received");
        //check Receive String Is correct or not.
        switch (vacancyDisStatus){
            case VacancyDisRunning:
                log.info(id+": Vacancy display is running. Show Updated Available Parking Spaces");
                //String[] spliInformation=msg.getDetails().split("\\s+");
                sendVacancyDisUpdateSignal();
                break;
            default:
                log.warning(id+" unknow vacancy display status ["+vacancyDisStatus+"]");
        }
    }


    /**
     * Send vacancy display update signal
     * @author Gong Yikai
     */
    public void sendVacancyDisUpdateSignal(){

        // fixme: send Vacancy Update signal to hardware
        log.info(id + ": sending vacancy update signal to hardware.");

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
     * Vacancy Status. One in total: VacancyDisRunning
     * @author Gong Yikai
     */
    private enum VacancyDisStatus {
        VacancyDisRunning,
    }
}
