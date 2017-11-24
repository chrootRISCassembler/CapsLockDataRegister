package capslockdataregister;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import trivial_common_logger.LogHandler;

/**
 * エントリポイント.
 */
public final class CapsLockDataRegister extends Application {

    /**
     * @param args the command line arguments
     */
    public static final void main(String[] args) {
        try{
            launch(args);
        }catch(Exception ex){
            LogHandler.inst.DumpStackTrace(ex);
        }
        
        PathUtil.inst.destroy();
        LogHandler.inst.close();
    }
    
    @Override
    public final void start(Stage stage) throws Exception {
        LogHandler.inst.finer("Application#start called.");

        final FXMLLoader loader;
        try{
            loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
        }catch(Exception ex){
            LogHandler.inst.severe("Failed to get resource.");
            LogHandler.inst.DumpStackTrace(ex);
            return;
        }
        
        final Parent root;

        try {
            root = loader.load();
        } catch (IOException ex) {
            LogHandler.inst.severe("Failed to load MainForm.fxml");
            LogHandler.inst.DumpStackTrace(ex);
            return;
        }
        
        MainFormController controller = (MainFormController)loader.getController();
        controller.setOwnStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle("CapsLockDataRegister ランチャー情報登録ツール");
        LogHandler.inst.finest("try to display MainForm window.");
        stage.show();
    }
}
