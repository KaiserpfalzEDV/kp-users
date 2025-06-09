package de.kaiserpfalzedv.commons.users.store.model.user;


import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.UserRemovedEvent;
import de.kaiserpfalzedv.commons.users.domain.services.UserReadService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-09
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@XSlf4j
public class R2dbcUserRepository implements UserReadService<KpUserDetails> {
  private final R2dbcUserInternalRepository repository;
  private final R2dbcEntityTemplate template;
  private final ApplicationEventPublisher bus;
  
  @Value("${spring.application.system:kp-users}")
  private String system = "kp-users";
  
  
  
  // Delegate for the R2DBC repository
  
  public Mono<KpUserDetails> findByEmail(@NotNull final String email) {
    log.entry(email);
    return log.exit(addRoles(repository.findByEmail(email)));
  }
  
  private Mono<KpUserDetails> addRoles(final Mono<KpUserDetails> user) {
    log.entry(user);
    
    Mono<KpUserDetails> result = user
        .flatMap(u  -> loadRolesForUser(u.getId())
                .collectList()
                .flatMap(roles -> {
                  roles.forEach(role -> u.addRole(role, null));
                  return Mono.just(u);
                })
        );
    
    return log.exit(result);
  }
  
  public Mono<KpUserDetails> findByIssuerAndSubject(@NotNull final String issuer, final String subject) {
    log.entry(issuer, subject);
    return log.exit(addRoles(repository.findByIssuerAndSubject(issuer, subject)));
  }
  
  public Mono<KpUserDetails> findByNameSpaceAndName(@NotNull final String nameSpace, final String name) {
    log.entry(nameSpace, name);
    return log.exit(addRoles(repository.findByNameSpaceAndName(nameSpace, name)));
  }
  
  public Mono<KpUserDetails> findById(@NotNull final UUID uuid) {
    return log.exit(addRoles(repository.findById(uuid)));
  }
  
  @Override
  public Mono<KpUserDetails> findByUsername(final String nameSpace, final String name) {
    return findByNameSpaceAndName(nameSpace, name);
  }
  
  @Override
  public Optional<KpUserDetails> findByOauth(final String issuer, final String sub) {
    return findByIssuerAndSubject(issuer, sub).blockOptional();
  }
  
  public Flux<KpUserDetails> findAll() {
    return log.exit(addRoles(repository.findAll()));
  }
  
  @Override
  public Flux<KpUserDetails> findByNamespace(final String nameSpace) {
    log.entry(nameSpace);
    return log.exit(addRoles(repository.findByNameSpace(nameSpace)));
  }
  
  public Flux<KpUserDetails> findByIssuer(@NotNull final String issuer) {
    log.entry(issuer);
    return log.exit(addRoles(repository.findByIssuer(issuer)));
  }
  
  public Flux<KpUserDetails> findByNameSpace(@NotNull final String nameSpace) {
    log.entry(nameSpace);
    return log.exit(addRoles(repository.findByNameSpace(nameSpace)));
  }
  
  private Flux<KpUserDetails> addRoles(final Flux<KpUserDetails> user) {
    log.entry(user);
    
    Flux<KpUserDetails> result = user
        .flatMap(u  -> loadRolesForUser(u.getId())
              .collectList()
              .flatMap(roles -> {
                roles.forEach(role -> u.addRole(role, null));
                return Mono.just(u);
              })
        );
    
    return log.exit(result);
  }
  
  private Flux<KpRole> loadRolesForUser(final UUID userId) {
    log.entry(userId);
    
    // Zuerst die Benutzer-Rollen-Verknüpfungen abrufen
    return log.exit(template.select(KpUsersRoles.class)
        .matching(query(where("USER_ID").is(userId)))
        .all()
        // Dann für jede Verknüpfung die zugehörige Rolle abrufen
        .flatMap(userRole -> template.selectOne(
            query(where("ID").is(userRole.getRoleId())),
            KpRole.class)
        )
    );
  }
  
  
  public Mono<KpUserDetails> save(@NotNull final KpUserDetails entity) {
    log.entry(entity);
    
    return saveRolesForUser(entity)
        .then(log.exit(repository.save(entity)));
  }
  
  private Mono<Void> saveRolesForUser(final KpUserDetails entity) {
    log.entry(entity);
    log.debug("Saving roles for user. user={}, roles={}", entity, entity.getAuthorities());
    
    return log.exit(Flux.fromIterable(entity.getAuthorities())
        .flatMap(role -> template
            .exists(
                query(where("USER_ID").is(entity.getId())
                    .and("ROLE_ID").is(role.getId())),
                KpUsersRoles.class
            )
            .flatMap(exists -> {
              if (!exists) {
                KpUsersRoles roleAssignement = KpUsersRoles.builder()
                    .userId(entity.getId())
                    .roleId(role.getId())
                    .build();
                
                return template.insert(roleAssignement);
              }
              return Mono.empty();
            }))
        .then()
    );
  }
  
  public Mono<Void> deleteById(@NotNull final UUID id) {
    log.entry(id);
    
    return log.exit(repository.deleteById(id))
        .doOnSuccess(result -> {
          log.info("User deleted successfully. id={}", id);
          bus.publishEvent(UserRemovedEvent.builder().application(system).id(id).build());
        });
  }
}
