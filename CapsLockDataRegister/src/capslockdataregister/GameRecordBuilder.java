package capslockdataregister;

import cross_paradigm_lib.tuple;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;

/**
 * 一括登録時に使われるGameRecordのビルダー.
 * <p>このクラスはイミュータブル.</p>
 */
class GameRecordBuilder{
    private boolean isFine = true;
    
    private Path MSEXE;
    private Path JavaArchive;
    private Path ShellScript;
    private Path BatchFile;
    
    private Path desc;
    private Path exe;
    private Path panel;
    private Set<Path> images = new HashSet<>();
    private Set<Path> movies = new HashSet<>();
    
    /**
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
     * 正常にGameRecordが生成できるかを返す.
     * この関数がfalseを返すとき,このクラスオブジェクトは直ちに破棄されるべき.
     */
    final boolean isFine(){
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
}
