package capslockdataregister;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * ファイルパスの解決や検証を行う.
 * <p>このクラスはシングルトン.</p>
 * <code>ResourceFilesInputWrapper.instance.*</code>
 * <p>として各メンバ,関数にアクセスする.</p>
 * <p>このクラスは各ゲームと独立した処理を担当する.ゲームごとに異なる処理が求められる関数は{@link capslockdataregister.LauncherResourceFilesValidator}に実装する.</p>
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
    
    /**
     * カレントディレクトリからの相対パスに変換する.
     * <p>環境に依存しないように,引数に渡されたパスを相対パスに変換する.</p>
     * @param path 変換するパス.
     * @return カレントディレクトリからの相対パス.
     */
    final Path toRelativePath(Path path){
        return CurrentDirectory.relativize(path);
    }
}
