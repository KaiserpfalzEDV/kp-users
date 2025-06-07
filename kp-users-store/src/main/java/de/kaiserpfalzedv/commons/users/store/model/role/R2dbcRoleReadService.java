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

package de.kaiserpfalzedv.commons.users.store.model.role;


import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.services.RoleReadService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcRoleReadService implements RoleReadService {
  private final R2dbcRoleRepository repository;
  
  @Override
  @Counted
  @Timed
  public Mono<KpRole> retrieve(@NotNull final UUID id) {
    log.entry(id);
    
    return log.exit(repository.findById(id));
  }
  
  @Override
  public Flux<KpRole> retrieveByName(@NotNull String name) {
    log.entry(name);
    
    return log.exit(repository.findByName(name));
  }
  
  @Override
  @Counted
  @Timed
  public Flux<KpRole> retrieveAll() {
    log.entry();
    
    return log.exit(repository.findAll());
  }
  
  @Override
  @Counted
  @Timed
  public Flux<KpRole> retrieveAllFromNamespace(@NotBlank final String namespace) {
    log.entry(namespace);
    
    return log.exit(repository.findByNameSpace(namespace));
  }
}
