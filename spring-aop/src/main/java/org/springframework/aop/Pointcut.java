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
 * Core Spring pointcut abstraction.
 *
 * <p>A pointcut is composed of a {@link ClassFilter} and a {@link MethodMatcher}.
 * Both these basic terms(术语,组件) and a Pointcut itself can be combined to build up combinations
 * (for example, through {@link org.springframework.aop.support.ComposablePointcut}).
 *
 * @author Rod Johnson
 * @see ClassFilter
 * @see MethodMatcher
 * @see org.springframework.aop.support.Pointcuts
 * @see org.springframework.aop.support.ClassFilters
 * @see org.springframework.aop.support.MethodMatchers
 */
public interface Pointcut {

	/**
	 * Return the ClassFilter for this pointcut.
	 * @return the ClassFilter (never {@code null})
	 */
	ClassFilter getClassFilter();

	/**
	 * Return the MethodMatcher for this pointcut.
	 * @return the MethodMatcher (never {@code null})
	 */
	MethodMatcher getMethodMatcher();


	/**
	 * Canonical Pointcut instance that always matches.
	 */
	Pointcut TRUE = TruePointcut.INSTANCE;

}

/**
 * 1. 核心公式
 * 在 Spring AOP 中，切入点并不是一个单一的规则，而是两个规则的交集：
 * Pointcut = ClassFilter（决定对哪些类生效） + MethodMatcher（决定对这些类里的哪些方法生效）
 * 2. 什么是“组合（Combinations）”？
 * 在实际业务中，我们往往需要非常复杂的拦截规则。Spring 允许你把多个基础的 ClassFilter、MethodMatcher
 * 甚至整个 Pointcut 像搭积木一样拼装起来（比如通过 ComposablePointcut 类进行 AND、OR、NOT 等逻辑运算）。
 */
