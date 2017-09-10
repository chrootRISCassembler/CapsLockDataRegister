package capslockdataregister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author RISCassembler
 */
public final class DescriptionFileParser {
    private String GameName = "";
    private String GameVersion = "";
    private String GameDescription = "1";
    private boolean FineFlag = true;
    
    public DescriptionFileParser(File file){
        try(final BufferedReader LineReader = new BufferedReader(new FileReader(file))){
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
    
    public final boolean isFine(){return FineFlag;}
    public final String getName(){return GameName;}
    public final String getVersion(){return GameVersion;}
    public final String getDescription(){return GameDescription;}
}
