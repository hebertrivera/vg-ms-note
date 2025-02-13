package pe.edu.vallegrande.vgmsnote.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsnote.domain.model.Note;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NoteRepository  extends ReactiveMongoRepository<Note, String> {
    Flux<Note> findByClassroomId(String classroomId);
    Flux<Note> findByStudentId(String studentId);
    Flux<Note> findByStatus(String status);
    Mono<Note> findByStudentIdAndCapacityId(String studentId, String capacityId);
}
