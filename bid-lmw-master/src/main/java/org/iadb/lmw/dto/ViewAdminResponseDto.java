package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ViewAdminResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String fullName;
    
    private String email;
    private String auth;
    private String profile;
    private Date regDate;

}
