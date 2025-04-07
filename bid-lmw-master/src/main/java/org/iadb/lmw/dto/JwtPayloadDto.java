package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JwtPayloadDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

}
