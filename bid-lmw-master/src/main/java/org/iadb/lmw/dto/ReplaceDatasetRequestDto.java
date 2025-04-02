package org.iadb.lmw.dto;

import java.io.Serial;
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
public class ReplaceDatasetRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank(message = "fileName no debe estár vacío")
    private String fileName;
    @NotBlank(message = "comment no debe estár vacío")
    private String comment;

}
