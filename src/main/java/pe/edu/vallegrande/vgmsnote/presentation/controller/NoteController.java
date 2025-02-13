package pe.edu.vallegrande.vgmsnote.presentation.controller;

import org.springframework.web.bind.annotation.*;

import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vgmsnote.application.service.NoteService;
import pe.edu.vallegrande.vgmsnote.domain.dto.CapacityDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.CompetenceDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.DidacticUnit;
import pe.edu.vallegrande.vgmsnote.domain.dto.NoteDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudentDetailDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudentDto;
import pe.edu.vallegrande.vgmsnote.domain.model.Note;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudyProgramDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@CrossOrigin(origins = "*", methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
@RequestMapping("/teacher/${api.version}/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping("/create")
    public Mono<Note> createNote(@RequestBody Note note) {
        return noteService.createNote(note);
    }

    @PutMapping("/update/{id}")
    public Mono<Note> updateNote(@PathVariable String id, @RequestBody Note note) {
        return noteService.updateNote(id, note);
    }

    // Listar estudiantes por aula
    @GetMapping("/classroom/{classroomId}/students")
    public Flux<StudentDto> getStudentsByClassroom(@PathVariable String classroomId) {
        return noteService.getStudentsByClassroom(classroomId);
    }

    // Listar Notas por Aula
    @GetMapping("/classroom/{classroomId}/notes")
    public Flux<NoteDto> getNotesByClassroom(@PathVariable String classroomId) {
        return noteService.getNotesByClassroom(classroomId);
    }

    // Listar Notas por Estudiante
    @GetMapping("/student/{studentId}/notes")
    public Flux<Note> getNotesByStudent(@PathVariable String studentId) {
        return noteService.getNotesByStudent(studentId);
    }

    // Listar uniddades didacticas por aula
    @GetMapping("/classroom/{classroomId}/didactic-units")
    public Flux<DidacticUnit> getDidacticUnitsByClassroom(@PathVariable String classroomId) {
        return noteService.getDidacticUnitsByClassroom(classroomId);
    }

    // Listar competencias por unidad didactica
    @GetMapping("/study-programs")
    public Flux<StudyProgramDto> getStudyPrograms() {
        return noteService.getStudyPrograms();
    }

    @GetMapping("/study-program/{programId}/didactic-units")
    public Flux<DidacticUnit> getDidacticUnitsByProgram(@PathVariable String programId) {
        return noteService.getDidacticUnitsByProgram(programId);
    }

    @GetMapping("/didactic-unit/{didacticUnitId}/classrooms")
    public Flux<ClassroomDto> getClassroomsByDidacticUnit(@PathVariable String didacticUnitId) {
        return noteService.getClassroomsByDidacticUnit(didacticUnitId);
    }

    @GetMapping("/didactic-unit/{didacticUnitId}/competencies")
    public Flux<CompetenceDto> getCompetenciesByDidacticUnit(@PathVariable String didacticUnitId) {
        return noteService.getCompetenciesByDidacticUnit(didacticUnitId);
    }

    @GetMapping("/competence/{competenceId}/capacities")
    public Flux<CapacityDto> getCapacitiesByCompetence(@PathVariable String competenceId) {
        return noteService.getCapacitiesByCompetence(competenceId);
    }

    @GetMapping("/study-program/{programId}/classrooms")
    public Flux<ClassroomDto> getClassroomsByProgram(@PathVariable String programId) {
        return noteService.getClassroomsByProgram(programId);
    }

    @GetMapping("/list/active")
    public Flux<NoteDto> getAllActiveNotes() {
        return noteService.findByStatus("A");
    }

    @GetMapping("/list/inactive")
    public Flux<NoteDto> getAllInactiveNotes() {
        return noteService.findByStatus("I");
    }

    @GetMapping("/classroom/{classroomId}/student-details")
    public Flux<StudentDetailDto> getStudentDetailsByClassroom(@PathVariable String classroomId) {
        return noteService.getStudentDetailsByClassroom(classroomId);
    }

    @PostMapping("/student/{studentId}/send-notes")
    public Mono<Void> sendNotesByEmail(@PathVariable String studentId, @RequestParam String email) {
        return noteService.sendStudentNotesByEmail(studentId, email);
    }

}
