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
 * DataStore is a class that serializes the settings data for saving and 
 * restoring to and from disc.
 */
package phillockett65.PDFBookGen;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import phillockett65.Debug.Debug;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    // Debug delta used to adjust the local logging level.
    private static final int DD = 0;

    public Double mainX;
    public Double mainY;
    public Double helpX;
    public Double helpY;

    public String sourceDocument;
    public String outputFileName;
    public String outputFilePath;

    public String paperSize;
    public Boolean rotateCheck;
    public Integer firstPage;
    public Integer lastPage;

    public Integer sigSize;



    /************************************************************************
     * Support code for the Initialization of the DataStore.
     */

    public DataStore() {
    }



    /************************************************************************
     * Support code for static public interface.
     */

    /**
     * Static method that receives a populated DataStore and writes it to disc.
     * @param dataStore contains the data.
     * @param settingsFile path of the settings data file.
     * @return true if data successfully written to disc, false otherwise.
     */
    public static boolean writeData(DataStore dataStore, String settingsFile) {
        boolean success = false;

        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(settingsFile));

            objectOutputStream.writeObject(dataStore);
            success = true;
        } catch (IOException e) {
            // e.printStackTrace();
            Debug.critical(DD, e.getMessage());
        }

        return success;
    }


    /**
     * Static method that instantiates a DataStore, populates it from disc 
     * and returns it.
     * @param settingsFile path of the settings data file.
     * @return a populated DataStore if data successfully read from disc, null otherwise.
     */
    public static DataStore readData(String settingsFile) {
        DataStore dataStore = null;

        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(new FileInputStream(settingsFile));

            dataStore = (DataStore)objectInputStream.readObject();
        } catch (IOException e) {
            Debug.critical(DD, e.getMessage());
        } catch (ClassNotFoundException e) {
            Debug.critical(DD, e.getMessage());
        }

        return dataStore;
    }


    /************************************************************************
     * Support code for debug.
     */

    /**
     * Print data store on the command line.
     */
    public void dump() {
        Debug.info(DD, "");
        Debug.info(DD, "main position (" + mainX + ", " + mainY + ")");
        Debug.info(DD, "help position (" + helpX + ", " + helpY + ")");
        Debug.info(DD, "");
        Debug.info(DD, "sourceDocument = " + sourceDocument);
        Debug.info(DD, "outputFileName = " + outputFileName);
        Debug.info(DD, "outputFilePath = " + outputFilePath);
        Debug.info(DD, "");
        Debug.info(DD, "paperSize = " + paperSize);
        Debug.info(DD, "rotateCheck = " + rotateCheck);
        Debug.info(DD, "firstPage = " + firstPage);
        Debug.info(DD, "lastPage = " + lastPage);
        Debug.info(DD, "");
        Debug.info(DD, "sigSize = " + sigSize);
        Debug.info(DD, "");
    }

}

