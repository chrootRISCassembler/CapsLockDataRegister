/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package capslockdataregister;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.json.JSONWriter;

/**
 *
 * @author RISCassembler
 */
public class CapsLockDataRegister {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        FileWriter writer;
        
        try{
            writer = new FileWriter("GamesInfo.json");
        }catch(IOException e){
            System.out.println(e);
            return;
        }
        
        UUID uuid = UUID.randomUUID();
        
        JSONWriter JsonWriter = new JSONWriter(writer)
                .object()
                    .key("UUID")
                    .value(uuid.toString())
                    .key("name")
                    .value("FooGame")
                    .key("executable")
                    .value("FooGame/FooGame.exe")
                    .key("version")
                    .value("1")
                    .key("image")
                    .value(null)
                    .key("movie")
                    .value(null)
                .endObject();
        
        try{
            writer.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }
    
}
