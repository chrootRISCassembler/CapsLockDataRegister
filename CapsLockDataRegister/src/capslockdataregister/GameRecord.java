/*  
    This file is part of CapsLockDataRegister. CapsLockDataRegister is a JSON generator for CapsLock.
    Copyright (C) 2017 RISCassembler

    CapsLockDataRegister is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Foobar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/

package capslockdataregister;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import org.json.JSONObject;

/**
 *
 * @author RISCassembler
 */
public final class GameRecord extends GameSignature{
    private final JSONObject json;
    private final ReadOnlyStringProperty UUIDProperty;
    private final ReadOnlyStringProperty nameProperty;
    private final ReadOnlyStringProperty descProperty;
    private final ReadOnlyStringProperty exeProperty;
    private final ReadOnlyStringProperty verProperty;
    private final ReadOnlyStringProperty panelProperty;
    private final ReadOnlyStringProperty imageProperty;
    private final ReadOnlyStringProperty movieProperty;
    private final ReadOnlyStringProperty IDProperty;

    private String toTextFieldString(String JSONArrayString){
        return JSONArrayString.substring(1, JSONArrayString.length() - 1).replace("\"", "");
    }

    public GameRecord(
            UUID uuid,
            String name,
            String desc, 
            Path exe,
            String ver,
            Path panel,
            List<Path> images,
            List<Path> movies,
            String ID
    ){
        super(uuid, desc, exe, panel, images, movies);
        
        UUIDProperty = new SimpleStringProperty(uuid.toString());
        nameProperty = new SimpleStringProperty(name);
        descProperty = new SimpleStringProperty(desc.isEmpty() ? "" : "exist");
        exeProperty = new SimpleStringProperty(exe.getFileName().toString());
        verProperty = new SimpleStringProperty(ver);
        panelProperty = new SimpleStringProperty(panel == null ? "" : "exist");
        imageProperty = new SimpleStringProperty(Integer.toString(images.size()));
        movieProperty = new SimpleStringProperty(Integer.toString(movies.size()));
        IDProperty = new SimpleStringProperty(ID);

        json = new JSONObject()
            .put("UUID", uuid)
            .put("name", name)
            .put("description", desc)
            .put("executable", exe)
            .put("version", ver)
            .put("panel", panel == null ? "" : panel)
            .put("image", images)
            .put("movie", movies)
            .put("ID", ID);
        System.err.println(json.toString());
    }
    
    public final ReadOnlyStringProperty uuidProperty(){return UUIDProperty;}
    public final ReadOnlyStringProperty nameProperty(){return nameProperty;}
    public final ReadOnlyStringProperty descProperty(){return descProperty;}
    public final ReadOnlyStringProperty exeProperty(){return exeProperty;}
    public final ReadOnlyStringProperty verProperty(){return verProperty;}
    public final ReadOnlyStringProperty panelProperty(){return panelProperty;}
    public final ReadOnlyStringProperty imageProperty(){return imageProperty;}
    public final ReadOnlyStringProperty movieProperty(){return movieProperty;}
    public final ReadOnlyStringProperty IDProperty(){return IDProperty;}
    public final JSONObject getJSON(){return json;}
    
    @Override
    final String getName(){
        return nameProperty.getValue();
    }
    
    @Override
    final String getVer(){
        return verProperty.getValue();
    }
    
    @Override
    final String getID(){
        return IDProperty.getValue();
    }
}
