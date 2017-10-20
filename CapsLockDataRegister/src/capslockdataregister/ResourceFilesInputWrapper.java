package capslockdataregister;

import java.util.UUID;

/**
 *
 */
enum ResourceFilesInputWrapper {
    instance;
    
    private final ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator> LRUlist = new ThreadSafeLRU_list<>();
    
}
