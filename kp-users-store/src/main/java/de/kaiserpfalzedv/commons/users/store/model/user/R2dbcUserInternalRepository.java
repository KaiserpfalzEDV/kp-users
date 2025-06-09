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
package de.kaiserpfalzedv.commons.users.store.model.user;

import de.kaiserpfalzedv.commons.users.domain.model.user.KpUserDetails;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2024-08-18
 */
@Repository
interface R2dbcUserInternalRepository extends ReactiveCrudRepository<KpUserDetails, UUID> {
    Mono<KpUserDetails> findByNameSpaceAndName(String nameSpace, String name);
    Mono<KpUserDetails> findByIssuerAndSubject(String issuer, String subject);
    Mono<KpUserDetails> findByEmail(String email);
    
    Flux<KpUserDetails> findByNameSpace(String nameSpace);
    Flux<KpUserDetails> findByIssuer(String issuer);
}
