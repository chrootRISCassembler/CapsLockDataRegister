package capslockdataregister;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.json.JSONArray;

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
    final Path GamesDirectory = Paths.get("./Games/").toAbsolutePath();
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
    
    final JSONArray genJSONArray(String RawString){
        String[] files = RawString.split(",");
        
        final JSONArray array = new JSONArray();
            
        Arrays.stream(files)
                .peek(ele -> System.err.println(ele))
                .forEach(ele -> array.put(ele));
        
        return array;
    }
    
    
    final Stream<String> parseToStringStream(String RawString){
        List<String> PathStringList = new ArrayList<>();
        
        boolean WaitingEndQuot = false;//トークンの終端の"を待機しているか
        boolean IsEscaped = false;//次の文字がエスケープされているか
        boolean IsWatingTokenFirst = false;//トークンの最初の有効な文字を待機しているか(始端の"の次の文字)
        boolean ErrorToken = false;//トークンにエラーがあった
        int TokenFirst = 0;
        
        for(int i = 0;i != RawString.length();++i){
            final char ch = RawString.charAt(i);
            switch(ch){
                case '\"':
                    if(IsEscaped){
                        IsEscaped = false;
                    }else{
                        if(WaitingEndQuot){
                            
                            //substring here
                            //check
                        }
                    }
                break;
                
                case '\\':
                    if(IsEscaped){
                        
                    }else{
                        IsEscaped = true;
                    }
                break;
                
                default:
                    if(IsWatingTokenFirst){
                        TokenFirst = i;
                        IsWatingTokenFirst = false;
                    }
                    
                    if(IsEscaped){
                        IsEscaped = false;
                        continue;
                    }
            }
        }
        return PathStringList.stream();
    }
}
