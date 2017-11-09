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
import java.util.stream.Stream;
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
    
    /**
     * ファイルパスと,そのファイルの種類をキャッシュするKey-Valueストア.必ず絶対パスで保存する.
     */
    private ConcurrentHashMap<Path, ResourceType> PathDB = new ConcurrentHashMap<>();
    private WatchService watchdog;
    
    
    /**
     * 新たにゲームを登録するときのValidatorのコンストラクタ.
     */
    LauncherResourceFilesValidator(){
        GameRootPath = null;//新規登録を表すフラグとして使う
    }
    
    
    /**
     * 登録済みゲーム用コンストラクタ.
     * @param ExePath ゲームの実行ファイルパス
     */
    LauncherResourceFilesValidator(String ExePath){
        crawl(Paths.get(ExePath).toAbsolutePath());
    }
    
    /**
     * ゲームの本体を基点として,ランチャーに必要なファイル群をかき集める.
     * <p>ゲーム本体のパスからゲームのルートディレクトリを定め,そのディレクトリ以下からランチャーが使用するファイルを収集する.
     * 収集したファイルはPathDBにキャッシュされる.</p>
     * @param ExePath ゲーム本体のパス
     */
    final void crawl(Path ExePath){
        GameRootPath = ResourceFilesInputWrapper.instance.toRelativePath(ExePath).subpath(0, 2).toAbsolutePath();
        try {
            Files.walk(GameRootPath, FileVisitOption.FOLLOW_LINKS)
                    .parallel()
                    .filter(file -> file.getFileName().toString().matches("__(description|panel|image|movie)__.*"))
                    .peek(file -> System.err.println(file))
                    .forEach(file -> PathDB.put(file.toAbsolutePath(), getResourceType(file.getFileName().toString())));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        
        try {
            watchdog = FileSystems.getDefault().newWatchService();
            GameRootPath.register(watchdog, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }
    
    /**
     * ファイルがゲームのルートディレクトリ以下にに存在するか検証する.
     * <p>ゲームのルートディレクトリが定まっていない場合は"Games/"以下にあるかで検証する.</p>
     * @param path 検証するファイルパス
     * @return ゲームのディレクトリ以下又は"Games/"以下に位置すればtrue.それ以外はfalse.
     */
    final boolean isLocatedValidly(Path path){
        if(GameRootPath == null){
            return path.startsWith(ResourceFilesInputWrapper.instance.GamesDirectory);
        }else{
            return path.startsWith(GameRootPath);
        }
    }
    
    Stream<Path> query(ResourceType type){
        return PathDB.entrySet()
                .stream()
                .parallel()
                .filter(KeyValue -> KeyValue.getValue() == type)
                .map(KeyValue -> KeyValue.getKey());
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
            if(watchdog == null)try{sleep(1000);}
            catch (InterruptedException ex) {
                System.err.println(ex);
            }
            
            try{
                final WatchKey watchKey = watchdog.take();
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    if (event.kind() == OVERFLOW)continue;

                    WatchEvent<Path> ev = (WatchEvent<Path>)event;
                    
                    Path name = ev.context();
                    Path child = GameRootPath.resolve(name);
                    System.out.format("%s: %s\n", event.kind().name(), child);
                    
                    if(ev.kind() == ENTRY_CREATE)PathDB.put(ev.context().toAbsolutePath(), validate(ev.context()));
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
    
     /**
     * 指定されたパスのファイルがアイコンとしてランチャーで正常に表示できるか検証する.
     * <p>表示可能な画像でも,アス比が1:1でないとfalseを返す</p>
     * @param PanelPath 検証するファイルのパス.
     * @return 正常に表示できるか否か.
     */
    final boolean isValidPanel(Path PanelPath){
        if(!ResourceFilesInputWrapper.instance.hasLeastPrivilege(PanelPath))return false;
        if(!isLocatedValidly(PanelPath.toAbsolutePath()))return false;
        
        final Image panel;
        
        try{
            panel = new Image(PanelPath.toUri().toString());
        }catch(NullPointerException | IllegalArgumentException ex){
            return false;
        }
        
        return panel.getHeight() == panel.getWidth();
    }
    
    /**
     * 画像がランチャーで正常に表示できるか検証する.
     * <p>個々のファイルパスは'"'で囲まれていなければならない.</p>
     * <p>Ex) "Games/FooGame/__image__0.png", "Games/FooGame/__image__1.png"</p>
     * <p>1つもパスが指定されていない又は異常なパスが1つ以上あるとき,この関数はfalseを返す.</p>
     * @param Images "で囲まれた画像のパスの羅列.
     * @return 指定された全ての画像が正常に表示できるか.
     */
    final boolean areValidImages(String Images){
        if(Images.isEmpty())return false;
        QuotedStringParser Parser = new QuotedStringParser(Images);
        if(Parser.hasError())return false;
        
        for(String StrPath : Parser.get()){
            final Path path = Paths.get(StrPath);
            if(!ResourceFilesInputWrapper.instance.hasLeastPrivilege(path))return false;
            if(!isLocatedValidly(path.toAbsolutePath()))return false;
        }
        return true;
    }
    
    /**
     * 動画がランチャーで正常に表示できるか検証する.
     * <p>個々のファイルパスは'"'で囲まれていなければならない.</p>
     * <p>Ex) "Games/FooGame/__movie__0.png", "Games/FooGame/__movie__1.png"</p>
     * <p>1つもパスが指定されていない又は異常なパスが1つ以上あるとき,この関数はfalseを返す.</p>
     * @param Movies '"'で囲まれた動画のパスの羅列.
     * @return 指定された全ての動画が正常に表示できるかどうか.
     */
    final boolean areValidMoves(String Movies){
        if(Movies.isEmpty())return false;
        QuotedStringParser Parser = new QuotedStringParser(Movies);
        if(Parser.hasError())return false;
        
        for(String StrPath : Parser.get()){
            final Path path = Paths.get(StrPath);
            if(!ResourceFilesInputWrapper.instance.hasLeastPrivilege(path))return false;
            if(!isLocatedValidly(path.toAbsolutePath()))return false;
        }
        return true;
    }
}
