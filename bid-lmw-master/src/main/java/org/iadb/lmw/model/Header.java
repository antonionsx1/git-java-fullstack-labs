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

    /**
     *
     * @param dataBaseHeader
     * @param frontEndHeader
     */
    public Header(String dataBaseHeader, String frontEndHeader) {
        this.dataBaseHeader = dataBaseHeader;
        this.frontEndHeader = frontEndHeader;
    }

    /**
     *
     * @return
     */
    public String getDataBaseHeader() {
        return dataBaseHeader;
    }

    /**
     *
     * @param dataBaseHeader
     */
    public void setDataBaseHeader(String dataBaseHeader) {
        this.dataBaseHeader = dataBaseHeader;
    }

    /**
     *
     * @return
     */
    public String getFrontEndHeader() {
        return frontEndHeader;
    }

    /**
     *
     * @param frontEndHeader
     */
    public void setFrontEndHeader(String frontEndHeader) {
        this.frontEndHeader = frontEndHeader;
    }

}
