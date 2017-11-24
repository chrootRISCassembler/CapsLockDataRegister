package capslockdataregister;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javafx.collections.FXCollections;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
    @FXML private ChoiceBox IDChoiceBox;
    @FXML private Button RegisterButton;
    
    private Stage ThisStage;
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
        {
            List<String> Nlist = IntStream.
                    rangeClosed(1, PathUtil.GAME_ID_MAX)
                    .boxed()
                    .map(integer -> integer.toString())
                    .collect(Collectors.toList());
            IDChoiceBox.setItems(FXCollections.observableArrayList(Nlist));
        }
        
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
                file -> {
                    final Path exe = file.get(0).toPath();
                    validator.crawl(exe);
                    ExeTextField.setText(PathUtil.inst.toRelativePath(exe).toString());
                    DescFileHandler(validator.query(ResourceType.desc).findAny().get());
                    
                    PanelTextField.setText(PathUtil.inst.toRelativePath(
                            validator.query(ResourceType.panel).findAny().get()).toString());
                    
                    ImageTextField.setText(MakeFileArray(validator.query(ResourceType.image)));
                    MovieTextField.setText(MakeFileArray(validator.query(ResourceType.movie)));
                    return true;
                }
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
                    PanelTextField.setText(PathUtil.inst.toRelativePath(PanelPath).toString());
                    return true;
                }
        ));
        
        FieldMap.put(ImageTextField, new FieldSet(FieldSet.State.WARN, ImageStateView, 
                Images -> validator.areValidImages(Images) ? FieldSet.State.OK : FieldSet.State.WARN,
                files -> true,
                files -> {
                    ImageTextField.setText(MakeFileArray(files));
                    return true;
                }
        ));
        
        FieldMap.put(MovieTextField, new FieldSet(FieldSet.State.WARN, MovieStateView,
                Movies -> validator.areValidMoves(Movies) ? FieldSet.State.OK : FieldSet.State.WARN,
                files -> true,
                files -> {
                    MovieTextField.setText(MakeFileArray(files));
                    return true;
                }
        ));
    }
    
    void onLoad(WindowEvent event){
        final GameSignature game;
        GameRecord record;
        
        try{
            game = (GameSignature)ThisStage.getUserData();
            if(game == null)throw new NullPointerException();
            validator = PathUtil.inst.add(
                    game.getUUID(),
                    () -> new LauncherResourceFilesValidator(game.getExe().toString())
            );
        }catch(NullPointerException e){
            System.err.println(e);
            AssignedUUIDLabel.setText((UUID.randomUUID()).toString());
            FieldMap.forEach((field, dummy) -> field.clear());
            return;
        }catch(Exception ex){
            System.err.println("onload failed");
            System.err.println(ex);
            return;
        }
         
        AssignedUUIDLabel.setText(game.getUUID().toString());
        NameTextField.setText(game.getName());
        DescTextField.setText(game.getDesc());
        ExeTextField.setText(game.getExe().toString());
        VerTextField.setText(game.getVer());
        {
            final Path panel = game.getPanel();
            PanelTextField.setText(panel == null ? "" : panel.toString());
        }
        ImageTextField.setText(MakeFileArray(game.getImages().stream()));
        MovieTextField.setText(MakeFileArray(game.getMovies().stream()));
        
        FieldMap.forEach((field, checker) -> checker.validate(field.getText()));
    }
    
    final void setOwnStage(Stage stage){ThisStage = stage;}
    
    @FXML
    private void Register(){
        if(!IsValidInput())return;
        
        PathUtil.inst.genJSONArray(ImageTextField.getText());
        
        String GameName = NameTextField.getText();
        if(GameName.isEmpty()){
            final String ExeFileName = Paths.get(ExeTextField.getText()).getFileName().toString();
            GameName = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
        }
        
        final QuotedStringParser ImageParser = new QuotedStringParser(ImageTextField.getText());
        final QuotedStringParser MovieParser = new QuotedStringParser(MovieTextField.getText());
        
        final GameRecordBuilder builder = new GameRecordBuilder();
        builder.setUUID(UUID.fromString(AssignedUUIDLabel.getText()))
                .setName(GameName)
                .setDesc(DescTextField.getText())
                .setExe(Paths.get(ExeTextField.getText()))
                .setVer(VerTextField.getText().isEmpty() ? "1" : VerTextField.getText())
                .setPanel(Paths.get(PanelTextField.getText()))
                .setImages(ImageParser.get().stream().map(str -> Paths.get(str)).collect(Collectors.toList()))
                .setMovies(MovieParser.get().stream().map(str -> Paths.get(str)).collect(Collectors.toList()))
                .setID((String)IDChoiceBox.getValue());
        
        if(!builder.canBuild()){
            System.err.println("Can't build Record.");
            return;
        }
        
        ThisStage.setUserData(builder.build());
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
                .map(file -> '\"' + PathUtil.inst.toRelativePath(file.toPath()).toString() + '\"')
                .collect(Collectors.joining(", "));
    }
    
    private final String MakeFileArray(Stream<Path> stream){
        return stream.map(path -> '\"' + PathUtil.inst.toRelativePath(path).toString() + '\"')
                .collect(Collectors.joining(", "));
    }
    
    @FXML
    private void onKeyReleased(KeyEvent event){
        final TextField EventSource = (TextField)event.getSource();
        FieldMap.get(EventSource).validate(EventSource.getText());
    }
    
    @FXML
    private final void onDragDropped_TextField(DragEvent event){
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
}