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
 * Marker for AOP proxy interfaces (in particular: introduction interfaces)
 * that explicitly intend to return the raw target object (which would normally
 * get replaced with the proxy object when returned from a method invocation).
 *
 * <p>Note that this is a marker interface in the style of {@link java.io.Serializable},
 * semantically(在语义上) applying to a declared interface rather than to the full class
 * of a concrete object. In other words, this marker applies to a particular
 * interface only (typically an introduction interface that does not serve
 * as the primary interface of an AOP proxy), and hence does not affect
 * other interfaces that a concrete AOP proxy may implement.
 *
 * @author Juergen Hoeller
 * @since 2.0.5
 * @see org.springframework.aop.scope.ScopedObject
 */
public interface RawTargetAccess {

}

/**
 * 1. 核心背景：Spring AOP 的“默认替换规则”
 * 正如你之前看过的源码，Spring AOP 有一个默认行为：为了防止 AOP 拦截链断裂，如果目标方法返回了 this（原始目标对象），Spring 会强制把它替换（substitute）成代理对象。
 * 2. 这个标记接口的作用：打破默认规则
 * 在某些特殊场景下，我们就是希望返回原始目标对象，而不是代理对象。这时，只要让某个接口继承这个“标记接口”，
 * Spring 在底层拦截时就会“网开一面”：如果发现返回值属于这个标记接口，就不再将其替换为代理对象，而是直接放行原始对象。
 * 3. 为什么强调“仅作用于特定接口”？
 * 这是这段话后半段的重点。在 Java 中，一个代理对象可能同时实现了多个接口（比如接口 A 和接口 B）。
 * 如果接口 A 继承了这个标记接口，那么通过接口 A 调用的方法返回原始对象。
 * 但这绝对不会影响接口 B。通过接口 B 调用的方法，依然会遵循默认的“替换为代理对象”的规则。
 * 总结：
 * “这是一个特权标记。如果你希望某个接口的返回值保留原始目标对象的真身，不被 Spring 偷偷替换成代理对象，就给它打上这个标记。
 * 而且这个特权是隔离的，只对打了标记的那个接口生效，不会波及代理对象身上的其他接口。”
 */
