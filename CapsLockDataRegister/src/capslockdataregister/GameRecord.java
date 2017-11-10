package capslockdataregister;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author RISCassembler
 */
public final class GameRecord{
    private final JSONObject json;
    private final ReadOnlyStringProperty uuid;
    private final ReadOnlyStringProperty name;
    private final ReadOnlyStringProperty description;
    private final ReadOnlyStringProperty executable;
    private final ReadOnlyStringProperty version;
    private final ReadOnlyStringProperty panel;
    private final ReadOnlyStringProperty image;
    private final ReadOnlyStringProperty movie;
    private final ReadOnlyStringProperty ID;

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
        ID = new SimpleStringProperty(record.getString("ID"));
    }

    GameRecord(
            String UUIDString,
            String NameString,
            String DescriptionString,
            String ExecutableString,
            String VersionString,
            String PanelString,
            JSONArray ImageArray,
            JSONArray MovieArray,
            String IDString
    ){
        uuid = new SimpleStringProperty(UUIDString);
        name = new SimpleStringProperty(NameString);
        description = new SimpleStringProperty(DescriptionString);
        executable = new SimpleStringProperty(ExecutableString);
        version = new SimpleStringProperty(VersionString);
        panel = new SimpleStringProperty(PanelString);
        image = new SimpleStringProperty(toTextFieldString(ImageArray.toString()));
        movie = new SimpleStringProperty(toTextFieldString(MovieArray.toString()));
        ID = new SimpleStringProperty(IDString);

        json = new JSONObject()
            .put("UUID", UUIDString)
            .put("name", NameString)
            .put("description", DescriptionString)
            .put("executable", ExecutableString)
            .put("version", VersionString)
            .put("panel", PanelString)
            .put("image", ImageArray)
            .put("movie", MovieArray)
            .put("ID", IDString);
        System.err.println(json.toString());
    }
    
    public final ReadOnlyStringProperty uuidProperty(){return uuid;}
    public final ReadOnlyStringProperty nameProperty(){return name;}
    public final ReadOnlyStringProperty descriptionProperty(){return description;}
    public final ReadOnlyStringProperty executableProperty(){return executable;}
    public final ReadOnlyStringProperty versionProperty(){return version;}
    public final ReadOnlyStringProperty panelProperty(){return panel;}
    public final ReadOnlyStringProperty imageProperty(){return image;}
    public final ReadOnlyStringProperty movieProperty(){return movie;}
    public final ReadOnlyStringProperty IDProperty(){return ID;}
    public final JSONObject geJSON(){return json;}
}
