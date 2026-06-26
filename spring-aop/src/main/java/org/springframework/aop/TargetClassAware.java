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

import org.jspecify.annotations.Nullable;

/**
 * Minimal interface for exposing the target class behind a proxy.
 *
 * <p>Implemented by AOP proxy objects and proxy factories
 * (via {@link org.springframework.aop.framework.Advised})
 * as well as by {@link TargetSource TargetSources}.
 *
 * 1. 核心概念：“最小化接口（Minimal interface）”
 * 在 Spring 中，这个接口就是 TargetClassAware。它非常精简，通常只包含一个方法（比如 getTargetClass()）。
 * 所谓“最小化”，意味着它只提供最核心的能力——让外部能够知道这个代理对象背后包装的原始目标类到底是什么，而不暴露其他复杂的代理逻辑。
 * 2. 为什么需要“暴露目标类”？
 * 在 AOP 中，外部调用者拿到的都是代理对象（Proxy）。但在某些底层场景下，框架或开发者必须知道“面具”背后的“真身”（比如在进行反射操作、类型判断或序列化时）。
 * 这个接口就提供了一扇查看真身的窗户。
 * 3. 谁实现了这个接口？（三大阵营）
 * 这段话列举了三类实现了该接口的核心组件：
 * AOP 代理对象：被代理出来的对象本身，自然需要知道自己代理的是谁。
 * 代理工厂（Proxy Factories）：通过 Advised 接口间接实现。因为代理工厂负责制造代理，它当然知道目标类是谁。
 * 目标源（TargetSources）：在 Spring AOP 中，TargetSource 负责提供实际的目标对象（比如实现对象池或延迟加载）。既然它负责提供目标，它也必须实现这个接口来暴露目标类。
 * 总结：
 * “这是一个非常基础的接口，专门用来‘揭开代理的面纱’，查看背后的目标类。无论是代理对象本身、制造代理的工厂，还是提供目标对象的源头，都需要实现它来提供这个查询能力。”
 *
 * @author Juergen Hoeller
 * @since 2.0.3
 * @see org.springframework.aop.support.AopUtils#getTargetClass(Object)
 */
public interface TargetClassAware {

	/**
	 * Return the target class behind the implementing object
	 * (typically a proxy configuration or an actual proxy).
	 * @return the target Class, or {@code null} if not known
	 */
	@Nullable Class<?> getTargetClass();

}
