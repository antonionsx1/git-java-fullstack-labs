package org.iadb.lmw.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
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
public class CreateCommentRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "comment no debe estár vacío")
    private String comment;

}
