package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;

@Data
public class StudentDto {
    private String id;
    private String documentType;
    private String documentNumber;
    private String lastNamePaternal;
    private String lastNameMaternal;
    private String email;
    private String names;
}
