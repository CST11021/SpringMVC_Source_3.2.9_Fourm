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

package org.springframework.context;

/**
 * Interface for objects that may participate in a phased process such as lifecycle management.
 * 可能参与到分阶段过程，例如生命周期管理的对象接口
 * @author Mark Fisher
 * @since 3.0
 * @see SmartLifecycle
 */

// 启动和关闭调用的顺序是很重要的。如果两个对象之间存在依赖关系，依赖类要在其依赖类后启动，依赖类也要在其依赖类前停止。
// 然而，有时候其之间的依赖关系不是那么直接。你可能仅仅知道某种类型的对象应该在另一类型对象前启动。在那些情况下，
// SmartLifecycle接口定义了另一个选项，在其父类接口Phased中定义命名为getPhase（）方法。
public interface Phased {

	// 当启动时，有最低phase的对象首先启动，并且停止时，按照相反的顺序结束。因此，实现了SmartLifecycle接口并且其
	// getPhase（）方法返回Integer.MIN_VALUE的一个对象将是首先被启动并且最后停止。
	// 与其相反对应的对象，Integer.MAX_VALUE的phase的值，将指明最后启动和最先停止（可能是其依赖其他对象工作运行）。
	// 当考虑phase值时，了解任何普通Lifecycle对象（没有实现SmartLifecycle其值将是0）的默认phase也很重要。
	// 因此，任何负数phase值将表示对象应该在那些标准组件前启动（并其之后停止），并且对于正数的phase值按照相反顺序启动停止。
	// Return the phase value of this object.
	int getPhase();

}
