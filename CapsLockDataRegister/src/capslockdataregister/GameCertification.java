package capslockdataregister;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;

/**
 *
 * @author RISCassembler
 */
public final class GameCertification {
    private UUID uuid;
    private String name;
    private Path ExecutablePath;
    private String version;
    private List<Path> ImagePathList;
    private List<Path> MoviePathList;
    
    public GameCertification(JSONObject record){
        uuid = UUID.fromString(record.getString("UUID"));
        name = record.getString("name");
        ExecutablePath = new File(record.getString("executable")).toPath();
        version = record.getString("version");
        record.getJSONArray("image").forEach(file -> ImagePathList.add(new File(file.toString()).toPath())); 
        record.getJSONArray("movie").forEach(file -> MoviePathList.add(new File(file.toString()).toPath()));
    }
    
    public void dump(){
        System.out.println(uuid.toString());
        System.out.println(name);
        System.out.println(ExecutablePath.toString());
        System.out.println(version);
        System.out.println(ImagePathList);
        System.out.println(MoviePathList);
    }
}
