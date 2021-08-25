package pk.training.basit.jackson2.deserializer;

import java.io.IOException;
import java.time.Instant;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import pk.training.basit.jpa.audit.Audit;
import pk.training.basit.jpa.audit.AuditDeletedDate;

public class AuditDeletedDateDeserializer extends JsonDeserializer<AuditDeletedDate> {

	private static final TypeReference<Instant> INSTANT = new TypeReference<Instant>() {};
	
	@Override
	public AuditDeletedDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		JsonNode jsonNode = mapper.readTree(jp);
		Long createdBy = readJsonNode(jsonNode, "createdBy").asLong();
		Long lastModifiedBy = readJsonNode(jsonNode, "lastModifiedBy").asLong();
		JsonNode createdDateNode = readJsonNode(jsonNode, "createdDate");
		JsonNode lastModifiedDateNode = readJsonNode(jsonNode, "lastModifiedDate");
		JsonNode deletedDateNode = readJsonNode(jsonNode, "deletedDate");
	
		Instant createdDate = mapper.readValue(createdDateNode.toString(), INSTANT);
		Instant lastModifiedDate = mapper.readValue(lastModifiedDateNode.toString(), INSTANT);
		Instant deletedDate = mapper.readValue(deletedDateNode.toString(), INSTANT);
		
		Audit audit = new Audit(createdBy, createdDate, lastModifiedBy, lastModifiedDate);
		AuditDeletedDate auditDeletedDate = new AuditDeletedDate(audit, deletedDate);
		
		return auditDeletedDate;
		
	}
	
	private JsonNode readJsonNode(JsonNode jsonNode, String field) {
		return jsonNode.has(field) ? jsonNode.get(field) : MissingNode.getInstance();
	}
	
}
