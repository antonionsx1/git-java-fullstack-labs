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
public class CreateCountryRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotBlank(message = "nameEs no debe estár vacío")
    private String nameEs;

    @NotBlank(message = "nameEn no debe estár vacío")
    private String nameEn;

    @NotBlank(message = "flag no debe estár vacío")
    private String flag;

    @NotBlank(message = "code no debe estár vacío")
    private String code;
    @NotBlank(message = "color no debe estár vacío")
    private String color;
}
