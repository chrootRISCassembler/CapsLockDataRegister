package capslockdataregister;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.json.JSONArray;
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
    @FXML TableColumn<GameRecord, String> ExecutableCol;
    @FXML TableColumn<GameRecord, String> VersionCol;
    @FXML TableColumn<GameRecord, String> ImageCol;
    @FXML TableColumn<GameRecord, String> MovieCol;
    
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
        }catch(Exception e){
            return true;
        }
        return true;
    }
    
    @FXML
    protected void onAddButtonClicked(){
        RegisterWindow.showAndWait();
        GameRecord NewRecord = (GameRecord)RegisterWindow.getUserData();
        if(NewRecord != null)DisplayCollection.add(NewRecord);
        RegisterWindow.setUserData(null);
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
        RegisterWindow.showAndWait();
        RegisterWindow.setUserData(null);
    }
    
    public static class GameRecord{
        private JSONObject json;
        private final SimpleStringProperty uuid;
        private final SimpleStringProperty name;
        private final SimpleStringProperty executable;
        private final SimpleStringProperty version;
        private final SimpleStringProperty image;
        private final SimpleStringProperty movie;
        
        private GameRecord(JSONObject record){
            json = record;
            uuid = new SimpleStringProperty(record.getString("UUID"));
            name = new SimpleStringProperty(record.getString("name"));
            executable = new SimpleStringProperty(record.getString("executable"));
            version = new SimpleStringProperty(record.getString("version"));
            image = new SimpleStringProperty(record.getJSONArray("image").join(","));
            movie = new SimpleStringProperty(record.getJSONArray("movie").join(","));
        }
        
        public GameRecord(String UUIDstr, String Name, String Executable, String Version, JSONArray Image, JSONArray Movie){
            uuid = new SimpleStringProperty(UUIDstr);
            name = new SimpleStringProperty(Name);
            executable = new SimpleStringProperty(Executable);
            version = new SimpleStringProperty(Version);
            
            String ImageString = Image.toString();
            image = new SimpleStringProperty(ImageString.substring(1, ImageString.length() - 1).replace("\"", ""));
            
            String MovieString = Movie.toString();
            movie = new SimpleStringProperty(MovieString.substring(1, MovieString.length() - 1).replace("\"", ""));
            
            json = new JSONObject()
                    .put("UUID", UUIDstr)
                    .put("name", Name)
                    .put("executable", Executable)
                    .put("version", Version)
                    .put("image", Image)
                    .put("movie", Movie);
            System.err.println(json.toString());
        }
        
        public StringProperty uuidProperty(){return uuid;}
        public StringProperty nameProperty(){return name;}
        public StringProperty executableProperty(){return executable;}
        public StringProperty versionProperty(){return version;}
        public StringProperty imageProperty(){return image;}
        public StringProperty movieProperty(){return movie;}
        public JSONObject geJSON(){return json;}
        
        public void Update(String UUIDstr, String Name, String Executable, String Version, JSONArray Image, JSONArray Movie){
            uuid.setValue(UUIDstr);
            name.setValue(Name);
            executable.setValue(Executable);
            version.setValue(Version);
            
            String ImageString = Image.toString();
            image.setValue(ImageString.substring(1, ImageString.length() - 1).replace("\"", ""));
            
            String MovieString = Movie.toString();
            movie.setValue(MovieString.substring(1, MovieString.length() - 1).replace("\"", ""));
            
            json.put("UUID", UUIDstr)
                .put("name", Name)
                .put("executable", Executable)
                .put("version", Version)
                .put("image", Image)
                .put("movie", Movie);
        }
    }
}
