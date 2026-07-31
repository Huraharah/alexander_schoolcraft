package com.group_8.universal_gift_registry;
/**@application: UniversalGiftRegistry
 * @author: Alexander Schoolcraft, Benjamin King, Brandon King, Gabe Woolums
 * @date: 3/21/2024
 * @version: 3.0
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**Universal Gift Registry Application Main Driver Class
 * Overrides JFX library's start method to load the login/register scene, and then launch it.
 */
public class UniversalGiftRegistryApplication extends Application {

    public static void main(String[] args) {
    	launch(args);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Login_Register.fxml"));
        Parent root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
