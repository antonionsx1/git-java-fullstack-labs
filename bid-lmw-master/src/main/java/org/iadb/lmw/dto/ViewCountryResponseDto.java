package org.iadb.lmw.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ViewCountryResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;

    private String flag;

    private String code;

    private String color;

}
