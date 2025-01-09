package org.iadb.lmw.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

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
public class QueryRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    
    private Boolean useDistinct;
    private Boolean usePagination;
    private Boolean useDates;
    private String startDate;
    private String endDate;
    private Integer pageOffset;
    private List<FilterDTO> filters;

}
