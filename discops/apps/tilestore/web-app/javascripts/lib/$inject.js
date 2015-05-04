/*
*	$inject.js
*	Inject script into closure
*/
(function($, env) {
"use strict";

var REGEX_COMMENTS = /((\/\/.*$)|(\/\*[\s\S]*?\*\/))/mg,
	REGEX_LINEBREAKS = /(\r\n|\n|\r)/gm,
	REGEX_TRIM = /^\s+|\s+$/g,
	REGEX_IIFHEAD = /^.*?\(\s*function\s*[^\(]*\(\s*([^\)]*)\)\s*\{/m,
	testSpecRunner,
	removeLineBreak = false,
	isAsync = true,
	use;
	
env.INJECTOR = env.INJECTOR || {};
env.INJECTOR.testSpecs = env.INJECTOR.testSpecs || [];

// convert function body to string
function getFnBodyString(fn) {
    var fnString,
        fnBody;
    
    if (typeof fn !== "function") {
        return "";
    }
    fnString = fn.toString();
    fnBody = fnString.substring(fnString.indexOf("{") + 1, fnString.lastIndexOf("}"));
    return fnBody;
}

function getIIFHead(fnString) {
	var ret = "";
	if (typeof fnString !== "string") {
		return ret;
	}
	ret = fnString.match(REGEX_IIFHEAD);
	return ret;
}

function getIIFBody(fnString) {
	var fnBody,
		fnText,
		ret = "";
	if (typeof fnString !== "string") {
		return ret;
	}
	fnBody = fnString.substring(fnString.indexOf("{") + 1, fnString.lastIndexOf("}"));
	fnText = fnBody.replace(REGEX_COMMENTS, '').replace(REGEX_TRIM, '');
	return fnText;
}

// Inject constructor
function Inject(uri, callback) {
	var self = this;
	if (uri && callback) {
		return self.fetch(uri, callback);
	}
	return self;
}

Inject.prototype = {
	rewrite: function(responseText, dataType) {
		var self = this,
			fnText,
			iifHeadArray,
			iifBody,
			iifHead,
			index = env.INJECTOR.testSpecs.length - 1,
			ret = "";
			
		if (typeof responseText !== "string") {
			return ret;
		}

		if (!testSpecRunner) {
			use.qunit();
		}
		fnText = responseText;
		fnText = $.trim(fnText); // trim
		fnText = fnText.replace(REGEX_COMMENTS, ""); // remove comments
		// remove line breaks - !!some bad formatted script will cause parser error
		if (removeLineBreak) {
			fnText = fnText.replace(REGEX_LINEBREAKS," ");  
		}
		iifHeadArray = getIIFHead(fnText);
		
		if (iifHeadArray && iifHeadArray.length) {
			iifBody = fnText.replace(iifHeadArray[0], "");
			iifHead = iifHeadArray[0];
			iifHead += testSpecRunner(index);
			ret = iifHead + iifBody;
		}
		return ret;
	},
	fetch: function (uri, callback) {
		var self = this;
		
		if (typeof uri !== "string" || typeof callback !== "function") {
			throw  "fetch: invalid arguments";
		}

		env.INJECTOR.testSpecs.push(getFnBodyString(callback));
		
		return $.ajax({
			url: uri,
			type: 'GET',
			dataType: "script",
			async: isAsync,
			cache:true,
			crossDomain: false,
			dataFilter: function(responseText, dataType) {
				return self.rewrite(responseText, dataType);
			},
			success: function(closureFn) {
				console.log(closureFn);
			},
			error: function(jqXHR, textStatus, errorThrown) {
				throw errorThrown;
			}
		});
	}
};


use = {
	removeLineBreak: function(isRemoveLineBreak) {
		var self = this;
		removeLineBreak = (typeof isRemoveLineBreak === "boolean") ? isRemoveLineBreak : true;
		return env.$inject;
	},
	async: function(useAsync) {
		isAsync = (typeof useAsync === "boolean") ? useAsync : true;
		return env.$inject;
	},
	jasmine: function() {
		var self = this;
		isAsync = false;
		testSpecRunner = function(index) {
			var ret =	"\n var testSpecFn = function() { eval(INJECTOR.testSpecs["+ index +"]);};";
				ret +=	"testSpecFn(); \n";
			return ret;
		};
		return env.$inject;
	},
	qunit: function() {
		var self = this;
		testSpecRunner = function(index) {
			var ret =	"\n var testSpecFn; \n";
				ret +=	" setTimeout(function() { \n";
				ret +=	" 	testSpecFn = function() { \n";
				ret +=	"		eval(INJECTOR.testSpecs["+ index +"]); \n";
				ret +=	" 	}; \n";
				ret +=	" 	testSpecFn(); \n";
				ret +=	" }, 15); \n";
			return ret;
		};
		return env.$inject;
	}
};

// expose $inject to global 
env.$inject = function(uri, callback) {
	return new Inject(uri, callback);
};
env.$inject.use = use;

}(jQuery, typeof window !== "undefined" ? window : this));