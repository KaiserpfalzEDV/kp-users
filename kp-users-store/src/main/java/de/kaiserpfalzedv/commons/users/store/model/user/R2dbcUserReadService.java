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
import de.kaiserpfalzedv.commons.users.domain.services.UserReadService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Order(1010)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcUserReadService implements UserReadService<KpUserDetails> {
    private final R2dbcUserRepository users;
    
    @Override
    @Counted
    @Timed
    public Mono<KpUserDetails> findById(final UUID id) {
        log.entry(id);
        
        return log.exit(users.findById(id));
    }
    
    @Override
    @Counted
    @Timed
    public Mono<KpUserDetails> findByUsername(final String namespace, final String name) {
        log.entry(namespace, name);
        
        return log.exit(users.findByNameSpaceAndName(namespace, name));
    }
    
    @Override
    @Counted
    @Timed
    public Mono<KpUserDetails> findByOauth(final String issuer, final String sub) {
        log.entry(issuer, sub);
        
        return log.exit(users.findByIssuerAndSubject(issuer, sub));
    }
    
    @Override
    @Counted
    @Timed
    public Flux<KpUserDetails> findByNamespace(final String nameSpace) {
        log.entry(nameSpace);
        
        return log.exit(users.findByNameSpace(nameSpace));
    }
    
    
    @Override
    @Counted
    @Timed
    public Flux<KpUserDetails> findAll() {
        log.entry();
        
        return log.exit(users.findAll());
    }
}
