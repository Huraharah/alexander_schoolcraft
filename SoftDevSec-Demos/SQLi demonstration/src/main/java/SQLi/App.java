package SQLi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage STAGE;
    private final LoginService service = new LoginService();

    private static final String LOC              = "/SQLi/";
    private static final String F_TEST           = LOC + "SQLitest.fxml";
    private static final String F_ATTACK         = LOC + "SQLiattack.fxml";
    private static final String F_INSECURE_PASS  = LOC + "SQLiinsecurepass.fxml";
    private static final String F_INSECURE_FAIL  = LOC + "SQLiinsecurefail.fxml";
    private static final String F_SECURE_PASS    = LOC + "SQLisecurepass.fxml";
    private static final String F_SECURE_FAIL    = LOC + "SQLisecurefail.fxml";

    @Override
    public void start(Stage stage) throws Exception {
        STAGE = stage;
        
        Db.wake_ping();
        Db.resetFreshDatabase();

        showTest();
        STAGE.setTitle("SQLi Tester and Demonstration");
        STAGE.show();
    }

    private void showTest() throws Exception {
        Parent root = load(F_TEST);
        setupUsersTable(root);
        bindTestHandlers(root);
        ensureScene(root);
    }
    
    @SuppressWarnings("unchecked")
    private void setupUsersTable(Parent root) {
        var tv = (javafx.scene.control.TableView<UserRow>) root.lookup("#users_table");
        if (tv == null) return;

        // Grab columns by order (or search by id if you prefer)
        var cols = tv.getColumns();
        var colUser = (javafx.scene.control.TableColumn<UserRow, String>) cols.get(0);
        var colPass = (javafx.scene.control.TableColumn<UserRow, String>) cols.get(1);
        javafx.scene.control.TableColumn<UserRow, String> colSecret = null;
        if (cols.size() > 2) {
            colSecret = (javafx.scene.control.TableColumn<UserRow, String>) cols.get(2);
        }

        // Bind columns to getters by name
        colUser.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("username"));
        colPass.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("passwd"));
        if (colSecret != null) {
            colSecret.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("secret"));
        }

        // Default rows for the demo (plaintext only because this is a teaching app)
        var items = javafx.collections.FXCollections.observableArrayList(
            new UserRow("admin", "admin123"),
            new UserRow("alice", "password1"),
            new UserRow("bob",   "hunter2")
        );

        tv.setItems(items);

        // Optional: stretch columns to fill width
        tv.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }


    private void showResult(String fxml, String displayedQuery, LoginService.Result r) throws Exception {
        Parent root = load(fxml);

        // Set the '#command' label if present
        Node cmdNode = root.lookup("#command");
        if (cmdNode instanceof Label lbl) {
            lbl.setText(displayedQuery);
        }

        // Wire the '#return' button to go back to main
        Node ret = root.lookup("#return");
        if (ret instanceof Button b) {
            b.setOnAction(e -> {
                try { showTest(); } catch (Exception ex) { ex.printStackTrace(); }
            });
        }
        
        Node dataNode = root.lookup("#data");
        if (dataNode instanceof Label dlbl) {
            dlbl.setText(r.data == null ? "(no rows)" : r.data);
            dlbl.setWrapText(true);
            dlbl.setMaxWidth(Double.MAX_VALUE);
            if (root instanceof Region rgn) dlbl.prefWidthProperty().bind(rgn.widthProperty().subtract(16));
            dlbl.setStyle("-fx-font-family: Consolas, 'Courier New', monospace;");
        }

        ensureScene(root);;
    }

    private void bindTestHandlers(Parent root) {
        TextArea username = (TextArea) root.lookup("#username");
        TextArea passwd = (TextArea) root.lookup("#passwd");
        Button btnInsecure = (Button) root.lookup("#insecure_login");
        Button btnSecure   = (Button) root.lookup("#secure_login");

        btnInsecure.setOnAction(e -> {
            String u = val(username);
            String p = val(passwd);
            LoginService.Result r = service.loginInsecure(u, p);
            try {
                switch (r.screen) {
                    case ATTACK -> showResult(F_ATTACK, r.displayQuery, r);
                    case INSECURE_PASS -> showResult(F_INSECURE_PASS, r.displayQuery, r);
                    case INSECURE_FAIL -> showResult(F_INSECURE_FAIL, r.displayQuery, r);
                    default -> showResult(F_INSECURE_FAIL, r.displayQuery, r);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        btnSecure.setOnAction(e -> {
            String u = val(username);
            String p = val(passwd);
            LoginService.Result r = service.loginSecure(u, p);
            try {
            	switch (r.screen) {
            	  case SECURE_PASS -> showResult(F_SECURE_PASS, r.displayQuery, r);
            	  case SECURE_FAIL -> showResult(F_SECURE_FAIL, r.displayQuery, r);
                    default -> showResult(F_SECURE_FAIL, r.displayQuery, r);
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    private String val(TextInputControl c) { return c == null ? "" : c.getText(); }

    private Parent load(String name) throws Exception {
        // Load FXML from the same package; place files next to these classes
        return FXMLLoader.load(getClass().getResource(name));
    }
    
 // Simple row model with JavaFX properties so TableView can read them
    public static class UserRow {
        private final javafx.beans.property.SimpleStringProperty username;
        private final javafx.beans.property.SimpleStringProperty passwd;

        public UserRow(String u, String p) {
            this.username = new javafx.beans.property.SimpleStringProperty(u);
            this.passwd   = new javafx.beans.property.SimpleStringProperty(p);
        }
        public String getUsername() { return username.get(); }
        public String getPasswd()   { return passwd.get(); }
    }

    private void ensureScene(Parent root) {
        if (STAGE.getScene() == null) {
            Scene scene = new Scene(root);     // no width/height → uses FXML prefs
            STAGE.setScene(scene);
            STAGE.sizeToScene();               // fit window to content
            STAGE.setMinWidth(760);            // pick a comfy minimum (adjust)
            STAGE.setMinHeight(520);
        } else {
            STAGE.getScene().setRoot(root);    // keep current window size
            STAGE.sizeToScene();               // or omit this to keep last size
        }
    }

    public static void main(String[] args) {
        // Optional: quick info in console
        System.out.println("DB: " + Db.info());
        launch(args);
    }
}
