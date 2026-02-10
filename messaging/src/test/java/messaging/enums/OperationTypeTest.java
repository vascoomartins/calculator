package messaging.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OperationTypeTest {

    @Test
    void testSumSymbol() {
        assertEquals("+", OperationType.SUM.getSymbol());
    }

    @Test
    void testSubtractSymbol() {
        assertEquals("-", OperationType.SUBTRACT.getSymbol());
    }

    @Test
    void testMultiplySymbol() {
        assertEquals("*", OperationType.MULTIPLY.getSymbol());
    }

    @Test
    void testDivideSymbol() {
        assertEquals("/", OperationType.DIVIDE.getSymbol());
    }

    @Test
    void testSumDescription() {
        assertEquals("Addition", OperationType.SUM.getDescription());
    }

    @Test
    void testSubtractDescription() {
        assertEquals("Subtraction", OperationType.SUBTRACT.getDescription());
    }

    @Test
    void testMultiplyDescription() {
        assertEquals("Multiplication", OperationType.MULTIPLY.getDescription());
    }

    @Test
    void testDivideDescription() {
        assertEquals("Division", OperationType.DIVIDE.getDescription());
    }

    @Test
    void testToString() {
        assertEquals("Addition (+)", OperationType.SUM.toString());
        assertEquals("Subtraction (-)", OperationType.SUBTRACT.toString());
        assertEquals("Multiplication (*)", OperationType.MULTIPLY.toString());
        assertEquals("Division (/)", OperationType.DIVIDE.toString());
    }

    @Test
    void testValueOf() {
        assertEquals(OperationType.SUM, OperationType.valueOf("SUM"));
        assertEquals(OperationType.SUBTRACT, OperationType.valueOf("SUBTRACT"));
        assertEquals(OperationType.MULTIPLY, OperationType.valueOf("MULTIPLY"));
        assertEquals(OperationType.DIVIDE, OperationType.valueOf("DIVIDE"));
    }

    @Test
    void testValues() {
        OperationType[] values = OperationType.values();
        assertEquals(4, values.length);
    }
}

