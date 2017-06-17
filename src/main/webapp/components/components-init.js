function $importComponents(components){
	if(components==null)
		return;
	
	for(var i=0;i<components.length;i++){
		if(components[i]=="syntax"){
			$import("$syntax/css/shCore.css");
			$import("$syntax/css/shThemeDefault.css");
			$import("$syntax/js/shCore.js");
			$import("$syntax/js/shBrushJScript.js");
			$import("$syntax/js/shBrushXml.js");
		}else if(components[i]=="wdatepicker"){
			$import("$WdatePicker/dependecies/jquery.js");
			$import("$WdatePicker/css/WdatePicker.css");
			$import("$WdatePicker/js/WdatePicker.js");
		}
	}
	
	function $import(importFile){
		var file = importFile.toString();
		var IsRelativePath = (file.indexOf("$")==0 ||file.indexOf("/")==-1);
		var path=file;
		if(IsRelativePath){
   
			if(file.indexOf("$")==0)
				file = file.substr(1);
			path = $dir() + file;
		}
		
		var newElement=null,
		i=0;        
		var ext = path.substr(path.lastIndexOf(".")+1);        
		if(ext.toLowerCase()=="js"){            
			var scriptTags = document.getElementsByTagName("script");            
			for(var i=0;scriptTags!=null && i < scriptTags.length;i++) {                  
				if(scriptTags[i].src && getFileName(scriptTags[i].src) == getFileName(path))                    
					return;
			}
			document.write('<script src="'+path+'" type="text/javascript"></script>\n');
		}else if(ext.toLowerCase()=="css"){
			var linkTags = document.getElementsByTagName("link");
			for(var i=0;linkTags!=null && i < linkTags.length;i++) {
				if(linkTags[i].href && getFileName(linkTags[i].href) == getFileName(path))                    
					return;
			}
			document.write('<link href="'+path+'" rel="stylesheet" type="text/css"></link>\n'); 
		}else            
			return;
		
		function getFileName(src){
			var dirs = src.split("/");
			return dirs[dirs.length-1];
		}
		
		function $dir(){
			var scriptTags = document.getElementsByTagName("script"); 
			for(var i=0;scriptTags!=null && i < scriptTags.length;i++) {                  
				if(scriptTags[i].src && scriptTags[i].src.match(/components-init\.js$/)) {                      
					path = scriptTags[i].src.replace(/components-init\.js$/,"");                     
					return path;                
				}
			}
			return "";
		}
	}
}

function getIEVersion() {
	if(navigator.userAgent.indexOf("MSIE 6.0")>0) { 
		return 6;
	}
	
	return 8;
}