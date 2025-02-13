package pe.edu.vallegrande.vgmsnote.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vgmsnote.domain.dto.CapacityDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.ClassroomDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.CompetenceDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.DidacticUnit;
import pe.edu.vallegrande.vgmsnote.domain.dto.EnrollmentDetailDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudentDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.StudyProgramDto;
import pe.edu.vallegrande.vgmsnote.domain.dto.EmailResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ExternalService {

    @Value("${services.classroom.url}")
    private String classroomServiceUrl;

    @Value("${services.capacity.url}")
    private String capacityServiceUrl;

    @Value("${services.competence.url}")
    private String competenceServiceUrl;

    @Value("${services.didactic-unit.url}")
    private String didacticUnitServiceUrl;

    @Value("${services.student.url}")
    private String studentServiceUrl;

    @Value("${services.studyProgram.url}")
    private String studyProgramUrl;

    @Value("${services.email.url}")
    private String emailServiceUrl;

    private final WebClient.Builder webClientBuilder;

    public ExternalService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<DidacticUnit> validateDidacticUnit(String didacticUnitId) {
        return fetchData(didacticUnitServiceUrl + "/",
                didacticUnitId, DidacticUnit.class);
    }

    public Mono<CompetenceDto> findCompetencyById(String competencyId) {
        return fetchData(competenceServiceUrl + "/",
                competencyId, CompetenceDto.class);
    }

    public Mono<CapacityDto> findCapacityById(String capacityId) {
        return fetchData(capacityServiceUrl + "/list/",
                capacityId, CapacityDto.class);
    }

    public Mono<StudentDto> findStudentById(String studentId) {
        return fetchData(studentServiceUrl + "/list/",
                studentId, StudentDto.class);
    }

    public Mono<CompetenceDto> getCompetenceById(String id) {
        return fetchData(classroomServiceUrl + "/competencies/", id, CompetenceDto.class);
    }

    // Cambiar el método a public para que sea accesible
    public Mono<ClassroomDto> getClassroomById(String classroomId) {
        return fetchData(classroomServiceUrl + "/findById/", classroomId, ClassroomDto.class);
    }

    private <T> Mono<T> fetchData(String baseUrl, String id, Class<T> responseType) {
        log.info("Fetching data from {} with ID {}", baseUrl, id);
        return webClientBuilder.build()
                .get()
                .uri(baseUrl + id)
                .retrieve()
                .bodyToMono(responseType)
                .doOnNext(data -> log.info("Fetched data: {}", data))
                .onErrorResume(e -> {
                    log.error("Error fetching data: ", e);
                    return Mono.empty();
                });
    }

    public Flux<StudyProgramDto> getAllStudyPrograms() {
        return webClientBuilder.build()
                .get()
                .uri(studyProgramUrl + "/list/active")
                .retrieve()
                .bodyToFlux(StudyProgramDto.class)
                .doOnNext(program -> log.info("Study program fetched: {}", program))
                .onErrorResume(e -> {
                    log.error("Error fetching study programs: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<DidacticUnit> getDidacticUnitsByProgram(String programId) {
        return webClientBuilder.build()
                .get()
                .uri(didacticUnitServiceUrl + "/program/" + programId)
                .retrieve()
                .bodyToFlux(DidacticUnit.class)
                .doOnNext(unit -> log.info("Didactic unit fetched: {}", unit))
                .onErrorResume(e -> {
                    log.error("Error fetching didactic units for program: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<ClassroomDto> getClassroomsByDidacticUnit(String didacticUnitId) {
        return webClientBuilder.build()
            .get()
            .uri(classroomServiceUrl + "/didactic-unit/" + didacticUnitId)
            .retrieve()
            .bodyToFlux(ClassroomDto.class)
            .doOnNext(classroom -> log.info("Classroom fetched: {}", classroom))
            .onErrorResume(e -> {
                log.error("Error fetching classrooms for didactic unit: {}", e.getMessage());
                return Flux.empty();
            });
    }

    public Flux<StudentDto> getStudentsByClassroom(String classroomId) {
        return webClientBuilder.build()
                .get()
                .uri(classroomServiceUrl + "/findById/" + classroomId)
                .retrieve()
                .bodyToMono(ClassroomDto.class)
                .flatMapMany(classroom -> Flux.fromIterable(classroom.getEnrollmentDetailId()))
                .map(EnrollmentDetailDto::getStudent)
                .doOnNext(student -> log.info("Student fetched: {}", student))
                .onErrorResume(e -> {
                    log.error("Error fetching students for classroom: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<CompetenceDto> getCompetenciesByDidacticUnit(String didacticUnitId) {
        return webClientBuilder.build()
                .get()
                .uri(competenceServiceUrl + "/didactic-unit/" + didacticUnitId + "/competencies")
                .retrieve()
                .bodyToFlux(CompetenceDto.class)
                .doOnNext(competence -> log.info("Competence fetched: {}", competence))
                .onErrorResume(e -> {
                    log.error("Error fetching competencies for didactic unit: {}", e.getMessage());
                    return Flux.empty();
                });
    }
    
    // Método para obtener capacidades por ID de competencia
    public Flux<CapacityDto> getCapacitiesByCompetence(String competenceId) {
        return webClientBuilder.build()
                .get()
                .uri(capacityServiceUrl + "/competency/" + competenceId + "/capacities")
                .retrieve()
                .bodyToFlux(CapacityDto.class)
                .onErrorResume(e -> {
                    log.error("Error fetching capacities for competence: ", e);
                    return Flux.empty();
                });
    }
    
    public Flux<DidacticUnit> getDidacticUnitsByClassroom(String classroomId) {
        return webClientBuilder.build()
                .get()
                .uri(classroomServiceUrl + "/{classroomId}/didactic-units", classroomId)
                .retrieve()
                .bodyToFlux(DidacticUnit.class)
                .doOnNext(unit -> log.info("Didactic unit fetched: {}", unit))
                .onErrorResume(e -> {
                    log.error("Error fetching didactic units for classroom: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<ClassroomDto> getClassroomsByStudyProgram(String studyProgramId) {
        return webClientBuilder.build()
            .get()
            .uri(classroomServiceUrl + "/study-program/{studyProgramId}/classrooms", studyProgramId)
            .retrieve()
            .bodyToFlux(ClassroomDto.class)
            .doOnNext(classroom -> log.info("Classroom fetched: {}", classroom))
            .onErrorResume(e -> {
                log.error("Error fetching classrooms for study program: {}", e.getMessage());
                return Flux.empty();
            });
    }

    // Método para enviar el correo electrónico utilizando WebClient
    public Mono<EmailResponse> sendEmail(Email email) {
        log.info("Enviando correo a través del microservicio de email");
        return webClientBuilder.build()
                .post()
                .uri(emailServiceUrl + "/send")
                .bodyValue(email)
                .retrieve()
                .bodyToMono(EmailResponse.class)
                .doOnNext(response -> {
                    if (!response.isSuccess()) {
                        log.error("Error al enviar el correo electrónico.");
                    } else {
                        log.info("Correo enviado exitosamente.");
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error al conectar con el servicio de correo electrónico: ", e);
                    return Mono.error(new RuntimeException("Error al conectar con el servicio de correo electrónico"));
                });
    }

}
