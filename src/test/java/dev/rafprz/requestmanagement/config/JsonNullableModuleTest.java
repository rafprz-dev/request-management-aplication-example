package dev.rafprz.requestmanagement.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class JsonNullableModuleTest {

    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new JsonNullableModule())
            .build();

    @Setter
    static class Sample {

        private JsonNullable<Long> number = JsonNullable.undefined();
        private JsonNullable<String> text = JsonNullable.undefined();

        @JsonProperty("number")
        public JsonNullable<Long> getNumber() {
            return number;
        }

        @JsonProperty("text")
        public JsonNullable<String> getText() {
            return text;
        }
    }

    @Nested
    class Serialize {

        @Test
        void serialize_writesWrappedValue_whenPresent() {
            //given
            final var sample = new Sample();
            sample.setNumber(JsonNullable.of(42L));

            //when
            final var json = mapper.writeValueAsString(sample);

            //then
            assertThat(json).contains("\"number\":42");
        }

        @Test
        void serialize_writesNull_whenUndefined() {
            //given
            final var sample = new Sample();

            //when
            final var json = mapper.writeValueAsString(sample);

            //then
            assertThat(json).contains("\"number\":null");
        }

        @Test
        void serialize_writesNull_whenExplicitlyNull() {
            //given
            final var sample = new Sample();
            sample.setNumber(JsonNullable.of(null));

            //when
            final var json = mapper.writeValueAsString(sample);

            //then
            assertThat(json).contains("\"number\":null");
        }

        @Test
        void serialize_supportsStringContentType() {
            //given
            final var sample = new Sample();
            sample.setText(JsonNullable.of("reason"));

            //when
            final var json = mapper.writeValueAsString(sample);

            //then
            assertThat(json).contains("\"text\":\"reason\"");
        }

        @Test
        void serialize_bareJsonNullable_writesWrappedValueWithoutPropertyContext() {
            //given
            final var json = mapper.writeValueAsString(JsonNullable.of("hi"));

            //then
            assertThat(json).isEqualTo("\"hi\"");
        }

        @Test
        void serialize_bareUndefinedJsonNullable_writesNullWithoutPropertyContext() {
            //given
            final var json = mapper.writeValueAsString(JsonNullable.undefined());

            //then
            assertThat(json).isEqualTo("null");
        }
    }

    @Nested
    class Deserialize {
        @Test
        void should_return_present_value() {
            //given
            final var sample = mapper.readValue("{\"number\":42}", Sample.class);

            //then
            assertThat(sample.getNumber().isPresent()).isTrue();
            assertThat(sample.getNumber().get()).isEqualTo(42L);
        }

        @Test
        void should_return_present_null_when_field_is_explicitly_null() {
            //given
            final var sample = mapper.readValue("{\"number\":null}", Sample.class);

            //then
            assertThat(sample.getNumber().isPresent()).isTrue();
            assertThat(sample.getNumber().get()).isNull();
        }

        @Test
        void should_return_undefined_when_field_is_absent() {
            //given
            final var sample = mapper.readValue("{}", Sample.class);

            //then
            assertThat(sample.getNumber()).isEqualTo(JsonNullable.undefined());
            assertThat(sample.getNumber().isPresent()).isFalse();
        }

        @Test
        void should_support_string_content_type() {
            //given
            final var sample = mapper.readValue("{\"text\":\"reason\"}", Sample.class);

            //then
            assertThat(sample.getText().isPresent()).isTrue();
            assertThat(sample.getText().get()).isEqualTo("reason");
        }
    }

    @Nested
    class SerializeAndDeserialize {

        @Test
        void present_value_survives_serialization_and_deserialization() {
            //given
            final var original = new Sample();
            original.setNumber(JsonNullable.of(7L));
            original.setText(JsonNullable.of("reason"));

            //when
            final var json = mapper.writeValueAsString(original);
            final var roundTripped = mapper.readValue(json, Sample.class);

            //then
            assertThat(roundTripped.getNumber()).isEqualTo(JsonNullable.of(7L));
            assertThat(roundTripped.getText()).isEqualTo(JsonNullable.of("reason"));
        }

        @Test
        void field_becomes_present_null_because_json_has_no_undefined_representation() {
            //given
            final var original = new Sample();
            // both fields left undefined

            //when
            final var json = mapper.writeValueAsString(original);
            final var roundTripped = mapper.readValue(json, Sample.class);

            // then
            // there was a problem with undefined javascript field
            assertThat(roundTripped.getNumber()).isEqualTo(JsonNullable.of(null));
            assertThat(roundTripped.getNumber().isPresent()).isTrue();
        }

        @Test
        void mapperConfiguration_doesNotThrow() {
            assertThatCode(() -> mapper.writeValueAsString(new Sample())).doesNotThrowAnyException();
        }
    }
}
