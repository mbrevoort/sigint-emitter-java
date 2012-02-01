package com.pearson.sigint.emitter.types;

import com.pearson.sigint.emitter.PublishProvider;
import com.pearson.sigint.emitter.SIGINTConfig.FORMAT;

public class Error extends Emission<Error> {
	public static String TYPE() { return "e"; }
	
	public Error(Throwable error, String app, String node, PublishProvider publisher, FORMAT format) {
		super(app, node, publisher, format);
		setData(new ErrorData(error.getMessage()));
	}
	
	@Override
	public String getType() {
		return TYPE();
	}
	
	public Error againstApplication(String a) {
		setAgainst(a);
		return this;
	}
	
	public Error operation(String o) {
		return _operation(o);
	}
	
	protected class ErrorData {
		private final String msg;

		public ErrorData(String msg) {
			super();
			this.msg = msg;
		}

		public String getMsg() {
			return msg;
		}
	}
}
