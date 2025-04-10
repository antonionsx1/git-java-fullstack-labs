package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReadCommentResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Long> readCommentsIds;
    private List<Long> notFoundCommentsIds;
    private String message;

}
