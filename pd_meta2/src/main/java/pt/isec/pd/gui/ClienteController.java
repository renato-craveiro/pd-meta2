package pt.isec.pd.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.isec.pd.client.client;
import pt.isec.pd.server.request;
import pt.isec.pd.types.user;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ClienteController {

    @FXML
    private Label nameL, nestudentL, emailL, passL, fileNameL;
    @FXML
    private TextField  nameTF, nestudentTF, emailTF, passTF, submitCodeTF;
    @FXML
    private Button signBTN, loginBTN, listBTN, exportBTN, editBTN, codeBTN, submitSignBTN, submitLoginBTN, submitCodeBTN, submitExportBTN, submitEditBTN;
    @FXML
    private TableView<Event> tableTV;

    public String srvAddress;
    public int srvPort;
    public String token;

    public void setClienteSocket(String srvAddress, int srvPort) {
        this.srvAddress = srvAddress;
        this.srvPort = srvPort;
    }

    public void popUpMensage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void divideResponse(List<Event> events, String response){

        String[] entradas = response.split("\n");

        for (int i = 1; i<entradas.length; i++){

            String[] re = entradas[i].split(";");
            Event e = new Event(re[0],re[1],re[2],re[3]);
            events.add(e);

        }

    }

    public static String sendRequestAndShowResponse(String uri, String verb, String authorizationValue, String body) throws MalformedURLException, IOException {

        String responseBody = null;
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(verb);
        connection.setRequestProperty("Accept", "application/xml, */*");

        if(authorizationValue!=null) {
            connection.setRequestProperty("Authorization", authorizationValue);
        }

        if(body!=null){
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "Application/Json");
            connection.getOutputStream().write(body.getBytes());
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " +  responseCode + " (" + connection.getResponseMessage() + ")");

        Scanner s;

        if(connection.getErrorStream()!=null) {
            s = new Scanner(connection.getErrorStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        }

        try {
            s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        } catch (IOException e){}

        connection.disconnect();

        System.out.println(verb + " " + uri + (body==null?"":" with body: "+body) + " ==> " + responseBody);
        System.out.println();

        return responseBody;
    }



    @FXML
    protected void onListButton() {

        tableTV.setVisible(true);
        tableTV.getItems().clear();

        String response = client.sendRequest("LIST", "1");
        List<Event> events = new ArrayList<>();

        divideResponse(events, response);

        for(Event event : events){
            tableTV.getItems().add(event);
        }


        listBTN.setVisible(true);
        exportBTN.setVisible(true);
        editBTN.setVisible(true);
        codeBTN.setVisible(true);

    }

    @FXML
    protected void onExportButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);

        submitExportBTN.setVisible(true);
        fileNameL.setVisible(true);
        nameL.setVisible(true);
        nameTF.setVisible(true);

    }

    @FXML
    protected void onExportSubmit() {

        // Check if the code is not empty
        if (nameTF.getText().isEmpty()) {
            // If the code is empty, show an error message
            popUpMensage("Error", "Preencha o campo");
        } else {

            client.exportCSV(nameTF.getText());

            popUpMensage("Success", "CSV escrito com sucesso");

            nameTF.clear();

            submitExportBTN.setVisible(false);
            fileNameL.setVisible(false);
            nameL.setVisible(false);
            nameTF.setVisible(false);

            listBTN.setVisible(true);
            exportBTN.setVisible(true);
            editBTN.setVisible(true);
            codeBTN.setVisible(true);
        }

    }

    @FXML
    protected void onEditButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);
        fileNameL.setVisible(false);

        nameTF.setVisible(true);
        nameL.setVisible(true);
        nestudentTF.setVisible(true);
        nestudentL.setVisible(true);
        emailTF.setVisible(true);
        passTF.setVisible(true);
        emailL.setVisible(true);
        passL.setVisible(true);
        submitEditBTN.setVisible(true);

    }

    @FXML
    protected void onEditSubmit() {

        // Check if any of the required fields is empty
        if (nameTF.getText().isEmpty() || nestudentTF.getText().isEmpty() || emailTF.getText().isEmpty() || passTF.getText().isEmpty()) {
            // If any field is empty, show an error message
            popUpMensage("Error", "Erro ao Editar: Preencha todos os campos");
        } else {

            client.auxUser = new user(nameTF.getText(), nestudentTF.getText(), emailTF.getText(), passTF.getText());

            // If all fields are filled, create a request to change user data
            request req = new request("CHANGE", client.currUser, client.auxUser);

            // Perform the user data change
            client.changeData(true);

            // Show other buttons
            listBTN.setVisible(true);
            exportBTN.setVisible(true);
            editBTN.setVisible(true);
            codeBTN.setVisible(true);

            // Hide the user information editing components
            nameTF.setVisible(false);
            nameL.setVisible(false);
            nestudentTF.setVisible(false);
            nestudentL.setVisible(false);
            emailTF.setVisible(false);
            passTF.setVisible(false);
            emailL.setVisible(false);
            passL.setVisible(false);
            submitEditBTN.setVisible(false);
        }

    }

    @FXML
    protected void onCodeButton() {

        listBTN.setVisible(false);
        exportBTN.setVisible(false);
        editBTN.setVisible(false);
        codeBTN.setVisible(false);

        tableTV.setVisible(false);
        fileNameL.setVisible(false);

        submitCodeTF.setVisible(true);
        submitCodeBTN.setVisible(true);

    }

    @FXML
    protected void onCodeSubmit() throws IOException {
        String code = submitCodeTF.getText();

        if (code.isEmpty())
            popUpMensage("Error", "Insira um código");
        else {

            String deleteStr = "http://localhost:8080/usercalls/registerPresence";

            String body = code;

            deleteStr = sendRequestAndShowResponse(deleteStr, "POST", "bearer " + token, body);

            if (deleteStr.contains("successfully")) {
                submitCodeTF.clear();
                popUpMensage("Success", "Presença registada com sucesso");
            } else if (deleteStr.contains("not found"))
                popUpMensage("Error", "Nenhum evento com esse id");
            else
                popUpMensage("Error", "Erro na comunicação com o server");
        }


    }

    @FXML
    protected void onExitButton() {System.exit(0);}












    public static String sendRequestAndShowResponse(String uri, String verb, String authorizationValue) throws MalformedURLException, IOException {

        String responseBody = null;
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(verb);
        connection.setRequestProperty("Accept", "application/xml, */*");

        if(authorizationValue!=null) {
            connection.setRequestProperty("Authorization", authorizationValue);
        }

        connection.connect();

        int responseCode = connection.getResponseCode();
        System.out.println("Response code: " +  responseCode + " (" + connection.getResponseMessage() + ")");

        Scanner s;

        if(connection.getErrorStream()!=null) {
            s = new Scanner(connection.getErrorStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        }

        try {
            s = new Scanner(connection.getInputStream()).useDelimiter("\\A");
            responseBody = s.hasNext() ? s.next() : null;
        } catch (IOException e){}

        connection.disconnect();

        System.out.println(verb + " " + uri + " -> " + responseBody);
        System.out.println();

        return responseBody;
    }




}
