/*
 * Copyright (c) 2024-2025. Kaiserpfalz EDV-Service, Roland T. Lichti
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or  (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package de.kaiserpfalzedv.commons.users.domain.model.user;

import de.kaiserpfalzedv.commons.users.domain.model.role.KpRole;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleAddedToUserEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.modification.RoleRemovedFromUserEvent;
import de.kaiserpfalzedv.commons.users.domain.model.user.events.state.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.Id;

import java.time.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Jacksonized
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Getter
@ToString(of = {"id", "nameSpace", "name"})
@EqualsAndHashCode(of = {"id"})
@XSlf4j
public class KpUserDetails implements User {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    
    @Builder.Default
    private OffsetDateTime created = OffsetDateTime.now(Clock.systemUTC());
    private OffsetDateTime modified;
    private OffsetDateTime deleted;
    
    private Duration detainmentDuration;
    private OffsetDateTime detainedTill;
    
    private OffsetDateTime bannedOn;

    @Builder.Default
    private String nameSpace = "./.";
    private String name;
    
    private String issuer;
    private String subject;
    
    @Email
    private String email;
    private String phone;
    private String discord;
    
    @Builder.Default
    private final Set<KpRole> authorities = new HashSet<>();
    
    
    @Override
    public KpUserDetails detain(@NotNull ApplicationEventPublisher bus, @Min(1) @Max(1095) long days) {
        log.entry(bus, days);
        
        detainmentDuration = Duration.ofDays(days);
        
        detainedTill = LocalDate.now()
            .atStartOfDay(ZoneId.of("UTC"))
            .plusDays(1 + days) // today end of day (1) + days
            .toOffsetDateTime();
        
        bus.publishEvent(UserDetainedEvent.builder().user(this).days(days).build());
        
        return log.exit(this);
    }
    
    @Override
    public KpUserDetails release(@NotNull ApplicationEventPublisher bus) {
        log.entry(bus);
        
        detainmentDuration = null;
        detainedTill = null;
        bannedOn = null;
        
        bus.publishEvent(UserReleasedEvent.builder().user(this).build());
        
        return log.exit(this);
    }
    
    @Override
    public KpUserDetails ban(@NotNull ApplicationEventPublisher bus) {
        log.entry(bus);

        bannedOn = OffsetDateTime.now(Clock.systemUTC());
        
        bus.publishEvent(UserBannedEvent.builder().user(this).timestamp(bannedOn).build());
        
        return log.exit(this);
    }
    
    @Override
    public KpUserDetails delete(@NotNull ApplicationEventPublisher bus) {
        log.entry(bus);
        
        this.deleted = OffsetDateTime.now(Clock.systemUTC());
        
        bus.publishEvent(UserDeletedEvent.builder().user(this).timestamp(deleted).build());
        log.info("Deleted user. banned={}, detained={}, deleted={}", isBanned(), isDetained(), isDeleted());
        
        return log.exit(this);
    }
    
    @Override
    public KpUserDetails undelete(@NotNull ApplicationEventPublisher bus) {
        log.entry(bus);
        
        this.deleted = null;
        
        bus.publishEvent(UserActivatedEvent.builder().user(this).build());
        log.info("Undeleted user. banned={}, detained={}", isBanned(), isDetained());
        
        return log.exit(this);
    }
    
    @Override
    public void eraseCredentials() {
        // nothing to do, there are no credentials anywhere ...
    }
    
    public void addRole(@NotNull final KpRole role, @NotNull ApplicationEventPublisher bus) {
        log.entry(role, bus);
        
        if (authorities.add(role)) {
            bus.publishEvent(RoleAddedToUserEvent.builder().user(this).role(role).build());
        }
        
        log.exit();
    }
    
    public void removeRole(@NotNull final KpRole role, @NotNull ApplicationEventPublisher bus) {
        log.entry(role, bus);
        
        if (authorities.remove(role)) {
            bus.publishEvent(RoleRemovedFromUserEvent.builder().user(this).role(role).build());
        }
        
        log.exit();
    }
}
