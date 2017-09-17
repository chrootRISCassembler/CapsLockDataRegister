package capslockdataregister;

import java.io.IOException;
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
    }
    
    @Override
    public final void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainForm.fxml"));
        
        Parent root;

        try {
            root = loader.load();
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return;
        }
        
        MainFormController controller = (MainFormController)loader.getController();
        controller.setOwnStage(stage);

        stage.setScene(new Scene(root));
        stage.setTitle("CapsLockDataRegister ランチャー情報登録ツール");
        stage.show();
    }
}
