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
    private final SimpleStringProperty description;
    private final SimpleStringProperty executable;
    private final SimpleStringProperty version;
    private final SimpleStringProperty panel;
    private final SimpleStringProperty image;
    private final SimpleStringProperty movie;

    private String toTextFieldString(String JSONArrayString){
        return JSONArrayString.substring(1, JSONArrayString.length() - 1).replace("\"", "");
    }
    
    GameRecord(JSONObject record){
        json = record;
        uuid = new SimpleStringProperty(record.getString("UUID"));
        name = new SimpleStringProperty(record.getString("name"));
        description = new SimpleStringProperty(record.getString("description"));
        executable = new SimpleStringProperty(record.getString("executable"));
        version = new SimpleStringProperty(record.getString("version"));
        panel = new SimpleStringProperty(record.getString("panel"));
        image = new SimpleStringProperty(record.getJSONArray("image").join(","));
        movie = new SimpleStringProperty(record.getJSONArray("movie").join(","));
    }

    GameRecord(
            String UUIDString,
            String NameString,
            String DescriptionString,
            String ExecutableString,
            String VersionString,
            String PanelString,
            JSONArray ImageArray,
            JSONArray MovieArray
    ){
        uuid = new SimpleStringProperty(UUIDString);
        name = new SimpleStringProperty(NameString);
        description = new SimpleStringProperty(DescriptionString);
        executable = new SimpleStringProperty(ExecutableString);
        version = new SimpleStringProperty(VersionString);
        panel = new SimpleStringProperty(PanelString);
        image = new SimpleStringProperty(toTextFieldString(ImageArray.toString()));
        movie = new SimpleStringProperty(toTextFieldString(MovieArray.toString()));

        json = new JSONObject()
            .put("UUID", UUIDString)
            .put("name", NameString)
            .put("description", DescriptionString)
            .put("executable", ExecutableString)
            .put("version", VersionString)
            .put("panel", PanelString)
            .put("image", ImageArray)
            .put("movie", MovieArray);
        System.err.println(json.toString());
    }
    
    void Update(
            String UUIDString,
            String NameString,
            String DescriptionString,
            String ExecutableString,
            String VersionString,
            String PanelString,
            JSONArray ImageArray,
            JSONArray MovieArray
    ){
        uuid.setValue(UUIDString);
        name.setValue(NameString);
        description.setValue(DescriptionString);
        executable.setValue(ExecutableString);
        version.setValue(VersionString);
        panel.setValue(PanelString);
        image.setValue(toTextFieldString(ImageArray.toString()));
        movie.setValue(toTextFieldString(MovieArray.toString()));

        json.put("UUID", UUIDString)
            .put("name", NameString)
            .put("description", DescriptionString)
            .put("executable", ExecutableString)
            .put("version", VersionString)
            .put("panel", PanelString)
            .put("image", ImageArray)
            .put("movie", MovieArray);
    }
    
    public final StringProperty uuidProperty(){return uuid;}
    public final StringProperty nameProperty(){return name;}
    public final StringProperty descriptionProperty(){return description;}
    public final StringProperty executableProperty(){return executable;}
    public final StringProperty versionProperty(){return version;}
    public final StringProperty panelProperty(){return panel;}
    public final StringProperty imageProperty(){return image;}
    public final StringProperty movieProperty(){return movie;}
    public final JSONObject geJSON(){return json;}
}
