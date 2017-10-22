package capslockdataregister;

import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 */
enum ResourceFilesInputWrapper {
    instance;
    
    final Path CurrentDirectory = new File(".").getAbsoluteFile().toPath().getParent();
    private final ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator> LRUlist = new ThreadSafeLRU_list<>();
    
    LauncherResourceFilesValidator add(UUID uuid, Supplier<LauncherResourceFilesValidator> CreatorLambda){
        LauncherResourceFilesValidator retValidator = LRUlist.get(uuid);
        if(retValidator != null)return retValidator;
        
        retValidator = CreatorLambda.get();
        LRUlist.put(uuid, retValidator);
        return retValidator;
    }
}
