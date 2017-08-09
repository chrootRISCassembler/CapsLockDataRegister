package capslockdataregister;

import java.io.IOException;

import javafx.application.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;

/**
 *
 * @author RISCassembler
 */
public class CapsLockDataRegister extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
    
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("RegisterForm.fxml"));
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return;
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
