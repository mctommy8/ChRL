/**
 * (C) ChRL 2014 - chrl-utils - at.chrl.nutils.configuration.transformers - FunctionTransformer.java
 * Created: 10.08.2014 - 21:05:46
 */
package at.chrl.nutils.configuration.transformers;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;

import at.chrl.nutils.ClassUtils;
import at.chrl.nutils.configuration.PropertyTransformer;
import at.chrl.nutils.configuration.TransformationException;

/**
 * @author Vinzynth
 * @param <T>
 * @param <R>
 *
 */
public class FunctionTransformer<T, R> implements PropertyTransformer<Function<T, R>> {

	public static final FunctionTransformer<Object, Object> SHARED_INSTANCE = new FunctionTransformer<>();
	
	public static final Class<?>[] EMPTY_ARRAY = new Class<?>[0];
	
	/**
	 * {@inheritDoc}
	 * @see at.chrl.nutils.configuration.PropertyTransformer#transform(java.lang.String, java.lang.reflect.Field)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Function<T, R> transform(String value, Field field, Class<?>... types) throws TransformationException {
		try {
			if(!value.contains("::"))
				throw new IllegalArgumentException("Syntax for Function Property: <class>::<method reference>");
			String clazzString = value.substring(0, value.indexOf("::"));
			String methodString = value.substring(value.indexOf("::")+ 2);
			Class<T> clazz = (Class<T>) Class.forName(clazzString);
			types = Arrays.stream(types).map(ClassUtils::getPrimitiveClass).toArray(Class[]::new);
			Method m = clazz.getDeclaredMethod(methodString, types);
			
			return new Function<T, R>() {

				@Override
				public R apply(T t) {
					try {
						return (R) m.invoke(null, t);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			
		} catch (Exception e) {
			throw new TransformationException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see at.chrl.nutils.configuration.PropertyTransformer#transform(java.lang.String, java.lang.reflect.Field)
	 */
	@Override
	public Function<T, R> transform(String value, Field field) throws TransformationException {
		return transform(value, field, EMPTY_ARRAY);
	}

}
