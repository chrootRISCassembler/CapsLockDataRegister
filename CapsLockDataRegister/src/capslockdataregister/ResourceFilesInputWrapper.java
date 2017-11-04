package capslockdataregister;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 *
 */
enum ResourceFilesInputWrapper {
    instance;
    
    final Path CurrentDirectory = Paths.get(".").toAbsolutePath().getParent();
    final Path GamesDirectory = Paths.get("./Games/");
    private final ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator> LRUlist = new ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator>(){
        @Override
        protected void onRemoveEntry(Map.Entry<UUID, LauncherResourceFilesValidator> eldest){
            eldest.getValue().killWatchdog();
        }
    };
    
    
    /**
     * Add Validator to the cache and return that Validator.
     * <p>If Validator is already cached, CreatorLambda isn't evaluated.</p>
     * <p>For use lazy evaluation, an instance of Validator should not be created expect here.</p>
     * @param uuid UUID of a game.
     * @param CreatorLambda lambda to create instance of Validator.
     * @return created/cached instance of Validator.
     */
    LauncherResourceFilesValidator add(UUID uuid, Supplier<LauncherResourceFilesValidator> CreatorLambda){
        LauncherResourceFilesValidator retValidator = LRUlist.get(uuid);
        if(retValidator != null){
            return retValidator;
        }
        
        retValidator = CreatorLambda.get();
        LRUlist.put(uuid, retValidator);
        retValidator.start();
        return retValidator;
    }
    
    void destroy(){
        LRUlist.forEach((dummy, validator) -> validator.killWatchdog());
    }
}
