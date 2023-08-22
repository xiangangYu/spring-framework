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

package org.springframework.beans.factory.aot;

/**
 * Record class holding key information for beans registered in a bean factory.
 *
 * 下面的这个record 所谓的Record class还是第一次见，是用来干什么的？
 *
 * 因为record关键词申明类主要是为了简化一些类的申明，所以它本质就是一类特殊的class，或者说是某一个模版的class。
 *
 * record申明的类，具备这些特点：
 *
 * 它是一个final类
 * 自动实现equals、hashCode、toString函数
 * 成员变量均为public属性
 *
 * 下面是等效的代码示例：
 * public final class range{
 *     final int start;
 *     final int end;
 *
 *     public range(int start, int end) {
 *         this.start = start;
 *         this.end = end;
 *     }
 *
 *     @Override
 *     public boolean equals(Object o) {
 *         if (this == o) return true;
 *         if (o == null || getClass() != o.getClass()) return false;
 *         range range = (range) o;
 *         return start == range.start && end == range.end;
 *     }
 *
 *     @Override
 *     public int hashCode() {
 *         return Objects.hash(start, end);
 *     }
 *
 *     @Override
 *     public String toString() {
 *         return "range{" +
 *                 "start=" + x +
 *                 ", end=" + y +
 *                 '}';
 *     }
 *
 *     public int start(){
 *         return start;
 *     }
 *
 *     public int end(){
 *         return end;
 *     }
 * }
 *
 *
 *
 * @param beanName the name of the registered bean
 * @param beanClass the type of the registered bean
 * @author Brian Clozel
 * @since 6.0.8
 */
record BeanRegistrationKey(String beanName, Class<?> beanClass) {
}

// read for mark