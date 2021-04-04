package tp1.api.server.resources;

import tp1.api.Spreadsheet;
import tp1.api.service.rest.RestSpreadsheets;

import java.util.List;

//TODO

public class SheetResource implements RestSpreadsheets {
    @Override
    public String createSpreadsheet(Spreadsheet sheet, String password) {
        return null;
    }

    @Override
    public void deleteSpreadsheet(String sheetId, String password) {

    }

    @Override
    public Spreadsheet getSpreadsheet(String sheetId, String userId, String password) {
        return null;
    }

    @Override
    public List<List<String>> getSpreadsheetValues(String sheetId, String userId, String password) {
        return null;
    }

    @Override
    public void updateCell(String sheetId, String cell, String rawValue, String userId, String password) {

    }

    @Override
    public void shareSpreadsheet(String sheetId, String userId, String password) {

    }

    @Override
    public void unshareSpreadsheet(String sheetId, String userId, String password) {

    }
}
