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
 * Advice invoked before a method is invoked. Such advices cannot
 * prevent the method call proceeding, unless they throw a Throwable(异常).
 *
 * 这句话揭示了前置通知（Before Advice）和环绕通知（Around Advice）在底层能力上的本质区别：
 * 前置通知的局限性（无法阻止执行）：
 * 前置通知的代码逻辑和目标方法的执行逻辑是分离的。Spring 框架在执行完你的前置通知后，会无条件地接着去执行目标方法。
 * 因此，你没办法在前置通知里写个 if (条件不满足) return; 来阻止目标方法执行。
 *
 * 唯一的例外（抛出异常）：
 * 既然不能通过常规代码阻止，那怎么让目标方法不执行呢？唯一的方法就是抛出异常（throw a Throwable）。一旦抛出异常，
 * 正常的执行流就会被打断，目标方法自然就不会再执行了（当然，异常会被向上抛出，由全局异常处理器或调用方捕获）。
 *
 * 总结：
 * 这句话其实是 Spring 源码在提醒开发者：“如果你想在方法执行前做拦截，并且有可能需要阻止该方法执行（比如权限校验不通过时直接拒绝访问），
 * 千万不要用前置通知（Before Advice），而应该使用环绕通知（Around Advice），因为只有环绕通知才拥有控制目标方法是否执行的最高权限。
 *
 * @author Rod Johnson
 * @see AfterReturningAdvice
 * @see ThrowsAdvice
 */
public interface MethodBeforeAdvice extends BeforeAdvice {

	/**
	 * Callback before a given method is invoked.
	 * @param method the method being invoked
	 * @param args the arguments to the method
	 * @param target the target of the method invocation. May be {@code null}.
	 * @throws Throwable if this object wishes to abort the call.
	 * Any exception thrown will be returned to the caller if it's
	 * allowed by the method signature. Otherwise the exception
	 * will be wrapped as a runtime exception.
	 */
	void before(Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable;

}
/**
 * 在 Spring 中，我们通常使用 @Aspect 注解来声明一个环绕通知
 * @Aspect
 * @Component
 * public class PerformanceAspect {
 *
 *     // 定义切入点：匹配 com.example.service 包下所有类的所有方法
 *     @Pointcut("execution(* com.example.service.*.*(..))")
 *     public void serviceLayer() {}
 *
 *     @Around("serviceLayer()")
 *     public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
 *         long startTime = System.currentTimeMillis();
 *
 *         try {
 *             // 【核心】调用 proceed() 让目标方法执行
 *             Object result = joinPoint.proceed();
 *
 *             // 可以在这里修改返回值，或者打印成功日志
 *             return result;
 *         } catch (Exception e) {
 *             // 可以在这里捕获异常，进行统一处理
 *             System.err.println("方法执行出错: " + e.getMessage());
 *             throw e; // 继续向上抛出，或者返回一个默认值
 *         } finally {
 *             long endTime = System.currentTimeMillis();
 *             System.out.println("方法耗时: " + (endTime - startTime) + "ms");
 *         }
 *     }
 * }
 *
 * Spring AOP 借用了 AspectJ 的“菜谱”（注解），但用的是自己的一套“烹饪方法”（底层实现）。
 * 为了彻底理清这个关系，梳理了以下三个核心要点：
 * 1. 为什么注解在 AspectJ 包下？
 * Spring 并没有自己发明一套新的注解，而是直接复用了 AspectJ 框架中定义好的注解接口（位于 org.aspectj.lang.annotation 包下），比如 @Aspect、@Before、@Pointcut 等。
 * Spring 官方这么做的原因是：AspectJ 的这套注解语法已经非常成熟且表达能力强，Spring 团队觉得没必要再重复造轮子，直接“拿来主义”作为自己 AOP 的配置风格。
 * 2. 为什么 Spring 要依赖 AspectJ 的包？
 * 虽然注解是 AspectJ 的，但 Spring AOP 的底层实现完全不依赖 AspectJ 的编译器或织入器。
 * Spring 在运行时，会通过自己内部的类（如 AnnotationAwareAspectJAutoProxyCreator）去扫描和解析这些 @Aspect 注解，然后通过 JDK 动态代理或 CGLIB 动态代理来生成代理对象，完成 AOP 的织入。
 * 3. 一个通俗的类比
 * 你可以把它们的关系理解为两家餐厅的关系：
 * AspectJ 和 Spring AOP 是两家不同的餐厅。
 * 它们使用了完全相同的菜谱（即相同的注解定义）。
 * 但是，AspectJ 餐厅使用的是“猛火快炒”（编译期静态织入，直接修改字节码）；而 Spring AOP 餐厅使用的是“摆盘装饰”（运行时动态代理，不修改原始代码）。
 *
 * 总结：
 *  @Aspect 确实是 AspectJ 的注解，但这仅仅是一个语法层面的借用。Spring AOP 只是在运行时读取了这些注解，其底层依然是纯正的 Spring 动态代理机制，与 AspectJ 的底层实现没有任何关系。
 *
 */
