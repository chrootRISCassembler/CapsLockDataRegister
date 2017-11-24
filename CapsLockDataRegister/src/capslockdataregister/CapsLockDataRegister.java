package capslockdataregister;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author RISCassembler
 */
public final class CapsLockDataRegister extends Application {

    /**
     * @param args the command line arguments
     */
    public static final void main(String[] args) {
        launch(args);
        ResourceFilesInputWrapper.instance.destroy();
        try {
            ResourceFilesInputWrapper.instance.LogWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(CapsLockDataRegister.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public final void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
        
        Parent root;

        try {
            root = loader.load();
        } catch (IOException ex) {
            System.out.println(ex);
            TrivialLogger.inst.log("Failed to load MainForm.fxml", 1);
            TrivialLogger.inst.log(ex, 1);
            return;
        }
        
        MainFormController controller = (MainFormController)loader.getController();
        controller.setOwnStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle("CapsLockDataRegister ランチャー情報登録ツール");
        stage.show();
    }
}
