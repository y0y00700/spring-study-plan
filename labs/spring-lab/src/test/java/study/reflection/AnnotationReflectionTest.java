package study.reflection;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationReflectionTest {

    @Test
    void annotationCheckAndMethodInvocationAreSeparateOperations() throws Exception {
        PaymentService service = new PaymentService();
        Method method = PaymentService.class.getDeclaredMethod("pay");

        boolean present = method.isAnnotationPresent(Tracked.class);
        int countAfterCheck = service.callCount;

        method.invoke(service);
        int countAfterInvoke = service.callCount;

        // TODO: present에 대한 assertion을 작성하세요.
        assertTrue(present);
        // TODO: countAfterCheck에 대한 assertion을 작성하세요.
        assertEquals(0,countAfterCheck);
        // TODO: countAfterInvoke에 대한 assertion을 작성하세요.
        assertEquals(1,countAfterInvoke);
    }

    @Test
    void classRetentionIsInvisibleToRuntimeReflectionButMethodCanStillRun() throws Exception {
        ClassRetentionPaymentService service = new ClassRetentionPaymentService();
        Method method = ClassRetentionPaymentService.class.getDeclaredMethod("pay");

        boolean present = method.isAnnotationPresent(ClassTracked.class);

        method.invoke(service);
        int countAfterInvoke = service.callCount;

        // TODO: present에 대한 assertion을 작성하세요.
        assertFalse(present);
        // TODO: countAfterInvoke에 대한 assertion을 작성하세요.
        assertEquals(1,countAfterInvoke);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Tracked {
    }

    @Retention(RetentionPolicy.CLASS)
    @interface ClassTracked {
    }

    static class PaymentService {
        int callCount = 0;

        @Tracked
        void pay() {
            callCount++;
        }
    }

    static class ClassRetentionPaymentService {
        int callCount = 0;

        @ClassTracked
        void pay() {
            callCount++;
        }
    }
}
