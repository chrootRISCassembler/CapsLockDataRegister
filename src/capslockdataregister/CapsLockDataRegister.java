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
