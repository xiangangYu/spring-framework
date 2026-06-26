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

/**
 * Filter that restricts matching of a pointcut(切入点) or introduction(引入) to a given set
 * of target classes.
 *
 * <p>Can be used as part of a {@link Pointcut} or for the entire targeting of
 * an {@link IntroductionAdvisor}.(引入通知器)
 *
 * <p><strong>WARNING</strong>: Concrete implementations of this interface must
 * provide proper implementations of {@link Object#equals(Object)},
 * {@link Object#hashCode()}, and {@link Object#toString()} in order to allow the
 * filter to be used in caching scenarios &mdash; for example, in proxies generated
 * by CGLIB. As of Spring Framework 6.0.13, the {@code toString()} implementation
 * must generate a unique string representation that aligns with the logic used
 * to implement {@code equals()}. See concrete implementations of this interface
 * within the framework for examples.
 *
 * @author Rod Johnson
 * @author Sam Brannen
 * @see Pointcut
 * @see MethodMatcher
 */
@FunctionalInterface
public interface ClassFilter {

	/**
	 * Should the pointcut apply to the given interface or target class?
	 * @param clazz the candidate target class
	 * @return whether the advice should apply to the given target class
	 */
	boolean matches(Class<?> clazz);


	/**
	 * Canonical instance of a {@code ClassFilter} that matches all classes.
	 */
	ClassFilter TRUE = TrueClassFilter.INSTANCE;

}

/**
 * 1. 核心术语解析
 * Filter（过滤器）：在 Spring AOP 中特指 ClassFilter。它的作用非常纯粹，就是用来判断“当前这个类，到底需不需要被代理/增强”。
 * Introduction（引入）：这是 AOP 中一个相对高级的功能，允许你在不修改原始类代码的情况下，动态地给目标类添加新的接口和实现（比如让一个普通的 User 类突然拥有了 Serializable 接口）。
 * IntroductionAdvisor（引入通知器）：专门用来管理“引入（Introduction）”功能的顾问类。
 * 2. 为什么需要这个 Filter？
 * 在 Spring AOP 的底层，一个完整的切入点（Pointcut）实际上是由两部分组成的：
 * ClassFilter（类过滤器）：先粗筛，判断这个类本身有没有资格被增强（比如只针对 UserService 类）。
 * MethodMatcher（方法匹配器）：再细筛，判断这个类里面的具体方法要不要被增强（比如只针对 login 方法）。
 * 这段注释的意思是：ClassFilter 可以在两个地方发挥作用：
 * 作为 Pointcut 的一部分：在普通的 AOP 拦截中，配合方法匹配器一起工作，精准定位目标。
 * 作为 IntroductionAdvisor 的整体目标：因为“引入（Introduction）”通常是给整个类赋予新的能力，所以它往往不需要精确到某个具体方法，直接用这个过滤器对整个类进行批量匹配即可。
 * 总结：
 * 这句话其实是 Spring 源码在解释 ClassFilter 的职责：“我是一个专门用来筛选目标类的工具。不管是普通的切面拦截，还是高级的动态引入，只要涉及到‘哪些类需要被处理’的问题，都可以用我来做限制。”
 */
