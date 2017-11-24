package capslockdataregister;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 簡易ロガー.
 * <p>IDE以外から起動してデバッグが必要な時用</p>
 */
enum TrivialLogger{
    inst;//インスタンス

    static final int QUIET = 0;
    static final int FULL = 1;
    
    private final int logLevel;
    
    private BufferedWriter writer;
    
    private TrivialLogger(){
        logLevel = readLogLevel();
        
        final Path logFile = Paths.get("./register.log");
        try {
            writer = Files.newBufferedWriter(logFile);
        } catch (IOException ex) {
            System.err.println("!!!CRITICAL!!!\nCan't open log file.(./register.log)");
            System.err.println(ex);
        }
    }
    
    final void log(String msg, int level){
        if(level <= logLevel){
            try {
                writer.write(msg);
            } catch (IOException ex){
                System.err.println("Can't write to log file.");
                System.err.println(ex);
            }
        }
    }
    
    private static final int readLogLevel(){
        try{
            final Path conf = Paths.get("./register.conf");
            final String firstLine = Files.newBufferedReader(conf).readLine();
            final int readLogLevel = Integer.getInteger(firstLine);
            
            return readLogLevel < 0 || readLogLevel > FULL ? FULL : readLogLevel;
        }catch(Exception ex){
            System.err.println(ex);
            return FULL;
        }
    }
}
