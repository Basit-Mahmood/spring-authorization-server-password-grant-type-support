package pk.training.basit.jackson2.deserializer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

import pk.training.basit.jpa.audit.AuditDeletedDate;
import pk.training.basit.jpa.entity.UserAuthority;
import pk.training.basit.jpa.entity.UserPrincipal;

public class UserPrincipalDeserializer extends JsonDeserializer<UserPrincipal> {

	private static final TypeReference<AuditDeletedDate> AUDIT_DELETED_DATE_OBJECT = new TypeReference<AuditDeletedDate>() {};
	
	@Override
	public UserPrincipal deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		JsonNode jsonNode = mapper.readTree(jp);
		final JsonNode authoritiesNode = readJsonNode(jsonNode, "authorities");
		Set<UserAuthority> userAuthorities = getUserAuthorities(mapper, authoritiesNode);
		
		Long id = readJsonNode(jsonNode, "id").asLong();
		String username = readJsonNode(jsonNode, "username").asText();
		JsonNode passwordNode = readJsonNode(jsonNode, "password");
		String password = passwordNode.asText("");
		boolean enabled = readJsonNode(jsonNode, "enabled").asBoolean();
		boolean accountNonExpired = readJsonNode(jsonNode, "accountNonExpired").asBoolean();
		boolean credentialsNonExpired = readJsonNode(jsonNode, "credentialsNonExpired").asBoolean();
		boolean accountNonLocked = readJsonNode(jsonNode, "accountNonLocked").asBoolean();
		final JsonNode auditNode = readJsonNode(jsonNode, "audit");
		
		AuditDeletedDate auditDeletedDate = null;
		if (!auditNode.isNull() || !auditNode.isMissingNode()) {
			auditDeletedDate = mapper.readValue(auditNode.toString(), AUDIT_DELETED_DATE_OBJECT);
		}
		
		UserPrincipal userPrincipal = new UserPrincipal(id, username, password, enabled, accountNonExpired, credentialsNonExpired, 
				accountNonLocked, userAuthorities, auditDeletedDate);
		if (passwordNode.asText(null) == null) {
			userPrincipal.eraseCredentials();
		}
		
		return userPrincipal;
		
	}
	
	private JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}
	
	private Set<UserAuthority> getUserAuthorities(final ObjectMapper mapper, final JsonNode authoritiesNode) throws JsonParseException, JsonMappingException, IOException {
		
		Set<UserAuthority> userAuthorities = new HashSet<>();
		if (authoritiesNode != null) {
			if (authoritiesNode.isArray()) {
				for (final JsonNode objNode : authoritiesNode) {
					if (objNode.isArray()) {
						ArrayNode arrayNode = (ArrayNode) objNode;
						for (JsonNode elementNode : arrayNode) {
							UserAuthority userAuthority = mapper.readValue(elementNode.traverse(mapper), UserAuthority.class);
							userAuthorities.add(userAuthority);
						}
					} 
				}
			}
		}
		return userAuthorities;
	}

}
