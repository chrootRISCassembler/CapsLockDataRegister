package capslockdataregister;

import cross_paradigm_lib.tuple;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

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
    private Path panel;
    private Set<Path> images = new HashSet<>();
    private Set<Path> movies = new HashSet<>();
    private byte ID;
    
    private Path MSEXE;
    private Path JavaArchive;
    private Path ShellScript;
    private Path BatchFile;
    
    /**
     * 一括登録用のコンストラクタ.
     * @param GameDir ゲームのルートディレクトリ
     */
    GameRecordBuilder(Path GameDir){
        try {
            Files.walk(GameDir)
                    .filter(path -> Files.isRegularFile(path))
                    .filter(path -> path.getFileName().toString().matches("__(description|panel|image|movie)__.*|.*\\.(exe|jar|sh|bat)"))
                    .map(path -> ResourceFilesInputWrapper.instance.toRelativePath(path).normalize())
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
                                desc = FileInfo.getB();
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
        }else{
            isFine = false;//実行できるファイルが一つも見つからなかった.
        }
    }
    
    /**
     * 登録画面用のコンストラクタ.
     */
    GameRecordBuilder(){
    }
    
    /**
     * 登録済み情報用のコンストラクタ.
     */
    GameRecordBuilder(JSONObject record){
        
    }

    /**
     * 正常にGameRecordが生成できるかを返す.
     * この関数がfalseを返すとき,このクラスオブジェクトは直ちに破棄されるべき.
     */
    final boolean canBuild() {
        return isFine;
    }
    
    /**
     * GameRecordを生成する.
     * <p>エラーがあるとnullを返す</p>
     * @return 生成されたGameRecord
     */
    final GameRecord build(){
        if(!isFine)return null;
        
        final String NameStr;
        final String DescStr;
        final String VerStr;
        
        if(desc != null){//ロジックが汚い　要整理
            final DescriptionFileParser FileParser =  new DescriptionFileParser(desc);
            if(FileParser.isFine()){
                NameStr = FileParser.getName();
                DescStr = FileParser.getDescription();
                VerStr = FileParser.getVersion();
            }else{
                final String ExeFileName = exe.getFileName().toString();
            
                NameStr = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
                DescStr = "";
                VerStr = "1";
            }
        }else{
            final String ExeFileName = exe.getFileName().toString();
            
            NameStr = ExeFileName.substring(0, ExeFileName.lastIndexOf("."));
            DescStr = "";
            VerStr = "1";
        }

        final String PanelStr = panel == null ? "" : panel.toString();
        
        return new GameRecord(
                    UUID.randomUUID().toString(),
                    NameStr,
                    DescStr,
                    exe.toString(),
                    VerStr,
                    PanelStr,
                    new JSONArray(images), 
                    new JSONArray(movies),
                    "1");
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
    
    final GameRecordBuilder setID(byte ID){
        this.ID = ID;
        return this;
    }
}
