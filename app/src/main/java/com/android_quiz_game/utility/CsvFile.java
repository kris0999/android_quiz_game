package com.android_quiz_game.utility;

import com.android_quiz_game.model.Csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvFile {
    public static CsvFile Instance = new CsvFile();

    public Csv read(InputStream inputStream){
        List resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                String[] row = csvLine.split("\t");
                resultList.add(row);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: "+e);
            }
        }

        List csvContent = new ArrayList();
        for (int i = 1; i < resultList.size(); i++) {
            csvContent.add(resultList.get(i));
        }

        List<String[]> header = resultList;

        return new Csv(csvContent, header.get(0));
    }
}
