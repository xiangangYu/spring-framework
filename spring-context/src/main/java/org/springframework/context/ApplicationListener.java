/*
 * Copyright 2002-2023 the original author or authors.
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

package org.springframework.context;

import java.util.EventListener;
import java.util.function.Consumer;

/**
 * Interface to be implemented by application event listeners.
 *
 * <p>Based on the standard {@link java.util.EventListener} interface for the
 * Observer design pattern.
 *
 * <p>An {@code ApplicationListener} can generically declare the event type that
 * it is interested in. When registered with a Spring {@code ApplicationContext},
 * events will be filtered accordingly, with the listener getting invoked for
 * matching event objects only.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @param <E> the specific {@code ApplicationEvent} subclass to listen to
 * @see org.springframework.context.ApplicationEvent
 * @see org.springframework.context.event.ApplicationEventMulticaster
 * @see org.springframework.context.event.SmartApplicationListener
 * @see org.springframework.context.event.GenericApplicationListener
 * @see org.springframework.context.event.EventListener
 */
@FunctionalInterface
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {

	/**
	 * Handle an application event.
	 * @param event the event to respond to
	 */
	void onApplicationEvent(E event);

	/**
	 * Return whether this listener supports asynchronous execution.
	 * @return {@code true} if this listener instance can be executed asynchronously
	 * depending on the multicaster configuration (the default), or {@code false} if it
	 * needs to immediately(立即) run within the original thread which published the event
	 * @since 6.1
	 * @see org.springframework.context.event.SimpleApplicationEventMulticaster#setTaskExecutor
	 */
	default boolean supportsAsyncExecution() {
		return true;
	}


	/**
	 * Create a new {@code ApplicationListener} for the given payload consumer.
	 * @param consumer the event payload consumer
	 * @param <T> the type of the event payload
	 * @return a corresponding {@code ApplicationListener} instance
	 * @since 5.3
	 * @see PayloadApplicationEvent
	 */
	static <T> ApplicationListener<PayloadApplicationEvent<T>> forPayload(Consumer<T> consumer) {
		return event -> consumer.accept(event.getPayload());
	}

	/**
	 * 在接口中添加default实现及静态方法，这种方式用得还是比较少
	 *
	 * Java 8 新特性：接口的静态方法和默认方法
	 * Java 8 允许给接口添加一个非抽象的方法实现，只需要使用 default 关键字即可，这个特征又叫做扩展方法（也称为默认方法或虚拟扩展方法
	 * 或防护方法）。在实现该接口时，该默认扩展方法在子类上可以直接使用，它的使用方式类似于抽象类中非抽象成员方法。
	 *
	 * 接口里可以声明静态方法，并且可以实现。
	 * private interface DefaulableFactory {
	 *    // Interfaces now allow static methods
	 *    static Defaulable create(Supplier< Defaulable > supplier ) {
	 *        return supplier.get();
	 *    }
	 * }
	 *
	 * public static void main( String[] args ) {
	 *    Defaulable defaulable = DefaulableFactory.create( DefaultableImpl::new );
	 *    System.out.println( defaulable.myDefalutMethod() );
	 *
	 *    defaulable = DefaulableFactory.create( OverridableImpl::new );
	 *    System.out.println( defaulable.myDefalutMethod() );
	 * }
	 */

}
