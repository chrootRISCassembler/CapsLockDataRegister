package capslockdataregister;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
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

import org.json.JSONArray;
import org.json.JSONWriter;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class FXMLController implements Initializable {

    @FXML Label AssignedUUID;
    @FXML Label ErrorMsg;
    @FXML TextField nameRawString;
    @FXML TextField executableRawString;
    @FXML TextField versionRawString;
    @FXML TextField imageRawString;
    @FXML TextField movieRawString;
    @FXML Button RegisterButton;
    
    private final JSONArray imagePathArray = new JSONArray();
    private final JSONArray moviePathArray = new JSONArray();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AssignedUUID.setText((UUID.randomUUID()).toString());
    }
    
    @FXML
    protected void Register(){
        
        FileWriter writer;
        
        try{
            writer = new FileWriter("GamesInfo.json");
        }catch(IOException e){
            System.out.println(e);
            return;
        }

        if(IsValidInput()){
            JSONWriter JsonWriter = new JSONWriter(writer)
                .object()
                .key("UUID")
                .value(AssignedUUID.getText())
                .key("name")
                .value(nameRawString.getText())
                .key("executable")
                .value(executableRawString.getText())
                .key("version")
                .value(versionRawString.getText().equals("") ? "1" : versionRawString.getText())
                .key("image")
                .value(imagePathArray)
                .key("movie")
                .value(moviePathArray)
                .endObject();
        }
        
        try{
            writer.close();
        }catch(IOException e){
            System.out.println(e);
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
            
        //if(files.length == 0)return;
            
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
            List<String> FileList = new ArrayList();
            board.getFiles().stream().forEach((f) -> {
                System.out.println(f.getPath());
                if(f.isFile())FileList.add(f.getPath());
            });
            String ArrayNotation = FileList.toString();
            System.out.println(ArrayNotation);
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
            if(files.size() == 1 && files.get(0).isFile()){
                System.out.println(files.get(0).toString());
                ((TextField)event.getSource()).setText(files.get(0).toString());
            }
            
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
}