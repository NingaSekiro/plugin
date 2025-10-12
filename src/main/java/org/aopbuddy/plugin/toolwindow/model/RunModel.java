package org.aopbuddy.plugin.toolwindow.model;

import org.aopbuddy.plugin.service.JvmService;
import org.aopbuddy.plugin.toolwindow.view.GroovyEditorView;

public class RunModel {
    private final JvmService jvmService;
    private final RunResultModel runResultModel;
    private final GroovyEditorView groovyEditorView;

    public RunModel(JvmService jvmService, RunResultModel runResultModel, GroovyEditorView groovyEditorView) {
        this.jvmService = jvmService;
        this.runResultModel = runResultModel;
        this.groovyEditorView = groovyEditorView;
    }

    public void eval() {
        String eval = jvmService.eval(groovyEditorView.getGroovyEditor().getText());
        runResultModel.setStatus(eval);
    }
}
