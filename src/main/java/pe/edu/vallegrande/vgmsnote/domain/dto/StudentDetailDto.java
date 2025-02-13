package pe.edu.vallegrande.vgmsnote.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class StudentDetailDto {
    private String studentId;
    private String name;
    private DidacticUnit didacticUnit;
    private List<CompetenceDto> competencies;
    private List<CapacityDto> capacities;
}
