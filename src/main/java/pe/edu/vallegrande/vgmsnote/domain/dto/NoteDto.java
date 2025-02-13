package pe.edu.vallegrande.vgmsnote.domain.dto;

import java.time.LocalDateTime;

import lombok.Data;


@Data
public class NoteDto {
    private String id;
    private StudentDto studentId;
    private ClassroomDto classroomId;
    private DidacticUnit didacticUnitId;
    private CompetenceDto competenceId; // Puedes agregar esto si usas competencias.
    private CapacityDto capacityId; // Si deseas validar capacidades específicas.
    private Double grade; // Nota entre 0 y 20.
    private String gradeStatus; // A, B, C, AD según los rangos.
    private String status; // Activo (A) o Inactivo (I).
    private LocalDateTime assignmentDate;
}
