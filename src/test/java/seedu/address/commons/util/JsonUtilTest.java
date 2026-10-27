package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void fromJsonString_trailingGarbage_throwsIoException() {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "GARBAGE";

        assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_secondRoot_throwsIoException() {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + "{}";

        assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, SerializableTestClass.class));
    }

    @Test
    public void fromJsonString_trailingWhitespace_returnsObject() throws IOException {
        String json = SerializableTestClass.JSON_STRING_REPRESENTATION + " \n\t";

        SerializableTestClass deserializedObject = JsonUtil.fromJsonString(json, SerializableTestClass.class);

        assertEquals(SerializableTestClass.getNameTestValue(), deserializedObject.getName());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
