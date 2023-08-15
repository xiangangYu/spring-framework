/*
 * Copyright 2002-2022 the original author or authors.
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

package org.springframework.beans.factory.aot;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import org.springframework.javapoet.CodeBlock;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Code generator to apply {@link AutowiredArguments}.
 *
 * <p>Generates code in the form: {@code args.get(0), args.get(1)} or
 * {@code args.get(0, String.class), args.get(1, Integer.class)}
 *
 * <p>The simpler form is only used if the target method or constructor is
 * unambiguous.
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 6.0
 */
public class AutowiredArgumentsCodeGenerator {

	private final Class<?> target;

	/**
	 * public abstract sealed class Executable extends AccessibleObject implements Member, GenericDeclaration permits Constructor, Method
	 *
	 * 密封类的作用
	 * 在面向对象语言中，我们可以通过继承（extend）来实现类的能力复用、扩展与增强。但有的时候，有些能力我们不希望被继承了去做一些不
	 * 可预知的扩展。所以，我们需要对继承关系有一些限制的控制手段。而密封类的作用就是限制类的继承。
	 *
	 * 已有的限制手段
	 * 对于继承能力的控制，Java很早就已经有一些了，主要是这两种方式：
	 *
	 * final修饰类，这样类就无法被继承了
	 * package-private类（非public类），可以控制只能被同一个包下的类继承
	 * 但很显然，这两种限制方式的粒度都非常粗，如果有更精细化的限制需求的话，是很难实现的。
	 *
	 * 新特性：密封类
	 * 为了进一步增强限制能力，Java 17中的密封类增加了几个重要关键词：
	 *
	 * sealed：修饰类/接口，用来描述这个类/接口为密封类/接口
	 * non-sealed：修饰类/接口，用来描述这个类/接口为非密封类/接口
	 * permits：用在extends和implements之后，指定可以继承或实现的类
	 */
	private final Executable executable;


	public AutowiredArgumentsCodeGenerator(Class<?> target, Executable executable) {
		this.target = target;
		this.executable = executable;
	}


	public CodeBlock generateCode(Class<?>[] parameterTypes) {
		return generateCode(parameterTypes, 0, "args");
	}

	public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex) {
		return generateCode(parameterTypes, startIndex, "args");
	}

	public CodeBlock generateCode(Class<?>[] parameterTypes, int startIndex,
			String variableName) {

		Assert.notNull(parameterTypes, "'parameterTypes' must not be null");
		Assert.notNull(variableName, "'variableName' must not be null");
		boolean ambiguous = isAmbiguous();
		CodeBlock.Builder code = CodeBlock.builder();
		for (int i = startIndex; i < parameterTypes.length; i++) {
			code.add((i != startIndex) ? ", " : "");
			if (!ambiguous) {
				code.add("$L.get($L)", variableName, i - startIndex);
			}
			else {
				code.add("$L.get($L, $T.class)", variableName, i - startIndex,
						parameterTypes[i]);
			}
		}
		return code.build();
	}

	private boolean isAmbiguous() {
		if (this.executable instanceof Constructor<?> constructor) {
			return Arrays.stream(this.target.getDeclaredConstructors())
					.filter(Predicate.not(constructor::equals))
					.anyMatch(this::hasSameParameterCount);
		}
		if (this.executable instanceof Method method) {
			return Arrays.stream(ReflectionUtils.getAllDeclaredMethods(this.target))
					.filter(Predicate.not(method::equals))
					.filter(candidate -> candidate.getName().equals(method.getName()))
					.anyMatch(this::hasSameParameterCount);
		}
		return true;
	}

	private boolean hasSameParameterCount(Executable executable) {
		return this.executable.getParameterCount() == executable.getParameterCount();
	}

	// read for mark

}
