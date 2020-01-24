package com.naveed.ytextractor;
import com.naveed.ytextractor.utils.HTTPUtility;
import com.naveed.ytextractor.utils.LogUtils;
import com.naveed.ytextractor.utils.RegexUtils;
import java.io.IOException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import com.naveed.ytextractor.utils.Utils;

public class CipherManager {
	
	private static final String RegexDesipherFunctionCode="\\{[a-zA-Z]{1,}=[a-zA-Z]{1,}.split\\(\"\"\\);[a-zA-Z0-9$]{2}\\.[a-zA-Z0-9$]{2}.*?[a-zA-Z]{1,}.join\\(\"\"\\)\\};";
	private static final String RegexVarName="[a-zA-Z0-9$]{2}\\.[a-zA-Z0-9$]{2}\\([a-zA-Z]\\,(\\d\\d|\\d)\\)";
	private static  String RegexFindVarCode="";
	private static String cachedDechiperFunction=null;
	

	
	public  static String getDecipherCode(String Basejs) {
		String DechipherCode;
		String DecipherFun="decipher=function(a)" + RegexUtils.matchGroup(RegexDesipherFunctionCode, Basejs);
		//Utils.copyToBoard(Basejs);
		LogUtils.log("decfun="+DecipherFun);
		String RawName=RegexUtils.matchGroup(RegexVarName, DecipherFun).replace("$","\\$");
		String RealVarName=RawName.split("\\.")[0];
		RegexFindVarCode = "var\\s" + RealVarName + "=.*?\\};";	// Word 1
		String varCode=RegexUtils.matchGroup(RegexFindVarCode, Basejs);
		DechipherCode = DecipherFun + "\n" + varCode;
		LogUtils.log("code= "+DechipherCode);
		
		return DechipherCode;
	}

	/*this function checks if the deciphered findings is already present if not gets the funtion*/
	public  static String dechiperSig(String sig,String playerUrl) throws IOException{
		if(cachedDechiperFunction==null){
			cachedDechiperFunction=getDecipherCode(getPlayerCode(playerUrl));
		}
		return RhinoEngine(sig);
	}
	
	
	/* just gets the js player file content */
	private static String getPlayerCode(String playerUrl) throws IOException {
        return HTTPUtility.downloadPageSource(playerUrl);
    }

	
	private static String RhinoEngine(String s) {
		Context rhino = Context.enter();
		rhino.setOptimizationLevel(-1);
		try {
			Scriptable scope = rhino.initStandardObjects();
			rhino.evaluateString(scope, cachedDechiperFunction, "JavaScript", 1, null);
			Object obj = scope.get("decipher", scope);

			if (obj instanceof Function) {
				Function jsFunction = (Function) obj;
				Object jsResult = jsFunction.call(rhino, scope, scope, new Object[]{s});
				String result = Context.toString(jsResult);
				return result ;
			}
		}
		finally {
			Context.exit();
		}
		return s;
	}

}
