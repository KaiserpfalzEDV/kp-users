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

package de.kaiserpfalzedv.commons.users.domain.services;


import de.kaiserpfalzedv.commons.users.domain.model.role.Role;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;


/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
public interface RoleReadService {
  Mono<? extends Role> retrieve(@NotNull UUID id);
  
  Flux<? extends Role> retrieveByName(@NotNull String name);
  Flux<? extends Role> retrieveAll();
  Flux<? extends Role> retrieveAllFromNamespace(@NotNull String namespace);
}
