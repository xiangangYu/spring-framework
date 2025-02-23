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

package org.springframework.beans.factory.annotation;

import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/**
 * {@link RuntimeHintsRegistrar} for Jakarta annotations.
 *
 * @author Brian Clozel
 */
class JakartaAnnotationsRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		if (ClassUtils.isPresent("jakarta.inject.Inject", classLoader)) {
			Stream.of("jakarta.inject.Inject", "jakarta.inject.Qualifier").forEach(annotationType ->
					hints.reflection().registerType(ClassUtils.resolveClassName(annotationType, classLoader)));
			//resolveClassName方法Resolve the given class name into a Class instance
		}
		Stream.of("jakarta.inject.Inject", "jakarta.inject.Provider", "jakarta.inject.Qualifier").forEach(typeName ->
				hints.reflection().registerType(TypeReference.of(typeName)));
		// javax.inject.Provider is omitted from the list, since we do not currently load
		// it via reflection.
		Stream.of(
				"jakarta.inject.Inject",
				"jakarta.inject.Provider",
				"jakarta.inject.Qualifier"
			).forEach(typeName -> hints.reflection().registerType(TypeReference.of(typeName)));
	}

	// read for mark
}
