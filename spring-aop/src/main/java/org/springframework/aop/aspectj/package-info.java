/**
 * AspectJ integration package. Includes Spring AOP advice implementations for AspectJ 5
 * annotation-style methods, and an AspectJExpressionPointcut: a Spring AOP Pointcut
 * implementation that allows use of the AspectJ pointcut expression language with the Spring AOP
 * runtime framework.
 *
 * <p>Note that use of this package does <i>not</i> require the use of the {@code ajc} compiler
 * or AspectJ load-time weaver(加载时织入器). It is intended to enable the use of a valuable subset of AspectJ
 * functionality, with consistent semantics, with the proxy-based Spring AOP framework.
 *
 * 这段话是 Spring 官方在“正名”，它向开发者传递了三个极其重要的信息：
 * 借壳生蛋（只借语法）：Spring 只是把 AspectJ 5 的注解语法和切入点表达式语言拿过来用（即所谓的“valuable subset” 有价值的子集），让你写起来更方便。
 * 底层依然是代理（Proxy-based）：Spring AOP 的底层依然是基于 JDK/CGLIB 的动态代理机制，而不是 AspectJ 的字节码修改机制。
 * 零额外成本（No ajc/LTW）：你不需要为了使用 @Aspect 注解去专门配置 AspectJ 的编译器或复杂的类加载器，Spring 容器启动时自己就能搞定。
 */
@NullMarked
package org.springframework.aop.aspectj;

import org.jspecify.annotations.NullMarked;
