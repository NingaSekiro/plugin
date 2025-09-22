package org.aopbuddy.plugin.infra.model

data class EvalRequest(
    var serverName: String? = null,
    var classloader: String? = null,
    var script: String? = null
)