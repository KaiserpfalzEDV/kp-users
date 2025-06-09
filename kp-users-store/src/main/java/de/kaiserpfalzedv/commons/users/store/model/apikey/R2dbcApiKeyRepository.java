/*
 * Copyright (c) 2024-2025. Roland T. Lichti, Kaiserpfalz EDV-Service.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.kaiserpfalzedv.commons.users.store.model.apikey;

import de.kaiserpfalzedv.commons.users.domain.model.apikey.*;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.events.ApiKeyCreatedEvent;
import de.kaiserpfalzedv.commons.users.domain.model.apikey.events.ApiKeyRevokedEvent;
import de.kaiserpfalzedv.commons.users.domain.services.ApiKeyReadService;
import de.kaiserpfalzedv.commons.users.domain.services.ApiKeyWriteService;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

/**
 * Repository for accessing the APIKEYs.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2025-06-07
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@XSlf4j
public class R2dbcApiKeyRepository implements ApiKeyReadService, ApiKeyWriteService {
  private static final Duration TIMEOUT = Duration.ofMillis(500L);
  
  private final R2dbcApiKeyInternalRepository repository;
  private final R2dbcEntityTemplate template;
  private final ApplicationEventPublisher bus;
  private final ApiKeyToImpl toImpl;
  
  @Value("${spring.application.system:kp-users}")
  private String system = "kp-users";
  
  @Override
  public Flux<ApiKeyImpl> retrieveForUser(final UUID userId) {
    log.entry(userId);
    
    return log.exit(
        template.select(ApiKeyImpl.class)
            .from("APIKEYS")
            .matching(query(where("user").is("userId")))
            .all()
    );
  }
  
  @Override
  public Mono<ApiKeyImpl> retrieve(final UUID id) {
    log.entry(id);
    
    return log.exit(repository.findById(id));
  }
  
  @Override
  public Mono<ApiKeyImpl> retrieve(final String id) {
    log.entry(id);
    
    return log.exit(repository.findById(UUID.fromString(id)));
  }
  
  public Mono<ApiKeyImpl> findById(@NotNull final UUID id) {
    log.entry(id);
    return log.exit(repository.findById(id));
  }

  
  @Override
  public Mono<ApiKeyImpl> create(final ApiKey apiKey) {
    log.entry(apiKey);
    
    ApiKeyImpl data = (ApiKeyImpl.class.isAssignableFrom(apiKey.getClass()))
                      ? (ApiKeyImpl) apiKey
                      : toImpl.apply(apiKey);
    
    Mono<ApiKeyImpl> result = repository.save(data)
        .switchIfEmpty(Mono.error(() -> new InvalidApiKeyException(apiKey)))
        .onErrorMap(IllegalArgumentException.class, e -> new InvalidApiKeyException(apiKey, e))
        .onErrorMap(OptimisticLockingFailureException.class, e -> new InvalidApiKeyException(apiKey, e))
        .doOnSuccess(a -> {
          log.info("Created API key. user={}, key={}", a.getUser(), a.getId());
          bus.publishEvent(ApiKeyCreatedEvent.builder().application(system).user(a.getUser()).apiKey(a).build());
        })
        .doOnError(e -> log.error("{}. user={}, key={}", e.getMessage(), data.getUser(), data.getId()));
    
    return log.exit(result);
  }
  
  @Override
  public Mono<ApiKeyImpl> refresh(final UUID apiKeyId, final long days) {
    log.entry(apiKeyId, days);
    
    Mono<ApiKeyImpl> result = findById(apiKeyId)
        .switchIfEmpty(Mono.error(() -> new ApiKeyNotFoundException(apiKeyId)))
        .flatMap(data -> {
          ApiKeyImpl refreshed = data.toBuilder().expiration(OffsetDateTime.now().plusDays(days)).build();
          return repository.save(refreshed);
        })
        .doOnSuccess(a -> log.info("Refreshed API key. id={}, expiration={}", a.getId(), a.getExpiration()))
        .doOnError(e -> log.error("{}. id={}, days={}", e.getMessage(), apiKeyId, days));
    
    return log.exit(result);
  }
  
  public Mono<ApiKeyImpl> save(@NotNull final ApiKeyImpl apiKey) {
    log.entry(apiKey);
    
    return log.exit(repository.save(apiKey));
  }
  
  public void deleteById(@NotNull final UUID id) {
    log.entry(id);
    
    repository.deleteById(id)
        .doOnSuccess(e -> {
          log.info("Deleted api key. id={}", id);
          bus.publishEvent(ApiKeyRevokedEvent.builder().application(system).id(id).build());
        })
        .block(TIMEOUT);
    
    log.exit();
  }
  
  public Mono<Long> deleteByUserId(@NotNull final UUID userId) {
    log.entry(userId);
    
    return log.exit(
        template.delete(ApiKeyImpl.class)
            .matching(query(where("user").is(userId)))
            .all()
    );
  }
  
}
