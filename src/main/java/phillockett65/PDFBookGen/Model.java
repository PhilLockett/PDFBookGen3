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
 * Model is the class that captures the dynamic shared data plus some 
 * supporting constants and provides access via getters and setters.
 */
package phillockett65.PDFBookGen;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import phillockett65.Debug.Debug;

public class Model {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private final static String DATAFILE = "Settings.ser";
    public static final double TOPBARHEIGHT = 32.0;
    private static final String TOPBARICON = "top-bar-icon";

    private static Model model = new Model();

    private Stage stage;


    /************************************************************************
     * General support code.
     */

    /**
     * @return the file path of the settings data file.
     */
    public String getSettingsFile() {
        return DATAFILE;
    }

    /**
     * Builds the cancel button as a Pane.
     * Does not include the mouse click handler.
     * @return the Pane that represents the cancel button.
     */
    public static Pane buildCancelButton() {
        final double iconSize = TOPBARHEIGHT;
        final double cancelPadding = 0.3;

        Pane cancel = new Pane();
        cancel.setPrefWidth(iconSize);
        cancel.setPrefHeight(iconSize);
        cancel.getStyleClass().add(TOPBARICON);

        double a = iconSize * cancelPadding;
        double b = iconSize - a;
        Line line1 = new Line(a, a, b, b);
        line1.setStroke(Color.WHITE);
        line1.setStrokeWidth(4.0);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);

        Line line2 = new Line(a, b, b, a);
        line2.setStroke(Color.WHITE);
        line2.setStrokeWidth(4.0);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);

        cancel.getChildren().addAll(line1, line2);

        return cancel;
    }

    /**
     * Add or remove the unfocussed style from the given pane object.
     * @param pane to add/remove unfocussed style.
     * @param style named in .css to define unfocussed style.
     * @param state is true if we have focus, false otherwise.
     */
    public static void styleFocus(Pane pane, String style, boolean state) {
        if (state) {
            pane.getStyleClass().remove(style);
        } else {
            if (!pane.getStyleClass().contains(style)) {
                pane.getStyleClass().add(style);
            }
        }
    }



    /************************************************************************
     * Support code for the Initialization of the Model.
     */

    /**
     * Private default constructor - part of the Singleton Design Pattern.
     * Called at initialization only, constructs the single private instance.
     */
    private Model() {
    }

    /**
     * Singleton implementation.
     * @return the only instance of the model.
     */
    public static Model getInstance() { return model; }


    /**
     * Called by the controller after the constructor to initialise any 
     * objects after the controls have been initialised.
     */
    public void initialize() {
        Debug.trace(DD, "Model initialized.");

        initializeFileNamesPanel();
        initializeOutputContentPanel();
        initializeSignatureStatePanel();
        initializeStatusLine();

        if (!readData())
            defaultSettings();

        // Calculate the signature data AFTER reading previous settings.
        BuildSignature();
    }

    /**
     * Called by the controller after the stage has been set. Completes any 
     * initialization dependent on other components being initialized.
     */
    public void init(Stage primaryStage) {
        Debug.trace(DD, "Model init.");
        
        stage = primaryStage;
    }

    public Stage getStage() { return stage; }
    public String getTitle() { return stage.getTitle(); }

    public void close() {
        stage.close();
    }

    /**
     * Set all attributes to the default values.
     */
    public void defaultSettings() {
        setSourceFilePath("");
        setOutputFilePath("./booklet");

        setPaperSize("Letter");
        setRotateCheck(true);
        setPageCount(100);
        setPageRanges(1, 100);

        setSigSize(1);
    }



    /************************************************************************
     * Support code for state persistence.
     */

    /**
     * Instantiate a DataStore, populate it with data and save it to disc.
     * @return true if data successfully written to disc, false otherwise.
     */
    public boolean writeData() {
        DataStore data = new DataStore();

        data.sourceDocument = getSourceFilePath();
        data.outputFileName = getOutputFilePath();

        data.paperSize = getPaperSize();
        data.rotateCheck = isRotateCheck();
        data.firstPage = getFirstPage();
        data.lastPage = getLastPage();

        data.sigSize = getSigSize();

        if (!DataStore.writeData(data, getSettingsFile())) {
            data.dump();

            return false;
        }

        return true;
    }

    /**
     * Get a DataStore populated with data previously stored to disc and update
     * the model with the data.
     * @return true if the model is successfully updated, false otherwise.
     */
    public boolean readData() {
        DataStore data = DataStore.readData(getSettingsFile());
        if (data == null)
            return false;

        setSourceFilePath(data.sourceDocument);
        setOutputFilePath(data.outputFileName);

        setPaperSize(data.paperSize);
        setRotateCheck(data.rotateCheck);
        setPageRanges(data.firstPage, data.lastPage);
        
        setSigSize(data.sigSize);
    
        return true;
    }



    /************************************************************************
     * Support code for "File Names" panel.
     */

    private String sourceDocument;
    private String outputFilePath;

    /**
     * Set the file path for the source PDF document.
     * @param text string of the source document file path.
     */
    public void setSourceFilePath(String text) {
        sourceDocument = text;
        setPageCount(fetchPageCount());
    }

    /**
     * @return the number of pages in the current source document.
     */
    private int fetchPageCount() {
        if (isSourceFilePath())
            return PDFBook.getPDFPageCount(sourceDocument);

        return 1;
    }

    /**
     * @return the file path for the current source PDF document.
     */
    public String getSourceFilePath() { return sourceDocument; }

    /**
     * @return true if a source document has been selected, false otherwise.
     */
    public boolean isSourceFilePath() { if (sourceDocument == null) return false; return !sourceDocument.isBlank(); }

    private String fileStem(String fileName) {
        if (!fileName.contains("."))
            return fileName;

        return fileName.substring(0,fileName.lastIndexOf("."));
    }

    private String getFileParent(String file) {
        if (file.isBlank())
            return "."; 

        File current = new File(file);
        return current.getParent();
    }

    private String getOutputFileParent() {
        if (isOutputFilePath())
            return getFileParent(getOutputFilePath());

        if (isSourceFilePath())
            return getFileParent(getSourceFilePath());

        return ".";
    }

    /**
     * Set the file name for the generated PDF document.
     * @param text string of the file name for the generated document.
     */
    public void setOutputFileName(String text) { outputFilePath = getOutputFileParent() + "\\" + text + ".pdf"; }

    /**
     * Set the file path for the generated PDF document.
     * @param text string of the file path for the generated document.
     */
    public void setOutputFilePath(String text) { outputFilePath = text; }

    /**
     * @return the file name for the generated PDF document.
     */
    public String getOutputFileName() {
        File current = new File(getOutputFilePath());
        return fileStem(current.getName());
    }

    /**
     * @return the full file path for the generated PDF document.
     */
    public String getOutputFilePath() { return outputFilePath; }

    /**
     * @return true if a source document has been selected, false otherwise.
     */
    public boolean isOutputFilePath() { if (outputFilePath == null) return false; return !outputFilePath.isBlank(); }

    /**
     * Initialize "File Names" panel.
     */
    private void initializeFileNamesPanel() {
    }



    /************************************************************************
     * Support code for "Output Content" panel.
     */

    private String paperSize;
    private ObservableList<String> paperSizeList = FXCollections.observableArrayList();

    private boolean rotateCheck;
    private int pageCount = 50;

    private SpinnerValueFactory<Integer> firstPageSVF;
    private SpinnerValueFactory<Integer> lastPageSVF;


    /**
     * @return the Observable List for the paper size choice box.
     */
    public ObservableList<String> getPaperSizeList() { return paperSizeList; }

    /**
     * Note the selected paper size.
     * @param value of the currently selected paper size as a string.
     */
    public void setPaperSize(String value) { paperSize = value; }

    /**
     * @return the currently selected paper size string.
     */
    public String getPaperSize() { return paperSize; }


    /**
     * Indicate whether the reverse side page is to be rotated.
     * @param state true if the reverse page is to be rotated, false otherwise.
     */
    public void setRotateCheck(boolean state) { rotateCheck = state; }

    /**
     * @return true if the reverse side page is to be rotated, false otherwise.
     */
    public boolean isRotateCheck() { return rotateCheck; }


    private int getPageCount() { return pageCount; }
    private void setPageCount(int value) {
        pageCount = value;
        firstPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, value, 1);
        lastPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, value, value);
        BuildSignature();
    }


    /**
     * @return the Value Factory for the first page spinner.
     */
    public SpinnerValueFactory<Integer> getFirstPageSVF() { return firstPageSVF; }

    private int getFirstPage() { return firstPageSVF.getValue(); }

    /**
     * Selected first page has changed, so synchronize values.
     */
    public void syncFirstPage() {
        // Make sure Last Page spinner has a minimum of the new First Page value.
        final int first = getFirstPage();
        int current = getLastPage();
        if (current < first)
            current = first;
        lastPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(first, getPageCount(), current);

        BuildSignature();
    }

    /**
     * @return the Value Factory for the last page spinner.
     */
    public SpinnerValueFactory<Integer> getLastPageSVF() { return lastPageSVF; }

    private int getLastPage() { return lastPageSVF.getValue(); }

    /**
     * Selected last page has changed, so synchronize values.
     */
    public void syncLastPage() {
        // Make sure First Page spinner has a maximum of the new Last Page value.
        final int last = getLastPage();
        int current = getFirstPage();
        if (current > last)
            current = last;
        firstPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, last, current);

        BuildSignature();
    }


    /**
     * Used on start up to ensure first and last are well behaved, particularly
     * if the source PDF has been reduced in size potentially making first or 
     * last invalid.
     * @param first selected page of source PDF to output.
     * @param last selected page of source PDF to output.
     */
    private void setPageRanges(int first, int last) {
        final int count = getPageCount();

        if (last > count)
            last = count;
        if (first > last)
            first = last;

        firstPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, last, first);
        lastPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(first, count, last);
        BuildSignature();
    }

    /**
     * Use a PDFBook instance to generate booklet.
     * @return true if document was generated, false otherwise.
     */
    public boolean generate() {
        PDFBook booklet = new PDFBook(getSourceFilePath(), getOutputFilePath());

        booklet.setPageSize(getPaperSize());
        booklet.setSheetCount(getSigSize());
        booklet.setRotate(isRotateCheck());

        final int first = getFirstPage();
        final int last = getLastPage();
        booklet.setFirstPage(first-1);
        booklet.setLastPage(last);

        booklet.genBooklet();

        return true;
    }


    /**
     * Initialize "Output Content" panel.
     */
    private void initializeOutputContentPanel() {
        paperSizeList.addAll("A0", "A1", "A2", "A3", "A4", "A5", "A6", "Letter", "Legal");
        firstPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        lastPageSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
    }



    /************************************************************************
     * Support code for "Signature State" panel.
     */

    private SpinnerValueFactory<Integer> sigSizeSVF;
    private Signature signature;

    /**
     * @return the Value Factory for the signature size spinner.
     */
    public SpinnerValueFactory<Integer> getSigSizeSVF() { return sigSizeSVF; }

    private int getSigSize() { return sigSizeSVF.getValue(); }
    private void setSigSize(int value) { sigSizeSVF.setValue(value); }

    /**
     * Signature size has changed, so synchronize values.
     */
    public void syncSigSize() { BuildSignature(); }


    private void BuildSignature() {
        signature = new Signature(getSigSize(), getFirstPage(), getLastPage());
    }

    /**
     * @return the number of source pages in the generated document.
     */
    public int getOutputPageCount() { return signature.getOutputPageCount(); }

    /**
     * @return the number of source pages in a signature.
     */
    public int getSigPageCount() { return signature.getSigPageCount(); }

    /**
     * @return the number of signatures that will be generated.
     */
    public int getSigCount() { return signature.getSigCount(); }

    /**
     * @return the source page number that the last signature starts with.
     */
    public int getLastSigFirstPage() { return signature.getLastSigFirstPage(); }

    /**
     * @return the number of source pages in the last signature.
     */
    public int getLastSigPageCount() { return signature.getLastSigPageCount(); }

    /**
     * @return the number of blank pages in the last signature.
     */
    public int getLastSigBlankCount() { return signature.getLastSigBlankCount(); }


    /**
     * Initialize "Signature State" panel.
     */
    private void initializeSignatureStatePanel() {
        sigSizeSVF = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1);
    }



    /************************************************************************
     * Support code for "Status Line" panel.
     */

    /**
     * Initialize "Status Line" panel.
     */
    private void initializeStatusLine() {
    }


}
