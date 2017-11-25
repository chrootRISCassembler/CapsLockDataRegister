package capslockdataregister;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Descriptionファイルを読み込んでパースするクラス.
 * <p>このクラスはイミュータブル.</p>
 * <p>{@link capslockdataregister.DescriptionFileParser#isFine()}がfalseを返すときはメンバメソッドを呼び出してはならない.</p>
 */
final class DescriptionFileParser {
    private String GameName = "";
    private String GameVersion = "";
    private String GameDescription = "1";
    private boolean FineFlag = true;
    
    DescriptionFileParser(Path path){
        List<String> lines = null;
        
        try{
            lines = Files.readAllLines(path);//UTF-8で全行読み込み
        } catch (IOException ex) {
            System.err.println("Failed to read by UTF-8");
        } catch (SecurityException ex){
            System.err.println(ex);
            FineFlag = false;
        }
        
        if(lines != null){
            if (lines.get(0).startsWith("\uFEFF")) {
                GameName = lines.get(0).substring(1);//skip BOM of UTF-8
            }else{
                GameName = lines.get(0);
            }
            GameVersion = lines.get(1);
            GameDescription = lines.stream()
                    .skip(2)
                    .collect(Collectors.joining());
            return;
        }
        
        try{
            lines = Files.readAllLines(path, Charset.forName("sjis"));//Shift_JISで全行読み込み
        } catch (IOException ex) {
            System.err.println("Failed to read by Shift-JIS");
            FineFlag = false;
            return;
        } catch (SecurityException ex){
            System.err.println(ex);
            FineFlag = false;
            return;
        }
        
        GameName = lines.get(0);
        GameVersion = lines.get(1);
        GameDescription = lines.stream()
                .skip(2)
                .collect(Collectors.joining());
    }
    
    /**
     * Descriptionが正常にパースできたかを返す.
     * <p>このメソッドがfalseを返すとき,get*系のメソッドの戻り値はもはや信用できない.</p>
     */
    final boolean isFine(){return FineFlag;}
    final String getName(){return GameName;}
    final String getVersion(){return GameVersion;}
    final String getDescription(){return GameDescription;}
}
