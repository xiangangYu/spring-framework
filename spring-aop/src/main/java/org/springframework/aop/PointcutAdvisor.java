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
 * Superinterface for all Advisors that are driven(驱动) by a pointcut.
 * This covers nearly all advisors except introduction advisors,
 * for which method-level matching doesn't apply.
 *
 * @author Rod Johnson
 */
public interface PointcutAdvisor extends Advisor {

	/**
	 * Get the Pointcut that drives this advisor.
	 */
	Pointcut getPointcut();

}

/**
 * 1. 核心术语解析
 * Superinterface（超级接口）：在 Java 中通常指顶层的父接口。在这里，它指的是 PointcutAdvisor 这个接口，它是所有常规 AOP 通知的基石。
 * Driven by a pointcut（由切入点驱动）：意思是这个 Advisor 是否生效，完全取决于切入点（Pointcut）的匹配结果。
 * 2. 为什么“引入（Introduction）”被排除在外？
 * 这段话的后半句完美呼应了（method matching doesn't make sense to introductions）。我们可以把 Spring 的 Advisor 分为两大阵营：
 * 常规 Advisor（受此接口管辖）：比如 @Before、@Around 等。它们必须配合 Pointcut 使用，精准拦截某些类的某些方法。
 * 引入 Advisor（IntroductionAdvisor）：它的目的是给整个类动态添加新接口。因为它是作用于整个类的，根本不需要去匹配具体的方法，所以它不属于这个由切入点驱动的超级接口。
 * 总结：
 * 这段注释其实是在给 Spring AOP 的体系“划清界限”：“这个接口是所有常规拦截器的老祖宗，但请注意，专门用来做动态引入（Introduction）的拦截器是个例外，因为它们根本不需要方法级别的匹配。
 */
