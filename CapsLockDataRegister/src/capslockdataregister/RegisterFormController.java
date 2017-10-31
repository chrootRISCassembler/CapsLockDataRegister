package capslockdataregister;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import org.json.JSONArray;

/**
 * ゲーム登録フォームのFXMLコントローラーclass.
 * <p>短縮規則</p>
 * <ul>
 * <li>description --&gt; desc</li>
 * <li>executable --&gt; exe</li>
 * <li>version --&gt; ver</li>
 * </ul>
 */
public class RegisterFormController implements Initializable {

    @FXML private Label AssignedUUIDLabel;
    @FXML private Label ErrorMsgLabel;
    @FXML private TextField NameTextField;
    @FXML private TextField DescTextField;
    @FXML private TextField ExeTextField;
    @FXML private TextField VerTextField;
    @FXML private TextField PanelTextField;
    @FXML private TextField ImageTextField;
    @FXML private TextField MovieTextField;
    @FXML private ImageView NameStateView;
    @FXML private ImageView DescStateView;
    @FXML private ImageView ExeStateView;
    @FXML private ImageView VerStateView;
    @FXML private ImageView PanelStateView;
    @FXML private ImageView ImageStateView;
    @FXML private ImageView MovieStateView;
    @FXML private Button RegisterButton;
    
    private Stage ThisStage;
    
    private final JSONArray imagePathArray = new JSONArray();
    private final JSONArray moviePathArray = new JSONArray();
    private final Path CurrentDirectory = new File(".").getAbsoluteFile().toPath().getParent();
    private LauncherResourceFilesValidator validator;
    
    static private final class FieldSet{
        private final static Image OKIcon = new Image(RegisterFormController.class.getResource("ok.png").toString());
        private final static Image NGIcon = new Image(RegisterFormController.class.getResource("ng.png").toString());
        private final static Image WarnIcon = new Image(RegisterFormController.class.getResource("warn.png").toString());
        
        private static enum State{
            OK,
            NG,
            WARN
        }
        private final TextField InputField;
        private final ImageView StateView;
        private State state;
        private final Function<String, State> ValidatingFunction;

        public FieldSet(State state, TextField InputField, ImageView StateView, Function<String, State> validator) {
            this.InputField = InputField;
            this.StateView = StateView;
            setState(state);
            ValidatingFunction = validator;
        }
        
        private void setState(State NewState){
            state = NewState;
            switch(state){
                case OK:
                    StateView.setImage(OKIcon);
                    return;
                case NG:
                    StateView.setImage(NGIcon);
                    return;
                case WARN:
                    StateView.setImage(WarnIcon);
            }
        }
        
        public final void validate(){
            setState(ValidatingFunction.apply(InputField.getText()));
        }
        
        public final boolean isValid(){
            return state != State.NG;
        }
    }

    private FieldSet NameFieldSet;
    private FieldSet DescFieldSet;
    private FieldSet ExeFieldSet;
    private FieldSet VerFieldSet;
    private FieldSet PanelFieldSet;
    private FieldSet ImageFieldSet;
    private FieldSet MovieFieldSet;
    
    private List<FieldSet> FieldSetList;
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        NameFieldSet = new FieldSet(FieldSet.State.WARN, NameTextField, NameStateView, name -> name.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK);
        DescFieldSet = new FieldSet(FieldSet.State.WARN, DescTextField, DescStateView, text -> text.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK);
        
        ExeFieldSet = new FieldSet(FieldSet.State.NG, ExeTextField, ExeStateView, 
                FileName -> {
                    final Path ExePath = Paths.get(FileName);
                    return Files.isExecutable(ExePath) ? FieldSet.State.OK : FieldSet.State.NG;
                }
        );
        
        VerFieldSet = new FieldSet(FieldSet.State.WARN, VerTextField, VerStateView, ver -> ver.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK);
        
        PanelFieldSet = new FieldSet(FieldSet.State.WARN, PanelTextField, PanelStateView,
                panel -> LauncherResourceFilesValidator.isSquareImage(panel) ? FieldSet.State.OK : FieldSet.State.WARN);
        
        ImageFieldSet = new FieldSet(FieldSet.State.WARN, ImageTextField, ImageStateView, 
                Images -> {
                    final String[] ImageStringArray = Images.split(",");
                    for(final String ImageString : ImageStringArray){
                        final Path ImagePath;
                        try{
                            ImagePath = Paths.get(ImageString.substring(1, ImageString.length() - 1));
                        }catch(IndexOutOfBoundsException e){
                            return FieldSet.State.WARN;
                        }
                        if(!Files.isRegularFile(ImagePath))return FieldSet.State.WARN;
                        try{
                            new Image(ImagePath.toUri().toString());
                        }catch(NullPointerException | IllegalArgumentException e){
                            return FieldSet.State.WARN;
                        }
                    }
                    return FieldSet.State.OK;
                });
        
        MovieFieldSet = new FieldSet(FieldSet.State.WARN, MovieTextField, MovieStateView,
                Movies -> {
                    final String[] MovieStringArray = Movies.split(",");
                    for(final String MovieString : MovieStringArray){
                        final Path MoviePath;
                        try{
                            MoviePath = Paths.get(MovieString.substring(1, MovieString.length() - 1));
                        }catch(IndexOutOfBoundsException e){
                            return FieldSet.State.WARN;
                        }
                        if(!Files.isRegularFile(MoviePath))return FieldSet.State.WARN;
                        try{
                            new Media(MoviePath.toUri().toString());
                        }catch(NullPointerException | IllegalArgumentException | UnsupportedOperationException | MediaException e){
                            return FieldSet.State.WARN;
                        }
                    }
                    return FieldSet.State.OK;
                });
        
        FieldSetList = Collections.unmodifiableList(Arrays.asList(
                NameFieldSet,
                DescFieldSet,
                ExeFieldSet,
                VerFieldSet,
                PanelFieldSet,
                ImageFieldSet,
                MovieFieldSet
        ));
    }
    
    void onLoad(WindowEvent event){
        GameRecord record;
        
        try{
            record = (GameRecord)ThisStage.getUserData();
            if(record == null)throw new NullPointerException();
            validator = ResourceFilesInputWrapper.instance.add(
                    UUID.fromString(record.uuidProperty().get()),
                    () -> new LauncherResourceFilesValidator(record.executableProperty().get())
            );
        }catch(NullPointerException e){
            System.err.println(e);
            AssignedUUIDLabel.setText((UUID.randomUUID()).toString());
            FieldSetList.parallelStream().forEach(field -> field.InputField.clear());
            return;
        }
         
        AssignedUUIDLabel.setText(record.uuidProperty().getValue());
        NameTextField.setText(record.nameProperty().getValue());
        DescTextField.setText(record.descriptionProperty().getValue());
        ExeTextField.setText(record.executableProperty().getValue());
        VerTextField.setText(record.versionProperty().getValue());
        PanelTextField.setText(record.panelProperty().getValue());
        ImageTextField.setText(record.imageProperty().getValue());
        MovieTextField.setText(record.movieProperty().getValue());
        
        FieldSetList.parallelStream().forEach(field -> field.validate());
    }
    
    final void setOwnStage(Stage stage){ThisStage = stage;}
    
    @FXML
    private void Register(){
        if(!IsValidInput())return;
        
        String GameName = NameTextField.getText();
        if(GameName.isEmpty()){
            final String ExeFileName = Paths.get(ExeTextField.getText()).getFileName().toString();
            GameName = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
        }
       
        final GameRecord record;
        
        try{
            record = (GameRecord)ThisStage.getUserData();
            if(record == null)throw new NullPointerException();
        }catch(NullPointerException e){
            ThisStage.setUserData(new GameRecord(
                AssignedUUIDLabel.getText(),
                GameName,
                DescTextField.getText(),
                ExeTextField.getText(),
                VerTextField.getText().isEmpty() ? "1" : VerTextField.getText(),
                PanelTextField.getText(),
                imagePathArray,
                moviePathArray
            ));
            ThisStage.close();
            return;
        }

        record.Update(AssignedUUIDLabel.getText(),
            GameName,
            DescTextField.getText(),
            ExeTextField.getText(),
            VerTextField.getText().isEmpty() ? "1" : VerTextField.getText(),
            PanelTextField.getText(),
            imagePathArray,
            moviePathArray
        );
        ThisStage.close();
    }
    
    private boolean IsValidInput(){
        boolean ReturnValue = true;

        final Optional<FieldSet> InvalidField = FieldSetList.parallelStream()
            .filter(field -> !field.isValid())
            .findAny();
        
        if(InvalidField.isPresent()){
            ErrorMsgLabel.setText("不正な入力項目があります");
            ReturnValue = false;
        }
        
        GenerateJSONArray(imagePathArray, ImageTextField.getText());   
        GenerateJSONArray(moviePathArray, MovieTextField.getText());
        
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
    private void TextFieldDragOver(DragEvent event){
        //accept only files.
        final Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }    
    }
    
    @FXML
    private void TextFieldDropped(DragEvent event) {
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
    private void ExeDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(!board.hasFiles()){
            event.setDropCompleted(false);
            return;
        }
        
        final File ExePath = board.getFiles().get(0);
        if(ExePath != null){
            ExeTextField.setText(CurrentDirectory.relativize(ExePath.toPath()).toString());
            ExeFieldSet.setState(FieldSet.State.OK);
        }else{
            ExeFieldSet.setState(FieldSet.State.NG);
        }

        final Path GamesBaseDirectory = CurrentDirectory.relativize(ExePath.toPath()).subpath(0, 2);
        System.err.println(GamesBaseDirectory.toString());

        try {
            final List<Path> CollectedFiles = Files.walk(GamesBaseDirectory, FileVisitOption.FOLLOW_LINKS)
                    .parallel()
                    .filter(file -> file.getFileName().toString().matches("__(description|panel|image|movie)__.*"))
                    .collect(Collectors.toList());
            
            {
                final boolean IsNameNull = NameTextField.getText().isEmpty();
                final boolean IsDescriptionNull = DescTextField.getText().isEmpty();
                final boolean IsVersionNull = VerTextField.getText().isEmpty();
                if(IsNameNull || IsDescriptionNull || IsVersionNull){
                    Optional<Path> DescriptionFile = CollectedFiles.stream()
                            .parallel()
                            .filter(file -> file.getFileName().toString().charAt(2) == 'd')
                            .findAny();
                    if(DescriptionFile.isPresent()){
                        final DescriptionFileParser FileParser = new DescriptionFileParser(DescriptionFile.get());
                        if(IsNameNull)NameTextField.setText(FileParser.getName());
                        if(IsDescriptionNull)DescTextField.setText(FileParser.getDescription());
                        if(IsVersionNull)VerTextField.setText(FileParser.getVersion());
                    }
                }
            }

            if(PanelTextField.getText().isEmpty()){
                Optional<Path> PanelFile = CollectedFiles.stream()
                        .parallel()
                        .filter(file -> file.getFileName().toString().charAt(2) == 'p')
                        .findAny();
                if(PanelFile.isPresent())PanelTextField.setText(PanelFile.get().toString());
            }

            if(ImageTextField.getText().isEmpty()){
                ImageTextField.setText(ExtractAndToString(CollectedFiles, 'i'));
            }

            if(MovieTextField.getText().isEmpty()){
                MovieTextField.setText(ExtractAndToString(CollectedFiles, 'm'));
            }
            
        } catch (IOException ex) {
            System.err.println(ex);
        }
        event.setDropCompleted(true);
    }
    
    @FXML
    private void onKeyReleasedExe(KeyEvent event){
        ExeFieldSet.validate();
    }
    
    @FXML
    private void DescFileDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(!board.hasFiles()){
            event.setDropCompleted(false);
            return;
        }
        final DescriptionFileParser FileParser = new DescriptionFileParser(board.getFiles().get(0).toPath());
        if(FileParser.isFine()){
            NameTextField.setText(FileParser.getName());
            DescTextField.setText(FileParser.getDescription());
            VerTextField.setText(FileParser.getVersion());
        }
        event.setDropCompleted(true);
    }
    
    @FXML
    private void PanelFileDropped(DragEvent event) {
        Dragboard board = event.getDragboard();
	if(board.hasFiles()) {
            final File PanelImage = board.getFiles().get(0);
            PanelTextField.setText(CurrentDirectory.relativize(PanelImage.toPath()).toString());
            event.setDropCompleted(true);
            return;
        }
        event.setDropCompleted(false);
    }
    
    private String ExtractAndToString(List<Path>PathList, char SecondChar){
        return PathList.stream()
                    .parallel()
                    .filter(file -> file.getFileName().toString().charAt(2) == SecondChar)
                    .map(file -> file.toString())
                    .collect(Collectors.joining(","));
    }
}