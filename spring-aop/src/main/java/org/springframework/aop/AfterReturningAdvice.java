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
 * After returning advice is invoked only on normal method return, not if an
 * exception is thrown. Such advice can see the return value, but cannot change it.
 *
 * 后置返回通知（After returning advice）仅在方法正常返回时被调用，如果抛出了异常，则不会被调用。
 *
 * After returning advice：方法正常返回 ➡️ 执行；方法抛出异常 ➡️ 不执行。
 * After advice：无论方法是正常返回还是抛出异常，都会执行（它的行为非常类似于 Java 中的 finally 代码块）。
 *
 * @author Rod Johnson
 * @see MethodBeforeAdvice
 * @see ThrowsAdvice
 */
public interface AfterReturningAdvice extends AfterAdvice {

	/**
	 * Callback after a given method successfully returned.
	 * @param returnValue the value returned by the method, if any
	 * @param method the method being invoked
	 * @param args the arguments to the method
	 * @param target the target of the method invocation. May be {@code null}.
	 * @throws Throwable if this object wishes to abort the call.
	 * Any exception thrown will be returned to the caller if it's
	 * allowed by the method signature. Otherwise the exception
	 * will be wrapped as a runtime exception.
	 */
	void afterReturning(@Nullable Object returnValue, Method method, @Nullable Object[] args, @Nullable Object target) throws Throwable;

}
