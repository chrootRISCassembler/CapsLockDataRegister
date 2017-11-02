package capslockdataregister;

/**
 *
 * @author RISCassembler
 */

final class Command {
    private Command(){
        System.err.println("CRITICAL : Instance of Command class is created.");
    }
    
    static void dump(final String[] ParsedStringArray, MainFormController controller){
        controller.DisplayCollection.forEach(ele -> System.err.println(ele.geJSON()));
    }

    /**
     * 0-7の文字に対して"rwx"のようなパーミッションを表す三文字を返す.
     * <p>PosixFilePermissions.fromString()に渡す引数を構築するときに使う</p>
     * <p>0から7以外の文字を渡すと"---"を返す</p>
     * @param num 0から7の文字.本物のchmodに渡す700のような引数の一文字分.
     * @return "rwx"とか"r--"のような三文字.
     */
    static final String toPermString(char num){
        switch(num){
            case '1':
                return "--x";
            case '2':
                return "-w-";
            case '3':
                return "-wx";
            case '4':
                return "r--";
            case '5':
                return "r-x";
            case '6':
                return "rw-";
            case '7':
                return "rwx";
            default:
                return "---";
        }
    }
}
