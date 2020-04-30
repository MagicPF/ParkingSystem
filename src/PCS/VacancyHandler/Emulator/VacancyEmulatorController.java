package PCS.VacancyHandler.Emulator;

import AppKickstarter.AppKickstarter;
import AppKickstarter.misc.MBox;
import AppKickstarter.misc.Msg;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

import java.util.logging.Logger;

/**
 * Vacancy Controller is an Emulator of Real Vacancy Hardware
 * @author Gong Yikai
 */
public class VacancyEmulatorController {
    /**
     * ID Get from Vacancy Emulator
     */
    private String id;
    /**
     * AppKickstarter Object
     */
    private AppKickstarter appKickstarter;
    /**
     * Logger from Vacancy Emulator
     */
    private Logger log;
    /**
     * Vacancy Emulator Object
     */
    private VacancyEmulator VacancyEmulator;
    /**
     * Vacancy Mox. Will be used to send messages
     */
    private MBox vacancyMBox;
    /**
     * Vacancy Text Area. Vacancy Emulator will add message to here
     */
    public TextArea VacancyTextArea;
    /**
     * Count How many Messages are added.
     */
    private int lineNo = 0;
    /**
     * Auto Poll Button
     */
    public Button autoPollButton;
    


    /**
     * Initialize Vacancy Controller GUI
     * @param id:Handler ID
     * @param appKickstarter; AppKickstarter in PCSEmulator Starter
     * @param log: Logger in Vacancy Emulator
     * @param VacancyEmulator:VacancyEmulator
     * @author Gong Yikai
     */
    public void initialize(String id, AppKickstarter appKickstarter, Logger log, PCS.VacancyHandler.Emulator.VacancyEmulator VacancyEmulator) {
        this.id = id;
        this.appKickstarter = appKickstarter;
        this.log = log;
        this.VacancyEmulator = VacancyEmulator;
        this.vacancyMBox = appKickstarter.getThread(id).getMBox();
    } // initialize


    /**
     * Button Information in VacancyEmulator.fxm
     * @param actionEvent:Button Event
     * @author Gong Yikai
     */
    public void buttonPressed(ActionEvent actionEvent) {
        Button btn = (Button) actionEvent.getSource();

        switch (btn.getText()) {
            case "Display Vacancy":


                vacancyMBox.send(new Msg(id, null, Msg.Type.DisplayVacancyRequest, id ));
                //VacancyMBox.send(new Msg(id, null, Msg.Type.ReceiveID, id ));
                break;
            case "Poll Request":
                appendTextArea("Send poll request.");
                vacancyMBox.send(new Msg(id, null, Msg.Type.Poll, ""));
                break;

            case "Poll ACK":
                appendTextArea("Send poll ack.");
                vacancyMBox.send(new Msg(id, null, Msg.Type.PollAck, ""));
                break;
            case "Auto Poll: On":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: Off"));
                vacancyMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
                break;

            case "Auto Poll: Off":
                Platform.runLater(() -> autoPollButton.setText("Auto Poll: On"));
                vacancyMBox.send(new Msg(id, null, Msg.Type.EmulatorAutoPollToggle, "ToggleAutoPoll"));
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
        Platform.runLater(() -> VacancyTextArea.appendText(String.format("[%04d] %s\n", ++lineNo, status)));
    } // appendTextArea

}
