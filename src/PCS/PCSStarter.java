package PCS;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.Msg;
import AppKickstarter.timer.Timer;

import PCS.CollectorHandler.CollectorHandler;
import PCS.DispatcherHandler.DispatcherHandler;
import PCS.DispatcherHandler.Emulator.DispatcherEmulator;
import PCS.MotionsensorHandler.MotionSensorHandler;
import PCS.PCSCore.PCSCore;
import PCS.GateHandler.GateHandler;

import PCS.PayMachineHandler.Emulator.PayMachineEmulator;
import PCS.PayMachineHandler.PayMachineHandler;
import PCS.VacancyHandler.VacancyHandler;
import javafx.application.Platform;

import java.util.ArrayList;


//======================================================================
// PCSStarter

/**
 * PCS Starter
 */
public class PCSStarter extends AppKickstarter {
    /**
     * Timer for wake up
     */
    protected Timer timer;

    /**
     * PCSCore from PCS Package
     */
    protected PCSCore pcsCore;

    /**
     * Dispatcher Handler
     */
    protected DispatcherHandler dispatcherHandler;

    /**
     * Exit Gate Handler
     */
    protected GateHandler exitGateHandler;

    /**
     * entranceGate Handler
     */
    protected GateHandler entranceGateHandler;

    /**
     * Collector Handler
     */
    protected CollectorHandler collectorHandler;

    /**
     * Vacancy Handler
     */
    protected VacancyHandler vacancyHandler;

    /**
     * Pay Machine List
     */
    protected ArrayList<PayMachineHandler> payMachineList = new ArrayList<PayMachineHandler>();

    /**
     * MotionSensor Detect Up List
     */
    protected  ArrayList<MotionSensorHandler> motionSensorDetectUpList=new ArrayList<>();

    /**
     * motionSensor Detect Down List
     */
    protected  ArrayList<MotionSensorHandler> motionSensorDetectDownList=new ArrayList<>();

    /**
     * How many pay Machines? If necessary, set the number in PCS.cfg
     */
    public int payMachineNumber;

    /**
     * How many floors? If necessary. set the number in PCS.cfg
     */
    public int numOfFloor=Integer.parseInt(this.getProperty("TotalFloorNumber"));

    //	public int PayMachineNumber;
    //------------------------------------------------------------
    // main

    /**
     * Create A PCSStart Object and run startApp()
     * @param args Arguments from user input.
     *             @author Chuanyang Zheng
     */
    public static void main(String[] args) {
        new PCSStarter().startApp();
    } // main


    //------------------------------------------------------------
    // PCSStart

    /**
     * PCSStarter Constructor
     * @author Chuanyang Zheng
     */
    public PCSStarter() {
        super("PCSStarter", "etc/PCS.cfg");
        payMachineNumber=Integer.parseInt(this.getProperty("PayMachineNumber"));
    } // PCSStart


    //------------------------------------------------------------
    // startApp

    /**
     * Start App method. Log information and start all handlers
     * @author Chuanyang Zheng
     */
    protected void startApp() {
        // start our application
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Starting...");

        startHandlers();
    } // startApp


    //------------------------------------------------------------
    // startHandlers

    /**
     * Start all handlers. First create all handlers. Then use create new Thread for each handlers and run start() method
     * @author Chuanyang Zheng
     */
    protected void startHandlers() {
        System.out.println("PCSStart!");
        payMachineNumber = Integer.parseInt(getProperty("PayMachineNumber"));
        // create handlers
        try {
            timer = new Timer("timer", this);
            pcsCore = new PCSCore("PCSCore", this);
            dispatcherHandler = new DispatcherHandler("DispatcherHandler", this);
            exitGateHandler = new GateHandler("ExitGateHandler", this);
            entranceGateHandler = new GateHandler("ExitGateHandler", this);
            collectorHandler = new CollectorHandler("CollectorHandler", this);
            vacancyHandler = new VacancyHandler("VacancyHandler", this);
            for (int i = 0; i < payMachineNumber; i++)
                payMachineList.add(new PayMachineHandler("PayMachineHandler " + Integer.toString(i), this));
            for(int i=0;i<numOfFloor;i++){
                motionSensorDetectUpList.add(new MotionSensorHandler("MotionSensorHandlerUp"+(i+1),this,(i+1),true));
                motionSensorDetectDownList.add(new MotionSensorHandler("MotionSensorHandlerDown"+(i+1),this,(i+1),false));
            }


        } catch (Exception e) {
            System.out.println("AppKickstarter: startApp failed");
            e.printStackTrace();
            Platform.exit();
        }

        // start threads
        new Thread(timer).start();
        new Thread(pcsCore).start();
        new Thread(dispatcherHandler).start();
        new Thread(exitGateHandler).start();
        new Thread(vacancyHandler).start();
        for (int i = 0; i < payMachineNumber; i++)
            new Thread(payMachineList.get(i)).start();
        for(int i=0;i<numOfFloor;i++){
            new Thread(motionSensorDetectUpList.get(i)).start();
            new Thread(motionSensorDetectDownList.get(i)).start();
        }

    } // startHandlers


    //------------------------------------------------------------
    // stopApp

    /**
     * Stop All handlers by send Terminate Msg
     * @author Chuanyang Zheng
     */
    public void stopApp() {
        log.info("");
        log.info("");
        log.info("============================================================");
        log.info(id + ": Application Stopping...");
        pcsCore.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        exitGateHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        entranceGateHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        collectorHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        dispatcherHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        vacancyHandler.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        for (int i = 0; i < payMachineList.size(); i++) {
            payMachineList.get(i).getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
        }
        for(int i=0;i<numOfFloor;i++){
            motionSensorDetectUpList.get(i).getMBox().send(new Msg(id,null,Msg.Type.Terminate,"Terminate now!"));
            motionSensorDetectDownList.get(i).getMBox().send(new Msg(id,null,Msg.Type.Terminate,"Terminate now!"));
        }
        timer.getMBox().send(new Msg(id, null, Msg.Type.Terminate, "Terminate now!"));
    } // stopApp
} // PCS.PCSStarter
