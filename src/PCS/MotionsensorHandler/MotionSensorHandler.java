package PCS.MotionsensorHandler;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.AppThread;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import PCS.GateHandler.GateHandler;

/**
 * Motion Sensor Class
 * @author Chuanyang Zheng, ZHANG YIJIA
 */
public class MotionSensorHandler extends AppThread {

    /**
     * PCSCore Mbox
     */
    protected final MBox pcsCore;

    /**
     * Motion Sensor Status: MotionSensorRunning, MotionSensorTerminated
     */
    private MotionSensorStatus motionSensorStatus;

    /**
     * The floor the the motion sensor located
     */
    private int floorNumber;

    /**
     * If detectUp is true, the motion sensor detect up. ELse, it detect down
     */
    private boolean detectUp;

    /**
     * Motion Sensor Detect Constructor
     * @param id The ID of the Handler
     * @param appKickstarter Appkiskstarter
     * @param floorNumber The floor that the motion sensor lcoated
     * @param detectUp If detectUp is true, the motion sensor detect up. ELse, it detect down
     */
    public MotionSensorHandler(String id, AppKickstarter appKickstarter, int floorNumber, boolean detectUp) {
        super(id, appKickstarter);
        pcsCore = appKickstarter.getThread("PCSCore").getMBox();
        motionSensorStatus = MotionSensorStatus.MotionSensorRunning;
        this.floorNumber=floorNumber;
        this.detectUp=detectUp;
    } // MotionSensorHandler

    /**
     * run Part of handler. It deal with logic commands
     *
     * @author Yijia Zhang
     *
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
     *
     * @param msg:a message received
     * @return if message type is terminated, return false. Else, return true
     *
     * @author Chuanyang Zheng, ZHANG YIJIA
     */

    protected boolean processMsg(Msg msg) {
        boolean quit = false;

        switch (msg.getType()) {

         case Poll:		   handlePollReq();	     break;
         case PollAck:	   handlePollAck();	     break;
            case MotionSensorDetect:
                handleMotionSensorDetect(msg);
                break;
            case Terminate:	   quit = true;		     break;
            default:
                log.warning(id + ": unknown message type: [" + msg + "]");
        }
        return quit;
    } // processMsg

    /**
     * When motion sensors detect motion, send signal to handler. And the handler will use the method to deal with detection logic
     *
     * @param msg:Msg Received From Controller
     * @author Chuanyang Zheng
     */
    public void handleMotionSensorDetect(Msg msg){
        log.info(id + ": motion sensor detect message received");
        switch (motionSensorStatus){
            case MotionSensorRunning:
                if(this.detectUp) {
                    pcsCore.send(new Msg(id, mbox, Msg.Type.MotionSensorDetectUp, floorNumber + ""));
                    log.info(id+": "+floorNumber+" Detect Up. Inform PCSCore");
                }
                else {
                    pcsCore.send(new Msg(id, mbox, Msg.Type.MotionSensorDetectDown, floorNumber + ""));
                    log.info(id+": "+floorNumber+" Detect Up. Inform PCSCore");
                }
                break;
            case MotionSensorTerminated:
                log.warning(id+" is terminated. Please Check");
        }
    }
    /**
     * Handle Pool Request
     * @author ZHANG YIJIA
     */
    protected final void handlePollReq() {
        log.info(id + ": poll request received.  Send poll request to hardware.");
        sendPollReq();
    } // handlePollReq
    /**
     * Handle Pool Acknowledgement
     * @author ZHANG YIJIA
     */
    protected final void handlePollAck() {
        log.info(id + ": poll ack received.  Send poll ack to PCS Core.");
        pcsCore.send(new Msg(id, mbox, Msg.Type.PollAck, id + " is up!"));
    } // handlePollAck
    /**
     * Send Pool Request
     * *@author ZHANG YIJIA
     */
    protected void sendPollReq() {

        log.info(id + ": poll request received");
    } // sendPollReq


    /**
     * MotionSensor Status. Totally Two:running and terminated.
     * @author YiJia Zhang
     */

    private enum MotionSensorStatus {
        MotionSensorRunning,
        MotionSensorTerminated
    }
}
