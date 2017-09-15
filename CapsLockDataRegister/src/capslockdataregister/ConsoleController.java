package capslockdataregister;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class ConsoleController implements Initializable {
    
    private MainFormController ParentController;
    
    @FXML
    private TextField InputField;
    @FXML
    private TextArea OutputArea;
    
    private final PrintStream stdout = System.out;
    private final PrintStream stderr = System.err;
    private OutputStream OutStream; 
            
    /**
     * Initializes the controller class.
     */
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        OutStream = new OutputStream() {
            @Override
            public void write(int i) throws IOException{
                OutputArea.appendText(String.valueOf((char)i));
            }
        };
        
        final PrintStream stream = new PrintStream(OutStream);
        
        System.setOut(stream);
        System.setErr(stream);
    }   
    
    @FXML
    private void onEnter(ActionEvent event){
        final String SpaceParsedStringArray[] = InputField.getText().split(" ");
        InputField.setText("");

        switch(SpaceParsedStringArray[0]){
            case "":
                break;
            case "dump":
                Command.dump(SpaceParsedStringArray, ParentController);
                break;
            default:
                System.err.println('\"' + SpaceParsedStringArray[0] + "\" is invalid command");
                break;
        }
    }
    
    final void beforeCloseWindow(WindowEvent event){
        System.setOut(stdout);
        System.setErr(stderr);
    }

    final void setParentController(MainFormController controller) {
        ParentController = controller;
    }
}
