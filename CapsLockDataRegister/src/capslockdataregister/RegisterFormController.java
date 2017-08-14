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

    @FXML Label AssignedUUID;
    @FXML Label ErrorMsg;
    @FXML TextField nameRawString;
    @FXML TextField executableRawString;
    @FXML TextField versionRawString;
    @FXML TextField imageRawString;
    @FXML TextField movieRawString;
    @FXML Button RegisterButton;
    
    Stage ThisStage;
    
    private final JSONArray imagePathArray = new JSONArray();
    private final JSONArray moviePathArray = new JSONArray();
    private final Path CurrentDirectory = new File(".").getAbsoluteFile().toPath().getParent();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }
    
    public void onLoad(WindowEvent event){
        MainFormController.GameRecord record;
        
        try{
            record = (MainFormController.GameRecord)ThisStage.getUserData();
            if(record == null)throw new NullPointerException();
        }catch(Exception e){
            System.err.println(e);
            AssignedUUID.setText((UUID.randomUUID()).toString());
            nameRawString.setText("");
            executableRawString.setText("");
            versionRawString.setText("");
            imageRawString.setText("");
            movieRawString.setText("");
            return;
        }
         
        AssignedUUID.setText(record.uuidProperty().getValue());
        nameRawString.setText(record.nameProperty().getValue());
        executableRawString.setText(record.executableProperty().getValue());
        versionRawString.setText(record.versionProperty().getValue());
        imageRawString.setText(record.imageProperty().getValue());
        movieRawString.setText(record.movieProperty().getValue());
    }
    
    public void setOwnStage(Stage stage){ThisStage = stage;}
    
    @FXML
    protected void Register(){
        if(IsValidInput()){
            MainFormController.GameRecord record;
            
            try{
                record = (MainFormController.GameRecord)AssignedUUID.getScene().getWindow().getUserData();
                if(record == null)throw new NullPointerException();
            }catch(Exception e){
                AssignedUUID.getScene().getWindow().setUserData(new MainFormController.GameRecord(
                    AssignedUUID.getText(),
                    nameRawString.getText(),
                    executableRawString.getText(),
                    versionRawString.getText().equals("") ? "1" : versionRawString.getText(),
                    imagePathArray,
                    moviePathArray
                ));
                return;
            }
            
            record.Update(
                AssignedUUID.getText(),
                nameRawString.getText(),
                executableRawString.getText(),
                versionRawString.getText().equals("") ? "1" : versionRawString.getText(),
                imagePathArray,
                moviePathArray
            );
            ThisStage.close();
        }
    }
    
    private boolean IsValidInput(){
        boolean ReturnValue = true;
        String ErrorMessage = new String();
        
        if(nameRawString.getText().equals("")){
            ErrorMessage += "\nname フィールドが未入力";
            ReturnValue = false;
        }
        
        if(executableRawString.getText().equals("")){
            ErrorMessage += "\nexecutable フィールドが未入力";
            ReturnValue = false;
        }else{
            File file = new File(executableRawString.getText());
            if(!file.exists()){
                ErrorMessage += "\nexecutable に指定されたファイルはありません";
                ReturnValue = false;
            }
        }
        
        GenerateJSONArray(imagePathArray, imageRawString.getText());   
        GenerateJSONArray(moviePathArray, movieRawString.getText());
        
        ErrorMsg.setText("Verification " + (ReturnValue ? "pathed" : "rejected") + ErrorMessage);
        
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
                executableRawString.setText(CurrentDirectory.relativize(files.get(0).toPath()).toString());
            }
            
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
}