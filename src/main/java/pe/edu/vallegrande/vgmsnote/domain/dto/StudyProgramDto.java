package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class StudyProgramDto {
 
    @Id
    private String programId;
    private String name;
    private String module;
    private String cetproId;
}
