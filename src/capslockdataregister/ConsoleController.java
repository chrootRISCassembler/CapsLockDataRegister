/*  
    This file is part of CapsLockDataRegister. CapsLockDataRegister is a JSON generator for CapsLock.
    Copyright (C) 2017 RISCassembler

    CapsLockDataRegister is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

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
