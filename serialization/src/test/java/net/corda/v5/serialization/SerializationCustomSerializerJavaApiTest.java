package net.corda.v5.serialization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializationCustomSerializerJavaApiTest {

    private final BaseProxyTestClass baseProxyTestClass = new BaseProxyTestClass();
    private final BaseTestClass<String> obj = new BaseTestClass<>();
    private final ProxyTestClass proxy = new ProxyTestClass();

    @Test
    public void toProxy() {
        var result = baseProxyTestClass.toProxy(obj);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(proxy);
    }

    @Test
    public void fromProxy() {
        var result = baseProxyTestClass.fromProxy(proxy);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(obj);
    }

    static class BaseTestClass<T> {

    }

      class BaseProxyTestClass implements SerializationCustomSerializer<BaseTestClass<?>, ProxyTestClass> {

        @Override
        public ProxyTestClass toProxy(BaseTestClass<?> baseTestClass) {
            return proxy;
        }

        @Override
        public BaseTestClass<?> fromProxy(ProxyTestClass proxyTestClass) {
            return obj;
        }
    }

    static class ProxyTestClass {

    }
}
