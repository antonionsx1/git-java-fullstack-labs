package org.iadb.lmw.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.ToString;

/**
 *
 */
@Entity
@ToString
public class Header {

    @Id
    private String dataBaseHeader;

    private String frontEndHeader;

    /**
     * 
     */
    public Header() {
    }

    public Header(String dataBaseHeader, String frontEndHeader) {
        this.dataBaseHeader = dataBaseHeader;
        this.frontEndHeader = frontEndHeader;
    }

    public String getDataBaseHeader() {
        return dataBaseHeader;
    }

    public void setDataBaseHeader(String dataBaseHeader) {
        this.dataBaseHeader = dataBaseHeader;
    }

    public String getFrontEndHeader() {
        return frontEndHeader;
    }

    public void setFrontEndHeader(String frontEndHeader) {
        this.frontEndHeader = frontEndHeader;
    }

}
