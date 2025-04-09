package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 *
 */
@Setter
@NoArgsConstructor
@ToString
public class ViewCommentResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String commment;
    private Boolean read;
    private Date regDate;

}
