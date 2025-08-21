package de.kaiserpfalzedv.commons.users.service.api;


import de.kaiserpfalzedv.commons.users.domain.model.user.User;
import de.kaiserpfalzedv.commons.users.store.model.user.R2dbcUserRepository;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * The Controller for the user services.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-21
 */
@RestController
@RequestMapping(
    path = "/api/users",
    produces = "application/json"
)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@XSlf4j
public class UsersController {
  private final R2dbcUserRepository userRepository;
 
  /**
   * Retrieves all users from the repository.
   *
   * @return a Flux of User objects representing all users.
   */
  @GetMapping(
      consumes = {"application/json;v1", "application/json"}
  )
  public Flux<User> getAllUsers() {
    log.entry();
    
    return log.exit(
        userRepository.findAll()
            .doOnComplete(() -> log.info("Users retrieved."))
    );
  }
}
