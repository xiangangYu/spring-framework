/*
 * Copyright 2002-2020 the original author or authors.
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
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates 'lookup' methods, to be overridden by the container
 * to redirect them back to the {@link org.springframework.beans.factory.BeanFactory}
 * for a {@code getBean} call. This is essentially an annotation-based version of the
 * XML {@code lookup-method} attribute, resulting in the same runtime arrangement.
 *
 * <p>The resolution of the target bean can either be based on the return type
 * ({@code getBean(Class)}) or on a suggested bean name ({@code getBean(String)}),
 * in both cases passing the method's arguments to the {@code getBean} call
 * for applying them as target factory method arguments or constructor arguments.
 *
 * <p>Such lookup methods can have default (stub) implementations that will simply
 * get replaced by the container, or they can be declared as abstract - for the
 * container to fill them in at runtime. In both cases, the container will generate
 * runtime subclasses of the method's containing class via CGLIB, which is why such
 * lookup methods can only work on beans that the container instantiates through
 * regular constructors: i.e. lookup methods cannot get replaced on beans returned
 * from factory methods where we cannot dynamically provide a subclass for them.
 *
 * <p><b>Recommendations for typical Spring configuration scenarios:</b>
 * When a concrete class may be needed in certain scenarios, consider providing stub
 * implementations of your lookup methods. And please remember that lookup methods
 * won't work on beans returned from {@code @Bean} methods in configuration classes;
 * you'll have to resort to {@code @Inject Provider<TargetBean>} or the like instead.
 *
 * @author Juergen Hoeller
 * @since 4.1
 * @see org.springframework.beans.factory.BeanFactory#getBean(Class, Object...)
 * @see org.springframework.beans.factory.BeanFactory#getBean(String, Object...)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Lookup {

	/**
	 * This annotation attribute may suggest a target bean name to look up.
	 * If not specified, the target bean will be resolved based on the
	 * annotated method's return type declaration.
	 */
	String value() default "";
	/**
	 * 背景：
	 * 在Spring的诸多应用场景中bean都是单例形式，当一个单利bean需要和一个非单利bean组合使用或者一个非单利bean和另一个非单利bean
	 * 组合使用时，我们通常都是将依赖以属性的方式放到bean中来引用，然后以@Autowired来标记需要注入的属性。但是这种方式在bean的生命
	 * 周期不同时将会出现很明显的问题，假设单利bean A需要一个非单利bean B（原型），我们在A中注入bean B，每次调用bean A中的方法时
	 * 都会用到bean B，我们知道Spring Ioc容器只在容器初始化时执行一次，也就是bean A中的依赖bean B只有一次注入的机会，但是实际上
	 * bean B我们需要的是每次调用方法时都获取一个新的对象（原型）所以问题明显就是：我们需要bean B是一个原型bean，而事实上bean B的
	 * 依赖只注入了一次变成了事实上的单例bean。
	 */
	/**
	 * 1。在bean A中引入ApplicationContext每次调用方法时用上下文的getBean(name,class)方法去重新获取bean B的实例。
	 * 2。使用@Lookup注解。
	 * 这两种解决方案都能解决我们遇到的问题，但是第二种相对而言更简单。以下给出两种解决方案的代码示例。
	 */
	/*
	@Component
	public class SingletonBean {
		private static final Logger logger = LoggerFactory.getLogger(SingletonBean.class);

		@Autowired
		private ApplicationContext context;

		public void print() {
			PrototypeBean bean = getFromApplicationContext();
			logger.info("Bean SingletonBean's HashCode : {}",bean.hashCode());
			bean.say();
		}


		// 每次都从ApplicatonContext中获取新的bean引用

		PrototypeBean getFromApplicationContext() {
			return this.context.getBean("prototypeBean",PrototypeBean.class);
		}
	}
	*/
		/*
		@Component
		public abstract class SingletonBean {
			private static final Logger logger = LoggerFactory.getLogger(SingletonBean.class);

			public void print() {
				PrototypeBean bean = methodInject();
				logger.info("Bean SingletonBean's HashCode : {}",bean.hashCode());
				bean.say();
			}
			// 也可以写成 @Lookup("prototypeBean") 来指定需要注入的bean
			@Lookup
			protected abstract PrototypeBean methodInject();
		}
		 */


}
