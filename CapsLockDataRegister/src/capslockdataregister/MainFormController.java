package capslockdataregister;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * FXML Controller class
 *
 * @author RISCassembler
 */
public class MainFormController implements Initializable {

    List<GameCertification> certifications = new ArrayList();
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        if(!LoadJSONDatabase())System.err.println("failed");
        certifications.forEach(e -> e.dump());
    }    
    
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
        
        JSONArray JSONDB = new JSONArray(jsonString);
        JSONDB.forEach(record -> certifications.add(new GameCertification((JSONObject)record)));
        
        return true;
    }
}
