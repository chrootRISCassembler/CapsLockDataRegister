package capslockdataregister;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
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
import org.json.JSONException;
import org.json.JSONObject;
import trivial_common_logger.LogHandler;

/**
 * メインフォームのコントローラークラス.
 * <p>一括登録,JSONファイル書き出し,表を使った大まかなプレビューを提供</p>
 */
public class MainFormController implements Initializable {
    
    @FXML private Button AddGameButton;
    @FXML private Button SaveButton;
    @FXML private Button AutoRegisterButton;
    @FXML private TableView<GameRecord> GameInfoView;
    @FXML private TableColumn<GameRecord, String> UUIDCol;
    @FXML private TableColumn<GameRecord, String> NameCol;
    @FXML private TableColumn<GameRecord, String> DescCol;
    @FXML private TableColumn<GameRecord, String> ExeCol;
    @FXML private TableColumn<GameRecord, String> VerCol;
    @FXML private TableColumn<GameRecord, String> PanelCol;
    @FXML private TableColumn<GameRecord, String> ImageCol;
    @FXML private TableColumn<GameRecord, String> MovieCol;
    @FXML private TableColumn<GameRecord, String> IDCol;
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
        LoadJSONDatabase();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("RegisterForm.fxml"));
        Scene scene;
        try {
            scene = new Scene(loader.load());
        } catch (IOException ex) {
            System.out.println(ex);
            TrivialLogger.inst.log("Failed to load RegisterForm.fxml", 1);
            TrivialLogger.inst.log(ex, 1);
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
        DescCol.setCellValueFactory(new PropertyValueFactory<>("desc"));
        ExeCol.setCellValueFactory(new PropertyValueFactory<>("exe"));
        VerCol.setCellValueFactory(new PropertyValueFactory<>("ver"));
        PanelCol.setCellValueFactory(new PropertyValueFactory<>("panel"));
        ImageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        MovieCol.setCellValueFactory(new PropertyValueFactory<>("movie"));
        IDCol.setCellValueFactory(new PropertyValueFactory<>("ID"));
         
        GameInfoView.setItems(DisplayCollection);
        
        System.gc();
    }
    
    final void setOwnStage(Stage stage){ThisStage = stage;}
    
    /**
     * 副作用でJSONファイルを読み込んで表に追加する.
     */
    private final void LoadJSONDatabase(){
        try{
            final String JSON_String = Files.newBufferedReader(Paths.get("./GamesInfo.json")).lines()
                    .collect(Collectors.joining());
            new JSONArray(JSON_String).forEach(record -> {
                final GameRecordBuilder builder = new GameRecordBuilder((JSONObject)record);
                DisplayCollection.add(builder.build());
                });
            
        } catch (SecurityException ex) {//セキュリティソフト等に読み込みを阻害されたとき
            LogHandler.inst.severe("File-loading is blocked by security manager");
            LogHandler.inst.DumpStackTrace(ex);
        } catch (IOException ex) {
            LogHandler.inst.severe("Failed to open ./GamesInfo.json");
            LogHandler.inst.DumpStackTrace(ex);
        } catch(JSONException ex){
            ex.printStackTrace();
            LogHandler.inst.severe("JSONException : ./GamesInfo.json");
            LogHandler.inst.DumpStackTrace(ex);
        } catch(Exception ex){
            LogHandler.inst.DumpStackTrace(ex);
        }
        
        UpdateNumberDisplay();
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
    
    
    /**
     * JSONファイル書き出しボタンのイベントハンドラ.
     */
    @FXML
    private final void onSaveClicked(){
        try(FileWriter writer = new FileWriter("GamesInfo.json")){
            JSONArray array = new JSONArray();
            DisplayCollection.forEach(record -> array.put(record.getJSON()));
            array.write(writer);
        } catch (IOException ex) {
            System.out.println(ex);
            TrivialLogger.inst.log("Failed to open GamesInfo.json", 1);
            TrivialLogger.inst.log(ex, 1);
        }
    }
    
    @FXML
    private final void onRecordDoubleClicked(){
        final GameRecord SelectedGame = GameInfoView.getSelectionModel().getSelectedItem();       
        RegisterWindow.setUserData(SelectedGame);
        RegisterWindow.setTitle("ゲーム情報変更");
        RegisterWindow.showAndWait();
        
        final GameRecord NewGameInfo = (GameRecord)RegisterWindow.getUserData();
        DisplayCollection.set(GameInfoView.getSelectionModel().getSelectedIndex(), NewGameInfo);
        RegisterWindow.setUserData(null);
    }
    
    /**
     * 一括登録ボタンが押された.
     * <p>Gamesディレクトリ以下を検索してゲームの一括登録を行う.実行ファイルが一つもないゲームは登録されない.
     * 現在の表にデータを追加するだけで,JSONへの書き出しは行わない.</p>
     */
    @FXML
    private final void onAutoRegisterClicked(){
        try {
            Files.list(ResourceFilesInputWrapper.instance.GamesDirectory)
                    .parallel()
                    .filter(path -> Files.isDirectory(path))
                    .map(path -> new GameRecordBuilder(path))
                    .filter(builder -> builder.canBuild())
                    .forEach(builder -> DisplayCollection.add(builder.build()));
        } catch (IOException ex) {
            System.err.println(ex);
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
            
            Console.setTitle("開発者向けコンソール");
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
