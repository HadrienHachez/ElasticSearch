/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License;
 * you may not use this file except in compliance with the Elastic License.
 */
package org.elasticsearch.xpack.core.monitoring.test;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.MockScriptEngine;
import org.elasticsearch.script.MockScriptPlugin;
import org.elasticsearch.script.ScriptContext;
import org.elasticsearch.script.ScriptEngine;
import org.elasticsearch.script.SearchScript;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * A mock script engine that registers itself under the 'painless' name so that watches that use it can still be used in tests.
 */
public class MockPainlessScriptEngine extends MockScriptEngine {

    public static final String NAME = "painless";

    public static class TestPlugin extends MockScriptPlugin {
        @Override
        public ScriptEngine getScriptEngine(Settings settings, Collection<ScriptContext<?>> contexts) {
            return new MockPainlessScriptEngine();
        }

        @Override
        protected Map<String, Function<Map<String, Object>, Object>> pluginScripts() {
            return Collections.emptyMap();
        }
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public <T> T compile(String name, String script, ScriptContext<T> context, Map<String, String> options) {
        MockCompiledScript compiledScript = new MockCompiledScript(name, options, script, p -> script);
        if (context.instanceClazz.equals(ExecutableScript.class)) {
            return context.factoryClazz.cast((ExecutableScript.Factory) compiledScript::createExecutableScript);
        } else if (context.instanceClazz.equals(SearchScript.class)) {
            return context.factoryClazz.cast((SearchScript.Factory) compiledScript::createSearchScript);
        }
        throw new IllegalArgumentException("mock painless does not know how to handle context [" + context.name + "]");
    }
}
