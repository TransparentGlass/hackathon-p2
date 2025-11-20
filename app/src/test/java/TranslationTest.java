

import java.net.HttpURLConnection;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.team.translator.Translator;

 
public class TranslationTest {
    private static final Logger logger = Logger.getLogger(TranslationTest.class.getName());

    @Test
    void testEstablishConnectionProperties() throws Exception {
        Translator translator = new Translator();
        HttpURLConnection conn = translator.establishConnection();

        assertNotNull(conn, "Connection should not be null");
        assertEquals("POST", conn.getRequestMethod(), "Request method should be POST");
        assertEquals("application/json", conn.getRequestProperty("Content-Type"), "Content-Type should be JSON");
        assertTrue(conn.getDoOutput(), "DoOutput should be true for sending data");
    }

    @Test
    void testEstablishConnectionResponseCode() throws Exception {
        Translator translator = new Translator();
        HttpURLConnection conn = translator.establishConnection();

        int status = conn.getResponseCode();
        assertTrue(status == 200 || status == 400, "Server should respond with a valid status");
    }


    @Test
    void translateToSpanishTest(){
        Translator translator = new Translator();
        String result = translator.translate("Hello", "auto", "fr");
        logger.info("Translation result: " + result);
        assertTrue(result.contains("Bonjour"), "Should contain bonjour");

    }

}
