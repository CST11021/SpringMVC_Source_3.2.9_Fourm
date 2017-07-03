/*
 * Copyright 2002-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.type;

// 封装类元数据的接口
public interface ClassMetadata {

	// 返回全限定类名
	String getClassName();
	// 是否是接口
	boolean isInterface();
	// 是否是抽象类，接口也是抽象类
	boolean isAbstract();
	// 是否代表一个具体类，即既不是接口也不是抽象类。
	boolean isConcrete();
	// 是否带有final关键字修饰
	boolean isFinal();

	/**
	 * Determine whether the underlying class is independent,
	 * i.e. whether it is a top-level class or a nested class (static inner class) that can be constructed independent from an enclosing class.
	 */
	boolean isIndependent();

	// 自己本身是否是内部类或是否包含内部类
	boolean hasEnclosingClass();

	// 返回底层类的封闭类的名称，比如A类是B类的内部类，则返回A类的元数据getEnclosingClassName()方法返回B
	String getEnclosingClassName();

	// 是否有父类，Object返回的false
	boolean hasSuperClass();

	// 返回父类的全限定类名
	String getSuperClassName();

	// 返回所有实现的接口
	String[] getInterfaceNames();

	// 返回所有内部类
	String[] getMemberClassNames();

}
