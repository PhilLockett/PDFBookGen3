/*  Tartan - a JavaFX based Tartan image generator.
 *
 *  Copyright 2024 Philip Lockett.
 *
 *  This file is part of Tartan.
 *
 *  Tartan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Tartan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Tartan.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * HelpControl is a class that is responsible for displaying the User Guide.
 */
package phillockett65.PDFBookGen;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class HelpControl extends Stage {



    /************************************************************************
     * Support code for "Help" pop-up. 
     */

    private Scene scene;

    private VBox root;

    private Button cancel;

    private double x = 0.0;
    private double y = 0.0;

    private boolean result = false;



    /************************************************************************
     * Support code for the Embedded Help page.
     */

    private class BodyFlow extends TextFlow {
        public BodyFlow(Text... text) {
            super(text);

            this.setTextAlignment(TextAlignment.JUSTIFY);
        }

    }
    private class Body extends Text {
        public Body(String text) {
            super(text);

            this.setFont(Font.font("Arial", FontWeight.NORMAL, 17));

            final Color fore = Color.web("#e8e8e8");
            this.setFill(fore);
        }

    }

    private class H1Flow extends BodyFlow {
        public H1Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(0, 0, 12, 0));
        }

    }
    private class H1 extends Body {
        public H1(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        }
    }

    private class H2Flow extends BodyFlow {
        public H2Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(8, 0, 10, 0));
        }

    }
    private class H2 extends Body {
        public H2(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        }
    }

    private class H3Flow extends BodyFlow {
        public H3Flow(Text... text) {
            super(text);

            this.setPadding(new Insets(8, 0, 10, 0));
        }

    }
    private class H3 extends Body {
        public H3(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.BOLD, 19));
        }
    }

    private class PFlow extends BodyFlow {
        public PFlow(Text... text) {
            super(text);

            this.setPadding(new Insets(0, 0, 16, 0));
            this.setPrefWidth(470);
            this.setMaxWidth(470);
        }

    }
    private class P extends Body {
        public P(String text) {
            super(text);
            this.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
            }
    }

    VBox help = new VBox(
        new H1Flow(new H1("PDF Book Generator User Guide")),
        new H2Flow(new H2("Introduction")),
        new PFlow(
            new P("For the purposes of “PDF Book Generator” a book has two pages arranged side-by-side on both sides of a sheet of paper, giving a total of four pages per sheet. "),
            new P("Such sheets can be folded to produce a booklet. "),
            new P("A number of folded and nested together sheets creates a signature. "),
            new P("Signatures are stitched together to form a book block which can then have a cover attached to create a book. ")
            ),
        new PFlow(
            new P("“PDF Book Generator” takes a Source PDF document and generates an output PDF which can be printed, folded into signatures and bound together as desired. ")
            ),
        new H2Flow(new H2("Usage")),
        new H3Flow(new H3("“File Names” Pane")),
        new PFlow(
            new P("To select a source PDF document either click on the “Load...” button or use the pull-down menu option “File -> Load...” which will launch a standard file chooser dialogue window. "),
            new P("The read-only “Source Document” text field displays the document selected. ")
            ),
        new PFlow(
            new P("The editable “Output File Name” text field allows for the output file need to be specified. "),
            new P("This will automatically have the “.pdf” extension added and, by default, uses the same directory as the Source document. "),
            new P("The read-only “Generate Document” text field displays the full output document file path. "),
            new P("Clicking the “Generate” button at the bottom of the window will generate the output document using the options selected. "),
            new P(""),
            new P("Alternatively use the pull-down menu option “File -> Generate Booklet”. ")
            ),
        new PFlow(
            new P("If a different output directory is needed, use the pull-down menu option “File -> Generate Booklet As...” which will launch a standard file save dialogue window. ")
            ),
        new H3Flow(new H3("“Output Content” Pane")),
        new PFlow(
            new P("The “Output Paper Size” selector allows the paper size of the output document to be selected. ")
            ),
        new PFlow(
            new P("The “First Page” and “Last Page” spinners allow for sub-sections of the source document to be selected if the whole document is not required. "),
            new P("The “Total page count” is calculated from these spinners. ")
            ),
        new PFlow(
            new P("Typically the two pages on the reverse side of the sheet need to be rotated relative to those on the front if the output document is being displayed on a full-duplex printer. "),
            new P("However, if printing a test sheet shows that the inner 2 pages are up-side-down relative to the outer 2 pages, then this setting needs to be changed. ")
            ),
        new H3Flow(new H3("“Signature State” Pane")),
        new PFlow(
            new P("For a “Signature Size” of 1 the source document is processed in groups of 4 pages for each sheet of paper. "),
            new P("The 4th page is rotated anti-clockwise and scaled to fit on the bottom half of one side of the sheet. "),
            new P("The 1st page is rotated anti-clockwise and scaled to fit on the top half of the same side of the sheet. "),
            new P("On the reverse side, the 2nd page is rotated clockwise and scaled to fit on the top half and the 3rd page is rotated clockwise and scaled to fit on the bottom half. "),
            new P("This process is repeated for all groups of 4 pages in the source document. ")
            ),
        new PFlow(
            new P("For a “Signature Size” of more than 1 sheet, more pages are grouped in multiples of 4 and arranged in a similar, but more complex manner. "),
            new P("The “Signature Size” specifies the number of folded sheets of paper to nest for each signature. ")
            ),
        new PFlow(
            new P("The rest of this pane simply displays useful information calculated from the “Signature Size”, the “First Page” and the “Last Page” selections. ")
            )
    );



    /************************************************************************
     * Support code for the Initialization of the Controller.
     */

    /**
     * Builds the top-bar as a HBox and includes the cancel button the mouse 
     * press and drag handlers.
     * @return the HBox that represents the top-bar.
     */
    private HBox buildTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER);
        topBar.setPrefHeight(Model.TOPBARHEIGHT);

        // Make window dragable.
        topBar.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        topBar.setOnMouseDragged(mouseEvent -> {
            this.setX(mouseEvent.getScreenX() - x);
            this.setY(mouseEvent.getScreenY() - y);
        });

        Image image = new Image(getClass().getResourceAsStream("pdf.jpg"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(28);
        imageView.setFitWidth(28);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        Label heading = new Label(" " + this.getTitle());
        Region region = new Region();

        Pane cancel = Model.buildCancelButton();
        cancel.setOnMouseClicked(event -> {
            result = false;
            this.close();
        });

        topBar.getChildren().add(imageView);
        topBar.getChildren().add(heading);
        topBar.getChildren().add(region);
        topBar.getChildren().add(cancel);
        
        HBox.setHgrow(region, Priority.ALWAYS);

        return topBar;
    }


    /**
     * Builds the options button as a HBox and includes the action event 
     * handler for cancel button.
     * @return the HBox that represents the options button.
     */
    private HBox buildOptions() {
        HBox options = new HBox();

        cancel = new Button("Cancel");

        cancel.setMnemonicParsing(false);
    
        cancel.setOnAction(event -> {
            result = false;
            this.close();
        });

        cancel.setTooltip(new Tooltip("Cancel Help"));

        options.getChildren().add(cancel);

        return options;
    }


    /**
     * Builds a ScrollPane and fills it with "help".
     * @return the ScrollPane filled with "help".
     */
    private ScrollPane buildHelpPage() {

        final Color back = Color.web("#101010");
        help.setBackground(Background.fill(back));
        help.setPadding(new Insets(4, 16, 4, 4));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setMaxSize(500, 600);
        scrollPane.setContent(help);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

        return scrollPane;
    }


    /**
     * Builds the User controls as a VBox.
     * @return the VBox that captures the User controls.
     */
    private VBox buildControlPanel() {
        VBox panel = new VBox();

        panel.setSpacing(10);
        panel.setPadding(new Insets(10.0));

        panel.getChildren().add(buildHelpPage());
        panel.getChildren().add(buildOptions());

        return panel;
    }

    /**
     * Initialize the control.
     */
    private void init(String title) {
        this.setTitle(title);
        this.resizableProperty().setValue(false);
        this.initStyle(StageStyle.UNDECORATED);
        this.initModality(Modality.APPLICATION_MODAL);

        root = new VBox();

        root.getChildren().add(buildTopBar());
        root.getChildren().add(buildControlPanel());
        root.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

        scene = new Scene(root);

        this.setScene(scene);
    }



    /************************************************************************
     * Support code for the Operation of the Controller.
     */

    /**
     * Constructor.
     */
    private HelpControl() {
        super();
    }


    /**
     * Construct and launch the User Guide and wait for user input.
     * @return false when cancelled.
     */
    public static boolean showControl(String title) {
        HelpControl control = new HelpControl();

        control.init(title);
        control.showAndWait();

        return control.result;
    }

}
