package capslockdataregister;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class MainFormController implements Initializable {
    
    @FXML Button AddGameButton;
    @FXML Button SaveButton;
    @FXML TableView<GameRecord> GameInfoView;
    @FXML TableColumn<GameRecord, String> UUIDCol;
    @FXML TableColumn<GameRecord, String> NameCol;
    @FXML TableColumn<GameRecord, String> DescriptionCol;
    @FXML TableColumn<GameRecord, String> ExecutableCol;
    @FXML TableColumn<GameRecord, String> VersionCol;
    @FXML TableColumn<GameRecord, String> PanelCol;
    @FXML TableColumn<GameRecord, String> ImageCol;
    @FXML TableColumn<GameRecord, String> MovieCol;
    @FXML Label RecordNumLabel;
    
    Stage ThisStage;
    Stage RegisterWindow = new Stage();
    ObservableList<GameRecord> DisplayCollection = FXCollections.observableArrayList();
    
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
    }
    
    public void setOwnStage(Stage stage){ThisStage = stage;}
    
    private boolean LoadJSONDatabase(){
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new FileReader("GamesInfo.json"));
        } catch (FileNotFoundException ex) {
            System.out.println(ex);
            return false;
        }
        
        String jsonString;
        
        try {
            jsonString = reader.readLine();
        } catch (IOException ex) {
            System.out.println(ex);
            return false;
        }
        
        System.out.println(jsonString);
        
        UUIDCol.setCellValueFactory(new PropertyValueFactory<>("uuid"));
        NameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        ExecutableCol.setCellValueFactory(new PropertyValueFactory<>("executable"));
        VersionCol.setCellValueFactory(new PropertyValueFactory<>("version"));
        ImageCol.setCellValueFactory(new PropertyValueFactory<>("image"));
        MovieCol.setCellValueFactory(new PropertyValueFactory<>("movie"));
         
        GameInfoView.setItems(DisplayCollection);
         
        if(jsonString == null)return true; 
        
        try{
            new JSONArray(jsonString).forEach(record -> DisplayCollection.add(new GameRecord((JSONObject)record)));
        }catch(JSONException exception){
            System.out.println(exception);
            return true;
        }
        UpdateNumberDisplay();
        return true;
    }
    
    @FXML
    protected void onAddButtonClicked(){
        RegisterWindow.setTitle("ゲーム追加");
        RegisterWindow.showAndWait();
        GameRecord NewRecord = (GameRecord)RegisterWindow.getUserData();
        if(NewRecord != null)DisplayCollection.add(NewRecord);
        RegisterWindow.setUserData(null);
        UpdateNumberDisplay();
    }
    
    @FXML
    protected void onSaveClicked(){
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
    protected void onRecordDoubleClicked(){
        RegisterWindow.setUserData(GameInfoView.getSelectionModel().getSelectedItem());
        RegisterWindow.setTitle("ゲーム情報変更");
        RegisterWindow.showAndWait();
        RegisterWindow.setUserData(null);
        UpdateNumberDisplay();
    }
    
    private final void UpdateNumberDisplay(){
        RecordNumLabel.setText("登録済みゲーム " + DisplayCollection.size() + "件");
    }
}
