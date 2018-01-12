
package org.springframework.beans.factory.parsing;

import java.util.Stack;

// 本质是一个栈，一个Bean的解析过程相当于一个Bean的入栈到出栈的过程
public final class ParseState {

	private static final char TAB = '\t';
	private final Stack state;


	public ParseState() {
		this.state = new Stack();
	}
	private ParseState(ParseState other) {
		this.state = (Stack) other.state.clone();
	}


	// 实体入栈（bean入栈）
	public void push(Entry entry) {
		this.state.push(entry);
	}
	// 实体出栈（bean出栈）
	public void pop() {
		this.state.pop();
	}
	// 返回一个栈顶元素，而不移除（即：返回一个当前解析的bean）
	public Entry peek() {
		return (Entry) (this.state.empty() ? null : this.state.peek());
	}

	// 返回这个 ParseState 的快照
	public ParseState snapshot() {
		return new ParseState(this);
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int x = 0; x < this.state.size(); x++) {
			if (x > 0) {
				sb.append('\n');
				for (int y = 0; y < x; y++) {
					sb.append(TAB);
				}
				sb.append("-> ");
			}
			sb.append(this.state.get(x));
		}
		return sb.toString();
	}

	// 实现了该接口才可以放入ParseState解析栈，该接口的实现有：
	// BeanEntry、PropertyEntry、ConstructorArgumentEntry和QualifierEntry
	public interface Entry {

	}

}
