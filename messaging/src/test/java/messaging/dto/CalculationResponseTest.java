package messaging.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CalculationResponseTest {

    @Test
    void testSuccessFactoryMethod() {
        CalculationResponse response = CalculationResponse.success("req-1", new BigDecimal("42"));

        assertEquals("req-1", response.getRequestId());
        assertEquals(new BigDecimal("42"), response.getResult());
        assertNull(response.getError());
        assertTrue(response.isSuccess());
        assertFalse(response.isError());
    }

    @Test
    void testErrorFactoryMethod() {
        CalculationResponse response = CalculationResponse.error("req-2", "Division by zero");

        assertEquals("req-2", response.getRequestId());
        assertNull(response.getResult());
        assertEquals("Division by zero", response.getError());
        assertFalse(response.isSuccess());
        assertTrue(response.isError());
    }

    @Test
    void testFullConstructor() {
        CalculationResponse response = new CalculationResponse("req-3", new BigDecimal("100"), null);

        assertEquals("req-3", response.getRequestId());
        assertEquals(new BigDecimal("100"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testSetters() {
        CalculationResponse response = new CalculationResponse();
        response.setRequestId("req-4");
        response.setResult(new BigDecimal("50"));
        response.setError(null);

        assertEquals("req-4", response.getRequestId());
        assertEquals(new BigDecimal("50"), response.getResult());
        assertNull(response.getError());
    }

    @Test
    void testEqualsSuccess() {
        CalculationResponse response1 = CalculationResponse.success("req-1", new BigDecimal("42"));
        CalculationResponse response2 = CalculationResponse.success("req-1", new BigDecimal("42"));

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testEqualsError() {
        CalculationResponse response1 = CalculationResponse.error("req-1", "Error");
        CalculationResponse response2 = CalculationResponse.error("req-1", "Error");

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testNotEquals() {
        CalculationResponse response1 = CalculationResponse.success("req-1", new BigDecimal("42"));
        CalculationResponse response2 = CalculationResponse.success("req-1", new BigDecimal("43"));

        assertNotEquals(response1, response2);
    }

    @Test
    void testToStringSuccess() {
        CalculationResponse response = CalculationResponse.success("req-1", new BigDecimal("42"));
        String str = response.toString();

        assertTrue(str.contains("req-1"));
        assertTrue(str.contains("42"));
        assertFalse(str.contains("error"));
    }

    @Test
    void testToStringError() {
        CalculationResponse response = CalculationResponse.error("req-2", "Division by zero");
        String str = response.toString();

        assertTrue(str.contains("req-2"));
        assertTrue(str.contains("Division by zero"));
    }

    @Test
    void testIsSuccessWithNullResult() {
        CalculationResponse response = new CalculationResponse("req-1", null, null);

        assertFalse(response.isSuccess());
    }
}

