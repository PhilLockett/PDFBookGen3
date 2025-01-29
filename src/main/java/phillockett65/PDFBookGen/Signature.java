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
 * 
 * Available calculated values are:
 *   o Number of source pages in the generated document
 *   o Number of source pages in a signature
 *   o Number of signatures that will be generated
 *   o Source page number that the last signature starts with
 *   o Number of source pages in the last signature
 *   o Number of blank pages in the last signature
 */
package phillockett65.PDFBookGen;

public class Signature {
    private final int pageCount;
    private final int sigPageCount;
    private final int sigCount;
    private final int lastSigFirstPage;
    private final int lastSigPageCount;
    private final int lastSigBlankCount;

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
    }

    /**
     * @return the number of source pages in the generated document.
     */
    public int getOutputPageCount() { return pageCount; }

    /**
     * @return the number of source pages in a signature.
     */
    public int getSigPageCount() { return sigPageCount; }

    /**
     * @return the number of signatures that will be generated.
     */
    public int getSigCount() { return sigCount; }

    /**
     * @return the source page number that the last signature starts with.
     */
    public int getLastSigFirstPage() { return lastSigFirstPage; }

    /**
     * @return the number of source pages in the last signature.
     */
    public int getLastSigPageCount() { return lastSigPageCount; }

    /**
     * @return the number of blank pages in the last signature.
     */
    public int getLastSigBlankCount() { return lastSigBlankCount; }

}
