package capslockdataregister;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 *
 * <p>このクラスはイミュータブル.</p>
 * <p>nameとverはプロパティが同一のものを保持するためここでは保持しない</p>
 */
public class GameSignature {
    private final UUID uuid;
    private final String desc;
    private final Path exe;
    private final Path panel;
    private final List<Path> images;
    private final List<Path> movies;
    private final byte ID;

    GameSignature(
            UUID uuid,
            String desc,
            Path exe,
            Path panel,
            List<Path> images,
            List<Path> movies,
            byte ID){
        
        this.uuid = uuid;
        this.desc = desc;
        this.exe = exe;
        this.panel = panel;
        this.images = images;
        this.movies = movies;
        this.ID = ID;
    }
}
