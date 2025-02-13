package pe.edu.vallegrande.vgmsnote.domain.dto;

import java.util.List;

import lombok.Data;

@Data
public class ClassroomDto {
    private String classroomId;
    private String name;
    private Header Header;
    private List<EnrollmentDetailDto> enrollmentDetailId;
    private int capacity;
    private String status;
}
