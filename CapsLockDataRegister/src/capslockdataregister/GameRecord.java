package capslockdataregister;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONObject;

/**
 *
 * @author RISCassembler
 */
public final class GameRecord extends GameSignature{
    private final JSONObject json;
    private final ReadOnlyStringProperty UUIDProperty;
    private final ReadOnlyStringProperty nameProperty;
    private final ReadOnlyStringProperty descProperty;
    private final ReadOnlyStringProperty exeProperty;
    private final ReadOnlyStringProperty verProperty;
    private final ReadOnlyStringProperty panelProperty;
    private final ReadOnlyStringProperty imageProperty;
    private final ReadOnlyStringProperty movieProperty;
    private final ReadOnlyStringProperty IDProperty;

    private String toTextFieldString(String JSONArrayString){
        return JSONArrayString.substring(1, JSONArrayString.length() - 1).replace("\"", "");
    }

    public GameRecord(
            UUID uuid,
            String name,
            String desc, 
            Path exe,
            String ver,
            Path panel,
            List<Path> images,
            List<Path> movies,
            byte ID
    ){
        super(uuid, desc, exe, panel, images, movies, ID);
        
        UUIDProperty = new SimpleStringProperty(uuid.toString());
        nameProperty = new SimpleStringProperty(name);
        descProperty = new SimpleStringProperty(desc.isEmpty() ? "none" : "exist");
        exeProperty = new SimpleStringProperty(exe.getFileName().toString());
        verProperty = new SimpleStringProperty(ver);
        panelProperty = new SimpleStringProperty(panel == null ? "none" : "exist");
        imageProperty = new SimpleStringProperty(Integer.toString(images.size()));
        movieProperty = new SimpleStringProperty(Integer.toString(movies.size()));
        IDProperty = new SimpleStringProperty(Integer.toString(ID));

        json = new JSONObject()
            .put("UUID", uuid)
            .put("name", name)
            .put("description", desc)
            .put("executable", exe)
            .put("version", ver)
            .put("panel", panel == null ? "" : panel)
            .put("image", images)
            .put("movie", movies)
            .put("ID", ID);
        System.err.println(json.toString());
    }
    
    public final ReadOnlyStringProperty uuidProperty(){return UUIDProperty;}
    public final ReadOnlyStringProperty nameProperty(){return nameProperty;}
    public final ReadOnlyStringProperty descriptionProperty(){return descProperty;}
    public final ReadOnlyStringProperty executableProperty(){return exeProperty;}
    public final ReadOnlyStringProperty versionProperty(){return verProperty;}
    public final ReadOnlyStringProperty panelProperty(){return panelProperty;}
    public final ReadOnlyStringProperty imageProperty(){return imageProperty;}
    public final ReadOnlyStringProperty movieProperty(){return movieProperty;}
    public final ReadOnlyStringProperty IDProperty(){return IDProperty;}
    public final JSONObject getJSON(){return json;}
    
    @Override
    final String getName(){
        return nameProperty.getValue();
    }
    
    @Override
    final String getVer(){
        return exeProperty.getValue();
    }
}
