package org.iadb.lmw.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class UpdateCountryRequestDto implements Serializable {

    private static final long serialVersionUID = 1L;
    @NotNull(message = "id no debe estár vacío")
    private Integer id;
    @NotBlank(message = "color no debe estár vacío")
    private String color;
}
