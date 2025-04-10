package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class ReadCommentsRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Long> ids;

}
