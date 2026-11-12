package com.omnifile.core.workspace

import com.omnifile.core.workspace.model.ProjectArea
import com.omnifile.core.workspace.model.ProjectMetadata

data class WorkspaceProject(
    val id: String,
    val name: String,
    val sourceUri: String? = null,
    val workingDirectory: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)
