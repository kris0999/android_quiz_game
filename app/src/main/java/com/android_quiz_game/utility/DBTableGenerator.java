package com.android_quiz_game.utility;

import com.android_quiz_game.model.Csv;

import java.util.List;

public class DBTableGenerator {
    public static DBTableGenerator Instance = new DBTableGenerator();

    public String createTable(String tableName, Csv csvObject, boolean autoIncrement)
    {
        List<String[]> _csvContent = csvObject.getContent();
        String[] sampleData = _csvContent.get(0);
        String sqlCreate = String.format("CREATE TABLE IF NOT EXISTS %s (", tableName);
        for (int i = 0; i < csvObject.getHeader().length; i++) {
            String sqlDataType;

            try {
                Integer.parseInt(sampleData[i]);
                sqlDataType = "INTEGER";
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                sqlDataType = "TEXT";
            }

            if (i == csvObject.getHeader().length - 1) {
                sqlCreate += String.format("%s %s)", csvObject.getHeader()[i], sqlDataType);
            }
            else if (i == 0) {
                if (autoIncrement) {
                    sqlCreate += String.format("%s INTEGER PRIMARY KEY AUTOINCREMENT, ", csvObject.getHeader()[i]);
                }
                else {
                    sqlCreate += String.format("%s INTEGER PRIMARY KEY, ", csvObject.getHeader()[i]);
                }
            }
            else {
                sqlCreate += String.format("%s %s, ", csvObject.getHeader()[i], sqlDataType);
            }
        }

        return sqlCreate;
    }
}
