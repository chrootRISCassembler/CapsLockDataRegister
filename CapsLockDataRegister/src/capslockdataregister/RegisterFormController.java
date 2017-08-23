package capslockdataregister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @FXML TextField DescriptionTextField;
    @FXML TextField ExecutableTextField;
    @FXML TextField VersionTextField;
    @FXML TextField PanelTextField;
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
            ClearAllTextField();
            return;
        }
         
        AssignedUUIDLabel.setText(record.uuidProperty().getValue());
        NameTextField.setText(record.nameProperty().getValue());
        DescriptionTextField.setText(record.descriptionProperty().getValue());
        ExecutableTextField.setText(record.executableProperty().getValue());
        VersionTextField.setText(record.versionProperty().getValue());
        PanelTextField.setText(record.panelProperty().getValue());
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
                    DescriptionTextField.getText(),
                    ExecutableTextField.getText(),
                    VersionTextField.getText().equals("") ? "1" : VersionTextField.getText(),
                    PanelTextField.getText(),
                    imagePathArray,
                    moviePathArray
                ));
                ThisStage.close();
                return;
            }
            
            record.Update(AssignedUUIDLabel.getText(),
                NameTextField.getText(),
                DescriptionTextField.getText(),
                ExecutableTextField.getText(),
                VersionTextField.getText().equals("") ? "1" : VersionTextField.getText(),
                PanelTextField.getText(),
                imagePathArray,
                moviePathArray
            );
            ThisStage.close();
        }
    }
    
    private boolean IsValidInput(){
        boolean ReturnValue = true;
        String ErrorMessage = new String();
        
        System.err.println(NameTextField.getText());
        
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

        String[] files = RawString.split(",");
            
        Arrays.stream(files).forEach(element -> {
            File file = new File(element);
            if(file.exists())target.put(element);
        });
    }
    
    @FXML
    protected void TextFieldDragOver(DragEvent event){
        //accept only files.
        final Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }    
    }
    
    @FXML
    protected void TextFieldDropped(DragEvent event) {
	final Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            String DisplayString =  board.getFiles().stream()
                    .filter(file -> file.isFile())
                    .map(file -> CurrentDirectory.relativize(file.toPath()).toString())
                    .collect(Collectors.joining(","));

            ((TextField)event.getSource()).setText(DisplayString);
            
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
    
    @FXML
    protected void executableDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            final File ExecutablePath = board.getFiles().get(0);
            if(ExecutablePath != null){
                ExecutableTextField.setText(CurrentDirectory.relativize(ExecutablePath.toPath()).toString());
            }
            
            final Path GamesBaseDirectory = CurrentDirectory.relativize(ExecutablePath.toPath()).subpath(0, 2);
            System.err.println(GamesBaseDirectory.toString());
            
            if(ImageTextField.getText().equals("") || MovieTextField.getText().equals("")){
                try {
                    final List<Path> Media = Files.walk(GamesBaseDirectory, FileVisitOption.FOLLOW_LINKS)
                            .parallel()
                            .filter(file -> file.getFileName().toString().matches("__(description|panel|image|movie)__.*"))
                            .collect(Collectors.toList());
                    
                    if(NameTextField.getText().equals("") || DescriptionTextField.getText().equals("")){
                        Optional<Path> DescriptionFile = Media.stream()
                                .parallel()
                                .filter(file -> file.getFileName().toString().charAt(2) == 'd')
                                .findAny();
                        if(DescriptionFile.isPresent()){
                            final BufferedReader LineReader = new BufferedReader(new FileReader(DescriptionFile.get().toFile()));
                            {
                                String name = LineReader.readLine();
                                if(NameTextField.getText().equals(""))NameTextField.setText(name);
                            }
                            StringBuilder description = new StringBuilder();
                            String line;
                            while((line = LineReader.readLine()) != null){
                                description.append(line);
                            }
                            DescriptionTextField.setText(description.toString());
                        }
                    }
                    
                    if(PanelTextField.getText().equals("")){
                        Optional<Path> PanelFile = Media.stream()
                                .parallel()
                                .filter(file -> file.getFileName().toString().charAt(2) == 'p')
                                .findAny();
                        if(PanelFile.isPresent())PanelTextField.setText(PanelFile.get().toString());
                    }
                    
                    if(ImageTextField.getText().equals("")){
                        String Images = Media.stream()
                            .parallel()
                            .filter(file -> file.getFileName().toString().charAt(2) == 'i')
                            .map(file -> file.toString())
                            .collect(Collectors.joining(","));
                        ImageTextField.setText(Images);
                    }
                    
                    if(MovieTextField.getText().equals("")){
                        String Movies = Media.stream()
                            .parallel()
                            .filter(file -> file.getFileName().toString().charAt(2) == 'm')
                            .map(file -> file.toString())
                            .collect(Collectors.joining(","));
                        MovieTextField.setText(Movies);
                    }
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
            event.setDropCompleted(true);
	}else{
            event.setDropCompleted(false);
	}
    }
    
     @FXML
    protected void DescriptionFileDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            final File DescriptionFile = board.getFiles().get(0);
            final BufferedReader LineReader;
            try {
                LineReader = new BufferedReader(new FileReader(DescriptionFile));
            } catch (FileNotFoundException ex) {
                event.setDropCompleted(false);
                System.err.println(ex);
                return;
            }
            
            {
                String name;
                try {
                    name = LineReader.readLine();
                } catch (IOException ex) {
                    event.setDropCompleted(false);
                    System.err.println(ex);
                    return;
                }
                NameTextField.setText(name);
            }
            
            StringBuilder description = new StringBuilder();
            String line;
            try {
                while((line = LineReader.readLine()) != null){
                    description.append(line);
                }
            } catch (IOException ex) {
                event.setDropCompleted(false);
                System.err.println(ex);
                return;
            }
            DescriptionTextField.setText(description.toString());
        }
        event.setDropCompleted(true);
    }
    
    @FXML
    protected void PanelFileDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            final File PanelImage = board.getFiles().get(0);
            PanelTextField.setText(CurrentDirectory.relativize(PanelImage.toPath()).toString());
            event.setDropCompleted(true);
            return;
        }
        event.setDropCompleted(false);
    }
    
    private void ClearAllTextField(){
        Stream.of(
            NameTextField,
            DescriptionTextField,
            ExecutableTextField,
            VersionTextField,
            PanelTextField,
            ImageTextField,
            MovieTextField)
        .parallel()
        .forEach(field -> field.setText(""));
    }
}