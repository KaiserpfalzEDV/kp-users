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

import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyImpl;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Repository for accessing the APIKEYs.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2025-06-07
 */
@Repository
public interface R2dbcApiKeyRepository extends ReactiveCrudRepository<ApiKeyImpl, UUID> {
  Flux<ApiKeyImpl> findByUserId(UUID userId);
  Mono<ApiKeyImpl> findByIdAndExpirationAfter(UUID id, OffsetDateTime expiration);
  Flux<ApiKeyImpl> findByExpirationBefore(OffsetDateTime expiration);
}
