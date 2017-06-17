package com.whz.support;

import com.whz.utils.Dom4jUtil;
import org.dom4j.Element;

public class BehaviourConfigParserHelper {

	public static String getTagName(Element root){
		Element personDossierElement = root.element("personDossier");
		String tagColumn = Dom4jUtil.getRequiredAttribute(personDossierElement, "tagColumn");
		return tagColumn;
	}

}
