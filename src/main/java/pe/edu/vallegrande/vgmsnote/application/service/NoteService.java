package pe.edu.vallegrande.vgmsnote.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vgmsnote.domain.dto.CapacityDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.CompetenceDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.DidacticUnit;
import pe.edu.vallegrande.vgmsnote.domain.dto.EnrollmentDetailDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.NoteDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudentDetailDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudentDto;
import pe.edu.vallegrande.vgmsnote.domain.model.Note;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudyProgramDto;
import pe.edu.vallegrande.vgmsnote.domain.repository.NoteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final ExternalService externalService;

    public NoteService(NoteRepository noteRepository, ExternalService externalService) {
        this.noteRepository = noteRepository;
        this.externalService = externalService;
    }

    @Value("${services.classroom.url}")
    private String classroomServiceUrl;

    public Flux<NoteDto> findByStatus(String status) {
        log.info("Buscando notas con estado {}", status);
        return noteRepository.findByStatus(status)
                .flatMap(this::convertToDto)
                .collectList()
                .flatMapMany(Flux::fromIterable);
    }

    public Mono<Note> createNote(Note note) {
        note.setAssignmentDate(LocalDateTime.now());
        log.info("Intentando crear nota: {}", note);
    
        return noteRepository.findByStudentIdAndCapacityId(note.getStudentId(), note.getCapacityId())
                .flatMap(existingNote -> {
                    if (existingNote != null) {
                        log.warn("El estudiante ya tiene una nota para esta capacidad");
                        return Mono.error(new IllegalArgumentException("El estudiante ya tiene una nota para esta capacidad"));
                    }
                    return validateStudentInClassroom(note)
                            .flatMap(isValid -> {
                                if (!isValid) {
                                    log.warn("El estudiante no pertenece al aula o unidad didáctica");
                                    return Mono.error(new IllegalArgumentException("El estudiante no pertenece al aula o unidad didáctica"));
                                }
                                return validateCompetenceAndCapacity(note)
                                        .flatMap(isCompetenceValid -> {
                                            if (!isCompetenceValid) {
                                                log.warn("La competencia o capacidad no es válida para la unidad didáctica");
                                                return Mono.error(new IllegalArgumentException("La competencia o capacidad no es válida para la unidad didáctica"));
                                            }
                                            note.setGradeStatus(determineGradeStatus(note.getGrade()));
                                            note.setStatus("A");
                                            log.info("Guardando nota: {}", note);
                                            return noteRepository.save(note);
                                        });
                            });
                })
                .switchIfEmpty(Mono.defer(() -> {
                    note.setGradeStatus(determineGradeStatus(note.getGrade()));
                    note.setStatus("A");
                    log.info("Guardando nueva nota: {}", note);
                    return noteRepository.save(note);
                }));
    }

    public Mono<Note> updateNote(String id, Note updatedNote) {
        log.info("Actualizando nota con ID: {}", id);
        return noteRepository.findById(id)
                .flatMap(existingNote -> {
                    existingNote.setGrade(updatedNote.getGrade());
                    existingNote.setGradeStatus(determineGradeStatus(updatedNote.getGrade()));
                    return noteRepository.save(existingNote);
                });
    }

    public Flux<Note> getNotesByStudent(String studentId) {
        return noteRepository.findByStudentId(studentId);
    }

    public Flux<ClassroomDto> getClassroomsByProgram(String programId) {
        return externalService.getClassroomsByStudyProgram(programId)
                .doOnNext(classroom -> log.info("Classroom fetched: {}", classroom))
                .onErrorResume(e -> {
                    log.error("Error fetching classrooms for study program: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    // Método para obtener todos los estudiantes de un aula
    public Flux<StudentDto> getStudentsByClassroom(String classroomId) {
        return externalService.getClassroomById(classroomId)
                .doOnNext(classroom -> log.info("Fetched classroom: {}", classroom))
                .flatMapMany(classroom -> Flux.fromIterable(classroom.getEnrollmentDetailId())
                        .map(EnrollmentDetailDto::getStudent)
                        .doOnNext(student -> log.info("Found student: {}", student))
                        .doOnError(e -> log.error("Error processing students for classroom: {}", classroomId, e)));
    }

    public Flux<NoteDto> getNotesByClassroom(String classroomId) {
        return noteRepository.findByClassroomId(classroomId)
                .flatMap(this::convertToDto)
                .doOnNext(noteDto -> log.info("Note found: {}", noteDto))
                .doOnError(e -> log.error("Error fetching notes for classroom ID: {}", classroomId, e));
    }

    public Flux<DidacticUnit> getDidacticUnitsByClassroom(String classroomId) {
        return externalService.getClassroomById(classroomId)
                .flatMapMany(classroom -> Flux.fromIterable(classroom.getEnrollmentDetailId()))
                .flatMap(enrollmentDetail -> Flux.fromIterable(enrollmentDetail.getDidacticUnit()))
                .distinct()
                .flatMap(didacticUnit -> externalService.validateDidacticUnit(didacticUnit.getDidacticId()));
    }

    public Flux<StudyProgramDto> getStudyPrograms() {
        return externalService.getAllStudyPrograms();
    }

    public Flux<DidacticUnit> getDidacticUnitsByProgram(String programId) {
        return externalService.getDidacticUnitsByProgram(programId);
    }

    public Flux<ClassroomDto> getClassroomsByDidacticUnit(String didacticUnitId) {
        return externalService.getClassroomsByDidacticUnit(didacticUnitId)
                .flatMap(classroom -> {
                    return externalService.getStudentsByClassroom(classroom.getClassroomId())
                            .collectList()
                            .map(students -> {
                                List<EnrollmentDetailDto> enrollmentDetails = students.stream()
                                        .map(student -> {
                                            EnrollmentDetailDto enrollmentDetail = new EnrollmentDetailDto();
                                            enrollmentDetail.setStudent(student);
                                            return enrollmentDetail;
                                        })
                                        .collect(Collectors.toList());

                                classroom.setEnrollmentDetailId(enrollmentDetails);
                                return classroom;
                            });
                });
    }

    public Flux<CompetenceDto> getCompetenciesByDidacticUnit(String didacticUnitId) {
        return externalService.getCompetenciesByDidacticUnit(didacticUnitId);
    }

    public Flux<CapacityDto> getCapacitiesByCompetence(String competenceId) {
        return externalService.getCapacitiesByCompetence(competenceId);
    }

    public Flux<DidacticUnit> getDidacticUnitsByClassrooms(String classroomId) {
        return externalService.getDidacticUnitsByClassroom(classroomId);
    }

    public Flux<ClassroomDto> getClassroomsByStudyProgram(String studyProgramId) {
        return externalService.getClassroomsByStudyProgram(studyProgramId);
    }

    // validadciones
    // Método para validar si un estudiante pertenece a un aula
    private Mono<Boolean> validateStudentInClassroom(Note note) {
        return externalService.getClassroomById(note.getClassroomId())
                .flatMap(classroom -> {
                    boolean isStudentValid = classroom.getEnrollmentDetailId().stream()
                            .anyMatch(detail -> detail.getStudent().getId().equals(note.getStudentId()));
                    return Mono.just(isStudentValid);
                });
    }

    // Validar Competencia y Capacidad
    private Mono<Boolean> validateCompetenceAndCapacity(Note note) {
        return externalService.findCompetencyById(note.getCompetenceId())
                .flatMap(competence -> {
                    if (!competence.getDidacticUnitId().equals(note.getDidacticUnitId())) {
                        return Mono.just(false);
                    }
                    return externalService.findCapacityById(note.getCapacityId())
                            .map(capacity -> capacity.getCompetencyId().equals(note.getCompetenceId()));
                });
    }

    private Mono<NoteDto> convertToDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setGrade(note.getGrade());
        dto.setGradeStatus(note.getGradeStatus());
        dto.setStatus(note.getStatus());

        Mono<ClassroomDto> classroomDto = externalService.getClassroomById(note.getClassroomId());
        Mono<StudentDto> studentDto = externalService.findStudentById(note.getStudentId());
        Mono<DidacticUnit> didacticUnit = externalService.validateDidacticUnit(note.getDidacticUnitId());
        Mono<CompetenceDto> competenceDto = externalService.findCompetencyById(note.getCompetenceId());
        Mono<CapacityDto> capacityDto = externalService.findCapacityById(note.getCapacityId());

        return Mono.zip(studentDto, didacticUnit, competenceDto, capacityDto, classroomDto)
                .map(tuple -> {
                    dto.setStudentId(tuple.getT1());
                    dto.setDidacticUnitId(tuple.getT2());
                    dto.setCompetenceId(tuple.getT3());
                    dto.setCapacityId(tuple.getT4());
                    dto.setClassroomId(tuple.getT5());
                    return dto;
                });
    }

    public Flux<StudentDetailDto> getStudentDetailsByClassroom(String classroomId) {
        return externalService.getStudentsByClassroom(classroomId)
            .flatMap(student -> externalService.getDidacticUnitsByClassroom(classroomId)
                .next()
                .flatMapMany(didacticUnit -> {
                    Flux<CompetenceDto> competenciesFlux = externalService.getCompetenciesByDidacticUnit(didacticUnit.getDidacticId());
                    Flux<CapacityDto> capacitiesFlux = competenciesFlux.flatMap(competence -> externalService.getCapacitiesByCompetence(competence.getCompetencyId()));
    
                    return Mono.zip(Mono.just(student), Mono.just(didacticUnit), competenciesFlux.collectList(), capacitiesFlux.collectList())
                        .map(tuple -> {
                            StudentDetailDto detail = new StudentDetailDto();
                            detail.setStudentId(student.getId());
                            detail.setName(student.getNames() + " " + student.getLastNamePaternal() + " " + student.getLastNameMaternal());
                            detail.setDidacticUnit(tuple.getT2());
                            detail.setCompetencies(tuple.getT3());
                            detail.setCapacities(tuple.getT4());
                            return detail;
                        });
                })
            );
    }

    public Mono<Void> sendStudentNotesByEmail(String studentId, String email) {
        return getNotesByStudent(studentId)
                .collectList()
                .flatMap(notes -> {
                    if (notes.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("No se encontraron notas para el estudiante."));
                    }
                    StringBuilder message = new StringBuilder("Aquí están tus notas:\n");
                    return Flux.fromIterable(notes)
                        .flatMap(note -> externalService.validateDidacticUnit(note.getDidacticUnitId())
                            .map(didacticUnit -> {
                                message.append("Asignatura: ").append(didacticUnit.getName())
                                       .append(", Nota: ").append(note.getGrade())
                                       .append(", Estado: ").append(note.getGradeStatus()).append("\n");
                                return note;
                            }))
                        .then(Mono.just(message.toString()));
                })
                .then();
    }

    private String determineGradeStatus(Double grade) {
        if (grade >= 18) {
            return "A";
        } else if (grade >= 15) {
            return "B";
        } else if (grade >= 12) {
            return "C";
        } else {
            return "D";
        }
    }

}
