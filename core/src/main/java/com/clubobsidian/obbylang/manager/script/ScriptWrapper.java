package com.clubobsidian.obbylang.manager.script;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class ScriptWrapper {

	private ScriptObjectMirror obj;
	private Object owner;
	
	public ScriptWrapper(ScriptObjectMirror obj, Object owner)
	{
		this.obj = obj;
		this.owner = owner;
	}
	
	public ScriptObjectMirror getScript()
	{
		return this.obj;
	}
	
	public Object getOwner()
	{
		return this.owner;
	}
}