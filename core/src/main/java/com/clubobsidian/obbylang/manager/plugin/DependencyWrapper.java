package com.clubobsidian.obbylang.manager.plugin;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class DependencyWrapper {

	private ScriptObjectMirror script;
	private String[] dependencies;
	public DependencyWrapper(ScriptObjectMirror script, String[] dependencies) 
	{
		this.script = script;
		this.dependencies = dependencies;
	}
	
	public ScriptObjectMirror getScript()
	{
		return this.script;
	}
	
	public String[] getDependencies()
	{
		return this.dependencies;
	}
}