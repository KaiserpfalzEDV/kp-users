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
package de.kaiserpfalzedv.commons.users.client;

import de.kaiserpfalzedv.commons.users.client.reactive.KpReactUserDetailsService;
import de.kaiserpfalzedv.commons.users.client.reactive.KpReactUserSecurityConfig;
import de.kaiserpfalzedv.commons.users.client.service.KpApiKeyAuthenticationManager;
import de.kaiserpfalzedv.commons.users.client.service.KpUserAuthenticationManager;
import de.kaiserpfalzedv.commons.users.client.service.KpUserAuthenticationService;
import de.kaiserpfalzedv.commons.users.client.service.KpUserDetailsService;
import de.kaiserpfalzedv.commons.users.client.service.KpUserLoggedInStateRepository;
import de.kaiserpfalzedv.commons.users.store.EnableR2dbcUsersStore;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;


/**
 * Enables the full user client functionality in a Spring application.
 * This includes user messaging and user store capabilities.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 4.1.0
 * @since 2025-05-24
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableR2dbcUsersStore
@Import({
    KpApiKeyAuthenticationManager.class,
    KpUserAuthenticationManager.class,
    KpUserDetailsService.class,
    KpUserAuthenticationService.class,
    KpUserLoggedInStateRepository.class,
    KpReactUserSecurityConfig.class,
    KpReactUserDetailsService.class,
})
public @interface EnableKpUserClient {}
