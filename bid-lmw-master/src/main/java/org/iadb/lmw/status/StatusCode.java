package org.iadb.lmw.status;

public enum StatusCode {

    OK(100),
    DB_COLUMNS_MISMATCH(101),
    DB_BACKUP_ERROR(102),
    DB_BACKUP_NOT_FOUND(103),
    DB_REPLACEMENT_ERROR(104),
    DB_FILE_NOT_FOUND(105),
    DB_TABLE_NOT_FOUND(106),
    DB_UPDATE_ERROR(107),
    DB_BAD_QUERY_REQUEST(108),
    DB_SAVE_ERROR(109);

    private int code;

    StatusCode(int code) {
        this.setCode(code);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
