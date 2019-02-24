package com.android_quiz_game.model;

import java.util.List;

public class Csv {
    private List _getContent;
    private String[] _getHeader;

    public Csv(List csvContent, String[] header) {
        _getContent = csvContent;
        _getHeader = header;
    }

    public String[] getHeader() {
        return _getHeader;
    }

    public List<String[]> getContent() {
        return _getContent;
    }
}
