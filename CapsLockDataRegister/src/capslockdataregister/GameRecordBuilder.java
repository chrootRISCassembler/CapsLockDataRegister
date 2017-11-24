package capslockdataregister;

import cross_paradigm_lib.tuple;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.json.JSONObject;
import trivial_common_logger.LogHandler;

/**
 * GameRecordのビルダー.
 * <p>JSONのレコード,登録画面,一括登録のいずれかから構築する.
 * このクラスは渡された情報が正しいかについて何ら検査をしない.
 * このクラスのset系メソッドはメソッドチェーンで書けるように自分自身のインスタンスを返す.</p>
 */
class GameRecordBuilder{
    private boolean isFine = true;
    
    private UUID uuid;
    private String name;
    private String desc;
    private Path exe;
    private String ver;
    private Path panel = null;
    private List<Path> images;
    private List<Path> movies;
    private String ID = "0";
    
    private Path MSEXE;
    private Path JavaArchive;
    private Path ShellScript;
    private Path BatchFile;
    
    /**
     * 一括登録用のコンストラクタ.
     * @param GameDir ゲームのルートディレクトリ
     */
    GameRecordBuilder(Path GameDir){
        images = new ArrayList<>();
        movies = new ArrayList<>();
        
        LogHandler.inst.config("Start crawling in " + GameDir.toString());
        
        try {
            Files.walk(GameDir)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.getFileName().toString().matches(".*__(description|panel|image|movie)__.*|.*\\.(exe|jar|sh|bat)"))
                    .map(path -> PathUtil.inst.toRelativePath(path).normalize())
                    .map(path -> new tuple<>(ResourceType.TypeSurjection(path), path))
                    .forEach(FileInfo -> {
                        switch(FileInfo.getA()){
                            case msexe:
                                MSEXE = FileInfo.getB();
                                break;
                            case jar:
                                JavaArchive = FileInfo.getB();
                                break;
                            case shell:
                                ShellScript = FileInfo.getB();
                                break;
                            case batch:
                                BatchFile = FileInfo.getB();
                                break;
                            case desc:
                                final DescriptionFileParser FileParser = new DescriptionFileParser(FileInfo.getB());
                                if(FileParser.isFine()){
                                    name = FileParser.getName();
                                    desc = FileParser.getDescription();
                                    ver = FileParser.getVersion();
                                }else{
                                    LogHandler.inst.warning(FileInfo.getA().toString() + " is wrong.This file may be not UTF-8.");
                                }
                                break;
                            case panel:
                                panel = FileInfo.getB();
                                break;
                            case image:
                                images.add(FileInfo.getB());
                                break;
                            case movie:
                                movies.add(FileInfo.getB());
                            }
                    });
        } catch (IOException ex) {
            System.err.println(ex);
        }

        if(MSEXE != null){
            exe = MSEXE;
        }else if(JavaArchive != null){
            exe = JavaArchive;
        }else if(ShellScript != null){
            exe = ShellScript;
        }else if(BatchFile != null){
            exe = BatchFile;
        }else{//実行できるファイルが一つも見つからなかった.
            LogHandler.inst.warning("There is no executable file in " + GameDir.toString());
            isFine = false;
        }
        
        if(isFine){
            if(desc == null)LogHandler.inst.fine("\tDescFile NotFound");
            if(panel == null)LogHandler.inst.fine("\tPanelFile NotFound");
            if(images.isEmpty())LogHandler.inst.fine("\tImageFile NotFound");
            if(movies.isEmpty())LogHandler.inst.fine("\tMovieFile NotFound");
        }
        
        LogHandler.inst.finest("Finished crawling in " + GameDir.toString());
    }
    
    /**
     * 登録画面用のコンストラクタ.
     * <p>各プロパティをsetterで登録してからbuild().</p>
     */
    GameRecordBuilder(){
        isFine = false;
    }
    
    /**
     * 登録済み情報用のコンストラクタ.
     */
    GameRecordBuilder(JSONObject record){
        this.uuid = UUID.fromString(record.getString("UUID"));
        this.name = record.getString("name");
        this.desc = record.getString("description");
        this.exe = Paths.get(record.getString("executable"));
        this.ver = record.getString("version");
        
        {
            final Path PanelPath = Paths.get(record.getString("panel"));
            this.panel = Files.isRegularFile(PanelPath) ? PanelPath : null;
        }
        this.images = record.getJSONArray("image").toList().stream()
                .map(jsonobject -> jsonobject.toString())
                .map(str -> Paths.get(str))
                .collect(Collectors.toList());
        
        this.movies = record.getJSONArray("movie").toList().stream()
                .map(jsonobject -> jsonobject.toString())
                .map(str -> Paths.get(str))
                .collect(Collectors.toList());
   
        this.ID = record.getString("ID");
    }

    /**
     * 正常にGameRecordが生成できるかを返す.
     * この関数がfalseを返すとき,このクラスオブジェクトは直ちに破棄されるべき.
     */
    final boolean canBuild(){
        if(isFine)return true;
        if(exe != null){
            isFine = true;
            return true;
        }
        return false;
    }
    
    /**
     * GameRecordを生成する.
     * <p>エラーがあるとnullを返す</p>
     * @return 生成されたGameRecord
     */
    final GameRecord build(){
        if(!isFine)return null;
        
        if(uuid == null)uuid = UUID.randomUUID();
        if(desc == null)desc = "";
        if(name == null){
            final String ExeFileName = exe.getFileName().toString();
            name = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
        }
        if(ver == null)ver = "";
        
        return new GameRecord(
                    uuid,
                    name,
                    desc,
                    exe,
                    ver,
                    panel,
                    images, 
                    movies,
                    ID);
    }
    
    final GameRecordBuilder setUUID(UUID uuid){
        this.uuid = uuid;
        return this;
    }
    
    final GameRecordBuilder setName(String name){
        this.name = name;
        return this;
    }
    
    final GameRecordBuilder setDesc(String desc){
        this.desc = desc;
        return this;
    }
    
    final GameRecordBuilder setExe(Path exe){
        this.exe = exe;
        return this;
    }
    
    final GameRecordBuilder setVer(String ver){
        this.ver = ver;
        return this;
    }
    
    final GameRecordBuilder setPanel(Path panel){
        this.panel = panel;
        return this;
    }
    
    final GameRecordBuilder setImages(List<Path> images){
        this.images = images;
        return this;
    }
        
    final GameRecordBuilder setMovies(List<Path> movies){
        this.movies = movies;
        return this;
    }
    
    final GameRecordBuilder setID(String ID){
        this.ID = ID;
        return this;
    }
}
