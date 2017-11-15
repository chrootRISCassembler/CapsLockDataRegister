package capslockdataregister;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;

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
        try(final BufferedReader LineReader = Files.newBufferedReader(path)){
            GameName = LineReader.readLine();
            GameVersion = LineReader.readLine();

            StringBuilder description = new StringBuilder();
            String line;
            while((line = LineReader.readLine()) != null){
                description.append(line);
            }
            GameDescription = description.toString();
        } catch(MalformedInputException ex) {
            System.err.println("Wrong character encoding.This file is not UTF-8.");
            FineFlag = false;
        } catch (IOException ex) {
            System.err.println(ex);
            FineFlag = false;
        }
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
