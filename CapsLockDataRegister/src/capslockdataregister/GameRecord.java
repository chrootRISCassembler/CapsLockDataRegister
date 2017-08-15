package capslockdataregister;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author RISCassembler
 */
public final class GameRecord{
    private JSONObject json;
    private final SimpleStringProperty uuid;
    private final SimpleStringProperty name;
    private final SimpleStringProperty executable;
    private final SimpleStringProperty version;
    private final SimpleStringProperty image;
    private final SimpleStringProperty movie;

    private String toTextFieldString(String JSONArrayString){
        return JSONArrayString.substring(1, JSONArrayString.length() - 1).replace("\"", "");
    }
    
    public GameRecord(JSONObject record){
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
        image = new SimpleStringProperty(toTextFieldString(Image.toString()));
        movie = new SimpleStringProperty(toTextFieldString(Movie.toString()));

        json = new JSONObject()
                .put("UUID", UUIDstr)
                .put("name", Name)
                .put("executable", Executable)
                .put("version", Version)
                .put("image", Image)
                .put("movie", Movie);
        System.err.println(json.toString());
    }
    
    public void Update(String UUIDstr, String Name, String Executable, String Version, JSONArray Image, JSONArray Movie){
        uuid.setValue(UUIDstr);
        name.setValue(Name);
        executable.setValue(Executable);
        version.setValue(Version);
        image.setValue(toTextFieldString(Image.toString()));
        movie.setValue(toTextFieldString(Movie.toString()));

        json.put("UUID", UUIDstr)
            .put("name", Name)
            .put("executable", Executable)
            .put("version", Version)
            .put("image", Image)
            .put("movie", Movie);
    }
    
    public StringProperty uuidProperty(){return uuid;}
    public StringProperty nameProperty(){return name;}
    public StringProperty executableProperty(){return executable;}
    public StringProperty versionProperty(){return version;}
    public StringProperty imageProperty(){return image;}
    public StringProperty movieProperty(){return movie;}
    public JSONObject geJSON(){return json;}
}
