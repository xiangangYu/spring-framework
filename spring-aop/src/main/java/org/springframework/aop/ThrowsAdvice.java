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
 * Tag interface for throws advice.
 *
 * <p>There are not any methods on this interface, as methods are invoked by
 * reflection. Implementing classes must implement methods of the form:
 *
 * <pre class="code">void afterThrowing([Method, args, target], ThrowableSubclass);</pre>
 *
 * <p>Some examples of valid methods would be:
 *
 * <pre class="code">public void afterThrowing(Exception ex)</pre>
 * <pre class="code">public void afterThrowing(RemoteException ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, Exception ex)</pre>
 * <pre class="code">public void afterThrowing(Method method, Object[] args, Object target, ServletException ex)</pre>
 *
 * <p>The first three arguments are optional, and only useful if we want further
 * information about the joinpoint, as in AspectJ <b>after-throwing</b> advice.
 *
 * <p><b>Note:</b> If a throws-advice method throws an exception itself, it will
 * override the original exception (i.e. change the exception thrown to the user).
 * The overriding exception will typically be a RuntimeException; this is compatible
 * with any method signature. However, if a throws-advice method throws a checked
 * exception, it will have to match the declared exceptions of the target method
 * and is hence(因此) to some degree coupled to(与...耦合) specific target method signatures.
 * <b>Do not throw an undeclared checked exception that is incompatible with
 * the target method's signature!</b>
 *
 *
 * 1. 核心机制：异常覆盖（Override the original exception）
 * 在 Spring AOP 底层，当目标方法抛出异常时，框架会触发 ThrowsAdvice。如果在这个通知方法里又抛出了一个新异常，
 * Spring 会直接丢弃原始异常，把新异常抛给上层调用者。这就实现了异常的转换（比如把底层的 SQLException 转换为上层的业务异常）。
 * 2. 为什么推荐抛出 RuntimeException？
 * Java 的语法规定，方法签名如果没有声明受检异常（Checked Exception），调用者就不需要强制捕获它。
 * 如果 ThrowsAdvice 抛出的是 RuntimeException，它可以无缝地套用在任何目标方法上，因为所有方法都允许抛出运行时异常。
 * 3. 受检异常（Checked Exception）的“坑”
 * 如果你非要在 ThrowsAdvice 里抛出一个受检异常（比如 IOException），就会遇到 Java 编译器和底层反射机制的严格限制：
 * 这个 IOException 必须出现在目标方法的 throws 声明中。
 * 如果你的 ThrowsAdvice 被绑定到了一个没有声明抛出 IOException 的目标方法上，底层在通过反射抛出这个异常时就会发生冲突，甚至导致程序崩溃。
 * 总结：
 * “在异常通知里转换异常时，最好抛出运行时异常，这样最安全。如果非要抛出受检异常，一定要确保目标方法也声明了这个异常，否则会导致签名不兼容的严重问题。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see AfterReturningAdvice
 * @see MethodBeforeAdvice
 */
public interface ThrowsAdvice extends AfterAdvice {

}

/**
 * 1. 为什么叫 ThrowsAdvice？
 * 在 Spring AOP 中，所有的增强逻辑（Advice）都是按照它们切入的时机来命名的：
 * BeforeAdvice：在方法执行前切入（前置通知）
 * AfterReturningAdvice：在方法正常返回后切入（返回后通知）
 * ThrowsAdvice：在方法抛出异常时切入（异常通知）
 * 所以，Throws 代表的是“抛出异常”这个动作，Advice 代表“通知/增强”。
 * 2. 它的核心作用
 * 当一个被代理的目标方法在执行过程中抛出了异常（Exception），Spring AOP 就会拦截这个异常，并触发绑定的 ThrowsAdvice。
 * 开发者可以在这里进行统一的异常处理、日志记录、告警上报等操作，而不需要在每个业务方法里写 try-catch。
 * 3. 源码层面的特殊性
 * 值得注意的是，在 Spring 源码中，ThrowsAdvice 本身只是一个标记接口（Marker Interface），它里面没有任何方法定义。
 * 具体的异常处理方法是由开发者自己定义的，但必须遵循特定的命名规范，例如：
 *
 * void afterThrowing(Method method, Object[] args, Object target, Exception ex);
 * Spring 底层会通过反射机制去探测并调用这个特定签名的方法。
 *
 * 源码小贴士：
 * 下次在源码里看到 ThrowsAdvice，你可以直接把它脑补为“专门用来拦截和处理方法异常的增强逻辑”。它代表着底层框架在说：“这个方法抛异常了，我来帮你处理一下。”
 */
