package sumit.ai.ai_engineering.ai.tools.utility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CalculatorToolTest {

    private CalculatorTool calculator;

    @BeforeEach
    void setUp() {
        calculator = new CalculatorTool();
    }

    @Test
    void add_twoPositiveNumbers_returnsSum() {
        assertThat(calculator.add(3.0, 4.0)).isEqualTo(7.0);
    }

    @Test
    void add_negativeNumbers_returnsCorrectSum() {
        assertThat(calculator.add(-2.5, -1.5)).isEqualTo(-4.0);
    }

    @Test
    void subtract_returnsCorrectDifference() {
        assertThat(calculator.subtract(10.0, 4.0)).isEqualTo(6.0);
    }

    @Test
    void multiply_twoNumbers_returnsProduct() {
        assertThat(calculator.multiply(3.0, 5.0)).isEqualTo(15.0);
    }

    @Test
    void multiply_byZero_returnsZero() {
        assertThat(calculator.multiply(100.0, 0.0)).isEqualTo(0.0);
    }

    @Test
    void divide_returnsCorrectQuotient() {
        assertThat(calculator.divide(10.0, 4.0)).isEqualTo(2.5);
    }

    @Test
    void divide_byZero_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> calculator.divide(5.0, 0.0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Division By Zero");
    }

    @Test
    void category_returnsUtility() {
        assertThat(calculator.category().name()).isEqualTo("UTILITY");
    }
}
