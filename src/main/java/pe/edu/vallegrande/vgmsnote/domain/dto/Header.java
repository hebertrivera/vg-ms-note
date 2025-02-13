package pe.edu.vallegrande.vgmsnote.domain.dto;

import lombok.Data;

@Data
public class Header {
    private String academicPeriodId;
    private String academicPeriodName;
    private String academicPeriodStatus;
    //
    private String programId;
    private String programName;
    private String programModule;
    private String programStatus;
    //
    private String didacticId;
    private String didacticName;
    private String didacticProgramId;
    private String didacticStatus;
}