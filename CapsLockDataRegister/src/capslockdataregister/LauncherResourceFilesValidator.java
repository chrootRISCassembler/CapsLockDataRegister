package capslockdataregister;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;

/**
 *
 */

class LauncherResourceFilesValidator extends Thread{
    static enum ResourceType{
        description,
        executable,
        panel,
        image,
        movie,
        none
    }
    
    private Path GameRootPath;
    private ConcurrentHashMap<Path, ResourceType> PathDB = new ConcurrentHashMap<>();
    private WatchService watchdog;
    
    LauncherResourceFilesValidator(String ExePath){
        GameRootPath = ResourceFilesInputWrapper.instance.CurrentDirectory.relativize(Paths.get(ExePath).toAbsolutePath()).subpath(0, 2);

        try {
            Files.walk(GameRootPath, FileVisitOption.FOLLOW_LINKS)
                    .parallel()
                    .filter(file -> file.getFileName().toString().matches("__(description|panel|image|movie)__.*"))
                    .peek(file -> System.err.println(file))
                    .forEach(file -> PathDB.put(file, getResourceType(file.getFileName().toString())));
        } catch (IOException ex) {
            Logger.getLogger(LauncherResourceFilesValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            watchdog = FileSystems.getDefault().newWatchService();
            GameRootPath.register(watchdog, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException ex) {
            Logger.getLogger(LauncherResourceFilesValidator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    boolean query(String FilePath){
        final Path CheckPath = Paths.get(FilePath);
        return PathDB.containsKey(CheckPath);
    }
    
    private ResourceType getResourceType(String FilePath){
        switch(FilePath.charAt(2)){
            case 'd':
                return ResourceType.description;
            case 'p':
                return ResourceType.panel;
            case 'i':
                return ResourceType.image;
            case 'm':
                return ResourceType.movie;
        }
        return ResourceType.none;
    }
    
    ResourceType validate(Path path){
        return getResourceType(path.toString());
    }
    
    void dump(){
        PathDB.forEach((path, type) -> System.out.println(path + " : " + type));
        System.err.println(PathDB.size());
    }
    
    @Override
    public void run() {
        while(true){
            try{
                final WatchKey watchKey = watchdog.take();
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    if (event.kind() == OVERFLOW)continue;

                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    
                    Path name = ev.context();
                    Path child = GameRootPath.resolve(name);
                    System.out.format("%s: %s\n", event.kind().name(), child);
                    
                    if(ev.kind() == ENTRY_CREATE)PathDB.put(ev.context(), validate(ev.context()));
                    if(ev.kind() == ENTRY_MODIFY)System.err.println("MODIFY");
                    if(ev.kind() == ENTRY_DELETE)PathDB.remove(ev.context());
                }
                watchKey.reset();
            }
            catch(ClosedWatchServiceException | InterruptedException ex){break;}
        }
    }
    
    void killWatchdog(){
        this.interrupt();
    }
    
    static final boolean isSquareImage(String ImagePath){
        final Path PanelPath = Paths.get(ImagePath);
        if(!Files.isRegularFile(PanelPath))return false;
        
        try{
            final Image panel = new Image(PanelPath.toUri().toString());
            return panel.getHeight() == panel.getWidth();
        }catch(NullPointerException | IllegalArgumentException e){
        }
        return false;
    }
}
