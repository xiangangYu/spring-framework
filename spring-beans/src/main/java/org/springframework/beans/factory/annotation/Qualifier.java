/*
 * Copyright 2002-2011 the original author or authors.
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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be used on a field or parameter as a qualifier for
 * candidate beans when autowiring. It may also be used to annotate other
 * custom annotations that can then in turn be used as qualifiers.
 *
 * 用于对于有资格的beans的获胜者，也可以用于注解别的客制化注解作为获胜者
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @since 2.5
 * @see Autowired
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Qualifier {

	String value() default "";
	//通过使用 @Qualifier 注解，我们可以消除需要注入哪个 bean 的问题。让我们重新回顾一下前面的例子，看看我们如何通过包含
	// @Qualifier 注释来指出我们想要使用哪个 bean 来解决问题
	/*
	 @Component
    public class FooService {
        @Autowired
        @Qualifier("fooFormatter")
        private Formatter formatter;
    }
    @Component
     @Qualifier("fooFormatter")
     public class FooFormatter implements Formatter {
         public String format() {
             return "foo";
         }
     }

     @Component
     @Qualifier("barFormatter")
     public class BarFormatter implements Formatter {
         public String format() {
             return "bar";
         }
     }

     还有另一个名为 @Primary 的注解，我们也可以用来发生依赖注入的歧义时决定要注入哪个 bean。
     当存在多个相同类型的 bean 时，此注解定义了首选项。除非另有说明，否则将使用与 @Primary 注释关联的 bean 。
     我们来看一个例子：

     @Bean
    public Employee tomEmployee() {
        return new Employee("Tom");
    }

    @Bean
    @Primary
    public Employee johnEmployee() {
        return new Employee("john");
    }
	 */

	// Qualifier的意思是合格者

	// read for mark
}
