package dev.rafprz.requestmanagement.config;

import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;

/**
 * Registers (de)serialization support for {@link JsonNullable} with Jackson 3
 * (used by Spring Boot's auto-configured {@code JsonMapper}). Without this module
 * {@code JsonNullable}-typed fields on the generated OpenAPI models (e.g.
 * {@code RequestDto.publishedNumber}, {@code RequestDto.reason}) serialize as their
 * internal representation instead of the wrapped value.
 */
public class JsonNullableModule extends SimpleModule {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public JsonNullableModule() {
        super("JsonNullableModule");
        addSerializer(JsonNullable.class, (ValueSerializer) new JsonNullableSerializer());
        addDeserializer(JsonNullable.class, new JsonNullableDeserializer());
    }

    private static final class JsonNullableSerializer extends ValueSerializer<JsonNullable<?>> {

        @Override
        public void serialize(final JsonNullable<?> value,
                              final JsonGenerator gen,
                              final SerializationContext ctxt) throws JacksonException {
            if (value == null || !value.isPresent() || value.get() == null) {
                gen.writeNull();
            } else {
                ctxt.writeValue(gen, value.get());
            }
        }

        @Override
        public boolean isEmpty(final SerializationContext ctxt, final JsonNullable<?> value) {
            return value == null || !value.isPresent();
        }
    }

    private static final class JsonNullableDeserializer extends ValueDeserializer<JsonNullable<?>> {

        private final ValueDeserializer<Object> contentDeserializer;

        JsonNullableDeserializer() {
            this(null);
        }

        JsonNullableDeserializer(final ValueDeserializer<Object> contentDeserializer) {
            this.contentDeserializer = contentDeserializer;
        }

        @Override
        public ValueDeserializer<?> createContextual(final DeserializationContext ctxt,
                                                      final BeanProperty property) {
            final JavaType wrapperType = property != null ? property.getType() : null;
            if (wrapperType == null || wrapperType.containedTypeCount() == 0) {
                return this;
            }
            final JavaType contentType = wrapperType.containedTypeOrUnknown(0);
            final ValueDeserializer<Object> resolved = ctxt.findContextualValueDeserializer(contentType, property);
            return new JsonNullableDeserializer(resolved);
        }

        @Override
        public JsonNullable<?> deserialize(final JsonParser p, final DeserializationContext ctxt) throws JacksonException {
            if (p.currentToken() == JsonToken.VALUE_NULL) {
                return JsonNullable.of(null);
            }
            final Object value = contentDeserializer != null
                    ? contentDeserializer.deserialize(p, ctxt)
                    : ctxt.readValue(p, Object.class);
            return JsonNullable.of(value);
        }

        @Override
        public Object getAbsentValue(final DeserializationContext ctxt) {
            return JsonNullable.undefined();
        }

        @Override
        public Object getNullValue(final DeserializationContext ctxt) {
            return JsonNullable.of(null);
        }
    }
}
