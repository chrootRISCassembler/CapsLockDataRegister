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

/**
 * ランチャーが使用するリソースファイルの種類を表す.
 */

enum ResourceType {
    desc,
    msexe,
    jar,
    shell,
    batch,
    panel,
    image,
    movie,
    none;
    
    /**
     * パスからリソースファイルの種類を返す.
     */
    static final ResourceType TypeSurjection(Path path){
        return TypeSurjection(path.getFileName().toString());
    }
    
    /**
     * ファイル名からリソースファイルの種類を返す.
     */
    static final ResourceType TypeSurjection(String FileName){
        if(FileName.matches(".*__(description|panel|image|movie)__.*")){
            if(FileName.matches(".*__description__.*")){
                return desc;
            }else if(FileName.matches(".*__panel__.*")){
                return panel; 
            }else if(FileName.matches(".*__image__.*")){
                return image;
            }else{
                return movie;
            }
        }
        
        final int StrLen = FileName.length();
        switch(FileName.substring(StrLen - 3, StrLen)){
            case "exe":
                return msexe;
            case "jar":
                return jar;
            case ".sh":
                return shell;
            case "bat":
                return batch;
        }
        return none;
    }
}
