package pe.edu.vallegrande.vgmsnote.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "note")
public class Note {
    @Id
    private String id;
    private String studentId;
    private String classroomId;
    private String didacticUnitId;
    private String competenceId; // Puedes agregar esto si usas competencias.
    private String capacityId; // Si deseas validar capacidades específicas.
    private Double grade; // Nota entre 0 y 20.
    private String gradeStatus; // A, B, C, AD según los rangos.
    private String status; // Activo (A) o Inactivo (I).
    private LocalDateTime assignmentDate;
}
