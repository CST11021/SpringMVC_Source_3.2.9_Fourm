
package org.springframework.beans.factory.parsing;

import org.springframework.core.io.Resource;


//  * Simple strategy allowing tools to control how source metadata is attached to the bean definition metadata.
//  * 简单策略接口，允许工具控制source元数据关联到bean definition元数据。
//  * Configuration parsers may provide the ability to attach source metadata during the parse phase. They will offer this metadata in a generic format which can be further modified by a SourceExtractor before being attached to the bean definition metadata.
//  * 配置分析器可以提供在解析阶段附加源元数据的能力。他们将在一个通用的格式，可以进一步修改的sourceextractor之前被连接到bean定义元数据提供元数据。

// 资源提取器：SourceExtractor 这是个提取解析document后返回的原生bean定义，如果你需要则可以实现这个接口，spring提供了
// NullSourceExtractor空实现，PassThroughSourceExtractor简单实现返回了对象资源。
public interface SourceExtractor {

	Object extractSource(Object sourceCandidate, Resource definingResource);

}
