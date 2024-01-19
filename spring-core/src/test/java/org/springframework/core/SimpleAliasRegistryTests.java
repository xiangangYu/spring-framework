/*
 * Copyright 2002-2024 the original author or authors.
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

package org.springframework.core;

import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.util.StringValueResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link SimpleAliasRegistry}.
 *
 * @author Juergen Hoeller
 * @author Nha Vuong
 * @author Sam Brannen
 */
class SimpleAliasRegistryTests {

	private static final String REAL_NAME = "real_name";
	private static final String NICKNAME = "nickname";
	private static final String NAME1 = "name1";
	private static final String NAME2 = "name2";
	private static final String NAME3 = "name3";
	private static final String NAME4 = "name4";
	private static final String NAME5 = "name5";
	private static final String ALIAS1 = "alias1";
	private static final String ALIAS2 = "alias2";
	private static final String ALIAS3 = "alias3";
	// TODO Determine if we can make SimpleAliasRegistry.resolveAliases() reliable.
	// See https://github.com/spring-projects/spring-framework/issues/32024.
	// When ALIAS4 is changed to "test", various tests fail due to the iteration
	// order of the entries in the aliasMap in SimpleAliasRegistry.
	private static final String ALIAS4 = "alias4";
	private static final String ALIAS5 = "alias5";


	private final SimpleAliasRegistry registry = new SimpleAliasRegistry();


	@Test
	void aliasChaining() {
		registerAlias(NAME1, ALIAS1);
		registerAlias(ALIAS1, ALIAS2);
		registerAlias(ALIAS2, ALIAS3);

		assertHasAlias(NAME1, ALIAS1);
		assertHasAlias(NAME1, ALIAS2);
		assertHasAlias(NAME1, ALIAS3);
		assertThat(registry.canonicalName(ALIAS1)).isEqualTo(NAME1);
		assertThat(registry.canonicalName(ALIAS2)).isEqualTo(NAME1);
		assertThat(registry.canonicalName(ALIAS3)).isEqualTo(NAME1);
	}

	@Test  // SPR-17191
	void aliasChainingWithMultipleAliases() {
		registerAlias(NAME1, ALIAS1);
		registerAlias(NAME1, ALIAS2);
		assertHasAlias(NAME1, ALIAS1);
		assertHasAlias(NAME1, ALIAS2);

		registerAlias(REAL_NAME, NAME1);
		assertHasAlias(REAL_NAME, NAME1);
		assertHasAlias(REAL_NAME, ALIAS1);
		assertHasAlias(REAL_NAME, ALIAS2);

		registerAlias(NAME1, ALIAS3);
		assertHasAlias(REAL_NAME, NAME1);
		assertHasAlias(REAL_NAME, ALIAS1);
		assertHasAlias(REAL_NAME, ALIAS2);
		assertHasAlias(REAL_NAME, ALIAS3);
	}

	@Test
	void removeAlias() {
		registerAlias(REAL_NAME, NICKNAME);
		assertHasAlias(REAL_NAME, NICKNAME);

		registry.removeAlias(NICKNAME);
		assertDoesNotHaveAlias(REAL_NAME, NICKNAME);
	}

	@Test
	void isAlias() {
		registerAlias(REAL_NAME, NICKNAME);
		assertThat(registry.isAlias(NICKNAME)).isTrue();
		assertThat(registry.isAlias(REAL_NAME)).isFalse();
		assertThat(registry.isAlias("bogus")).isFalse();
	}

	@Test
	void getAliases() {
		assertThat(registry.getAliases(NAME1)).isEmpty();

		registerAlias(NAME1, ALIAS1);
		assertThat(registry.getAliases(NAME1)).containsExactly(ALIAS1);

		registerAlias(ALIAS1, ALIAS2);
		registerAlias(ALIAS2, ALIAS3);
		assertThat(registry.getAliases(NAME1)).containsExactlyInAnyOrder(ALIAS1, ALIAS2, ALIAS3);
		assertThat(registry.getAliases(ALIAS1)).containsExactlyInAnyOrder(ALIAS2, ALIAS3);
		assertThat(registry.getAliases(ALIAS2)).containsExactly(ALIAS3);
		assertThat(registry.getAliases(ALIAS3)).isEmpty();
	}

	@Test
	void checkForAliasCircle() {
		// No aliases registered, so no cycles possible.
		assertThatNoException().isThrownBy(() -> registry.checkForAliasCircle(NAME1, ALIAS1));

		registerAlias(NAME1, ALIAS1); // ALIAS1 -> NAME1

		// No cycles possible.
		assertThatNoException().isThrownBy(() -> registry.checkForAliasCircle(NAME1, ALIAS1));

		assertThatIllegalStateException()
				// NAME1 -> ALIAS1 -> NAME1
				.isThrownBy(() -> registerAlias(ALIAS1, NAME1)) // internally invokes checkForAliasCircle()
				.withMessageContaining("'%s' is a direct or indirect alias for '%s'", ALIAS1, NAME1);

		registerAlias(ALIAS1, ALIAS2); // ALIAS2 -> ALIAS1 -> NAME1
		assertThatIllegalStateException()
				// NAME1 -> ALIAS1 -> ALIAS2 -> NAME1
				.isThrownBy(() -> registerAlias(ALIAS2, NAME1)) // internally invokes checkForAliasCircle()
				.withMessageContaining("'%s' is a direct or indirect alias for '%s'", ALIAS2, NAME1);
	}

	@Test
	void resolveAliasesPreconditions() {
		assertThatIllegalArgumentException().isThrownBy(() -> registry.resolveAliases(null));
	}

	@Test
	void resolveAliasesWithoutPlaceholderReplacement() {
		StringValueResolver valueResolver = new StubStringValueResolver();

		registerAlias(NAME1, ALIAS1);
		registerAlias(NAME1, ALIAS3);
		registerAlias(NAME2, ALIAS2);
		registerAlias(NAME2, ALIAS4);
		assertThat(registry.getAliases(NAME1)).containsExactlyInAnyOrder(ALIAS1, ALIAS3);
		assertThat(registry.getAliases(NAME2)).containsExactlyInAnyOrder(ALIAS2, ALIAS4);

		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME1)).containsExactlyInAnyOrder(ALIAS1, ALIAS3);
		assertThat(registry.getAliases(NAME2)).containsExactlyInAnyOrder(ALIAS2, ALIAS4);

		registry.removeAlias(ALIAS1);
		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME1)).containsExactly(ALIAS3);
		assertThat(registry.getAliases(NAME2)).containsExactlyInAnyOrder(ALIAS2, ALIAS4);
	}

	@Test
	void resolveAliasesWithPlaceholderReplacement() {
		StringValueResolver valueResolver = new StubStringValueResolver(Map.of(
			NAME1, NAME2,
			ALIAS1, ALIAS2
		));

		registerAlias(NAME1, ALIAS1);
		assertThat(registry.getAliases(NAME1)).containsExactly(ALIAS1);

		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME1)).isEmpty();
		assertThat(registry.getAliases(NAME2)).containsExactly(ALIAS2);

		registry.removeAlias(ALIAS2);
		assertThat(registry.getAliases(NAME1)).isEmpty();
		assertThat(registry.getAliases(NAME2)).isEmpty();
	}

	@Test
	void resolveAliasesWithPlaceholderReplacementConflict() {
		StringValueResolver valueResolver = new StubStringValueResolver(Map.of(ALIAS1, ALIAS2));

		registerAlias(NAME1, ALIAS1);
		registerAlias(NAME2, ALIAS2);

		// Original state:
		// ALIAS1 -> NAME1
		// ALIAS2 -> NAME2

		// State after processing original entry (ALIAS1 -> NAME1):
		// ALIAS2 -> NAME1 --> Conflict: entry for ALIAS2 already exists
		// ALIAS2 -> NAME2

		assertThatIllegalStateException()
				.isThrownBy(() -> registry.resolveAliases(valueResolver))
				.withMessage("Cannot register resolved alias '%s' (original: '%s') for name '%s': " +
						"It is already registered for name '%s'.", ALIAS2, ALIAS1, NAME1, NAME2);
	}

	@Test
	void resolveAliasesWithComplexPlaceholderReplacement() {
		StringValueResolver valueResolver = new StubStringValueResolver(Map.of(
			ALIAS3, ALIAS1,
			ALIAS4, ALIAS5,
			ALIAS5, ALIAS2
		));

		registerAlias(NAME3, ALIAS3);
		registerAlias(NAME4, ALIAS4);
		registerAlias(NAME5, ALIAS5);

		// Original state:
		// WARNING: Based on ConcurrentHashMap iteration order!
		// ALIAS3 -> NAME3
		// ALIAS5 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS3 -> NAME3):
		// ALIAS1 -> NAME3
		// ALIAS5 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS5 -> NAME5):
		// ALIAS1 -> NAME3
		// ALIAS2 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS4 -> NAME4):
		// ALIAS1 -> NAME3
		// ALIAS2 -> NAME5
		// ALIAS5 -> NAME4

		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME3)).containsExactly(ALIAS1);
		assertThat(registry.getAliases(NAME4)).containsExactly(ALIAS5);
		assertThat(registry.getAliases(NAME5)).containsExactly(ALIAS2);
	}

	// TODO Remove this test once we have implemented reliable processing in SimpleAliasRegistry.resolveAliases().
	// See https://github.com/spring-projects/spring-framework/issues/32024.
	// This method effectively duplicates the @ParameterizedTest version below,
	// with aliasX hard coded to ALIAS4; however, this method also hard codes
	// a different outcome that passes based on ConcurrentHashMap iteration order!
	@Test
	void resolveAliasesWithComplexPlaceholderReplacementAndNameSwitching() {
		StringValueResolver valueResolver = new StubStringValueResolver(Map.of(
			NAME3, NAME4,
			NAME4, NAME3,
			ALIAS3, ALIAS1,
			ALIAS4, ALIAS5,
			ALIAS5, ALIAS2
		));

		registerAlias(NAME3, ALIAS3);
		registerAlias(NAME4, ALIAS4);
		registerAlias(NAME5, ALIAS5);

		// Original state:
		// WARNING: Based on ConcurrentHashMap iteration order!
		// ALIAS3 -> NAME3
		// ALIAS5 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS3 -> NAME3):
		// ALIAS1 -> NAME4
		// ALIAS5 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS5 -> NAME5):
		// ALIAS1 -> NAME4
		// ALIAS2 -> NAME5
		// ALIAS4 -> NAME4

		// State after processing original entry (ALIAS4 -> NAME4):
		// ALIAS1 -> NAME4
		// ALIAS2 -> NAME5
		// ALIAS5 -> NAME3

		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME3)).containsExactly(ALIAS5);
		assertThat(registry.getAliases(NAME4)).containsExactly(ALIAS1);
		assertThat(registry.getAliases(NAME5)).containsExactly(ALIAS2);
	}

	@Disabled("Fails for some values unless alias registration order is honored")
	@ParameterizedTest  // gh-32024
	@ValueSource(strings = {"alias4", "test", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"})
	void resolveAliasesWithComplexPlaceholderReplacementAndNameSwitching(String aliasX) {
		StringValueResolver valueResolver = new StubStringValueResolver(Map.of(
			NAME3, NAME4,
			NAME4, NAME3,
			ALIAS3, ALIAS1,
			aliasX, ALIAS5,
			ALIAS5, ALIAS2
		));

		// If SimpleAliasRegistry ensures that aliases are processed in declaration
		// order, we need to register ALIAS5 *before* aliasX to support our use case.
		registerAlias(NAME3, ALIAS3);
		registerAlias(NAME5, ALIAS5);
		registerAlias(NAME4, aliasX);

		// Original state:
		// WARNING: Based on LinkedHashMap iteration order!
		// ALIAS3 -> NAME3
		// ALIAS5 -> NAME5
		// aliasX -> NAME4

		// State after processing original entry (ALIAS3 -> NAME3):
		// ALIAS5 -> NAME5
		// aliasX -> NAME4
		// ALIAS1 -> NAME4

		// State after processing original entry (ALIAS5 -> NAME5):
		// aliasX -> NAME4
		// ALIAS1 -> NAME4
		// ALIAS2 -> NAME5

		// State after processing original entry (aliasX -> NAME4):
		// ALIAS1 -> NAME4
		// ALIAS2 -> NAME5
		// alias5 -> NAME3

		registry.resolveAliases(valueResolver);
		assertThat(registry.getAliases(NAME3)).containsExactly(ALIAS5);
		assertThat(registry.getAliases(NAME4)).containsExactly(ALIAS1);
		assertThat(registry.getAliases(NAME5)).containsExactly(ALIAS2);
	}

	private void registerAlias(String name, String alias) {
		registry.registerAlias(name, alias);
	}

	private void assertHasAlias(String name, String alias) {
		assertThat(registry.hasAlias(name, alias)).isTrue();
	}

	private void assertDoesNotHaveAlias(String name, String alias) {
		assertThat(registry.hasAlias(name, alias)).isFalse();
	}


	/**
	 * {@link StringValueResolver} that replaces each value with a supplied
	 * placeholder and otherwise returns the original value if no placeholder
	 * is configured.
	 */
	private static class StubStringValueResolver implements StringValueResolver {

		private final Map<String, String> placeholders;

		StubStringValueResolver() {
			this(Map.of());
		}

		StubStringValueResolver(Map<String, String> placeholders) {
			this.placeholders = placeholders;
		}

		@Override
		public String resolveStringValue(String str) {
			return (this.placeholders.containsKey(str) ? this.placeholders.get(str) : str);
		}

	}

}
