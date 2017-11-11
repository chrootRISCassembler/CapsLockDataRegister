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
        if(FileName.matches("__(description|panel|image|movie)__.*")){
            switch(FileName.charAt(2)){
                case 'd':
                    return desc;
                case 'p':
                    return panel;
                case 'i':
                    return image;
                case 'm':
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
