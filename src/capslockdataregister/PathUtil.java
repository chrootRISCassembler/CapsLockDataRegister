/*  
    This file is part of CapsLockDataRegister. CapsLockDataRegister is a JSON generator for CapsLock.
    Copyright (C) 2017 RISCassembler

    CapsLockDataRegister is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package capslockdataregister;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.json.JSONArray;

/**
 * ファイルパスの解決や検証を行う.
 * <p>このクラスはシングルトン.</p>
 * <code>PathUtil.inst.*</code>
 * <p>として各メンバ,関数にアクセスする.</p>
 * <p>このクラスは各ゲームと独立した処理を担当する.ゲームごとに異なる処理が求められる関数は{@link capslockdataregister.LauncherResourceFilesValidator}に実装する.</p>
 */
enum PathUtil {
    inst;
    
    final Path CurrentDirectory = Paths.get(".").toAbsolutePath().getParent();
    final Path GamesDirectory = Paths.get("./Games/").normalize().toAbsolutePath();
    final boolean isDOSFileSystem;
    
    /**
     * ゲームの登録IDの上限.登録画面からも参照される.
     */
    static final int GAME_ID_MAX = 50;
            
    private PathUtil(){
        final DosFileAttributeView DOSattr = Files.getFileAttributeView(GamesDirectory, DosFileAttributeView.class);
        isDOSFileSystem = DOSattr != null;
    }
    
    private final ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator> LRUlist = new ThreadSafeLRU_list<UUID, LauncherResourceFilesValidator>(){
        @Override
        protected void onRemoveEntry(Map.Entry<UUID, LauncherResourceFilesValidator> eldest){
            eldest.getValue().killWatchdog();
        }
    };
    
    
    /**
     * Add Validator to the cache and return that Validator.
     * <p>If Validator is already cached, CreatorLambda isn't evaluated.</p>
     * <p>For use lazy evaluation, an inst of Validator should not be created expect here.</p>
     * @param uuid UUID of a game.
     * @param CreatorLambda lambda to create inst of Validator.
     * @return created/cached inst of Validator.
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
        return CurrentDirectory.relativize(path.toAbsolutePath());
    }
    
    final JSONArray genJSONArray(String RawString){
        String[] files = RawString.split(",");
        
        final JSONArray array = new JSONArray();
            
        Arrays.stream(files)
                .peek(ele -> System.err.println(ele))
                .forEach(ele -> array.put(ele));
        
        return array;
    }
    
    /**
     * ファイルが最小の権限を付与されているかチェックする.
     * <p>端的にはchmod 444になってるかチェック.
     * 画像や動画などは読み込む権限だけあればいい.余計な権限があるとfalseを返す.
     * 引数にディレクトリを渡すと必ずfalseを返す.</p>
     * @param path 検査するファイルパス.
     * @return 最小権限が付与されているか.
     */
    final boolean hasLeastPrivilege(Path path){
        if(!Files.isRegularFile(path))return false;
        
        if(!Files.isReadable(path))return false;
        if(Files.isWritable(path))return false;
        
        return isDOSFileSystem || !Files.isExecutable(path);
    }
}
