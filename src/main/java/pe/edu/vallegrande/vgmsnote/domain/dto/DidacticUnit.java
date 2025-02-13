package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class DidacticUnit {
    @Id
    private String didacticId;
    private String name;
    private String credit;
    private String hours;
    private String condition;
    private String correction;
    private String status;
    private String studyProgramId;
}
