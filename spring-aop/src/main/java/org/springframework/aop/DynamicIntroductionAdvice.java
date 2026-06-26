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

import org.aopalliance.aop.Advice;

/**
 * Subinterface of AOP Alliance Advice that allows additional interfaces
 * to be implemented by an Advice, and available via a proxy using that
 * interceptor. This is a fundamental AOP concept called <b>introduction</b>.
 *
 * <p>Introductions are often <b>mixins</b>, enabling the building of composite(组合)
 * objects that can achieve many of the goals of multiple inheritance in Java.
 *
 * <p>Compared to {@link IntroductionInfo}, this interface allows an advice to
 * implement a range of interfaces that is not necessarily known in advance(提前).
 * Thus an {@link IntroductionAdvisor} can be used to specify which interfaces
 * will be exposed in an advised object.
 *
 * @author Rod Johnson
 * @since 1.1.1
 * @see IntroductionInfo
 * @see IntroductionAdvisor
 */
public interface DynamicIntroductionAdvice extends Advice {

	/**
	 * Does this introduction advice implement the given interface?
	 * @param intf the interface to check
	 * @return whether the advice implements the specified interface
	 */
	boolean implementsInterface(Class<?> intf);

}

/**
 * 1. 核心术语解析
 * IntroductionInfo：这是 Spring AOP 中较早期的一个接口。它的设计比较死板，要求你在写 Advice 的时候，就必须明确写出你要引入哪些接口（通常是写死在代码里的）。
 * Advice（通知）：在这里特指实现了 Introduction 能力的通知。
 * IntroductionAdvisor（引入通知器）：Spring AOP 中专门用来管理“引入”功能的顾问类。
 * 2. 这段话背后的架构哲学
 * 这段话解释了 Spring 为什么要引入更高级的接口（通常是指 IntroductionInterceptor 或相关机制）来替代或补充 IntroductionInfo：
 * 过去的痛点（IntroductionInfo）：如果你用 IntroductionInfo，你在写代码时就必须知道目标类要引入哪些接口。这在很多动态场景下是不灵活的。
 * 现在的优势（动态决定）：新的机制允许在运行时动态决定要引入什么接口。因为接口列表在编译期是未知的，
 * 所以 Spring 提供了 IntroductionAdvisor 这个“顾问”角色，让它在创建代理对象时，动态地指定并暴露出需要引入的接口。
 * 总结：
 * 这段注释其实是在夸赞 Spring AOP 设计的灵活性：“以前的做法太死板了，必须提前知道要引入啥；现在的设计更聪明，允许我们在运行时动态决定，
 * 并通过 IntroductionAdvisor 来灵活配置要暴露给代理对象的接口。”
 */
