package capslockdataregister;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.image.Image;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;

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
    
     /**
     * 指定されたパスのファイルがアイコンとしてランチャーで正常に表示できるか検証する.
     * <p>表示可能な画像でも,アス比が1:1でないとfalseを返す</p>
     * @param PanelPath 検証するファイルのパス.
     * @return 正常に表示できるか否か.
     */
    final boolean isValidPanel(Path PanelPath){
        if(!Files.isRegularFile(PanelPath))return false;
        
        /*パネル画像ファイルはUnixパーミッションで　r--r--r-- であるべき*/
        
        if(Files.isWritable(PanelPath))return false;
        if(!Files.isReadable(PanelPath))return false;
        
        {//Windowsのファイルシステムでは実行権限と読み取り権限を完全には切り離せない
            DosFileAttributeView DOS_Attribute = Files.getFileAttributeView(PanelPath, DosFileAttributeView.class);
            if(DOS_Attribute == null && Files.isExecutable(PanelPath))return false;
        }
        
        if(GameRootPath == null){
            if(!PanelPath.startsWith(ResourceFilesInputWrapper.instance.CurrentDirectory))return false;//カレントディレクトリ以下にない
        }else{
            if(!PanelPath.startsWith(GameRootPath))return false;//ゲームのルートディレクトリ以下にない
        }
        
        final Image panel;
        
        try{
            panel = new Image(PanelPath.toUri().toString());
        }catch(NullPointerException | IllegalArgumentException ex){
            return false;
        }
        
        return panel.getHeight() == panel.getWidth();
    }
    
    /**
     * Verify images.
     * <p>Each path must be quoted by ".</p>
     * <p>Ex) "aaa", "bbb"</p>
     * @param Images Array of path.
     * @return Either all images are valid.
     */
    static final boolean areValidImages(String Images){
        if(Images.isEmpty())return false;
        try{
            for(final Path path : parseFiles(Images)){
                if(!Files.isRegularFile(path))return false;
                new Image(path.toUri().toString());
            }
        }catch(NullPointerException | IllegalArgumentException ex){
            return false;
        }
        return true;
    }
    
    /**
     * 動画がランチャーで正常に表示できるか検証する.
     * <p>個々のファイルパスは'"'で囲まれていなければならない.</p>
     * <p>Ex) "aaa", "bbb"</p>
     * <p>この関数は引数中に一つでも異常なパスを検出するとfalseを返す.一つもパスが指定されていないときもfalseを返す</p>
     * @param Movies '"'で囲まれた動画のパスの羅列.
     * @return 全ての動画が正常に表示できるかどうか.
     */
    static final boolean areValidMoves(String Movies){
        if(Movies.isEmpty())return false;
        try{
            for(final Path path : parseFiles(Movies)){
                if(!Files.isRegularFile(path))return false;
                new Media(path.toUri().toString());
            }
        }catch(NullPointerException | IllegalArgumentException | UnsupportedOperationException | MediaException ex){
            return false;
        }
        return true;
    }
    
    /**
     * Parse String of path array.
     * <p>Each path must be quoted by ".</p>
     * <p>Ex) "aaa", "bbb"</p>
     * @param source Array of path.
     * @return Set of parsed file's Path.
     * <h2>This function is incomplete.Someone fix it!</h2>
     */
    static final Set<Path> parseFiles(String source) throws InvalidPathException{
        boolean InDoubleQuotation = false;
        boolean IsEscaped = false;
        boolean IsWatingTokenFirst = false;
        int TokenFirst = 0;
        
        final Set<Path> PathSet = new HashSet<>();
        
        for(int i = 0;i != source.length();++i){
            final char ch = source.charAt(i);
            switch(ch){
                case '\"':
                    if(IsEscaped){
                        IsEscaped = false;
                        continue;
                    }
                    
                    if(InDoubleQuotation){
                        PathSet.add(Paths.get(source.substring(TokenFirst, i)));
                        InDoubleQuotation = false;
                    }else{
                        InDoubleQuotation = true;
                        IsWatingTokenFirst = true;
                    }
                break;
                case ',':
                    if(InDoubleQuotation)continue;
                    
                break;
                case ' ':
                    
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
                    break;
            }
        }
        return PathSet;
    }
}
