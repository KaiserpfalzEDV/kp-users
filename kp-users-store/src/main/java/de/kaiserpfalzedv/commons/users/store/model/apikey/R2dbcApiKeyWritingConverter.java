package de.kaiserpfalzedv.commons.users.store.model.apikey;


import de.kaiserpfalzedv.commons.users.domain.model.apikey.ApiKeyImpl;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.r2dbc.mapping.OutboundRow;
import org.springframework.r2dbc.core.Parameter;
import org.springframework.stereotype.Component;

/**
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-09
 */
@Component
@WritingConverter
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@ToString(onlyExplicitlyIncluded = true)
@XSlf4j
public class R2dbcApiKeyWritingConverter implements Converter<ApiKeyImpl, OutboundRow> {
  @SuppressWarnings("deprecation")
  @Override
  public OutboundRow convert(@Nullable final ApiKeyImpl source) {
    log.entry(source);
    
    if (source == null) {
      return log.exit(new OutboundRow());
    }
    
    OutboundRow result = new OutboundRow();
    
    result.put("id", Parameter.from(source.getId()));
    result.put("created", Parameter.from(source.getCreated()));
    result.put("modified", Parameter.from(source.getModified()));
    result.put("deleted", Parameter.from(source.getDeleted()));
    result.put("namespace", Parameter.from(source.getNameSpace()));
    result.put("expiration", Parameter.from(source.getExpiration()));
    result.put("user", Parameter.from(source.getUser().getId()));

    return log.exit(result);
  }
}
