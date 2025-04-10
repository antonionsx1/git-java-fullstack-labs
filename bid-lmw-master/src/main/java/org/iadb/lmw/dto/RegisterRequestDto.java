package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@ToString
public class RegisterRequestDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "fullName no debe estár vacío")

    private String fullName;

    @NotBlank(message = "email no debe estár vacío")
    private String email;

    @NotBlank(message = "auth no debe estár vacío")
    private String auth;

    @NotNull(message = "super no debe estár vacío")
    private Boolean su;

}