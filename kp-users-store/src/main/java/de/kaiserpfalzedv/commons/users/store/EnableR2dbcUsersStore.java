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
package de.kaiserpfalzedv.commons.users.store;

import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKey;
import de.kaiserpfalzedv.commons.users.domain.model.role.Role;
import de.kaiserpfalzedv.commons.users.domain.model.user.User;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.lang.annotation.*;


/**
 * Enables the user store in a Spring Boot application.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @version 1.0.0
 * @since 2024-09-27
 */
@SuppressWarnings("unused")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@ComponentScan(basePackageClasses = {
    EnableR2dbcUsersStore.class,
    ApiKey.class,
    Role.class,
    User.class,
})
@EnableR2dbcRepositories(basePackageClasses = EnableR2dbcUsersStore.class)
public @interface EnableR2dbcUsersStore {}
