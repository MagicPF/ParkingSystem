package PCS;

import AppKickstarter.misc.AppThread;
import AppKickstarter.timer.Timer;

import PCS.CollectorHandler.CollectorHandler;
import PCS.CollectorHandler.Emulator.CollectorEmulator;
import PCS.MotionsensorHandler.Emulator.MotionSensorEmulator;
import PCS.MotionsensorHandler.MotionSensorHandler;
import PCS.PCSCore.PCSCore;
import PCS.GateHandler.GateHandler;
import PCS.GateHandler.Emulator.GateEmulator;
import PCS.DispatcherHandler.Emulator.DispatcherEmulator;
import PCS.DispatcherHandler.DispatcherHandler;

import PCS.PayMachineHandler.Emulator.PayMachineEmulator;
import PCS.PayMachineHandler.PayMachineHandler;
import PCS.VacancyHandler.Emulator.VacancyEmulator;
import PCS.VacancyHandler.VacancyHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.ArrayList;

//======================================================================
// PCSEmulatorStarter

/**
 * Run the Program By Running The class
 * @author Chuanyang Zheng(17251311)
 */
public class PCSEmulatorStarter extends PCSStarter {
    //------------------------------------------------------------
    // main

    /**
     * PCS Emulator Main method.
     * Create a PCSEmulatorStart Object and run startApp() method
     * @param args Arguments from Users
     *             @author Chuanyang Zheng
     */
    public static void main(String [] args) {
	new PCSEmulatorStarter().startApp();
    } // main


    //------------------------------------------------------------
    // startHandlers


    @Override

    protected void startHandlers() {
        Emulators.pcsEmulatorStarter = this;
        new Emulators().start();
    } // startHandlers


    //------------------------------------------------------------
    // Emulators

    /**
     * Emulator class will start all Emulators
     * @author Chuanyang Zheng
     */
    public static class Emulators extends Application {
        private static PCSEmulatorStarter pcsEmulatorStarter;
        private int PayMachineNumber = pcsEmulatorStarter.payMachineNumber;


	//----------------------------------------
	// start
        public void start() {
            launch();
	} // start

	//----------------------------------------
	// start

        /**
         * Start all Emulators. First Create All Emulators. Then run corresponding GUIs by running start() method.
         * Finally, create new Thread and running all handlers
         * @param primaryStage Stage
         *                     @author Chuanyang Zheng
         */
        public void start(Stage primaryStage) {
	    Timer timer = null;
	    PCSCore pcsCore = null;
	    DispatcherEmulator dispatcherEmulator=null;
	    GateEmulator entranceGateEmulator = null;
        GateEmulator exitGateEmulator = null;
        CollectorEmulator collectorEmulator=null;
	    PayMachineEmulator payMachineEmulator = null;
	    VacancyEmulator vacancyEmulator=null;
	    ArrayList<PayMachineEmulator> payMachineEmulatorList = new ArrayList<PayMachineEmulator>();
        ArrayList<MotionSensorEmulator> motionSensorDetectUpList=new ArrayList<>();
        ArrayList<MotionSensorEmulator> motionSensorDetectDownList=new ArrayList<>();
	    // create emulators
	    try {
	        timer = new Timer("timer", pcsEmulatorStarter);
	        pcsCore = new PCSCore("PCSCore", pcsEmulatorStarter);
            dispatcherEmulator=new DispatcherEmulator("DispatcherHandler",pcsEmulatorStarter);
            collectorEmulator=new CollectorEmulator("CollectorHandler",pcsEmulatorStarter);
            vacancyEmulator=new VacancyEmulator("VacancyHandler", pcsEmulatorStarter);
	        entranceGateEmulator = new GateEmulator("EntranceGateHandler", pcsEmulatorStarter);
            exitGateEmulator = new GateEmulator("ExitGateHandler", pcsEmulatorStarter);
//            payMachineEmulator = new PayMachineEmulator("PayMachineHandler",pcsEmulatorStarter);
            for(int i = 0; i < PayMachineNumber; i++)
                payMachineEmulatorList.add(new PayMachineEmulator("PayMachineHandler" + Integer.toString(i), pcsEmulatorStarter));
            for(int i=0;i<pcsEmulatorStarter.numOfFloor;i++){
                motionSensorDetectUpList.add(new MotionSensorEmulator("MotionSensorHandlerUp"+(i+1),pcsEmulatorStarter,(i+1),true));
                motionSensorDetectDownList.add(new MotionSensorEmulator("MotionSensorHandlerDown"+(i+1),pcsEmulatorStarter,(i+1),false));
            }

            // start emulator GUIs
        dispatcherEmulator.start();
		entranceGateEmulator.start();
		exitGateEmulator.start();
		collectorEmulator.start();
		vacancyEmulator.start();
//		payMachineEmulator.start();
		for(int i = 0; i < PayMachineNumber; i++)
                payMachineEmulatorList.get(i).start();
        for(int i=0;i<pcsEmulatorStarter.numOfFloor;i++){
            motionSensorDetectUpList.get(i).start();
            motionSensorDetectDownList.get(i).start();
        }

	    } catch (Exception e) {
		System.out.println("Emulators: start failed");
		e.printStackTrace();
		Platform.exit();
	    }
	    pcsEmulatorStarter.setTimer(timer);
	    pcsEmulatorStarter.setPCSCore(pcsCore);
	    pcsEmulatorStarter.setDispatcherHandler(dispatcherEmulator);
	    pcsEmulatorStarter.setExitGateHandler(exitGateEmulator);
	    pcsEmulatorStarter.setEntranceGateHandler(entranceGateEmulator);
	    pcsEmulatorStarter.setCollectorHandler(collectorEmulator);
	    pcsEmulatorStarter.setVacancyHandler(vacancyEmulator);
	    pcsEmulatorStarter.setMotionSensor(motionSensorDetectUpList,motionSensorDetectDownList);
//	    pcsEmulatorStarter.setPayMachineHandler(payMachineEmulator);
         for(int i = 0; i < PayMachineNumber; i++)
             pcsEmulatorStarter.setPayMachineHandler(payMachineEmulatorList.get(i));


	    // start threads
	    new Thread(timer).start();
	    new Thread(pcsCore).start();
	    new Thread(entranceGateEmulator).start();
	    new Thread(exitGateEmulator).start();
	    new Thread(dispatcherEmulator).start();
	    new Thread(collectorEmulator).start();
	    new Thread(vacancyEmulator).start();
//	    new Thread(payMachineEmulator).start();
        for(int i = 0; i < PayMachineNumber; i++)
            new Thread(payMachineEmulatorList.get(i)).start();
        for(int i=0;i<motionSensorDetectUpList.size();i++){
            new Thread(motionSensorDetectUpList.get(i)).start();
            new Thread(motionSensorDetectDownList.get(i)).start();
        }


	} // start
    } // Emulators


    //------------------------------------------------------------
    //  setters

    /**
     * Set Timer Object
     * @param timer Set the time for PCSEmulator
     *              @author Chuanyang Zheng
     */
    private void setTimer(Timer timer) {
        this.timer = timer;
    }

    /**
     * Set PCSCore for PCSEmulator
     * @param pcsCore PCSCore for PCSEmulator
     *                @author Chuanyang Zheng
     */
    private void setPCSCore(PCSCore pcsCore) {
        this.pcsCore = pcsCore;
    }

    /**
     * Set Exit Gate Handler for PCSEmulator
     * @param gateHandler Exit Gate Handler for PCSEmulator
     *                    @author Chuanyang Zheng
     */
    private void setExitGateHandler(GateHandler gateHandler) {
	this.exitGateHandler = gateHandler;
    }

    /**
     * Set Entrance Gate Handler for PCSEmulator
     * @param gateHandler  Entrance Gate Handler for PCSEmulator
     *                     @author Chuanyang Zheng
     */
    private void setEntranceGateHandler(GateHandler gateHandler) {
        this.entranceGateHandler= gateHandler;
    }

    /**
     * Set Dispatcher Handler for PCSEmulator
     * @param dispatcherHandler Dispatcher Handler for PCSEmulator
     *                          @author Chuanyang Zheng
     */
    private void setDispatcherHandler(DispatcherHandler dispatcherHandler) {
        this.dispatcherHandler = dispatcherHandler;
    }

    /**
     * Set Collector Handler For PCSEmulator
     * @param collectorHandler Collector Handler For PCSEmulator
     *                         @author Chuanyang Zheng
     */
    private void setCollectorHandler(CollectorHandler collectorHandler){
        this.collectorHandler=collectorHandler;
    }

    /**
     * Set VacancyHandler for PCSEmulator
     * @param vacancyHandler VacancyHandler for PCSEmulator
     *                       @author Chuanyang Zheng
     */
    private void setVacancyHandler(VacancyHandler vacancyHandler){this.vacancyHandler=vacancyHandler;}
    private void setPayMachineHandler(PayMachineHandler payMachineHandler){
        this.payMachineList.add(payMachineHandler);
    }

    /**
     * Set MotionSensor DetectUpList and MotionSensor DetectDown List for PCSEmulator
     * @param motionSensorDetectUpList MotionSensor DetectUpList  for PCSEmulator
     * @param motionSensorDetectDownList MotionSensor DetectDown for PCSEmulator
     *                                   @author Chuanyang Zheng
     */
    private void setMotionSensor(ArrayList<MotionSensorEmulator> motionSensorDetectUpList,ArrayList<MotionSensorEmulator> motionSensorDetectDownList){
        this.motionSensorDetectUpList.addAll(motionSensorDetectUpList);
        this.motionSensorDetectDownList.addAll(motionSensorDetectDownList);
    }


} // PCSEmulatorStarter
