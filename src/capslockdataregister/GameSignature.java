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

/**
 *
 * <p>このクラスはイミュータブル.</p>
 * <p>nameとverはプロパティが同一のものを保持するためここでは保持しない</p>
 */
abstract public class GameSignature {
    private final UUID uuid;
    private final String desc;
    private final Path exe;
    private final Path panel;
    private final List<Path> images;
    private final List<Path> movies;

    GameSignature(
            UUID uuid,
            String desc,
            Path exe,
            Path panel,
            List<Path> images,
            List<Path> movies){
        
        this.uuid = uuid;
        this.desc = desc;
        this.exe = exe;
        this.panel = panel;
        this.images = images;
        this.movies = movies;
    }
    
    final UUID getUUID(){return uuid;}
    abstract String getName();
    final String getDesc(){return desc;}
    final Path getExe(){return exe;}
    abstract String getVer();
    final Path getPanel(){return panel;}
    final List<Path> getImages(){return images;}
    final List<Path> getMovies(){return movies;}
    abstract String getID();
    
}
