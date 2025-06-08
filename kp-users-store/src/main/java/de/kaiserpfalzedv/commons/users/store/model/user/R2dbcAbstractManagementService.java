package de.kaiserpfalzedv.commons.users.store.model.user;


import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.UserNotFoundException;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-07
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcAbstractManagementService {
  protected final R2dbcUserRepository repository;
  protected final ApplicationEventPublisher bus;
  @ToString.Include
  protected final String system;
  
  
  @PreDestroy
  public void close() {
    log.entry(repository, bus, system);
    log.exit();
  }
  
  
  /**
   * Saves the user and publishes the appropriate event.
   *
   * @param user           The user to save.
   * @param successMessage The message to log on success.
   * @param errorMessage   The message to log on error.
   * @return A Mono containing the saved user or an error if the save failed.
   */
  protected Mono<KpUserDetails> saveUser(
      @NotNull KpUserDetails user,
      @NotNull final String successMessage,
      @NotNull final String errorMessage
  ) {
    log.entry(user, successMessage, errorMessage);
    
    Mono<KpUserDetails> result = repository.save(user)
        .switchIfEmpty(Mono.error(() -> new UserNotFoundException(user.getId())))
        .doOnSuccess(savedUser -> log.info("{}. user={}", successMessage, savedUser))
        .doOnError(error -> log.error("{}: {}. user={}", errorMessage, error.getMessage(), user));
    
    return log.exit(result);
  }
}
