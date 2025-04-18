/**
 * Package containing Spring's basic AOP infrastructure, compliant with(兼容，符合) the
 * <a href="http://aopalliance.sourceforge.net">AOP Alliance</a> interfaces.
 *
 * <p>Spring AOP supports proxying interfaces or classes, introductions, and offers
 * static and dynamic pointcuts.
 *
 * <p>Any Spring AOP proxy can be cast to(被投射到) the ProxyConfig AOP configuration interface
 * in this package to add or remove interceptors.
 *
 * <p>The ProxyFactoryBean is a convenient way to create AOP proxies in a BeanFactory
 * or ApplicationContext. However, proxies can be created programmatically using the
 * ProxyFactory class.
 */
@NullMarked
package org.springframework.aop.framework;

import org.jspecify.annotations.NullMarked;
