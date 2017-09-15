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
}
