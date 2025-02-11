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
 * Signature is a class that encapsulates the necessary calculations when 
 * changes to the selected first source page, last source page or the count of 
 * sheets in a signature are made. A source page is a page from the source 
 * document. The generated document has 2 source pages on each side of each 
 * sheet of paper, so there are 4 source pages on each printed sheet.
 */
package phillockett65.PDFBookGen;

public class Signature {
    /**
     * Number of source pages in the generated document.
     */
    public final int pageCount;

    /**
     * Number of sheets of paper needed for the generated document.
     */
    public final int sheetCount;

    /**
     * Number of source pages in a signature.
     */
    public final int sigPageCount;

    /**
     * Number of signatures that will be generated.
     */
    public final int sigCount;

    /**
     * Source page number that the last signature starts with.
     */
    public final int lastSigFirstPage;

    /**
     * Number of source pages in the last signature.
     */
    public final int lastSigPageCount;

    /**
     * Number of blank pages in the last signature.
     */
    public final int lastSigBlankCount;

    /**
     * Calculate the number of sheets of paper needed for the generated 
     * document.
     * @param sigSize number of sheets of paper in each signature.
     * @return the calculated sheet count.
     */
    private int calculateSheetCount(int sigSize) {
        int count;

        // May only need a partial last signature.
        if (lastSigPageCount < (sigPageCount / 2)) {
            count = (sigCount-1) * sigSize;
            count += (lastSigPageCount+1) / 2;
        } else {
            count = sigCount * sigSize;
        }

        return count;
    }

    /**
     * Construct a Signature and populate it with the required data.
     * @param sigSize number of sheets of paper in each signature.
     * @param firstPage from the source document to be included.
     * @param lastPage from the source document to be included.
     */
    public Signature(int sigSize, int firstPage, int lastPage)
    {
        final int pageDiff = lastPage - firstPage;
        pageCount = pageDiff + 1;
        sigPageCount = sigSize * 4;
        final int fullSigCount = pageDiff / sigPageCount;
        final int fullSigPageCount = fullSigCount * sigPageCount;
        sigCount = fullSigCount + 1;
        lastSigFirstPage = firstPage + fullSigPageCount;
        lastSigPageCount = pageCount - fullSigPageCount;
        lastSigBlankCount = sigPageCount - lastSigPageCount;

        sheetCount = calculateSheetCount(sigSize);
    }

}
