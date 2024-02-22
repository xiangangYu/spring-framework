/*
 * Copyright 2002-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans.factory.aot;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.*;

import org.springframework.aot.hint.ExecutableMode;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.core.MethodParameter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.function.ThrowingConsumer;

/**
 * Resolver used to support the autowiring of methods. Typically used in
 * AOT-processed applications as a targeted alternative to the
 * {@link org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * AutowiredAnnotationBeanPostProcessor}.
 *
 * <p>When resolving arguments in a native image, the {@link Method} being used
 * must be marked with an {@link ExecutableMode#INTROSPECT introspection} hint
 * so that field annotations can be read. Full {@link ExecutableMode#INVOKE
 * invocation} hints are only required if the
 * {@link #resolveAndInvoke(RegisteredBean, Object)} method of this class is
 * being used (typically to support private methods).
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public final class AutowiredMethodArgumentsResolver extends AutowiredElementResolver {

	private final String methodName;

	private final Class<?>[] parameterTypes;

	private final boolean required;

	@Nullable
	private final String[] shortcuts;


	private AutowiredMethodArgumentsResolver(String methodName, Class<?>[] parameterTypes,
			boolean required, @Nullable String[] shortcuts) {

		Assert.hasText(methodName, "'methodName' must not be empty");
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.required = required;
		this.shortcuts = shortcuts;
	}


	/**
	 * Create a new {@link AutowiredMethodArgumentsResolver} for the specified
	 * method where injection is optional.
	 * @param methodName the method name
	 * @param parameterTypes the factory method parameter types
	 * @return a new {@link AutowiredFieldValueResolver} instance
	 */
	public static AutowiredMethodArgumentsResolver forMethod(String methodName, Class<?>... parameterTypes) {
		return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, false, null);
	}

	/**
	 * Create a new {@link AutowiredMethodArgumentsResolver} for the specified
	 * method where injection is required.
	 * @param methodName the method name
	 * @param parameterTypes the factory method parameter types
	 * @return a new {@link AutowiredFieldValueResolver} instance
	 */
	public static AutowiredMethodArgumentsResolver forRequiredMethod(String methodName, Class<?>... parameterTypes) {
		return new AutowiredMethodArgumentsResolver(methodName, parameterTypes, true, null);
	}

	/**
	 * Return a new {@link AutowiredMethodArgumentsResolver} instance
	 * that uses direct bean name injection shortcuts for specific parameters.
	 * @param beanNames the bean names to use as shortcuts (aligned with the
	 * method parameters)
	 * @return a new {@link AutowiredMethodArgumentsResolver} instance that uses
	 * the shortcuts
	 */
	public AutowiredMethodArgumentsResolver withShortcut(String... beanNames) {
		return new AutowiredMethodArgumentsResolver(this.methodName, this.parameterTypes, this.required, beanNames);
	}

	/**
	 * Resolve the method arguments for the specified registered bean and
	 * provide it to the given action.
	 * @param registeredBean the registered bean
	 * @param action the action to execute with the resolved method arguments
	 */
	public void resolve(RegisteredBean registeredBean, ThrowingConsumer<AutowiredArguments> action) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(action, "'action' must not be null");
		AutowiredArguments resolved = resolve(registeredBean);
		if (resolved != null) {
			action.accept(resolved);
		}
	}

	/**
	 * Resolve the method arguments for the specified registered bean.
	 * @param registeredBean the registered bean
	 * @return the resolved method arguments
	 */
	@Nullable
	public AutowiredArguments resolve(RegisteredBean registeredBean) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		return resolveArguments(registeredBean, getMethod(registeredBean));
	}

	/**
	 * Resolve the method arguments for the specified registered bean and invoke
	 * the method using reflection.
	 * @param registeredBean the registered bean
	 * @param instance the bean instance
	 */
	public void resolveAndInvoke(RegisteredBean registeredBean, Object instance) {
		Assert.notNull(registeredBean, "'registeredBean' must not be null");
		Assert.notNull(instance, "'instance' must not be null");
		Method method = getMethod(registeredBean);
		AutowiredArguments resolved = resolveArguments(registeredBean, method);
		if (resolved != null) {
			ReflectionUtils.makeAccessible(method);
			ReflectionUtils.invokeMethod(method, instance, resolved.toArray());
		}
	}

	@Nullable
	private AutowiredArguments resolveArguments(RegisteredBean registeredBean,
			Method method) {

		String beanName = registeredBean.getBeanName();
		Class<?> beanClass = registeredBean.getBeanClass();
		ConfigurableBeanFactory beanFactory = registeredBean.getBeanFactory();
		Assert.isInstanceOf(AutowireCapableBeanFactory.class, beanFactory);
		AutowireCapableBeanFactory autowireCapableBeanFactory = (AutowireCapableBeanFactory) beanFactory;
		int argumentCount = method.getParameterCount();
		Object[] arguments = new Object[argumentCount];
		Set<String> autowiredBeanNames = CollectionUtils.newLinkedHashSet(argumentCount);
		TypeConverter typeConverter = beanFactory.getTypeConverter();
		for (int i = 0; i < argumentCount; i++) {
			MethodParameter parameter = new MethodParameter(method, i);
			DependencyDescriptor descriptor = new DependencyDescriptor(parameter, this.required);
			descriptor.setContainingClass(beanClass);
			String shortcut = (this.shortcuts != null ? this.shortcuts[i] : null);
			if (shortcut != null) {
				descriptor = new ShortcutDependencyDescriptor(descriptor, shortcut);
			}
			try {
				Object argument = autowireCapableBeanFactory.resolveDependency(
						descriptor, beanName, autowiredBeanNames, typeConverter);
				if (argument == null && !this.required) {
					return null;
				}
				arguments[i] = argument;
			}
			catch (BeansException ex) {
				throw new UnsatisfiedDependencyException(null, beanName, new InjectionPoint(parameter), ex);
			}
		}
		registerDependentBeans(beanFactory, beanName, autowiredBeanNames);
		return AutowiredArguments.of(arguments);
	}

	private Method getMethod(RegisteredBean registeredBean) {
		Method method = ReflectionUtils.findMethod(registeredBean.getBeanClass(),
				this.methodName, this.parameterTypes);
		Assert.notNull(method, () ->
				"Method '%s' with parameter types [%s] declared on %s could not be found.".formatted(
						this.methodName, toCommaSeparatedNames(this.parameterTypes),
						registeredBean.getBeanClass().getName()));
		return method;
	}

	private String toCommaSeparatedNames(Class<?>... parameterTypes) {
		// 把类数组取名字后用逗号做分隔符，构建字符串，这种方式值得学习,下面是关于Stream的注释
		/**
		 * A sequence of elements supporting sequential and parallel aggregate
		 * operations.  The following example illustrates an aggregate operation using
		 * {@link Stream} and {@link IntStream}:
		 *
		 * <pre>{@code
		 *     int sum = widgets.stream()
		 *                      .filter(w -> w.getColor() == RED)
		 *                      .mapToInt(w -> w.getWeight())
		 *                      .sum();
		 * }</pre>
		 *
		 * In this example, {@code widgets} is a {@code Collection<Widget>}.  We create
		 * a stream of {@code Widget} objects via {@link Collection#stream Collection.stream()},
		 * filter it to produce a stream containing only the red widgets, and then
		 * transform it into a stream of {@code int} values representing the weight of
		 * each red widget. Then this stream is summed(总结、相加) to produce a total weight.
		 *
		 * <p>In addition to {@code Stream}, which is a stream of object references,
		 * there are primitive specializations for {@link IntStream}, {@link LongStream},
		 * and {@link DoubleStream}, all of which are referred to as "streams" and
		 * conform to the characteristics and restrictions described here.
		 *
		 * <p>To perform a computation(计算), stream
		 * <a href="package-summary.html#StreamOps">operations</a> are composed(组成) into a
		 * <em>stream pipeline</em>.  A stream pipeline consists of a source (which
		 * might be an array, a collection, a generator(发生器) function, an I/O channel(渠道),
		 * etc), zero or more <em>intermediate operations</em> (which transform a
		 * stream into another stream, such as {@link Stream#filter(Predicate)}), and a
		 * <em>terminal operation</em> (which produces a result or side-effect, such
		 * as {@link Stream#count()} or {@link Stream#forEach(Consumer)}).
		 * Streams are lazy; computation on the source data is only performed when the
		 * terminal operation is initiated, and source elements are consumed only
		 * as needed.
		 *
		 * <p>A stream implementation is permitted significant latitude(纬度) in optimizing
		 * the computation of the result.  For example, a stream implementation is free
		 * to elide(省略) operations (or entire stages) from a stream pipeline -- and
		 * therefore elide invocation of behavioral parameters -- if it can prove that
		 * it would not affect the result of the computation.  This means that
		 * side-effects of behavioral parameters may not always be executed and should
		 * not be relied upon, unless otherwise specified (such as by the terminal
		 * operations {@code forEach} and {@code forEachOrdered}). (For a specific
		 * example of such an optimization, see the API note documented on the
		 * {@link #count} operation.  For more detail, see the
		 * <a href="package-summary.html#SideEffects">side-effects</a> section of the
		 * stream package documentation.)
		 *
		 * <p>Collections and streams, while bearing(承受) some superficial(肤浅的) similarities,
		 * have different goals.  Collections are primarily concerned with the efficient
		 * management of, and access to, their elements.  By contrast, streams do not
		 * provide a means to directly access or manipulate their elements, and are
		 * instead concerned with declaratively describing their source and the
		 * computational(计算) operations which will be performed in aggregate on that source.
		 * However, if the provided stream operations do not offer the desired
		 * functionality, the {@link #iterator()} and {@link #spliterator()} operations
		 * can be used to perform a controlled traversal(遍历).
		 *
		 * <p>A stream pipeline, like the "widgets" example above, can be viewed as
		 * a <em>query</em> on the stream source.  Unless the source was explicitly
		 * designed for concurrent modification (such as a {@link ConcurrentHashMap}),
		 * unpredictable or erroneous behavior may result from modifying the stream
		 * source while it is being queried.
		 *
		 * <p>Most stream operations accept parameters that describe user-specified
		 * behavior, such as the lambda expression {@code w -> w.getWeight()} passed to
		 * {@code mapToInt} in the example above.  To preserve correct behavior,
		 * these <em>behavioral parameters</em>:
		 * <ul>
		 * <li>must be <a href="package-summary.html#NonInterference">non-interfering</a>
		 * (they do not modify the stream source); and</li>
		 * <li>in most cases must be <a href="package-summary.html#Statelessness">stateless</a>
		 * (their result should not depend on any state that might change during execution
		 * of the stream pipeline).</li>
		 * </ul>
		 *
		 * <p>Such parameters are always instances of a
		 * <a href="../function/package-summary.html">functional interface</a> such
		 * as {@link java.util.function.Function}, and are often lambda expressions or
		 * method references.  Unless otherwise specified these parameters must be
		 * <em>non-null</em>.
		 *
		 * <p>A stream should be operated on (invoking an intermediate or terminal stream
		 * operation) only once.  This rules out, for example, "forked" streams, where
		 * the same source feeds two or more pipelines, or multiple traversals(遍历) of the
		 * same stream.  A stream implementation may throw {@link IllegalStateException}
		 * if it detects that the stream is being reused. However, since some stream
		 * operations may return their receiver rather than a new stream object, it may
		 * not be possible to detect reuse in all cases.
		 *
		 * <p>Streams have a {@link #close()} method and implement {@link AutoCloseable}.
		 * Operating on a stream after it has been closed will throw {@link IllegalStateException}.
		 * Most stream instances do not actually need to be closed after use, as they
		 * are backed by collections, arrays, or generating functions, which require no
		 * special resource management. Generally, only streams whose source is an IO channel,
		 * such as those returned by {@link Files#lines(Path)}, will require closing. If a
		 * stream does require closing, it must be opened as a resource within a try-with-resources
		 * statement or similar control structure to ensure that it is closed promptly(及时地) after its
		 * operations have completed.
		 *
		 * <p>Stream pipelines may execute either sequentially or in
		 * <a href="package-summary.html#Parallelism">parallel</a>.  This
		 * execution mode is a property of the stream.  Streams are created
		 * with an initial choice of sequential or parallel execution.  (For example,
		 * {@link Collection#stream() Collection.stream()} creates a sequential stream,
		 * and {@link Collection#parallelStream() Collection.parallelStream()} creates
		 * a parallel one.)  This choice of execution mode may be modified by the
		 * {@link #sequential()} or {@link #parallel()} methods, and may be queried with
		 * the {@link #isParallel()} method.
		 */
		return Arrays.stream(parameterTypes).map(Class::getName)
				.collect(Collectors.joining(", "));
	}

}
