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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-05-11
 */
public interface RoleWriteService {
  Mono<? extends Role> create(@NotNull Role role);
  
  Mono<? extends Role> updateNameSpace(@NotNull UUID id, @NotBlank String namespace);
  Mono<? extends Role> updateName(@NotNull UUID id, @NotBlank String name);
  
  Mono<Void> remove(@NotNull UUID id);
}
