package dev.ueslei.cloakform.mapper;

import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;
import org.keycloak.representations.idm.AuthenticationExecutionInfoRepresentation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.extensions.spring.converter.ConversionServiceAdapter;
import org.springframework.core.convert.converter.Converter;

@Mapper(componentModel = "spring", uses = ConversionServiceAdapter.class)
public interface AuthenticationExecutionMapper extends
    Converter<AuthenticationExecutionExportRepresentation, AuthenticationExecutionInfoRepresentation> {

    @Mapping(source = "authenticatorFlow", target = "authenticationFlow")
    @Mapping(source = "authenticatorConfig", target = "authenticationConfig")
    @Mapping(source = "authenticator", target = "providerId")
    @Mapping(source = "flowAlias", target = "displayName")
    AuthenticationExecutionInfoRepresentation convert(AuthenticationExecutionExportRepresentation source);
}
