# PDFBookGen3
'PDFBookGen' is a simple JavaFX application that generates a booklet from of a 
PDF document.

**USE AT OWN RISK.**

## Overview
This project has been set up as a Maven project that utilises JavaFX, FXML and 
CSS to render the GUI. 
Maven can be run from the command line as shown below.
Maven resolves dependencies and builds the application independently of an IDE.

## Dependencies
'PDFBookGen' is dependent on the following in order to execute:

  * Java 15.0.1
  * Apache Maven 3.6.3

The code is structured as a standard Maven project that requires Maven and a 
JDK to be installed. 
A quick web search will help, alternatively
[Oracle](https://www.java.com/en/download/) and 
[Apache](https://maven.apache.org/install.html) should guide you through the
install.

Also [OpenJFX](https://openjfx.io/openjfx-docs/) can help set up your 
favourite IDE to be JavaFX compatible, however, Maven does not require this.

## Cloning and Running
The following commands clone and execute the code:

	git clone https://github.com/PhilLockett/PDFBookGen.git
	cd PDFBookGen/
	mvn clean javafx:run

## Bookbinding
This code supports multi-sheet sections. For more information on bookbinding 
terms and techniques refer to:
 * [Terms](https://en.wikipedia.org/wiki/Bookbinding#Terms_and_techniques)
 * [Layout](https://www.formaxprinting.com/blog/2016/11/booklet-layout-how-to-arrange-the-pages-of-a-saddle-stitched-booklet/)
 * [Bindings](https://www.studentbookbinding.co.uk/blog/how-to-set-up-pagination-section-sewn-bindings)


## Implementation Summary
A booklet has two pages arranged side-by-side on both sides of a sheet of 
paper such that the sheet can be folded to produce a booklet.

For a "Signature Size" of "1 sheet" the document is processed in groups of 4 
pages for each sheet of paper. 
The 4th page is rotated anti-clockwise and scaled to fit on the bottom half of 
one side of the sheet. 
The 1st page is rotated anti-clockwise and scaled to fit on the top half of the 
same side of the sheet. 
On the reverse side, the 2nd page is rotated clockwise and scaled to fit on the 
top half and the 3rd page is rotated clockwise and scaled to fit on the bottom 
half. 
This process is repeated for all groups of 4 pages in the source document.

For a "Signature Size" of more than 1 sheet, more pages are grouped in 
multiples of 4 and arranged in a similar, but more complex manner.
The "Signature Size" specifies the number of folded sheets of paper to nest 
for each signature.

## Customization
The GUI layout can be modified as desired by editing the 'primary.fxml' file. 
The SceneBuilder application makes editing the layout easier than modifiying 
'primary.fxml' directly.

FXML also uses cascading style sheets for the presentation. 
To change the colours and fonts used, edit the 'application.css' file.

## Points of interest
This code has the following points of interest:

  * PDFBookGen is a Maven project that uses JavaFX.
  * PDFBookGen is styled with CSS.
  * PDFBookGen is structured as an MVC project (FXML being the Video component).
  * Multi stage initialization minimizes the need for null checks. 
  * Data persistence is provided by the Serializable DataStore object.
  * The GUI is implemented in FXML using SceneBuilder.
  * PDFBookGen3 is an improved version of [PDFBookGen](https://github.com/PhilLockett/PDFBookGen).
  * A static Debug object helps control diagnostic output.
  * The Command Pattern is used to support an Undo/Redo mechanism.
