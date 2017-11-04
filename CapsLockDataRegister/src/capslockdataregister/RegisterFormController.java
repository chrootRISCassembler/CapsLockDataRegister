package capslockdataregister;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
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
        private final ImageView StateView;
        private State state;
        private final Function<String, State> ValidatingFunction;
        private final Predicate<List<File>> onDragOverPredicate;
        private final Predicate<List<File>> onDragDroppedPredicate;

        public FieldSet(State state, ImageView StateView, Function<String, State> validator, Predicate<List<File>> onDragOver, Predicate<List<File>> onDragDropped){
            this.StateView = StateView;
            setState(state);
            ValidatingFunction = validator;
            onDragOverPredicate = onDragOver;
            onDragDroppedPredicate = onDragDropped;
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
        
        public final void validate(String RawString){
            setState(ValidatingFunction.apply(RawString));
        }
        
        final boolean DragOver(List<File> files){
            return onDragOverPredicate.test(files);
        }
        
        final boolean DragDropped(List<File> files){//一つでも受け付けられないファイルがあればfalse
            return onDragDroppedPredicate.test(files);
        }
        
        public final boolean isValid(){
            return state != State.NG;
        }
    }
    
    private final Map<TextField, FieldSet> FieldMap = new HashMap<>();
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        FieldMap.put(NameTextField, new FieldSet(FieldSet.State.WARN, NameStateView,
                name -> name.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK,
                files -> files.size() == 1,
                file -> DescFileHandler(file.get(0).toPath())
        ));
        
        FieldMap.put(DescTextField, new FieldSet(FieldSet.State.WARN, DescStateView,
                text -> text.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK,
                files -> files.size() == 1,
                file -> DescFileHandler(file.get(0).toPath())
        ));
        
        FieldMap.put(ExeTextField, new FieldSet(FieldSet.State.NG, ExeStateView, 
                FileName -> {
                    final Path ExePath = Paths.get(FileName);
                    return Files.isExecutable(ExePath) ? FieldSet.State.OK : FieldSet.State.NG;
                },
                files -> files.size() == 1,
                files -> true
        ));
        
        FieldMap.put(VerTextField, new FieldSet(FieldSet.State.WARN, VerStateView,
                ver -> ver.isEmpty() ? FieldSet.State.WARN : FieldSet.State.OK,
                files -> files.size() == 1,
                file -> DescFileHandler(file.get(0).toPath())
        ));
        
        FieldMap.put(PanelTextField, new FieldSet(FieldSet.State.WARN, PanelStateView,
                panel -> validator.isValidPanel(Paths.get(panel)) ? FieldSet.State.OK : FieldSet.State.WARN,
                files -> files.size() == 1,
                files -> {
                    final Path PanelPath = files.get(0).toPath();
                    PanelTextField.setText(ResourceFilesInputWrapper.instance.toRelativePath(PanelPath).toString());
                    return true;
                }
        ));
        
        FieldMap.put(ImageTextField, new FieldSet(FieldSet.State.WARN, ImageStateView, 
                Images -> LauncherResourceFilesValidator.areValidImages(Images) ? FieldSet.State.OK : FieldSet.State.WARN,
                files -> true,
                files -> {
                    ImageTextField.setText(MakeFileArray(files));
                    return true;
                }
        ));
        
        FieldMap.put(MovieTextField, new FieldSet(FieldSet.State.WARN, MovieStateView,
                Movies -> LauncherResourceFilesValidator.areValidMoves(Movies) ? FieldSet.State.OK : FieldSet.State.WARN,
                files -> true,
                files -> {
                    MovieTextField.setText(MakeFileArray(files));
                    return true;
                }
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
            FieldMap.forEach((field, dummy) -> field.clear());
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
        
        FieldMap.forEach((field, checker) -> checker.validate(field.getText()));
//        System.err.println(Files.isDirectory(ResourceFilesInputWrapper.instance.GamesDirectory));
//        System.err.println(ResourceFilesInputWrapper.instance.CurrentDirectory);
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
        
        final JSONArray imagePathArray = new JSONArray();
        final JSONArray moviePathArray = new JSONArray();
        GenerateJSONArray(imagePathArray, ImageTextField.getText());   
        GenerateJSONArray(moviePathArray, MovieTextField.getText());
        
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
        final Optional<Map.Entry<TextField, FieldSet>> InvalidField = FieldMap.entrySet()
                .parallelStream()
                .filter(set -> !set.getValue().isValid())
                .findAny();
        
        if(InvalidField.isPresent()){
            ErrorMsgLabel.setText("不正な入力項目があります");
            return false;
        }
        return true;
    }
    
    private final String MakeFileArray(List<File> files){
        return files.stream()
                .map(file -> '\"' + ResourceFilesInputWrapper.instance.toRelativePath(file.toPath()).toString() + '\"')
                .collect(Collectors.joining(", "));
    }
    
    private void GenerateJSONArray(JSONArray target, String RawString){

        String[] files = RawString.split(",");
            
        Arrays.stream(files).forEach(element -> {
            File file = new File(element);
            if(file.exists())target.put(element);
        });
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
            FieldMap.get(ExeTextField).setState(FieldSet.State.OK);
        }else{
            FieldMap.get(ExeTextField).setState(FieldSet.State.NG);
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
    private void onKeyReleased(KeyEvent event){
        final TextField EventSource = (TextField)event.getSource();
        FieldMap.get(EventSource).validate(EventSource.getText());
    }
    
    
    
    @FXML
    private final void onDragDropped_TextField(DragEvent event){

        System.err.println("DragDrop");
        //validatorのDBを参照して処理を高速化    
        
        final Dragboard board = event.getDragboard();
        final TextField EventSource = (TextField)event.getSource();

        //ここでファイルが検出できればvalidatorのDBにINSERT INTO

//        for(final File file : board.getFiles()){
//            System.err.println(file);
//        }

        FieldMap.get(EventSource).DragDropped(board.getFiles());
        
        event.setDropCompleted(true);
        event.consume();
    }
    
    @FXML
    private final void onDragOver_TextField(DragEvent event){
        System.err.println("DragOver");
        final Dragboard board = event.getDragboard();
        if(board.hasFiles()){
            
            //ここでファイルが検出できればvalidatorのDBにINSERT INTO
            
            Optional<Path> result = board.getFiles().stream()
                    .parallel()
                    .map(file -> file.toPath())
                    .filter(path -> !validator.isLocatedValidly(path.toAbsolutePath()))
                    .findAny();
            
            if(!result.isPresent()){//ドロップしようとしているファイル中に一つでも「不正な位置のファイル」があれば受け付けない
//                for(final File file : board.getFiles()){
//                    System.err.println(file);
//                }

                final TextField EventSource = (TextField)event.getSource();
                if(FieldMap.get(EventSource).DragOver(board.getFiles()))event.acceptTransferModes(TransferMode.LINK);
                //acceptTransferModesでドラッグイベントを受け付けるようにしないとDragDroppedイベントが発生しない
            }
        }
        event.consume();
    }
    
    private final boolean DescFileHandler(Path DescFile){
        final DescriptionFileParser FileParser = new DescriptionFileParser(DescFile);
        if(FileParser.isFine()){
            NameTextField.setText(FileParser.getName());
            DescTextField.setText(FileParser.getDescription());
            VerTextField.setText(FileParser.getVersion());
            return true;
        }
        return false;
    }
    
    private String ExtractAndToString(List<Path>PathList, char SecondChar){
        return PathList.stream()
                    .parallel()
                    .filter(file -> file.getFileName().toString().charAt(2) == SecondChar)
                    .map(file -> file.toString())
                    .collect(Collectors.joining(","));
    }
}