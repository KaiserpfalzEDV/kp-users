/*
 * Copyright (c) 2025. Roland T. Lichti, Kaiserpfalz EDV-Service.
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
import de.kaiserpfalzedv.commons.users.domain.services.ApiKeyWriteService;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcApiKeyWriteService implements ApiKeyWriteService {
  private final R2dbcApiKeyRepository repository;
  
  private final ApplicationEventPublisher bus;
  private final ApiKeyToImpl toImpl;
  
  @Value("${spring.application.system:kp-users}")
  private String system = "kp-users";
  
  @Override
  public Mono<ApiKeyImpl> create(final ApiKey apiKey) {
    log.entry(apiKey);
    
    ApiKeyImpl data = (ApiKeyImpl.class.isAssignableFrom(apiKey.getClass()))
                     ? (ApiKeyImpl) apiKey
                     : toImpl.apply(apiKey);

    Mono<ApiKeyImpl> result = repository.save(data)
        .switchIfEmpty(Mono.error(new InvalidApiKeyException(apiKey)))
        .onErrorMap(IllegalArgumentException.class, e -> new InvalidApiKeyException(apiKey, e))
        .onErrorMap(OptimisticLockingFailureException.class, e -> new InvalidApiKeyException(apiKey, e))
        .doOnSuccess(a -> {
          log.info("Created API key. user={}, key={}", a.getUser(), a.getId());
          bus.publishEvent(ApiKeyCreatedEvent.builder().application(system).user(a.getUser()).apiKey(a).build()));
        })
        .doOnError(e -> log.error("{}. user={}, key={}", e.getMessage(), data.getUser(), data.getId()));
    
    return log.exit(result);
  }
  
  @Override
  public Mono<ApiKeyImpl> refresh(final UUID apiKeyId, final long days) throws ApiKeyNotFoundException {
    log.entry(apiKeyId, days);
    
    Mono<ApiKeyImpl> result = repository.findById(apiKeyId)
        .switchIfEmpty(Mono.error(new ApiKeyNotFoundException(apiKeyId)))
        .flatMap(data -> {
          ApiKeyImpl refreshed = data.toBuilder().expiration(OffsetDateTime.now().plusDays(days)).build();
          return repository.save(refreshed);
        })
        .doOnSuccess(a -> log.info("Refreshed API key. id={}, expiration{}", a.getId(), a.getExpiration()))
        .doOnError(e -> log.error("{}. id={}, days={}", e.getMessage(), apiKeyId, days));
    
    return log.exit(result);
  }
  
  @Override
  public Mono<Void> delete(final UUID apiKeyId) {
    log.entry(apiKeyId);
    
    Mono<Void> result = repository.deleteById(apiKeyId)
        .doOnSuccess(a -> log.info("Deleted API key. id={}", apiKeyId))
        .doOnError(e -> log.error("{}. id={}", e.getMessage(), apiKeyId));
    
    return log.exit(result);
  }
}
