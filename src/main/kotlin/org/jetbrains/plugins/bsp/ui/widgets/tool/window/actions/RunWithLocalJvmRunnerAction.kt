package org.jetbrains.plugins.bsp.ui.widgets.tool.window.actions

import ch.epfl.scala.bsp4j.JvmEnvironmentItem
import com.intellij.openapi.project.Project
import org.jetbrains.magicmetamodel.impl.workspacemodel.BuildTargetInfo
import org.jetbrains.magicmetamodel.impl.workspacemodel.toBsp4JTargetIdentifier
import org.jetbrains.plugins.bsp.config.BspPluginBundle
import org.jetbrains.plugins.bsp.server.tasks.JvmRunEnvironmentTask
import org.jetbrains.plugins.bsp.ui.widgets.tool.window.components.getBuildTargetName

internal class RunWithLocalJvmRunnerAction(
  targetInfo: BuildTargetInfo,
  text: (() -> String)? = null,
  isDebugMode: Boolean = false,
  verboseText: Boolean = false,
) : LocalJvmRunnerAction(
  targetInfo = targetInfo,
  text = {
    if (text != null) text()
    else if (isDebugMode) BspPluginBundle.messageVerbose(
      "target.debug.with.jvm.runner.action.text",
      verboseText,
      targetInfo.getBuildTargetName()
    )
    else BspPluginBundle.messageVerbose(
      "target.run.with.jvm.runner.action.text",
      verboseText,
      targetInfo.getBuildTargetName()
    )
  },
  isDebugMode = isDebugMode
) {
  override fun getEnvironment(project: Project): JvmEnvironmentItem? =
    JvmRunEnvironmentTask(project).connectAndExecute(targetInfo.id.toBsp4JTargetIdentifier())?.items?.first()
}
