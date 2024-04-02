package org.jetbrains.plugins.bsp.magicmetamodel.impl.workspacemodel

import org.jetbrains.plugins.bsp.magicmetamodel.impl.ModuleState
import org.jetbrains.plugins.bsp.magicmetamodel.impl.toState
import java.nio.file.Path

public data class GoModuleDependency(
  val importPath: String,
  val root: Path,
)

public data class GoModule(
  val module: GenericModuleInfo,
  val sourceRoots: List<GenericSourceRoot>,
  val resourceRoots: List<ResourceRoot>,
  val importPath: String,
  val root: Path,
  val goDependencies: List<GoModuleDependency>,
) : WorkspaceModelEntity(), Module {
  override fun toState(): ModuleState = ModuleState(
    module = module.toState(),
    sourceRoots = sourceRoots.map { it.toState() },
    resourceRoots = resourceRoots.map { it.toState() },
    goAddendum = GoAddendum(importPath, root, goDependencies).toState(),
  )

  override fun getModuleName(): String = module.name
}
