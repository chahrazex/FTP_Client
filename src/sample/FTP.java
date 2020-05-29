/**
 * Chahrazed Benrebbouh Master 1 RSD
 * Module : Réseau 2   TP : FTP Client
 */
package sample;

import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconName;
import javafx.application.Platform;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import javax.swing.*;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class FTP  implements Initializable
{
    /**
     * Snackbar c'est une msg appareu a l'interface de l'utlisateur
     * pour donner des information / erreur .....etc
     */
    private JFXSnackbar snackbar ;

    /**
     * Le Socket avec le quel on va connecter avec le serveur
     */
    private Socket connectionSocket ;

    /**
     * Le socket avec le quel en evoie et recevoir les données
     */
    private Socket dataSocket ;

    /**
     * Flux général de sortie
     */
    private BufferedWriter outputStream = null;

    /**
     * Flux général d'entrée
     */
    private BufferedInputStream inputStream = null;

    /**
     * L'@ ip pour le quel en va envoiyer et recevoir les données
     */
    private  String dataIP;
    /**
     * Le pour le quel en va envoiyer et recevoir les données
     */
    private  int dataPort;
    /**
     * Flux  de sortie pour les donnéer
     */
    private BufferedWriter outputStreamData = null;

    /**
     * Flux  d'entrée pour les données
     */
    private BufferedInputStream inputStreamData = null;


    @FXML
    private TreeView<String> treeview;
    @FXML
    public StackPane rootPane;


    @FXML
    private Label path;

    @FXML
    public AnchorPane rootAnchorPane;

    @FXML
    public Label status2;

    @FXML
    private Label status1;

    @FXML
    public AnchorPane PrinciplePane;

    @FXML
    private JFXListView listView;

    @FXML
    public JFXTextField showUsername;

    @FXML
    public JFXTextField showHost;

    @FXML
    public JFXTextField showPort;

    @FXML
    public AnchorPane PaneDir;

    private TextInputDialog textInputDialog ;
    private TextField filePath;
    private File file ;
    PrintWriter printWriter ;

    ObservableList<String > list = FXCollections.observableArrayList() ;

    @FXML
    void actualiser() throws IOException, InterruptedException {
        String list = showListFileDir();
        Thread.sleep(500);
        setItems(list);
        Thread.sleep(100);
        getCurantDir(list);

    }


    @FXML
    void disconnect(ActionEvent event) throws IOException
    {
        executeCommand("QUIT");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/Connexion.fxml"));
        Pane parent = loader.load() ;

        //En passe les info d'utlisateur vers le main interface
        rootPane.getChildren().clear();
        rootPane.getChildren().setAll(parent);
    }


    @FXML
    void help()
    {
        BoxBlur boxBlur = new BoxBlur(3,3,3) ;
        JFXButton Okey = new JFXButton("Okey") ;
        JFXDialogLayout layout = new JFXDialogLayout() ;
        ImageView imageView = new ImageView(new Image("Ressources/logo.png",50,50,true,false)) ;


        Okey.getStyleClass().add("dialogButtonYes") ;
        layout.setActions(Okey);
        layout.setBody(imageView);
        layout.setHeading(new Text("FTP Client \n  copyright (C) 2019-2020 \n Written by : Chahrazed Benrebbouh "));
        JFXDialog dialog = new JFXDialog(rootPane,layout,JFXDialog.DialogTransition.TOP) ;
        dialog.show();
        rootAnchorPane.setEffect(boxBlur);
        Okey.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseevnt) ->
        {
            rootAnchorPane.setEffect(null);
            dialog.close();
        });

    }


    @FXML
    void newFile(ActionEvent event) throws IOException, InterruptedException {
        textInputDialog = new TextInputDialog() ;
        textInputDialog.setTitle("New File");
        textInputDialog.setHeaderText(null);
        TextField input = textInputDialog.getEditor() ;
        input.setPromptText("Choose File Name ");

        Optional<String> result = textInputDialog.showAndWait() ;
        if (result.isPresent())
        {
            STOR(input.getText());
        }
        actualiser();
    }

    /**
     * Méthode pour créer une nouveau fichier
     * @param fileName
     * @throws IOException
     */
    public  void STOR(String fileName) throws IOException
    {
        openPasv();
        createDataSocket();
        executeCommand("STOR "+fileName+".txt");
        if (getServerReply().startsWith("150"))
        {
            showToast("File successfully created");
        }
        dataSocket.close();
        inputStreamData.close();
        outputStreamData.close();
        getServerReply() ;
    }

    @FXML
    void newFolder(ActionEvent event) throws IOException, InterruptedException {
        textInputDialog = new TextInputDialog() ;
        textInputDialog.setTitle("New Folder");
        textInputDialog.setHeaderText(null);
        TextField input = textInputDialog.getEditor() ;
        input.setPromptText("Choose Folder Name");

        Optional<String> result = textInputDialog.showAndWait() ;
        if (result.isPresent())
        {
            MKD(input.getText());
        }
        actualiser();

    }

    @FXML
    void quit(ActionEvent event) throws IOException
    {
        executeCommand("QUIT");
        Platform.exit();
    }

    @FXML
    void uploadFile(ActionEvent event) throws IOException, InterruptedException {
        showDialog();
        Optional<String> result = textInputDialog.showAndWait() ;
        if (result.isPresent())
        {
            storeData(file);
        }
        actualiser();
    }




    public void  showDialog()
    {
        textInputDialog = new TextInputDialog() ;
        textInputDialog.setTitle("Choose File");
        Pane pane = new Pane() ;
        pane.setPrefSize(350,130);

        filePath = new TextField() ;
        filePath.setPrefSize(250,20);
        filePath.setPromptText("Path File :");
        filePath.setLayoutX(20);
        filePath.setLayoutY(50);


        JFXButton Browse = new JFXButton("Browse") ;
        Browse.setStyle(" -fx-background-color :#ffffff");
        Browse.setLayoutX(280);
        Browse.setLayoutY(50);

        Browse.setButtonType(JFXButton.ButtonType.RAISED);

        pane.getChildren().addAll(filePath,Browse) ;
        textInputDialog.setHeaderText(null);
        textInputDialog.getDialogPane().setContent(pane);



        Browse.setOnMouseClicked(event ->
        {
            FileChooser fileChooser = new FileChooser() ;
            file = fileChooser.showOpenDialog(rootAnchorPane.getScene().getWindow()) ;
            filePath.setText(file.getAbsolutePath());

        });

    }

    public void storeData(File file) throws IOException
    {
        executeCommand("TYPE ASCII");
        getServerReply() ;
        openPasv();
        createDataSocket();
        OutputStream outputStream = dataSocket.getOutputStream();


        executeCommand("STOR "+file.getName());
        getServerReply() ;

        // Get the size of the file
        long length = file.length();
        byte[] bytes = new byte[16 * 1024];

        InputStream in = new FileInputStream(file);


        int count;
        while ((count = in.read(bytes)) > 0)
        {
            outputStream.write(bytes, 0, count);
        }


        dataSocket.close();
        outputStream.close();
        if (getServerReply().startsWith("226"))
        {

            showToast("file successfully uploaded ");
        }
    }

    /**
     * Méthode pour créer un nouveau Dossier en execute la command : MKD (MAKE DIRECTORY)
     * @param folderName
     * @throws IOException
     */
    public void MKD(String folderName) throws IOException
    {
        executeCommand("MKD "+folderName);
        if (getServerReply().startsWith("257") )
        {
            showToast("Folder scceussfuly created ");
        }
    }

    public void rename(String oldname) throws IOException, InterruptedException {
        textInputDialog = new TextInputDialog() ;
        textInputDialog.setTitle("Edit name File / Folder");
        textInputDialog.setHeaderText(null);
        TextField input = textInputDialog.getEditor() ;
        input.setText(oldname);

        Optional<String> result = textInputDialog.showAndWait() ;
        if (result.isPresent())
        {
            executeCommand("RNFR "+oldname);
            status2.setText(getServerReply()) ;

            if (oldname.contains("."))
            {
                int indexDebut  = oldname.lastIndexOf(".");
                String getExtension = oldname.substring(indexDebut);
                executeCommand("RNTO "+input.getText()+getExtension);

            }
            else
            {
                executeCommand("RNTO "+input.getText());
            }
           String reply =  getServerReply() ;
            status2.setText(reply);
            if (reply.startsWith("250"))
            {
                showToast("File/Folder Renamed Seccussfuly ..");
                actualiser();
            }
        }



    }

    public  void  removeFile(String filename) throws IOException, InterruptedException {
        executeCommand("DELE "+filename);
        String reply = getServerReply() ;
        if (reply.startsWith("250"))
        {
            showToast("File Sccucfuly deleted");
            actualiser();
        }
    }
    public  void  removeFolder(String folderName) throws IOException, InterruptedException {
        executeCommand("RMD "+folderName);
        String reply = getServerReply() ;
       if (reply.startsWith("550") )
        {
            snackbar.getStyleClass().clear();
            snackbar.getStyleClass().add("jfx-snackbar-content2") ;
            showToast("Folder not empty");
        }
       actualiser();

    }

    public void  parentDirectory() throws IOException, InterruptedException {
        executeCommand("CDUP");

        String dir = getServerReply() ;
        if (dir.startsWith("200"))
        {
            System.out.println("Back ");

        }

        actualiser();

    }

    /**
     * Methode pour affichier la liste des fichier et dossier
     * exist dans le répertoir courant
     */
    public  String  showListFileDir() throws IOException, InterruptedException {
        //Il est a noté que le listing des fichiers et dossier nécéssite,
        //comme le transfer de fichiers, d'une ouverture de port, coté client
        //ou coté serveur en fonction du mode de connection (passif ou actif).

        //On passe en mode passif

        listView.getItems().clear();
        executeCommand("TYPE ASCII");
        getServerReply() ;
        openPasv();
        createDataSocket();
        executeCommand("NLST");
        String result = getDataReplyCommand() ;
        dataSocket.close();
        inputStreamData.close();
        outputStreamData.close();
        Thread.sleep(100);
        getServerReply() ;

        return  result ;
    }
    public  void setItems( String result ) throws IOException {

        StringTokenizer files = new StringTokenizer(result ,"\n") ;
        while (files.hasMoreTokens())
        {
            String nameFile = files.nextToken() ;
            setListItems(nameFile);
        }
    }

    public void getCurantDir(String listFile) throws IOException
    {
        executeCommand("PWD");
        String dir = getServerReply() ;
        int debut = dir.indexOf((char)34);
        int fin = dir.indexOf((char)34,debut+1);
        treeOfFolder( dir.substring(debut+1,fin),listFile);

    }

    public  void  selectItem(MouseEvent event) throws IOException, InterruptedException
    {

        if (treeview.getSelectionModel().getSelectedItem() !=null)
        {
            TreeItem<String> item = treeview.getSelectionModel().getSelectedItem() ;
            System.out.println(item.getValue());
            executeCommand("CWD "+item.getValue());
            getServerReply() ;
            actualiser();
        }

    }
    public void treeOfFolder(String dir, String listFile) {
        Text icon = GlyphsDude.createIcon(FontAwesomeIconName.FOLDER_OPEN,"1em");
        icon.setFill(Color.valueOf("#ffb200"));
        path.setText(dir);
        String lastToken = dir ;
        if (dir.length()!=1)
        {

            StringTokenizer buffer = new StringTokenizer(dir ,"/") ;
            while (buffer.hasMoreTokens())
            {
                lastToken = buffer.nextToken() ;
            }
        }

        TreeItem root = new TreeItem(lastToken);
        root.setGraphic(icon);

        StringTokenizer files = new StringTokenizer(listFile ,"\n") ;
        while (files.hasMoreTokens())
        {
            String nameFile = files.nextToken() ;
            if (!nameFile.contains("."))
            {
                Text label = GlyphsDude.createIcon(FontAwesomeIconName.FOLDER,"1em");
                label.setFill(Color.valueOf("#ffb200"));
                TreeItem treeItem = new TreeItem(nameFile.trim()) ;
                treeItem.setGraphic(label);
                root.getChildren().add(treeItem) ;
            }
        }
        root.setExpanded(true);
        treeview.getStyleClass().add("treeView") ;
        treeview.setRoot(root);
        VBox vbox = new VBox(treeview);

        vbox.setPrefSize(156,385);
        PaneDir.getChildren().add(vbox) ;

    }

    public  String   getFilleSize(String fileName) throws IOException
    {
        executeCommand("SIZE "+fileName);
        String size = getServerReply() ;

       return   size.substring(4);
    }

    public void  setListItems(String item ) throws IOException
    {
        list.add(item) ;
        listView.setItems(list);

        listView.setCellFactory(param -> new Cell(){

            });
    }
    public void retreiveFile(String file, String path ) throws IOException {

        executeCommand("TYPE ASCII");
        getServerReply() ;
        openPasv();
        createDataSocket();
        InputStream inputStream = dataSocket.getInputStream();

        System.out.println(file);

        executeCommand("RETR "+file);
        getServerReply() ;

        // Get the size of the file

        byte[] bytes = new byte[16 * 1024];

        File f = new File( path+(char)47+file.trim()) ;
        OutputStream out = new FileOutputStream(f);

        int count;
        while ((count = inputStream.read(bytes)) > 0)
        {
            out.write(bytes, 0, count);
        }


        dataSocket.close();
        inputStream.close();
        if (getServerReply().startsWith("226"))
        {

            showToast("file successfully uploaded ");
        }
    }
    public void download(String filename) throws IOException
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showSaveDialog(null);

        String path = String.valueOf(fileChooser.getSelectedFile());
        retreiveFile(filename,path);
    }

    /**
     * pour passer au mode passive en doit envoie le command "PASV"
     * et le serveur va lui réponder par le numéro de port et l'@ ip
     * pour le quel en échanger des donnéer
     */
    public void openPasv() throws IOException
    {
        executeCommand("PASV");

        //On lit la réponse de serveur
        String reponse = getServerReply() ;
        status2.setText(reponse);
        splitPortAndIp(reponse);
    }

    /**
     * Le port est un entier de type int mais cet entier est découpé en deux
     * la première partie correspond aux 4 premiers bits de l'octet la deuxième
     * au 4 derniers Il faut donc multiplier le premier nombre par 256
     * et l'additionner au second pour avoir le numéro de ports défini par le serveur
     */
    public  void  splitPortAndIp(String reponse) throws IOException {
        String ip = null;
        int port = -1;
        //On décortique ici la réponse retournée par le serveur pour récupérer
        //l'adresse IP et le port à utiliser pour le canal data
        int debut = reponse.indexOf('(');
        int fin = reponse.indexOf(')', debut + 1);
        if (debut > 0)
        {
            String dataLink = reponse.substring(debut + 1, fin);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try
            {
                //L'adresse IP est séparée par des virgules
                //on les remplace donc par des points...
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();

                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
                dataIP = ip;
                dataPort = port;
            }
            catch (Exception e)
            {
                throw new IOException("SimpleFTP received bad data link information: " + reponse);
            }
        }
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
        inputStream = new BufferedInputStream(connectionSocket.getInputStream() );
        stream = inputStream.read(b);
        response = new String(b, 0, stream);
        MessageInvitCommand(response);
        return response;
    }


    /**
     * Execute une commande simple sur le FTP et retourne juste
     * le code réponse du message de retour.
     */
    public void executeCommand(String command)throws IOException
    {
        command += System.getProperty("line.separator");

        MessageInvitCommand(command);

        outputStream = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream())) ;
        outputStream.write(command);
        outputStream.flush();
    }

    /**
     * Lir la répose de serveur qui contien des donner
     * @return
     * @throws IOException
     */
    public  String  getDataReplyCommand() throws IOException {
        String response = "";
        byte[] b = new byte[1024];
        int stream;

        while((stream = inputStreamData.read(b)) != -1)
        {
            response  = new String(b, 0, stream);
        }

        MessageInvitCommand(response);
        return response;
    }

    /**
     * Créer un socket pour échanger les donéer
     */
    public void createDataSocket() throws IOException
    {
        System.out.println("@IP "+dataIP+" Port "+dataPort);
        dataSocket = new Socket(dataIP,dataPort) ;
        outputStreamData = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream()));
        inputStreamData = new BufferedInputStream(dataSocket.getInputStream());
        printWriter = new PrintWriter( dataSocket.getOutputStream(),true) ;
    }



    /**
     * Pour affichier des information supplimentair dans l'invite de commade
     * @param msg
     */
    public  void MessageInvitCommand(String msg)
    {
        System.out.println("> "+msg);
    }

    /**
     * Methode pour recevoir le socket de connection depuis l'interface
     * de connexion
     * @param connectionSocket
     * @param username
     */
    public  void getInfoConnexion(Socket connectionSocket , String username) throws IOException, InterruptedException {

        this.connectionSocket = connectionSocket ;
        outputStream = new BufferedWriter(new OutputStreamWriter(connectionSocket.getOutputStream()));
        inputStream = new BufferedInputStream(connectionSocket.getInputStream());


        showUsername.setText("Username : "+username);
        showHost.setText("Host name : "+connectionSocket.getInetAddress());
        showPort.setText("Port : "+connectionSocket.getPort());
        showToast("Successfully Sign in.....");
        status1.setTextFill(Color.GREEN);
        status1.setText("Connected");
        actualiser();

    }
    /**
     * Retourne la date de dernière modification d'un fichier
     */
    public String getModificationTime(String fileName)throws IOException
    {
        executeCommand("MDTM " + fileName);
        String reponse = getServerReply() ;

        String annee = reponse.substring(4,8) ;
        String moin = reponse.substring(8,10) ;
        String jour = reponse.substring(10,12) ;
        System.out.println(annee+"/"+moin+"/"+jour);

        String h = reponse.substring(12,14) ;
        String m = reponse.substring(14,16);
        String s = reponse.substring(16,18);
        System.out.println("Time ="+h+":"+m+":"+s);

        String Time = annee+"/"+moin+"/"+jour+" "+h+":"+m+":"+s ;

        return Time ;

    }

    /**
     * Methode pour afficher des msg comme notification a l'utilsateur
     * @param text: le contenu de message a afficher
     */
    public void showToast(String text)
    {
        Text icon = GlyphsDude.createIcon(FontAwesomeIconName.CHECK_CIRCLE,"1.5em");
        icon.setFill(Color.WHITE);
        Label l = new Label(text) ;
        l.setTextFill(Color.WHITE);
        l.setGraphic(icon);
        snackbar = new JFXSnackbar(PrinciplePane);
        snackbar.enqueue( new JFXSnackbar.SnackbarEvent(l));
    }
    class Cell extends ListCell<String>
    {
        HBox hBox = new HBox() ;

        Text download = GlyphsDude.createIcon(FontAwesomeIconName.CLOUD_DOWNLOAD,"1.5em") ;
        Text rename = GlyphsDude.createIcon(FontAwesomeIconName.EDIT,"1.5em") ;
        Text delet = GlyphsDude.createIcon(FontAwesomeIconName.REMOVE,"1.5em") ;
        Text folder =  GlyphsDude.createIcon(FontAwesomeIconName.FOLDER,"1.5em") ;
        Text file =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_TEXT,"1.5em") ;
        Text image =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_IMAGE_ALT,"1.5em") ;
        Text pdf =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_PDF_ALT,"1.5em") ;
        Text rar =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_ARCHIVE_ALT,"1.5em") ;
        Text mp4 =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_SOUND_ALT,"1.5em") ;
        Text inconu =  GlyphsDude.createIcon(FontAwesomeIconName.FILE_ALT,"1.5em") ;

        JFXButton btn = new JFXButton("") ;

        JFXButton btn2 = new JFXButton("") ;
        JFXButton btn3 = new JFXButton("") ;

        Pane pane = new Pane() ;
        Label label = new Label("") ;
        Label label2 = new Label("") ;
        Label label3 = new Label("") ;
        Label label4 = new Label("") ;
        Label label5 = new Label("") ;
        Label label6 = new Label("") ;
        Label label7 = new Label("") ;



        public Cell()
        {
            super();
            download.setFill(Color.valueOf("#00a0ff"));
            rename.setFill(Color.valueOf("#00a0ff"));
            delet.setFill(Color.valueOf("#00a0ff"));
            folder.setFill(Color.valueOf("#ffb200"));
            file.setFill(Color.valueOf("#afcada"));
            image.setFill(Color.valueOf("#afcada"));
            pdf.setFill(Color.RED);
            rar.setFill(Color.valueOf("#afcada"));
            mp4.setFill(Color.valueOf("#afcada"));
            inconu.setFill(Color.valueOf("#afcada"));



            btn.setGraphic(download);
            btn2.setGraphic(rename);
            btn3.setGraphic(delet);
            hBox.setSpacing(20);
            hBox.setAlignment(Pos.CENTER_LEFT);

            label2.setTextFill(Color.valueOf("#afcada"));
            label3.setTextFill(Color.valueOf("#afcada"));
            label4.setTextFill(Color.valueOf("#afcada"));
            label5.setTextFill(Color.valueOf("#afcada"));
            label6.setTextFill(Color.valueOf("#afcada"));
            label7.setTextFill(Color.valueOf("#afcada"));

            hBox.setHgrow(pane, Priority.ALWAYS);
            btn.setOnAction(event ->
            {
                System.out.println("Download "+getItem());
                try {
                    download(getItem());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            btn2.setOnAction(event ->
            {
                System.out.println("rename "+getItem());
                try {
                    String item = getItem() ;
                    rename(item);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }

            });
            btn3.setOnAction(event ->
            {
                BoxBlur boxBlur = new BoxBlur(3,3,3) ;
                JFXButton no = new JFXButton("No") ;
                JFXButton yes = new JFXButton("Yes") ;
                JFXDialogLayout layout = new JFXDialogLayout() ;
                no.getStyleClass().add("dialogButtonNo") ;
                yes.getStyleClass().add("dialogButtonYes") ;
                layout.setActions(no,yes);
                layout.setHeading(new Text("Are you sure you want to delete this Item ?"));
                JFXDialog dialog = new JFXDialog(rootPane,layout,JFXDialog.DialogTransition.TOP) ;
                dialog.show();
                rootAnchorPane.setEffect(boxBlur);

                no.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseevnt) ->
                {
                    rootAnchorPane.setEffect(null);
                    dialog.close();
                });
                yes.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseevnt) ->
                {
                    try
                    {   String item = getItem() ;
                        if (item.contains("."))
                        {
                            removeFile(getItem());
                        }
                        else
                        {
                            removeFolder(getItem());
                        }

                       // getListView().getItems().remove(getItem());
                        rootAnchorPane.setEffect(null);
                        dialog.close();
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }

                });

            });
        }
        public void updateItem(String name ,boolean empty)
        {
            super.updateItem(name,empty);
            setText(null);
            setGraphic(null);

            if (name !=null && !empty)
            {
                hBox.getChildren().clear();
                label.setText(name.trim());
                if (!name.contains("."))//folder
                {
                    hBox.getChildren().clear();
                    hBox.getChildren().addAll(folder ,label,pane,btn2,btn3);
                }

                else {
                    if (name.trim().equals(".") || name.trim().equals("..")) {
                    } else {
                        int indexDebut = name.lastIndexOf(".") + 1;
                        String extension = name.substring(indexDebut);
                        String size;
                        String Time = null;
                        Double Size = null;
                        try {
                            Time = getModificationTime(name);

                            size = getFilleSize(name);
                            Size = Double.parseDouble(size.trim());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(extension);
                        if (name.contains(".txt") || name.contains(".pdf")
                                || name.contains(".png") || name.contains(".rar")
                                || name.contains(".mp4") || name.contains(".jpg") || name.contains(".zip")) {
                            if (name.contains(".txt")) {
                                label2.setText(Size / 1000 + " Ko" + "  " + Time);
                                hBox.getChildren().addAll(file, label, label2, pane, btn, btn2, btn3);
                            }
                            if (name.contains(".png") || name.contains(".jpg")) {
                                label3.setText(Size / 1000 + " Ko" + "  " + Time);
                                hBox.getChildren().addAll(image, label, label3, pane, btn, btn2, btn3);
                            }
                            if (name.contains(".pdf")) {
                                label4.setText(Size / 1000 + " Ko" + "  " + Time);
                                hBox.getChildren().addAll(pdf, label, label4, pane, btn, btn2, btn3);
                            }
                            if (name.contains(".rar") || name.contains(".zip")) {
                                label5.setText(Size / 1000 + " Ko" + "  " + Time);
                                hBox.getChildren().addAll(rar, label, label5, pane, btn, btn2, btn3);
                            }
                            if (name.contains(".mp4") || name.contains("mp3")) {
                                label6.setText(Size / 1000 + " Ko" + "  " + Time);
                                hBox.getChildren().addAll(mp4, label, label6, pane, btn, btn2, btn3);
                            }
                        } else {
                            label7.setText(Size / 1000 + " Ko" + "  " + Time);
                            
                            hBox.getChildren().addAll(inconu, label, label7, pane, btn, btn2, btn3);
                        }


                    }
                }
                setGraphic(hBox);
            }
        }
    }
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
    }
}
