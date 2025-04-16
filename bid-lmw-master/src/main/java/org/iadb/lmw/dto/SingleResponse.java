package org.iadb.lmw.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @param <T>
 */
@Getter
@Setter
@AllArgsConstructor
@ToString
public class SingleResponse<T> {

    private Integer code;
    private T data;

    public SingleResponse() {
        super();
    }

}
