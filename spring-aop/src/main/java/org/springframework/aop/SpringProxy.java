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
 * Marker interface implemented by all AOP proxies. Used to detect
 * whether objects are Spring-generated proxies.
 *
 * @author Rob Harrop
 * @since 2.0.1
 * @see org.springframework.aop.support.AopUtils#isAopProxy(Object)
 */
public interface SpringProxy {

}

/**
 * 1. 什么是“标记接口（Marker Interface）”？
 * 在 Java 中，标记接口通常不包含任何方法。它的作用纯粹是为了“打标签”。比如我们熟知的 java.io.Serializable（标记对象可被序列化）和 java.lang.Cloneable（标记对象可被克隆）。
 * 2. 这个接口的作用：Spring 的“防伪标志”
 * 在 Spring 框架中，这个接口是 org.springframework.aop.SpringProxy。
 * 当 Spring AOP 在底层通过 JDK 动态代理或 CGLIB 为目标对象生成代理类时，会强制让这个代理类实现 SpringProxy 接口。这就像是在每个代理对象身上盖了一个“Spring 官方认证”的钢印。
 * 3. 在底层代码中的实际应用
 * 有了这个标记接口，Spring 在运行时就可以非常安全、优雅地判断一个对象是不是自己生成的代理。
 * 在源码中，Spring 提供的工具类 AopUtils.isAopProxy(Object) 方法，底层其实就是去检查这个对象是否实现了 SpringProxy 接口。
 * 总结：
 * “只要是被 Spring AOP 动态生成的代理对象，就一定带有这个标记接口。框架底层就是靠查验这个‘防伪标志’，来精准识别出哪些对象是代理对象的。”
 */
