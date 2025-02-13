package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;

@Data
public class CompetenceDto {
    private String competencyId;
    private String name;
    private String description;
    private String status;
    private String didacticUnitId;
}
