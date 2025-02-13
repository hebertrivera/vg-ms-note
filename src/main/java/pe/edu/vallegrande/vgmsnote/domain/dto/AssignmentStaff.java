package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class AssignmentStaff {

    @Id
    private String assignmentId;
    private String teacher;
    private String didacticUnit;
    private String status;

}
