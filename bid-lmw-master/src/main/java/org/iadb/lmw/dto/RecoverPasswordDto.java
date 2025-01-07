package org.iadb.lmw.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class RecoverPasswordDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotBlank(message = "email no debe estar vacío")
    private String email;
}
