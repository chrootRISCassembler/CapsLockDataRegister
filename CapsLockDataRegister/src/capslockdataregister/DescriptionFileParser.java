package capslockdataregister;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author RISCassembler
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
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
        } catch(IOException ex){
            System.err.println(ex);
        } finally{
            FineFlag = false;
        }
    }
    
    final boolean isFine(){return FineFlag;}
    final String getName(){return GameName;}
    final String getVersion(){return GameVersion;}
    final String getDescription(){return GameDescription;}
}
