package capslockdataregister;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.json.JSONArray;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class RegisterFormController implements Initializable {

    @FXML Label AssignedUUIDLabel;
    @FXML Label ErrorMsgLabel;
    @FXML TextField NameTextField;
    @FXML TextField ExecutableTextField;
    @FXML TextField VersionTextField;
    @FXML TextField ImageTextField;
    @FXML TextField MovieTextField;
    @FXML Button RegisterButton;
    
    Stage ThisStage;
    
    private final JSONArray imagePathArray = new JSONArray();
    private final JSONArray moviePathArray = new JSONArray();
    private final Path CurrentDirectory = new File(".").getAbsoluteFile().toPath().getParent();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
    public void onLoad(WindowEvent event){
        GameRecord record;
        
        try{
            record = (GameRecord)ThisStage.getUserData();
            if(record == null)throw new NullPointerException();
        }catch(Exception e){
            System.err.println(e);
            AssignedUUIDLabel.setText((UUID.randomUUID()).toString());
            NameTextField.setText("");
            ExecutableTextField.setText("");
            VersionTextField.setText("");
            ImageTextField.setText("");
            MovieTextField.setText("");
            return;
        }
         
        AssignedUUIDLabel.setText(record.uuidProperty().getValue());
        NameTextField.setText(record.nameProperty().getValue());
        ExecutableTextField.setText(record.executableProperty().getValue());
        VersionTextField.setText(record.versionProperty().getValue());
        ImageTextField.setText(record.imageProperty().getValue());
        MovieTextField.setText(record.movieProperty().getValue());
    }
    
    public void setOwnStage(Stage stage){ThisStage = stage;}
    
    @FXML
    protected void Register(){
        if(IsValidInput()){
            GameRecord record;
            
            try{
                record = (GameRecord)ThisStage.getUserData();
                if(record == null)throw new NullPointerException();
            }catch(Exception e){
                ThisStage.setUserData(new GameRecord(
                    AssignedUUIDLabel.getText(),
                    NameTextField.getText(),
                    ExecutableTextField.getText(),
                    VersionTextField.getText().equals("") ? "1" : VersionTextField.getText(),
                    imagePathArray,
                    moviePathArray
                ));
                return;
            }
            
            record.Update(AssignedUUIDLabel.getText(),
                NameTextField.getText(),
                ExecutableTextField.getText(),
                VersionTextField.getText().equals("") ? "1" : VersionTextField.getText(),
                imagePathArray,
                moviePathArray
            );
            ThisStage.close();
        }
    }
    
    private boolean IsValidInput(){
        boolean ReturnValue = true;
        String ErrorMessage = new String();
        
        if(NameTextField.getText().equals("")){
            ErrorMessage += "\nname フィールドが未入力";
            ReturnValue = false;
        }
        
        if(ExecutableTextField.getText().equals("")){
            ErrorMessage += "\nexecutable フィールドが未入力";
            ReturnValue = false;
        }else{
            File file = new File(ExecutableTextField.getText());
            if(!file.exists()){
                ErrorMessage += "\nexecutable に指定されたファイルはありません";
                ReturnValue = false;
            }
        }
        
        GenerateJSONArray(imagePathArray, ImageTextField.getText());   
        GenerateJSONArray(moviePathArray, MovieTextField.getText());
        
        ErrorMsgLabel.setText("Verification " + (ReturnValue ? "pathed" : "rejected") + ErrorMessage);
        
        return ReturnValue;
    }
    
    private void GenerateJSONArray(JSONArray target, String RawString){

        String[] files = RawString.split(", ");
            
        Arrays.stream(files).forEach(element -> {
            File file = new File(element);
            if(file.exists())target.put(element);
        });
    }
    
    @FXML
    protected void TextFieldDragOver(DragEvent event){
        //accept only files.
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }    
    }
    
    @FXML
    protected void TextFieldDropped(DragEvent event) {
	Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            List<Path> RelativePathList = new ArrayList();
            board.getFiles().stream().forEach((f) -> {
                System.out.println(f.getPath());
                if(f.isFile())RelativePathList.add(CurrentDirectory.relativize(f.toPath()));
            });
            String ArrayNotation = RelativePathList.toString();

            ((TextField)event.getSource()).setText(ArrayNotation.substring(1, ArrayNotation.length() - 1));
            
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
    
    @FXML
    protected void executableDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            List<File> files = board.getFiles();
            if(!files.isEmpty()){
                ExecutableTextField.setText(CurrentDirectory.relativize(files.get(0).toPath()).toString());
            }
            
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
}