package PCS.VacancyHandler.Emulator;

import AppKickstarter.misc.Msg;
import PCS.PCSCore.PCSCore;
import PCS.VacancyHandler.VacancyHandler;
import PCS.PCSStarter;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Vacancy Emulator Combine by Vacancy Handler and Vacancy Controller
 * This class is mainly combined by Vacancy Handler and Vacancy Controller. It is a subclass of Vacancy Handler.
 *
 * @author Gong Yikai
 */
public class VacancyEmulator extends VacancyHandler {
    /**
     * It will be used in Start() method to create Vacancy GUI
     */
    private Stage myStage;
    /**
     * It will be used in Start() method to create Vacancy GUI
     */
    private VacancyEmulatorController vacancyEmulatorController;
    /**
     * PCS Start Object
     */
    private final PCSStarter pcsStarter;
    /**
     * The ID of the Vacancy Emulator
     */
    private final String id;

    /**
     * Auto pull
     */
    private boolean autoPoll;


    /**
     * Vacancy Constructor
     *
     * @param id         The ID Of Vacancy Emulator
     * @param pcsStarter PCS Starter Object
     */
    public VacancyEmulator(String id, PCSStarter pcsStarter) {
        super(id, pcsStarter);
        this.pcsStarter = pcsStarter;
        this.id = id + "Emulator";
        this.autoPoll = true;
    } // VacancyEmulator

    /**
     * Start A GUI
     * The method starts a GUI that is interactive with users
     *
     * @throws Exception throw Exception
     * @author Gong Yikai
     */
    public void start() throws Exception {
        Parent root;
        myStage = new Stage();
        FXMLLoader loader = new FXMLLoader();
        String fxmlName = "VacancyEmulator.fxml";
        loader.setLocation(VacancyEmulator.class.getResource(fxmlName));
        root = loader.load();
        vacancyEmulatorController = loader.getController();
        vacancyEmulatorController.initialize(super.id, pcsStarter, log, this);
        myStage.initStyle(StageStyle.DECORATED);
        myStage.setScene(new Scene(root, 420, 470));
        myStage.setTitle(id);
        myStage.setResizable(false);
        myStage.setOnCloseRequest((WindowEvent event) -> {
            pcsStarter.stopApp();
            Platform.exit();
        });
        myStage.show();
    } // VacancyEmulator

    /**
     * Deal with logic
     *
     * @param msg:a message received
     * @return if message type is terminated, return false. Else, return true
     * @author Gong Yikai
     */
    protected final boolean processMsg(Msg msg) {
        boolean quit = false;

        switch (msg.getType()) {
            case EmulatorAutoPollToggle:
                handleEmulatorAutoPollToggle();
                break;

            default:
                quit = super.processMsg(msg);
        }
        return quit;
    }



    /**
     * Handle Vacancy Display Update Request
     * Display vacancy information on the text area
     *
     * @author Gong Yikai
     */
    @Override
    public void handleVacancyDisUpdateRequest(Msg mymsg) {
        String[] str = mymsg.getDetails().split("\\s+");
        Date nowT = new Date(System.currentTimeMillis());
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        String timestr = PCSCore.getDate(nowT);
        vacancyEmulatorController.appendTextArea("This is a real time vacancy display:\n");
        StringBuilder floorNumberInformation = new StringBuilder();
        for (int i = 0; i < str.length; i++) {
            floorNumberInformation.append("Floor " + (i + 1) + ": " + str[i] + "\n");
        }
        floorNumberInformation.append("Last update time: " + timestr);
        vacancyEmulatorController.appendTextArea(floorNumberInformation.toString());
        log.info(id + "\n" + floorNumberInformation.toString());
    }
    /**
     * Handle vacancy Emulator Auto Poll Toggle
     *
     * @return return autoPoll
     * @author Gong Yikai
     */
    public final boolean handleEmulatorAutoPollToggle() {
        autoPoll = !autoPoll;
        logFine("Auto poll change: " + (autoPoll ? "off --> on" : "on --> off"));
        return autoPoll;
    } // handleGateEmulatorAutoPollToggle
    /**
     * Send Poll Request
     * @author Gong Yikai
     */
    protected void sendPollReq() {
        logFine("Poll request received.  [autoPoll is " + (autoPoll ? "on]" : "off]"));
        if (autoPoll) {
            logFine("Send poll ack.");
            mbox.send(new Msg(id, mbox, Msg.Type.PollAck, ""));
        }
    }
    /**
     * Log Fine Type Information and Add it to Controller
     *
     * @param logMsg:Log Msg
     * @author Gong Yikai
     */
    private final void logFine(String logMsg) {
        vacancyEmulatorController.appendTextArea("[FINE]: " + logMsg);
        log.fine(id + ": " + logMsg);
    } // logFine

    /**
     * Log Info Type Information and Add it to Controller
     *
     * @param logMsg:Log Msg
     * @author Gong Yikai
     */
    private final void logInfo(String logMsg) {
        vacancyEmulatorController.appendTextArea("[INFO]: " + logMsg);
        log.info(id + ": " + logMsg);
    } // logInfo
}
