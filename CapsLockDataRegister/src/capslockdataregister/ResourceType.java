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
