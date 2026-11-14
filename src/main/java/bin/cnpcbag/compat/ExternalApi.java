package bin.cnpcbag.compat;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/** Only optional mod API names are reflected; Minecraft methods use mapped Java calls. */
public final class ExternalApi {
    private static final Map<String, Method> METHODS = new ConcurrentHashMap<>();

    public static Object call(Object receiver, String name, Class<?>[] parameters, Object... arguments)
            throws ReflectiveOperationException {
        Class<?> owner = receiver instanceof Class<?> type ? type : receiver.getClass();
        String key = owner.getName() + "#" + name + Arrays.toString(parameters);
        Method method = METHODS.get(key);
        if (method == null) {
            method = owner.getMethod(name, parameters);
            METHODS.put(key, method);
        }
        return method.invoke(receiver instanceof Class<?> ? null : receiver, arguments);
    }

    public static Object call(Object receiver, String name) throws ReflectiveOperationException {
        return call(receiver, name, new Class<?>[0]);
    }

    public static Class<?> type(String name) throws ClassNotFoundException {
        return Class.forName(name, true, ExternalApi.class.getClassLoader());
    }

    private ExternalApi() {}
}
