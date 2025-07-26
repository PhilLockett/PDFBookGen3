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
 * As a standalone file, PDFBookGen is a simple application to generate a booklet
 * from of a source PDF document. It requires 2 parameters, the source PDF and
 * the name of the new PDF. However, it can be used as a java class, in which
 * case PDFBookGen.main() should be superseded.
 *
 * Example usage:
 *  java -jar path-to-PDFBookGen.jar path-to-source.pdf path-to-new.pdf
 *
 * Dependencies:
 *  PDFbox (pdfbox-app-2.0.19.jar)
 *  https://pdfbox.apache.org/download.cgi
 *
 * This code supports multi-sheet sections. For more information on bookbinding
 * terms and techniques refer to:
 *  https://en.wikipedia.org/wiki/Bookbinding#Terms_and_techniques
 *  https://www.formaxprinting.com/blog/2016/11/
 *      booklet-layout-how-to-arrange-the-pages-of-a-saddle-stitched-booklet/
 *  https://www.studentbookbinding.co.uk/blog/
 *      how-to-set-up-pagination-section-sewn-bindings
 *
 * The document is processed in groups of 4 pages for each sheet of paper, where
 * each page is captured as a BufferedImage. The 4th page is rotated anti-
 * clockwise and scaled to fit on the bottom half of one side of the sheet. The
 * 1st page is rotated anti-clockwise and scaled to fit on the top half of the
 * same side of the sheet. On the reverse side, the 2nd page is rotated
 * clockwise and scaled to fit on the top half and the 3rd page is rotated
 * clockwise and scaled to fit on the bottom half. This process is repeated for
 * all groups of 4 pages in the source document.
 */
package phillockett65.PDFBookGen;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.util.Matrix;

import phillockett65.Debug.Debug;

/**
 *
 * @author Phil
 */
public class PDFBook {

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    private PDRectangle pageSize = PDRectangle.LETTER;
    private int sheetCount = 1;
    private int firstPage = 0;
    private int lastPage = 0;
    private boolean rotate = true;      // Required?

    private final String sourcePDF;     // The source PDF filepath.
    private final String outputPDF;     // The generated PDF filepath.
    private int maxPage = 0;

    private PDDocument inputDoc;        // The source PDF document.
    private PDDocument outputDoc;       // The generated PDF document.
    private PDPage page;                // Current page of "outputDoc".


    /**
     * Find the number of pages in the source PDF document.
     *
     * @param inPDF file path for source PDF.
     */
    static public int getPDFPageCount(String inPDF) {
        Debug.trace(DD, "getPDFPageCount(" + inPDF + ")");

        PDDocument inputDoc;
        int maxPage = 0;

        try {
            File file = new File(inPDF);
            if (file.exists() == false) {
                return maxPage;
            }

            if (file.isFile() == false) {
                return maxPage;
            }

            inputDoc = PDDocument.load(file);
            maxPage = inputDoc.getNumberOfPages();

            if (inputDoc != null) {
                inputDoc.close();
            }

        } catch (IOException e) {
            Debug.critical(DD, e.getMessage());
        }

        return maxPage;
    }

    /**
     * Constructor.
     *
     * @param inPDF file path for source PDF.
     * @param outPDF file path for generated PDF.
     */
    public PDFBook(String inPDF, String outPDF) {
        Debug.trace(DD, "PDFBook(" + inPDF + ", " + outPDF + ")");
        sourcePDF = inPDF;
        outputPDF = outPDF;

        maxPage = getPDFPageCount(sourcePDF);
        lastPage = maxPage;
    }

    private static PDRectangle getPS(String size) {
        switch (size) {
            case "A0":      return PDRectangle.A0;
            case "A1":      return PDRectangle.A1;
            case "A2":      return PDRectangle.A2;
            case "A3":      return PDRectangle.A3;
            case "A4":      return PDRectangle.A4;
            case "A5":      return PDRectangle.A5;
            case "A6":      return PDRectangle.A6;
            case "Legal":   return PDRectangle.LEGAL;
            case "Letter":  return PDRectangle.LETTER;
            case "Tabloid": return PDRectangle.TABLOID;
        }

        return PDRectangle.LETTER;
    }
    /**
     * System entry point for stand alone, command line version.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            PDFBook booklet = new PDFBook(args[0], args[1]);
            if (args.length > 2) {
                booklet.setPageSize(getPS(args[2]));
            }

            booklet.genBooklet();
        }
    }


    /**
     * Set the size of the page in the output document.
     * 
     * @param size of output page defined by getPS() compatible Strings.
     */
    public void setPageSize(String size) {
        pageSize = getPS(size);
    }

    /**
     * Set the size of the page in the output document.
     * 
     * @param size of standard portrait page defined by PDRectangle.
     */
    public void setPageSize(PDRectangle size) {
        pageSize = size;
    }

    /**
     * Set to number of sheets to be used in a section (4 pages to a sheet).
     * 
     * @param count of sheets in a section (1 to 6).
     */
    public void setSheetCount(int count) {
        sheetCount = count;
    }

    /**
     * Select the first page to be added to the booklet (0 to maxPage).
     * 
     * @param page number of first page starting from 0.
     */
    public void setFirstPage(int page) {
        if (page < 0) {
            firstPage = 0;

            return;
        }

        if (page > maxPage)
            page = maxPage;

        if (page > lastPage)
            lastPage = page;

        firstPage = page;
    }

    /**
     * Select the page after the last page to be added to the booklet (0 to 
     * maxPage).
     * 
     * @param page number of last page not to be exceeded.
     */
    public void setLastPage(int page) {
        if (page > maxPage) {
            lastPage = maxPage;

            return;
        }

        if (page < 0)
            page = 0;

        if (page < firstPage)
            firstPage = page;

        lastPage = page;
    }

    /**
     * Get the current first page number (0 to maxPage).
     * 
     * @return the current first page number. 
     */
    public int getFirstPage() {
        return firstPage;
    }

    /**
     * Get the current last page number (0 to maxPage).
     * 
     * @return the current last page number. 
     */
    public int getLastPage() {
        return lastPage;
    }

    /**
     * Get the number of pages in the source PDF document.
     * 
     * @return number of pages in the PDF.
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * Indicate whether the pages on the reverse side should be rotated in the 
     * opposite direction to the front side of the sheet.
     * 
     * @param flip true if the reverse side should be rotated, false otherwise.
     */
    public void setRotate(boolean flip) {
        rotate = flip;
    }


    /**
     * Generate a booklet style PDF.
     */
    public void genBooklet() {
        try {
            inputDoc = PDDocument.load(new File(sourcePDF));

            try {
                outputDoc = new PDDocument();
                final int MAX = lastPage;
                int last = firstPage;
                for (int first = last; first < MAX; first = last) {
                    last += 4 * sheetCount;
                    if (last > MAX) {
                        last = MAX;
                    }

                    addPDPagesToPdf(first, last);

                    Debug.trace(DD, "Pages " + (first+1) + " to " + last);
                }
                outputDoc.save(outputPDF);
                if (outputDoc != null) {
                    outputDoc.close();
                }
            } catch (IOException e) {
                Debug.critical(DD, e.getMessage());
            }

            if (inputDoc != null) {
                inputDoc.close();
            }

            Debug.info(DD, "File created in: " + outputPDF);
        } catch (IOException e) {
            Debug.critical(DD, e.getMessage());
        }
    }

    /**
     * Add a section of pages to the PDF document.
     *
     * @param fpn first page number to grab from inputDoc (pages start from 0).
     * @param lpn page number for grabbing pages BEFORE reaching the last page.
     */
    private void addPDPagesToPdf(int fpn, int lpn) {

        // Create an array of page numbers from a PDF document.
        int i = 0;
        int[] pages = new int[lpn-fpn];
        for (int target = fpn; target < lpn; ++target) {
            pages[i++] = target;
        }

        // Add pages in pairs to both side of the sheet.
        final int LAST = 4 * sheetCount;
        int first = 0;
        int last = LAST - 1;
        for (int sheet = 0; sheet < sheetCount; ++sheet) {
            addPDPagesToPage(pages, first++, last--, false);
            addPDPagesToPage(pages, last--, first++, rotate);
        }
    }

    /**
     * Add two pages, scale and rotate to fit on portrait 'pageSize' page.
     *
     * @param pages array to be added to document in booklet arrangement.
     * @param right index into pages for the right page.
     * @param left index into pages for the left page.
     * @param flip flag to indicate if the images should be rotated clockwise.
     */
    private void addPDPagesToPage(int[] pages, int right, int left,
            boolean flip) {

        if (add2PagesToPage(pages, right, left)) {
            try {
                PDPage imported = outputDoc.importPage(page);
                addPageToPdf(imported, flip);
            } catch (IOException e) {
            }
        }

    }

    /**
     * Add two pages, side by side, to a single page of a PDF document.
     *
     * @param pages array to be added to document in booklet arrangement.
     * @param right index into pages for the right page.
     * @param left index into pages for the left page.
     */
    private boolean add2PagesToPage(int[] pages, int right, int left) {

        final int count = pages.length;
        boolean lpa = false;
        boolean rpa = false;
        int lpn = 0;
        int rpn = 0;
        if (count > left) {
            lpa = true;
            lpn = pages[left];
        }
        if (count > right) {
            rpa = true;
            rpn = pages[right];
        }
        if ((lpa == false) && (rpa == false))
            return false;

        try {
            // Create output PDF frame.
            PDRectangle lFrame = inputDoc.getPage(lpn).getCropBox();
            PDRectangle rFrame = inputDoc.getPage(rpn).getCropBox();

            final float lw = lFrame.getWidth();
            final float lh = lFrame.getHeight();
            final float rw = rFrame.getWidth();
            final float rh = rFrame.getHeight();

            // Vertically centre the shorter of the two pages.
            float h = lh;
            float lty = 0.0f;
            float rty = 0.0f;

            if (rh > lh) {
                h = rh;
                lty = (rh - lh) / 2;
            }
            else {
                rty = (lh - rh) / 2;
            }
            PDRectangle outPdfFrame = new PDRectangle(lw + rw, h);

            final int idx = outputDoc.getNumberOfPages();

            // Create page with calculated frame and add it to the document.
            COSDictionary dict = new COSDictionary();
            dict.setItem(COSName.TYPE, COSName.PAGE);
            dict.setItem(COSName.MEDIA_BOX, outPdfFrame);
            dict.setItem(COSName.CROP_BOX, outPdfFrame);
            dict.setItem(COSName.ART_BOX, outPdfFrame);
            page = new PDPage(dict);

            // Source PDF pages has to be imported as form XObjects to be able
            // to insert them at a specific point in the output page.
            LayerUtility layer = new LayerUtility(outputDoc);
            PDFormXObject lForm = layer.importPageAsForm(inputDoc, lpn);
            PDFormXObject rForm = layer.importPageAsForm(inputDoc, rpn);

            // Add form objects to output page.
            if (lpa) {
                AffineTransform af = AffineTransform.getTranslateInstance(
                        0.0, lty);
                layer.appendFormAsLayer(page, lForm, af, "left" + idx);
            }
            if (rpa) {
                AffineTransform af = AffineTransform.getTranslateInstance(
                        lw, rty);
                layer.appendFormAsLayer(page, rForm, af, "right" + idx);
            }

            return true;

        } catch (IOException e) {
        }

        return false;
    }

    /**
     * Scale and rotate a landscape page to fit on portrait 'pageSize' page.
     *
     * @param copyPage to add to document (in landscape orientation).
     * @param flip flag to indicate if the images should be rotated clockwise.
     */
    private void addPageToPdf(PDPage copyPage, boolean flip) {

        PDPage outputSize = new PDPage(pageSize);
        PDRectangle outputPage = outputSize.getCropBox();
        PDPageContentStream stream; // Current stream of "outputDoc".

        final double degrees = Math.toRadians(flip ? 270 : 90);
        Matrix matrix = Matrix.getRotateInstance(degrees, 0, 0);

        PDRectangle cropBox = copyPage.getCropBox();
        final float iw = cropBox.getWidth();
        final float ih = cropBox.getHeight();
        final float ow = outputPage.getWidth();
        final float oh = outputPage.getHeight();

        final float sw = ow / ih;
        final float sh = oh / iw;

        float scale;
        float dx = 0.0f;
        float dy = 0.0f;
        if (sw < sh) {
            scale = sw;
            // Centre the pages on the output sheet.
            dx = (oh - (iw * scale)) / (2 * scale);
        }
        else {
            scale = sh;
            // Centre the pages on the output sheet.
            dy = (ow - (ih * scale)) / (2 * scale);
        }

        float tx = iw / 2;
        float ty = ih / 2;

        try {
            stream = new PDPageContentStream(outputDoc, copyPage,
                    PDPageContentStream.AppendMode.PREPEND, false, false);

            stream.transform(Matrix.getTranslateInstance(tx, ty));
            stream.transform(matrix);
            stream.transform(Matrix.getScaleInstance(scale, scale));

            if (flip) {
                ty = tx / scale;
                tx -= (ih - oh) / (2 * scale);
                ty -= dy;
            }
            else {
                tx = ty / scale;
                ty -= (iw - ow) / (2 * scale);
                tx -= dx;
            }

            stream.transform(Matrix.getTranslateInstance(-tx, -ty));

            copyPage.setMediaBox(outputSize.getMediaBox());
            copyPage.setCropBox(outputSize.getCropBox());

            stream.close();
        } catch (IOException e) {
        }

    }

}
