package lk.ijse.hibernate.hostel.controller;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import lk.ijse.hibernate.hostel.bo.BOFactory;
import lk.ijse.hibernate.hostel.bo.custom.StudentBO;
import lk.ijse.hibernate.hostel.dto.StudentDTO;
import lk.ijse.hibernate.hostel.util.Navigation;
import lk.ijse.hibernate.hostel.util.SessionFactoryConfig;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

public class StudentFormController implements Initializable {
    public JFXTextField txtStId;
    public JFXTextField txtName;
    public JFXTextField txtAddress;
    public JFXTextField txtContact;
    public JFXComboBox cmbGender;
    public JFXDatePicker txtDate;
    public TableView tblStudent;
    public TableColumn colstId;
    public TableColumn colStName;
    public TableColumn colAddress;
    public TableColumn colContact;
    public TableColumn colDob;
    public TableColumn colGender;
    private JFXPanel root;


    private Session session;
    private StudentBO studentBO = (StudentBO) BOFactory. getBO (BOFactory.BOTypes.STUDENT);


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setGender ();
        setTableStudent ();
        loadAllStudent ();
        setStID ();
    }

    public void onActionDelete(ActionEvent actionEvent) {
        String dob = String.valueOf (txtDate.getValue ());
        String gender = cmbGender.getValue ().toString ();
        StudentDTO studentDTO = new StudentDTO (txtStId.getText (), txtName.getText (), txtAddress.getText (), txtContact.getText (), dob, gender);

        boolean isDeleted=studentBO.deleteStudent (studentDTO);

        if (isDeleted){
            new Alert(Alert.AlertType.INFORMATION, "Student Delete Succuss").show ();
            tblStudent.getItems ().clear ();
            clearData ();
            loadAllStudent ();
            setStID ();
        }else{
            new Alert (Alert.AlertType.ERROR, "Something went Wrong").show ();
        }

    }

    public void onActionUpdate(ActionEvent actionEvent) {
        String dob = String.valueOf (txtDate.getValue ());
        String gender = cmbGender.getValue ().toString ();
        StudentDTO studentDTO = new StudentDTO (txtStId.getText (), txtName.getText (), txtAddress.getText (), txtContact.getText (), dob, gender);

        boolean isUpdate=studentBO.updateStudent (studentDTO);

        if (isUpdate){
            new Alert (Alert.AlertType.INFORMATION, "Student Update Succuss").show ();
            tblStudent.getItems ().clear ();
            clearData ();
            loadAllStudent ();
            setStID ();
        }else {
            new Alert (Alert.AlertType.ERROR, "Something went Wrong").show ();
        }
    }

    public boolean checkDuplicate() {
        List<StudentDTO> allStudents = studentBO.loadAll ();
        for (StudentDTO s : allStudents) {
            if (s.getStId ().equals (txtStId.getText ())) {
                new Alert (Alert.AlertType.ERROR, "This ID Already Have").show ();
                return false;
            }
        }
        return true;
    }

    public void onActionSave(ActionEvent actionEvent) {
        String dob = String.valueOf (txtDate.getValue ());
        String gender = cmbGender.getValue ().toString ();
        StudentDTO studentDTO = new StudentDTO (txtStId.getText (), txtName.getText (), txtAddress.getText (), txtContact.getText (), dob, gender);

        if(checkDuplicate ()){
            boolean isCheckValidate=checkValidation ();
            if(isCheckValidate){
                studentBO.saveStudent (studentDTO);
                new Alert (Alert.AlertType.CONFIRMATION, "Student saved").show ();
                tblStudent.getItems ().clear ();
                clearData ();
                loadAllStudent ();
                setStID ();
            }

        }
    }

    public void setGender() {
        ObservableList<String> data = FXCollections.observableArrayList ("Male", "Female", "Other");
        cmbGender.setItems (data);
    }

    public void OnActionMouseClicked(MouseEvent mouseEvent) {
        int index = tblStudent.getSelectionModel ().getSelectedIndex ();
        String stId = colstId.getCellData (index).toString ();//select Column value

        try {
            StudentDTO dto = studentBO.getStudent (stId);
            txtStId.setText (dto.getStId ());
            txtName.setText (dto.getStName ());
            txtAddress.setText (dto.getAddress ());
            txtContact.setText (dto.getContact ());
            txtDate.setValue (LocalDate.parse (dto.getDob ()));
            cmbGender.setValue (dto.getGender ());
        } catch (Exception e) {
            System.out.println (e);
        }

    }

    public void setTableStudent() {
        colstId.setCellValueFactory (new PropertyValueFactory<>("stId"));
        colStName.setCellValueFactory (new PropertyValueFactory<> ("stName"));
        colAddress.setCellValueFactory (new PropertyValueFactory<> ("address"));
        colDob.setCellValueFactory (new PropertyValueFactory<> ("dob"));
        colContact.setCellValueFactory (new PropertyValueFactory<> ("contact"));
        colGender.setCellValueFactory (new PropertyValueFactory<> ("gender"));
    }

    public void clearData() {
        txtStId.clear ();
        txtName.clear ();
        txtAddress.clear ();
        txtContact.clear ();
        txtDate.setValue (null);
        cmbGender.setValue (null);
    }

    public void loadAllStudent() {

        try {
            List allStudents = studentBO.loadAll ();
            ObservableList observableList = FXCollections.observableArrayList (allStudents);
            tblStudent.setItems (observableList);

        } catch (Exception e) {
            System.out.println (e);
        }
    }
    private boolean checkValidation(){
        String nameText = txtName.getText();
        String addressText = txtAddress.getText();
        String contactText = txtContact.getText();

        if (!addressText.matches(".{2,}")) {
            new Alert(Alert.AlertType.ERROR, "Address should be at least 3 characters long").show();
            txtAddress.requestFocus();
            return false;
            //} else if (!contactText.matches(".*(?:7|0|(?:\\\\\\\\+94))[0-9]{9,10}")) {
        } else if (!contactText.matches("\\d{10}")) {
            new Alert(Alert.AlertType.ERROR, "Invalid Contact").show();
            txtContact.requestFocus();
            return false;
        }else {
            return true;
        }

    }
    public String nextStID() {
        Session session = SessionFactoryConfig.getInstance ().getSession ();
        Transaction transaction = session.beginTransaction ();

        Query query = session.createQuery ("select studentId from Student order by studentId desc");

        String nextId = "S001";

        if (query.list ().size () == 0) {
            return nextId;
        } else {
            String id = (String) query.list ().get (0);

            String[] SUs = id.split ("00");

            for (String a : SUs) {
                id = a;
            }

            int idNum = Integer.valueOf (id);

            id = "S00" + (idNum + 1);

            transaction.commit ();
            session.close ();

            return id;
        }
    }

    public void setStID(){
        String stID=nextStID ();
        txtStId.setText (stID);
    }


    public void navigateToHome(MouseEvent mouseEvent) throws IOException {
//        URL resource = this.getClass().getResource("/lk/ijse/hibernate/hostel/view/DashboardForm.fxml");
//        Parent root = FXMLLoader.load(resource);
//        Scene scene = new Scene(root);
//        Stage primaryStage = (Stage) (this.root.getScene().getWindow());
//        primaryStage.setScene(scene);
//        primaryStage.centerOnScreen();
//        Platform.runLater(() -> primaryStage.sizeToScene());

        Navigation.switchNavigation("DashboardForm.fxml",mouseEvent);
    }
}
