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

import org.jspecify.annotations.Nullable;

/**
 * A {@code TargetSource} is used to obtain the current "target" of
 * an AOP invocation, which will be invoked via reflection if no around
 * advice chooses to end the interceptor chain itself.
 *
 * <p>If a {@code TargetSource} is "static", it will always return
 * the same target, allowing optimizations in the AOP framework. Dynamic
 * target sources can support pooling, hot swapping, etc.
 *
 * <p>Application developers don't usually need to work with
 * {@code TargetSources} directly: this is an AOP framework interface.
 *
 *
 * 1. 核心概念：什么是 TargetSource？
 * 在 Spring AOP 中，代理对象（Proxy）并不是直接持有目标对象（Target）的引用，而是持有一个 TargetSource。
 * 每次 AOP 拦截器链执行时，都会通过这个 TargetSource 去动态获取当前的目标对象。这种设计实现了目标对象的“解耦”，
 * 使得 Spring 可以实现对象池、延迟加载（Lazy）、热替换（Hot Swapping）等高级功能。
 * 2. 为什么强调“通过反射调用”？
 * AOP 拦截器链（Interceptor Chain）在底层执行时，当所有的增强逻辑（Advice）都走完，最终要调用目标方法时，
 * Spring 底层使用的是 Java 的反射机制（Method.invoke()）来触发这个目标对象。
 * 3. 关键条件：“如果没有环绕通知终结拦截器链”
 * 这是这段话的精髓。在 AOP 中，环绕通知（Around Advice） 拥有最高权限。
 * 正常情况：环绕通知内部会调用 proceed()，让拦截器链继续往下走，最终通过反射调用目标对象。
 * 短路情况：如果环绕通知内部不调用 proceed()（比如命中了缓存直接返回，或者发生异常直接抛出），拦截器链就被“终结”了。此时，TargetSource 获取到的目标对象根本不会被执行。
 * 总结：
 * “TargetSource 就是负责在运行时把真正的目标对象‘掏出来’的组件。只要拦截器链没有被环绕通知强行打断，这个被掏出来的目标对象最终就会被反射调用。”
 *
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 */
public interface TargetSource extends TargetClassAware {

	/**
	 * Return the type of targets returned by this {@link TargetSource}.
	 * <p>Can return {@code null}, although certain usages of a {@code TargetSource}
	 * might just work with a predetermined target class.
	 * @return the type of targets returned by this {@link TargetSource}
	 */
	@Override
	@Nullable Class<?> getTargetClass();

	/**
	 * Will all calls to {@link #getTarget()} return the same object?
	 * <p>In that case, there will be no need to invoke {@link #releaseTarget(Object)},
	 * and the AOP framework can cache the return value of {@link #getTarget()}.
	 * <p>The default implementation returns {@code false}.
	 * @return {@code true} if the target is immutable
	 * @see #getTarget
	 */
	default boolean isStatic() {
		return false;
	}

	/**
	 * Return a target instance. Invoked immediately before the
	 * AOP framework calls the "target" of an AOP method invocation.
	 * @return the target object which contains the joinpoint,
	 * or {@code null} if there is no actual target instance
	 * @throws Exception if the target object can't be resolved
	 */
	@Nullable Object getTarget() throws Exception;

	/**
	 * Release the given target object obtained from the
	 * {@link #getTarget()} method, if any.
	 * <p>The default implementation is empty.
	 * @param target object obtained from a call to {@link #getTarget()}
	 * @throws Exception if the object can't be released
	 */
	default void releaseTarget(Object target) throws Exception {
	}

}
