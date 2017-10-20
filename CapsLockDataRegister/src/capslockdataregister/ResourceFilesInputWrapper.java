package capslockdataregister;

import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 */
enum ResourceFilesInputWrapper {
    instance;
    
    private final ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator> LRUlist = new ThreadSafeLRU_list<>();
    
    LauncherResourceFilesValidator add(UUID uuid, Supplier<LauncherResourceFilesValidator> CreatorLambda){
        LauncherResourceFilesValidator retValidator = LRUlist.get(uuid);
        if(retValidator != null)return retValidator;
        
        retValidator = CreatorLambda.get();
        LRUlist.put(uuid, retValidator);
        return retValidator;
    }
}
