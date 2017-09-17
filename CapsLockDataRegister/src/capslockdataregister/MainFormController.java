package capslockdataregister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class MainFormController implements Initializable {
    
    @FXML private Button AddGameButton;
    @FXML private Button SaveButton;
    @FXML private Button AutoRegisterButton;
    @FXML private TableView<GameRecord> GameInfoView;
    @FXML private TableColumn<GameRecord, String> UUIDCol;
    @FXML private TableColumn<GameRecord, String> NameCol;
    @FXML private TableColumn<GameRecord, String> DescriptionCol;
    @FXML private TableColumn<GameRecord, String> ExecutableCol;
    @FXML private TableColumn<GameRecord, String> VersionCol;
    @FXML private TableColumn<GameRecord, String> PanelCol;
    @FXML private TableColumn<GameRecord, String> ImageCol;
    @FXML private TableColumn<GameRecord, String> MovieCol;
    @FXML private Label RecordNumLabel;
    @FXML private Button RemoveGameButton;
    @FXML private Button ReloadButton;
    
    private final KeyCombination ConsoleKeys = new KeyCodeCombination(KeyCode.C, KeyCombination.SHIFT_DOWN, KeyCodeCombination.CONTROL_DOWN);
    
    private Stage ThisStage;
    private final Stage RegisterWindow = new Stage();
    final ObservableList<GameRecord> DisplayCollection = FXCollections.observableArrayList();
    private boolean hasConsole = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        if(!LoadJSONDatabase())System.err.println("failed");
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterForm.fxml"));
        Scene scene;
        try {
            scene = new Scene(loader.load());
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return;
        }
        
        RegisterWindow.initOwner(ThisStage);
        RegisterWindow.initModality(Modality.APPLICATION_MODAL);
        RegisterWindow.setScene(scene);
        
        RegisterFormController controller = (RegisterFormController)loader.getController();
        controller.setOwnStage(RegisterWindow);
        RegisterWindow.setOnShowing((event) -> controller.onLoad(event));
        
        UUIDCol.setCellValueFactory(new PropertyValueFactory<>("uuid"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ExecutableCol.setCellValueFactory(new PropertyValueFactory<>("executable"));
        VersionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        ImageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        MovieCol.setCellValueFactory(new PropertyValueFactory<>("movie"));
         
        GameInfoView.setItems(DisplayCollection);
    }
    
    final void setOwnStage(Stage stage){ThisStage = stage;}
    
    private boolean LoadJSONDatabase(){
        try(final BufferedReader reader = new BufferedReader(new FileReader("GamesInfo.json"))){
            final String JsonString = reader.readLine();
            new JSONArray(JsonString).forEach(record -> DisplayCollection.add(new GameRecord((JSONObject)record)));
        }catch(FileNotFoundException ex){
            System.err.println(ex);
            return true;
        }catch(IOException ex){
            System.err.println(ex);
            return false;
        }
        
        UpdateNumberDisplay();
        return true;
    }
    
    @FXML
    private void onAddButtonClicked(){
        RegisterWindow.setTitle("ゲーム追加");
        RegisterWindow.showAndWait();
        GameRecord NewRecord = (GameRecord)RegisterWindow.getUserData();
        if(NewRecord != null)DisplayCollection.add(NewRecord);
        RegisterWindow.setUserData(null);
        UpdateNumberDisplay();
    }
    
    @FXML
    private void onSaveClicked(){
        FileWriter writer;
        
        try{
            writer = new FileWriter("GamesInfo.json");
        }catch(IOException e){
            System.out.println(e);
            return;
        }
        
        JSONArray array = new JSONArray();
        DisplayCollection.forEach(record -> array.put(record.geJSON()));
        array.write(writer);
        
        try{
            writer.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }
    
    @FXML
    private void onRecordDoubleClicked(){
        RegisterWindow.setUserData(GameInfoView.getSelectionModel().getSelectedItem());
        RegisterWindow.setTitle("ゲーム情報変更");
        RegisterWindow.showAndWait();
        RegisterWindow.setUserData(null);
        UpdateNumberDisplay();
    }
    
    @FXML
    private void onAutoRegisterClicked(){
        final Path CurrentDirectory = new File(".").getAbsoluteFile().toPath().getParent();

        final List<Path> CollectedFiles;
        try {
            CollectedFiles = Files.walk(new File("Games").toPath(), FileVisitOption.FOLLOW_LINKS)
                    .parallel()
                    .filter(file -> file.getFileName().toString().matches("__(description|panel|image|movie)__.*|.*\\.bat"))
                    .peek(file -> System.err.println(file))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
        
        ArrayList<Path> executables = new ArrayList();
        
        Iterator<Path> ite = CollectedFiles.iterator();
        while(ite.hasNext()){
            final Path CheckFile = ite.next();
            if(CheckFile.getFileName().toString().endsWith(".bat")){
                executables.add(CheckFile);
                ite.remove();
            }
        }
        
        for(Path exe : executables){
            final Path GamesBaseDirectory = CurrentDirectory.relativize(exe.toAbsolutePath()).subpath(0, 2);
            
            String name = "";
            String description = "";
            String version = "1";
            String panel = "";
            List<Path> Images = new ArrayList();
            List<Path> Movies = new ArrayList();
            
            for(Path file : CollectedFiles){
                if(!file.startsWith(GamesBaseDirectory))continue;
                String FileName = file.getFileName().toString();
                switch(FileName.charAt(2)){
                    case 'd':
                        DescriptionFileParser FileParser =  new DescriptionFileParser(file);
                        name = FileParser.getName();
                        description = FileParser.getDescription();
                        version = FileParser.getVersion();
                        
                        break;
                    case 'p':
                        panel = file.toString();
                        break;
                    case 'i':
                        Images.add(file);
                        break;
                    case 'm':
                        Movies.add(file);
                        break;
                }
            }
            
            if(name.equals("")){
                final String ExeFileName = exe.getFileName().toString();
                name = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
            }
            
            DisplayCollection.add(new GameRecord(
                    UUID.randomUUID().toString(),
                    name,
                    description,
                    exe.toString(),
                    version,
                    panel,
                    new JSONArray(Images), 
                    new JSONArray(Movies)
            ));
        }
        UpdateNumberDisplay();
    }
    
    @FXML
    private void onRemoveClicked(){
        try{
            final int RecordIndex = GameInfoView.getSelectionModel().getSelectedIndex();
            DisplayCollection.remove(RecordIndex);
        }catch(Exception e){
            return;
        }
        UpdateNumberDisplay();
    }
    
    @FXML
    private void onReloadClicked(){
        DisplayCollection.clear();
        LoadJSONDatabase();
    }
    
    @FXML
    private void onKeyPressed(KeyEvent event){
        if(hasConsole)return;
        
        if(ConsoleKeys.match(event)){
            hasConsole = true;
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Console.fxml"));
            Scene scene;
            try {
                scene = new Scene(loader.load());
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
                return;
            }

            Stage Console = new Stage();
            
            Console.initOwner(ThisStage);
            Console.setScene(scene);

            ConsoleController controller = (ConsoleController)loader.getController();
            controller.setParentController(this);
            Console.setOnCloseRequest(ev -> {
                controller.beforeCloseWindow(ev);
                hasConsole = false;
            });
            
            Console.show();
        }
    }
    
    private void UpdateNumberDisplay(){
        RecordNumLabel.setText("登録済みゲーム " + DisplayCollection.size() + "件");
    }

    public ObservableList<GameRecord> getDisplayCollection() {
        return DisplayCollection;
    }
}
