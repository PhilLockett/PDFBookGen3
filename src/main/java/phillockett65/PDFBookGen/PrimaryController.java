/*  PDFBookGen - a simple application to generate a booklet from of a PDF.
 *
 *  Copyright 2024 Philip Lockett.
 *
 *  This file is part of PDFBookGen.
 *
 *  PDFBookGen is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  PDFBookGen is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with PDFBookGen.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * PrimaryController is the class that is responsible for centralizing control.
 * It is instantiated by the FXML loader creates the Model.
 */
package phillockett65.PDFBookGen;

import java.io.File;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import phillockett65.Debug.Debug;
import phillockett65.PDFBookGen.Command.FirstPageCommand;
import phillockett65.PDFBookGen.Command.Invoker;
import phillockett65.PDFBookGen.Command.LastPageCommand;
import phillockett65.PDFBookGen.Command.OutputDocumentCommand;
import phillockett65.PDFBookGen.Command.OutputFileNameCommand;
import phillockett65.PDFBookGen.Command.PaperSizeCommand;
import phillockett65.PDFBookGen.Command.RotateCommand;
import phillockett65.PDFBookGen.Command.SignatureSizeCommand;
import phillockett65.PDFBookGen.Command.SourceDocumentCommand;


public class PrimaryController {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private Model model;
    private Invoker invoker;

    @FXML
    private VBox root;


    /************************************************************************
     * Support code for the Initialization of the Controller.
     */

    /**
     * Responsible for constructing the Model and any local objects. Called by 
     * the FXMLLoader().
     */
    public PrimaryController() {
        Debug.trace(DD, "PrimaryController constructed.");
        model = Model.getInstance();
        invoker = Invoker.getInstance();
    }

    /**
     * Called by the FXML mechanism to initialize the controller. Called after 
     * the constructor to initialise all the controls.
     */
    @FXML
    public void initialize() {
        Debug.trace(DD, "PrimaryController initialize()");
        model.initialize();

        initializeTopBar();
        initializeFileNamesPanel();
        initializeOutputContentPanel();
        initializeSignatureStatePanel();
        initializeStatusLine();
    }

    /**
     * Called by Application after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     * 
     * @param mainController used to call the centralized controller.
     */
    public void init(Stage primaryStage) {
        Debug.trace(DD, "PrimaryController init()");
        model.init(primaryStage, this);
        syncUI();
        setStatusMessage("Ready.");
        invoker.clear();

        // Use filter so text based controls do not affect the undo/redo.
        primaryStage.getScene().addEventFilter(KeyEvent.KEY_TYPED, 
            new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    final String key = event.getCharacter();
        
                    switch ((int)key.charAt(0)) {
                    case 25:
                        invoker.redo();
                        break;
        
                    case 26:
                        invoker.undo();
                        break;
        
                    case 13:
                        invoker.dump();
                        break;
        
                    default:
                        break;
                    }
                }
            });
    }

    /**
     * Save the current state to disc, called by the application on shut down.
     */
    public void saveState() {
        model.writeData();
    }

    /**
     * Set the styles based on the focus state.
     * @param state is true if we have focus, false otherwise.
     */
    public void setFocus(boolean state) {
        Model.styleFocus(root, "unfocussed-root", state);
        Model.styleFocus(topBar, "unfocussed-bar", state);
    }

    /**
     * Synchronise all controls with the model. This should be the last step 
     * in the initialisation.
     */
    public void syncUI() {
        syncSourceDocumentTextField();

        final boolean genAvailable = model.isSourceDocument();
        generateButton.setDisable(!genAvailable);
        genMenuItem.setDisable(!genAvailable);
        asMenuItem.setDisable(!genAvailable);

        syncOutputFileNameTextField();
        syncOutputDocumentTextField();

        syncFirstPageSpinner();
        syncLastPageSpinner();

        rotateCheckBox.setSelected(model.isRotateCheck());

        paperSizeChoiceBox.setValue(model.getPaperSize());
        syncSigSizeSpinner();

        setTotalPageCountMessage();
        setSignatureStateMessages();
    }

    private void syncSourceDocumentTextField() {
        sourceDocumentTextField.setText(model.getSourceDocument());
    }
    public void syncOutputFileNameTextField() {
        outputFileNameTextField.setText(model.getOutputFileName());
    }
    public void syncOutputDocumentTextField() {
        outputDocumentTextField.setText(model.getOutputDocument());
    }


    /************************************************************************
     * Support code for "Top Bar" panel.
     */

    private double x = 0.0;
    private double y = 0.0;

    @FXML
    private HBox topBar;

    @FXML
    void topBarOnMousePressed(MouseEvent event) {
        x = event.getSceneX();
        y = event.getSceneY();
    }

    @FXML
    void topBarOnMouseDragged(MouseEvent event) {
        model.getStage().setX(event.getScreenX() - x);
        model.getStage().setY(event.getScreenY() - y);
    }
 

    /**
     * Initialize "Top Bar" panel.
     */
    private void initializeTopBar() {
        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> model.close());

        topBar.getChildren().add(cancel);
    }



    /************************************************************************
     * Support code for Pull-down Menu structure.
     */

    @FXML
    private MenuItem genMenuItem;

    @FXML
    private MenuItem asMenuItem;

    @FXML
    private void fileLoadOnAction() {
        launchLoadWindow();
    }

    @FXML
    private void fileSaveOnAction() {
        if (model.isOutputDocument()) {
            fileSaved(model.generate());
        }
        else
            launchSaveAsWindow();
    }

    @FXML
    private void fileSaveAsOnAction() {
        launchSaveAsWindow();
    }

    @FXML
    private void fileCloseOnAction() {
        model.close();
    }
 
    @FXML
    private void editClearOnAction() {
        clearData();
    }

    @FXML
    private void helpAboutOnAction() {
        final String title = model.getTitle();

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About " + title);
        alert.setHeaderText(title);
        alert.setContentText(title + " is an application for converting a PDF document into a 2-up booklet form.");

        alert.showAndWait();
    }

    @FXML
    private void helpUserGuideOnAction() {
        model.showUserGuide();
    }

    /**
     * Use a file chooser to select a test file.
    * @return true if a file was selected and loaded, false otherwise.
    */
    private void launchLoadWindow() {
        openFile();
    }

    private void launchSaveAsWindow() {
        saveAs();
    }

    public void fileLoaded(boolean loaded) {
        if (loaded) {
            syncSourceDocumentTextField();
            setStatusMessage("Loaded file: " + model.getSourceDocument());
        }
    }

    public void fileSaved(boolean saved) {
        if (saved) {
            setStatusMessage("Saved file: " + model.getOutputDocument());
        }
    }


    /************************************************************************
     * Support code for "File Names" panel.
     */

    @FXML
    private TextField sourceDocumentTextField;

    @FXML
    private TextField outputFileNameTextField;

    @FXML
    private TextField outputDocumentTextField;

    @FXML
    private Button browseButton;

    @FXML
    private void outputFileNameTextFieldKeyTyped(KeyEvent event) {
        Debug.trace(DD, "outputFileNameTextFieldKeyTyped() " + outputFileNameTextField.getText());
        // model.setOutputFileName(outputFileNameTextField.getText());
        // outputDocumentTextField.setText(model.getOutputFilePath());
        OutputFileNameCommand command = new OutputFileNameCommand(outputFileNameTextField.getText());
        invoker.invoke(command);

    }

    @FXML
    private void browseButtonActionPerformed(ActionEvent event) {
        launchLoadWindow();
    }


    /**
     * Use a FileChooser dialogue to select the source PDF file.
     * @return true if a file is selected, false otherwise.
     */
    private void openFile() {
        // Set up the file chooser.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDF Document File");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        if (model.isSourceDocument()) {
            File current = new File(model.getSourceDocument());
            if (current.exists()) {
                fileChooser.setInitialDirectory(new File(current.getParent()));
                fileChooser.setInitialFileName(current.getName());
            }
        }

        // Use the file chosser.
        File file = fileChooser.showOpenDialog(model.getStage());
        if (file != null) {
            // model.setSourceFilePath(file.getAbsolutePath());
            // syncUI();
            SourceDocumentCommand command = new SourceDocumentCommand(file.getAbsolutePath());
            invoker.invoke(command);
        }
    }

    private void saveAs() {
        // Set up the file chooser.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Document File");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        if (model.isOutputFileParent()) {
            fileChooser.setInitialFileName(model.getFullOutputFileName());

            File parent = new File(model.getOutputFileParent());
            if (parent.exists()) {
                fileChooser.setInitialDirectory(parent);
            }
        }

        // Use the file chosser.
        File file = fileChooser.showSaveDialog(model.getStage());
        if (file != null) {
            // model.setOutputDocument(file.getAbsolutePath());
            // syncOutputFileNameTextField();
            // syncOutputDocumentTextField();

            // fileSaved(model.generate());
            OutputDocumentCommand command = new OutputDocumentCommand(file.getAbsolutePath());
            invoker.invoke(command);
        }
    }


    /**
     * Initialize "File Names" panel.
     */
    private void initializeFileNamesPanel() {
        sourceDocumentTextField.setTooltip(new Tooltip("Source PDF document"));
        outputFileNameTextField.setTooltip(new Tooltip("Name of generated output file, .pdf will be added automatically"));
        outputDocumentTextField.setTooltip(new Tooltip("Full path of generated output file"));
        browseButton.setTooltip(new Tooltip("Select source PDF document"));
    }



    /************************************************************************
     * Support code for "Output Content" panel.
     */

    @FXML
    private ChoiceBox<String> paperSizeChoiceBox;

    @FXML
    private CheckBox rotateCheckBox;

    @FXML
    private Spinner<Integer> firstPageSpinner;

    @FXML
    private Spinner<Integer> lastPageSpinner;

    @FXML
    private Label countLabel;

    @FXML
    private Button generateButton;

    @FXML
    private void rotateCheckBoxActionPerformed(ActionEvent event) {
        // model.setRotateCheck(rotateCheckBox.isSelected());
        RotateCommand command = new RotateCommand(rotateCheckBox.isSelected());
        invoker.invoke(command);
    }

    @FXML
    private void generateButtonActionPerformed(ActionEvent event) {
        final boolean success = model.generate();
        if (success)
            setStatusMessage("Generated: " + model.getOutputDocument());
        else
            setStatusMessage("Failed to generate: " + model.getOutputDocument());
    }


    private void setTotalPageCountMessage() {
        countLabel.setText(String.valueOf(model.getOutputPageCount()));
    }

    public void syncFirstPageSpinner() {
        firstPageSpinner.setValueFactory(model.getFirstPageSVF());
    }

    public void syncLastPageSpinner() {
        lastPageSpinner.setValueFactory(model.getLastPageSVF());
    }

    /**
     * Initialize "Output Content" panel.
     */
    private void initializeOutputContentPanel() {
        paperSizeChoiceBox.setItems(model.getPaperSizeList());

        paperSizeChoiceBox.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace(DD, "paperSizeChoiceBox.Listener(" + newValue + "))");
            // model.setPaperSize(newValue);
            PaperSizeCommand command = new PaperSizeCommand(oldValue, newValue);
            invoker.invoke(command);
        });


        syncFirstPageSpinner();
        firstPageSpinner.getValueFactory().wrapAroundProperty().set(false);
        
        firstPageSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace(DD, "firstPageSpinner.Listener(" + newValue + "))");
            // model.syncFirstPage();
            // syncLastPageSpinner();
            // syncUI();
            FirstPageCommand command = new FirstPageCommand(oldValue, newValue);
            invoker.invoke(command);
        });

        syncLastPageSpinner();
        lastPageSpinner.getValueFactory().wrapAroundProperty().set(false);
        
        lastPageSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace(DD, "lastPageSpinner.Listener(" + newValue + "))");
            // model.syncLastPage();
            // syncFirstPageSpinner();
            // syncUI();
            LastPageCommand command = new LastPageCommand(oldValue, newValue);
            invoker.invoke(command);
        });
        
        paperSizeChoiceBox.setTooltip(new Tooltip("Paper size of the generated PDF document"));
        rotateCheckBox.setTooltip(new Tooltip("Rotate reverse side of sheet 180 degrees"));
        firstPageSpinner.setTooltip(new Tooltip("First page of source document to include in the generated document"));
        lastPageSpinner.setTooltip(new Tooltip("Last page of source document to include in the generated document"));
        countLabel.setTooltip(new Tooltip("Number of pages from the source document that will be included in the generated document"));
        generateButton.setTooltip(new Tooltip("Generate the PDF document in booklet form"));
    }



    /************************************************************************
     * Support code for "Signature State" panel.
     */

    @FXML
    private Spinner<Integer> sigSizeSpinner;

    @FXML
    private Label sigLabel;

    @FXML
    private Label sigCountLabel;

    @FXML
    private Label lastSigBeginLabel;

    @FXML
    private Label lastSigCountLabel;

    @FXML
    private Label lastSigBlanksLabel;

    private void setSignatureStateMessages() {
        sigLabel.setText(String.valueOf(model.getSigPageCount()));
        sigCountLabel.setText(String.valueOf(model.getSigCount()));
        lastSigBeginLabel.setText(String.valueOf(model.getLastSigFirstPage()));
        lastSigCountLabel.setText(String.valueOf(model.getLastSigPageCount()));
        lastSigBlanksLabel.setText(String.valueOf(model.getLastSigBlankCount()));
    }

    private void syncSigSizeSpinner() {
        sigSizeSpinner.setValueFactory(model.getSigSizeSVF());
    }


    /**
     * Initialize "Signature State" panel.
     */
    private void initializeSignatureStatePanel() {
        sigSizeSpinner.setTooltip(new Tooltip("Number of sheets of paper in each signature"));
        sigLabel.setTooltip(new Tooltip("Number of pages from the source document in each signature"));
        sigCountLabel.setTooltip(new Tooltip("Number of signatures in generated document"));
        lastSigBeginLabel.setTooltip(new Tooltip("First page from the source document in the last signature"));
        lastSigCountLabel.setTooltip(new Tooltip("Number of pages from the source document in the last signature"));
        lastSigBlanksLabel.setTooltip(new Tooltip("Number of blank pages in the last signature"));

        syncSigSizeSpinner();
        sigSizeSpinner.getValueFactory().wrapAroundProperty().set(false);
        
        sigSizeSpinner.valueProperty().addListener( (v, oldValue, newValue) -> {
            Debug.trace(DD, "sigSizeSpinner.Listener(" + newValue + "))");
            // model.syncSigSize();
            // syncUI();
            SignatureSizeCommand command = new SignatureSizeCommand(oldValue, newValue);
            invoker.invoke(command);
        });
    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    @FXML
    private Label statusLabel;

    private void clearData() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Clear Data");
        alert.setHeaderText("Caution! This irreversible action will reset the form data to default values");
        alert.setContentText("Do you wish to continue?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            model.defaultSettings();
            syncUI();
            setStatusMessage("Data reset.");
        }
    }

    private void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    /**
     * Initialize "Status Line" panel.
     */
    private void initializeStatusLine() {
        statusLabel.setTooltip(new Tooltip("Current status"));
    }

}