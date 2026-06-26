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

import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;

/**
 * Extension of the AOP Alliance {@link org.aopalliance.intercept.MethodInvocation}
 * interface, allowing access to the proxy that the method invocation was made through.
 *
 * <p>Useful to be able to substitute(替换，替代) return values with the proxy,
 * if necessary, for example if the invocation target returned itself.
 *
 * 1. 核心背景：为什么要“替换返回值”？
 * 在 AOP 中，外部调用者拿到的其实是代理对象（Proxy）。但是，如果目标类（Target）的某个方法内部返回了 this（即目标对象自身），
 * 那么 AOP 拦截器拿到的返回值就是原始目标对象，而不是代理对象。
 * 2. 如果不替换会发生什么灾难？
 * 如果不做处理，调用者拿到原始目标对象后，再次调用该对象的方法时，就会绕过代理对象，导致 AOP 的拦截逻辑（如事务、日志、缓存等）完全失效。
 * 3. 这个接口的作用
 * 这个扩展接口（在 Spring 源码中通常是 ProxyMethodInvocation）提供了一个能力：让拦截器能够感知到当前的代理对象。当拦截器发现目标方法返回了 this 时，
 * 就可以利用这个接口，把返回值中的原始目标对象替换（substitute）成代理对象，从而保证 AOP 拦截链的完整性。
 * 总结：
 * “这个接口让拦截器能够拿到代理对象。当目标方法不小心返回了它自己（this）时，拦截器就可以用代理对象把它替换掉，从而防止 AOP 拦截链断裂。”
 *
 * @author Juergen Hoeller
 * @author Adrian Colyer
 * @since 1.1.3
 * @see org.springframework.aop.framework.ReflectiveMethodInvocation
 * @see org.springframework.aop.support.DelegatingIntroductionInterceptor
 */
public interface ProxyMethodInvocation extends MethodInvocation {

	/**
	 * Return the proxy that this method invocation was made through.
	 * @return the original proxy object
	 */
	Object getProxy();

	/**
	 * Create a clone of this object. If cloning is done before {@code proceed()}
	 * is invoked on this object, {@code proceed()} can be invoked once per clone
	 * to invoke the joinpoint (and the rest of the advice chain) more than once.
	 * invocable：可被调用的
	 *
	 * 1. 核心背景：为什么需要“克隆”？
	 * 在 Spring AOP 中，MethodInvocation（方法调用对象）代表了当前正在执行的拦截过程。默认情况下，一个 MethodInvocation
	 * 对象内部的拦截器索引（Index）是单向递增的，proceed() 方法通常只能被调用一次。如果强行调用第二次，它不会重新执行整个拦截器链。
	 * 2. 克隆机制的“魔法”
	 * 这段注释描述的是 Spring 提供的一种“重试/重入机制”：
	 * 当你在拦截器中，在第一次调用 proceed() 之前，先调用 clone() 复制出几个一模一样的对象。
	 * 因为每个克隆对象都有自己独立的状态（比如独立的索引），所以你可以对克隆对象 A 调用一次 proceed()，再对克隆对象 B 调用一次 proceed()。
	 * 结果就是：同一个目标方法（以及它后面的拦截器链）被完整地执行了多次！
	 * 3. 实际应用场景
	 * 这种机制在实际开发中非常有用，通常用于：
	 * 自动重试机制：比如调用远程服务或数据库时，如果发生网络异常，拦截器可以捕获异常，然后利用克隆对象重新调用 proceed()，实现透明的重试。
	 * 循环/重试逻辑：在满足特定条件时，反复执行目标方法。
	 * 总结：
	 * “这个拦截器对象支持克隆。只要你在第一次执行（proceed）之前把它复制几份，你就可以通过分别执行这些副本，让同一个方法和拦截器链被反复调用多次。”
	 *
	 * @return an invocable clone of this invocation.
	 * {@code proceed()} can be called once per clone.
	 */
	MethodInvocation invocableClone();

	/**
	 * Create a clone of this object. If cloning is done before {@code proceed()}
	 * is invoked on this object, {@code proceed()} can be invoked once per clone
	 * to invoke the joinpoint (and the rest of the advice chain) more than once.
	 * @param arguments the arguments that the cloned invocation is supposed to use,
	 * overriding the original arguments
	 * @return an invocable clone of this invocation.
	 * {@code proceed()} can be called once per clone.
	 */
	MethodInvocation invocableClone(@Nullable Object... arguments);

	/**
	 * Set the arguments to be used on subsequent(随后的) invocations in any advice
	 * in this chain.
	 * @param arguments the argument array
	 */
	void setArguments(@Nullable Object... arguments);

	/**
	 * Add the specified user attribute with the given value to this invocation.
	 * <p>Such attributes are not used within the AOP framework itself. They are
	 * just kept as part of the invocation object, for use in special interceptors.
	 * @param key the name of the attribute
	 * @param value the value of the attribute, or {@code null} to reset it
	 */
	void setUserAttribute(String key, @Nullable Object value);

	/**
	 * Return the value of the specified user attribute.
	 * @param key the name of the attribute
	 * @return the value of the attribute, or {@code null} if not set
	 * @see #setUserAttribute
	 */
	@Nullable Object getUserAttribute(String key);

}
