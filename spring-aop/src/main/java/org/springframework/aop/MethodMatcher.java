/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.aop;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;

/**
 * Part of a {@link Pointcut}: Checks whether the target method is eligible for advice.
 *
 * <p>A {@code MethodMatcher} may be evaluated <b>statically</b> or at <b>runtime</b>
 * (dynamically). Static matching involves a method and (possibly) method attributes.
 * Dynamic matching also makes arguments for a particular call available, and any
 * effects of running previous advice applying to the joinpoint.
 *
 * <p>If an implementation returns {@code false} from its {@link #isRuntime()}
 * method, evaluation(评估) can be performed statically, and the result will be the same
 * for all invocations of this method, whatever their arguments. This means that
 * if the {@link #isRuntime()} method returns {@code false}, the 3-arg
 * {@link #matches(Method, Class, Object[])} method will never be invoked.
 *
 * <p>If an implementation returns {@code true} from its 2-arg
 * {@link #matches(Method, Class)} method and its {@link #isRuntime()} method
 * returns {@code true}, the 3-arg {@link #matches(Method, Class, Object[])}
 * method will be invoked <i>immediately before each potential execution of the
 * related advice</i> to decide whether the advice should run. All previous advice,
 * such as earlier interceptors in an interceptor chain, will have run, so any
 * state changes they have produced in parameters or {@code ThreadLocal} state will
 * be available at the time of evaluation.
 *
 * <p><strong>WARNING</strong>: Concrete implementations of this interface must
 * provide proper implementations of {@link Object#equals(Object)},
 * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the
 * matcher to be used in caching scenarios &mdash; for example, in proxies generated
 * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation
 * must generate a unique string representation that aligns with the logic used
 * to implement {@code equals()}. See concrete implementations of this interface
 * within the framework for examples.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @since 11.11.2003
 * @see Pointcut
 * @see ClassFilter
 */
public interface MethodMatcher {

	/**
	 * Perform static checking to determine whether the given method matches.
	 * <p>If this method returns {@code false} or if {@link #isRuntime()}
	 * returns {@code false}, no runtime check (i.e. no
	 * {@link #matches(Method, Class, Object[])} call) will be made.
	 * @param method the candidate method
	 * @param targetClass the target class
	 * @return whether this method matches statically
	 */
	boolean matches(Method method, Class<?> targetClass);

	/**
	 * Is this {@code MethodMatcher} dynamic, that is, must a final check be made
	 * via the {@link #matches(Method, Class, Object[])} method at runtime even
	 * if {@link #matches(Method, Class)} returns {@code true}?
	 * <p>Can be invoked when an AOP proxy is created, and need not be invoked
	 * again before each method invocation.
	 * @return whether a runtime match via {@link #matches(Method, Class, Object[])}
	 * is required if static matching passed
	 */
	boolean isRuntime();

	/**
	 * Check whether there is a runtime (dynamic) match for this method, which
	 * must have matched statically.
	 * <p>This method is invoked only if {@link #matches(Method, Class)} returns
	 * {@code true} for the given method and target class, and if
	 * {@link #isRuntime()} returns {@code true}.
	 * <p>Invoked immediately before potential running of the advice, after any
	 * advice earlier in the advice chain has run.
	 * @param method the candidate method
	 * @param targetClass the target class
	 * @param args arguments to the method
	 * @return whether there's a runtime match
	 * @see #matches(Method, Class)
	 */
	boolean matches(Method method, Class<?> targetClass, @Nullable Object... args);


	/**
	 * Canonical instance of a {@code MethodMatcher} that matches all methods.
	 */
	MethodMatcher TRUE = TrueMethodMatcher.INSTANCE;

	/**
	 * 1. 核心术语解析
	 * Evaluated statically（静态评估）：在 Spring 中，这通常发生在创建代理对象时。此时 Spring 会检查目标类的方法，
	 * 判断它是否匹配切入点。因为此时还没有真正的方法调用，所以只能看“方法签名”和“方法属性”。
	 * Evaluated at runtime / dynamically（运行时/动态评估）：这发生在真正调用方法时。此时不仅有方法签名，还有调用时传入的真实参数（比如 login("admin", "123") 中的参数）。
	 * 2. 为什么需要区分这两种匹配？
	 * 这是 Spring AOP 为了极致的性能优化而设计的：
	 * 静态匹配是“第一道防线”：如果静态匹配就失败了，Spring 就不需要再管这个代理了，直接走原方法，这非常高效。
	 * 动态匹配是“第二道防线”：如果静态匹配成功了，但我们需要根据传入的参数来决定是否拦截（比如：只拦截参数包含 "admin" 的调用），这时候就需要用到动态匹配。
	 * 3. 关于“先前通知的影响（effects of running previous advice）”
	 * 这句话的后半段稍微有些晦涩。在 Spring AOP 中，如果有多个通知（Advice）拦截同一个方法，它们会形成一个拦截器链。动态匹配器在判断时，
	 * 能够“感知”到在这个拦截器链中，排在前面的通知执行后留下的状态或上下文信息。
	 * 总结：
	 * 这段注释是在说：“方法匹配不是一锤子买卖。我们可以先做粗粒度的静态匹配（看方法长什么样），
	 * 再做细粒度的动态匹配（看实际传入了什么参数以及前面拦截器的状态）。这样既保证了功能的强大，又兼顾了运行时的性能。”
	 */

}
