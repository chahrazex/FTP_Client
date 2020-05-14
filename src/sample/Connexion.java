package sample;

import com.jfoenix.controls.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Connexion extends Application implements Initializable
{
    @FXML
    StackPane rootStackPane ;

    @FXML
    ImageView imageFTP;

    @FXML
    JFXTextField usernameTextField;

    @FXML
    JFXPasswordField passwordTextField;

    @FXML
    AnchorPane container;

    JFXSnackbar snackbar ;
    TextInputDialog textInputDialog ;
    TextField hostTextField,portTextField;
    Text toast ;

    /**
     * Le socket avec lequel l'on se connecte
     */
    private Socket connectionSocket = null;
    /**
     * Flux général de sortie
     */
    private BufferedWriter outputStream = null;

    /**
     * Flux général d'entrée
     */
    private BufferedInputStream inputStream = null;


    /**
     * Se connect au FTP donné sur le port donné
     */
    public void connect(String host, int port) throws IOException, InterruptedException
    {
        connectionSocket = new Socket(host, port);
        outputStream = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
        inputStream = new BufferedInputStream(connectionSocket.getInputStream());
        String reponse = getServerReply() ;

        /**********************************************************************************/
        //Si la réponse commance par "220"  il'y une erreur de connexion avec le serveur
        if (!reponse.startsWith("220"))
        {
            //en affiche une sample toast a l'utilisateur avec le message d'erre
            toast = new Text("Erreur de connexion au FTP : \n" + reponse);
            showToast(toast);
            connectionSocket = null ;
            return;
        }
        //Si la connexion avec le serveur est établir don en envoi le nom d'utilsateur au serveur
        login(usernameTextField.getText(),passwordTextField.getText());

    }

    /**
     * On se log sur le ftp avec un login et un password.
     */
    public void login(String username, String password) throws IOException, InterruptedException {
        executeCommand("USER "+username) ;
        String reponse = getServerReply() ;

        /***************************************************************************************/
        //Si la réponse commance par "331"  il'y erreur de connexion avec le compte utilisateur
        if (!reponse.startsWith("331"))
        {
            //en affiche une sample toast a l'utilisateur avec le message d'erreur
            toast = new Text("Erreur de connexion avec le compte utilisateur : \n" + reponse);
            showToast(toast);
            connectionSocket = null ;
            return;
        }


        //Si le nom d'utilsateur est correct en envoi le password
        String passwd =password;
        executeCommand("PASS " + passwd);

        //On lit la réponse de serveur
        reponse = getServerReply();

        /***************************************************************************************/
        //Si la réponse commence par "230" donc le password est incorect
        if (!reponse.startsWith("230"))
        {
            //en affiche une sample toast a l'utilisateur avec le message d'erreur
            toast = new Text("Erreur de connexion avec le compte utilisateur : \n" + reponse);
            showToast(toast);

            connectionSocket = null ;
            return;
        }
        //Si tout va passer bien en va lancer le deuxieme interface
        loadMainInterface() ;
    }

    /**
     * Execute une commande simple sur le FTP et retourne juste
     * le code réponse du message de retour.
     */
    public void executeCommand(String command)throws IOException
    {
        command += System.getProperty("line.separator");

        MessageInvitCommand(command);

        outputStream.write(command);
        outputStream.flush();
    }

    /**
     * Récupère le code de réponse du server FTP. En effet les serveurs FTP répondent
     * par des phrases de ce type "xxx message". L'on récupère donc ce code à
     * 3 chiffres pour identifier la nature de la réponse (erreur, envoi, etc...).
     */
    private String getServerReply() throws IOException
    {
        String response = "";
        int stream;
        byte[] b = new byte[1024];
        stream = inputStream.read(b);
        response = new String(b, 0, stream);
        MessageInvitCommand(response);
        return response;
    }


    public  void MessageInvitCommand(String msg)
    {
        System.out.println("> "+msg);
    }




    public  void Setting()
    {
        textInputDialog.showAndWait() ;
    }

    public  void SignIn() throws IOException, InterruptedException
    {

        ValidationSupport validationSupport = new ValidationSupport();
        validationSupport.registerValidator(usernameTextField, Validator.createEmptyValidator("Username is Required")) ;
        validationSupport.registerValidator(passwordTextField, Validator.createEmptyValidator("password is Required")) ;

        if (!usernameTextField.getText().isEmpty()&
                !passwordTextField.getText().isEmpty() &
                !hostTextField.getText().isEmpty() &
                !portTextField.getText().isEmpty())
        {
            connect(hostTextField.getText(),Integer.parseInt(portTextField.getText())) ;
        }

    }
    public  void showToast(Text text)
    {
        snackbar = new JFXSnackbar() ;
        snackbar = new JFXSnackbar(container) ;
        text.setStyle("-fx-fill: white ; -fx-font-size: 12");
        snackbar.enqueue( new JFXSnackbar.SnackbarEvent(text));

    }

    public void  loadMainInterface () throws IOException, InterruptedException {
        String username = usernameTextField.getText() ;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/MainInterface.fxml"));
        Pane parent = loader.load() ;
        FTP ftp = loader.getController() ;

        //En passe les info d'utlisateur vers le main interface
        ftp.getInfoConnexion(connectionSocket ,username);
        rootStackPane.getChildren().setAll(parent);
    }
    @FXML
    void dribbbel(ActionEvent event)
    {
        getHostServices().showDocument("https://dribbble.com/chahrazex");
    }

    @FXML
    void facebook(ActionEvent event)
    {
        getHostServices().showDocument("https://www.facebook.com/Chahrazed.Benrebbouh");
    }

    @FXML
    void github(ActionEvent event)
    {
        getHostServices().showDocument("https://github.com/chahrazex");
    }

    @FXML
    void instagram(ActionEvent event)
    {
        getHostServices().showDocument("https://www.instagram.com/chahrazed_benrebbouh");
    }

    @FXML
    void youtube(ActionEvent event)
    {
         getHostServices().showDocument("https://www.youtube.com/channel/UCj4FKnu9aRJ65XVtI4k_TFw");
    }


    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        textInputDialog = new TextInputDialog() ;
        textInputDialog.setTitle("Setting");

        Pane pane = new Pane() ;
        pane.setPrefSize(350,130);

        hostTextField = new TextField() ;
        hostTextField.setPrefSize(250,20);
        hostTextField.setPromptText("host");
        hostTextField.setLayoutX(65);
        hostTextField.setLayoutY(50);
        hostTextField.setText("localhost");
        Label l = new Label("Host :") ;
        l.setLayoutX(20);
        l.setLayoutY(54);

        portTextField = new TextField() ;
        portTextField.setPromptText("Port");
        portTextField.setLayoutX(65);
        portTextField.setLayoutY(100);
        Label l1 = new Label("Port :") ;
        l1.setLayoutX(20);
        l1.setLayoutY(104);
        portTextField.setText("9090");
        portTextField.setPrefSize(250,20);

        pane.getChildren().addAll(hostTextField,portTextField,l1,l) ;
        textInputDialog.setHeaderText(null);
        textInputDialog.getDialogPane().setContent(pane);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}
