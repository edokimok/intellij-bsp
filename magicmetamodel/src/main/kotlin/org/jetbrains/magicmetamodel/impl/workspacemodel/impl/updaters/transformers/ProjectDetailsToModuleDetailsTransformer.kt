package org.jetbrains.magicmetamodel.impl.workspacemodel.impl.updaters.transformers

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.DependencySourcesItem
import ch.epfl.scala.bsp4j.JavacOptionsItem
import ch.epfl.scala.bsp4j.PythonOptionsItem
import ch.epfl.scala.bsp4j.ResourcesItem
import ch.epfl.scala.bsp4j.ScalacOptionsItem
import ch.epfl.scala.bsp4j.SourcesItem
import org.jetbrains.magicmetamodel.ProjectDetails
import org.jetbrains.magicmetamodel.impl.workspacemodel.ModuleDetails

internal class ProjectDetailsToModuleDetailsTransformer(
  private val projectDetails: ProjectDetails,
) {
  private val libraryGraph = LibraryGraph(projectDetails.libraries.orEmpty())

  fun moduleDetailsForTargetId(targetId: BuildTargetIdentifier): ModuleDetails {
    val target = calculateTarget(projectDetails, targetId)
    val allDependencies = libraryGraph.findAllTransitiveDependencies(target)
    val sources = calculateSources(projectDetails, targetId)
    val resources = calculateResources(projectDetails, targetId)
    return ModuleDetails(
      target = target,
      sources = sources,
      resources = resources,
      dependenciesSources = calculateDependenciesSources(projectDetails, targetId),
      javacOptions = calculateJavacOptions(projectDetails, targetId),
      scalacOptions = calculateScalacOptions(projectDetails, targetId),
      pythonOptions = calculatePythonOptions(projectDetails, targetId),
      outputPathUris = emptyList(),
      libraryDependencies = allDependencies.libraryDependencies.map { it.uri }
        .takeIf { projectDetails.libraries != null },
      moduleDependencies = allDependencies.targetDependencies.map { it.uri },
      defaultJdkName = projectDetails.defaultJdkName,
    )
  }

  private fun calculateTarget(projectDetails: ProjectDetails, targetId: BuildTargetIdentifier): BuildTarget =
    projectDetails.targets.first { it.id == targetId }

  private fun calculateSources(projectDetails: ProjectDetails, targetId: BuildTargetIdentifier): List<SourcesItem> =
    projectDetails.sources.filter { it.target == targetId }

  private fun calculateResources(projectDetails: ProjectDetails, targetId: BuildTargetIdentifier): List<ResourcesItem> =
    projectDetails.resources.filter { it.target == targetId }

  private fun calculateDependenciesSources(
    projectDetails: ProjectDetails,
    targetId: BuildTargetIdentifier,
  ): List<DependencySourcesItem> =
    projectDetails.dependenciesSources.filter { it.target == targetId }

  private fun calculateJavacOptions(
    projectDetails: ProjectDetails,
    targetId: BuildTargetIdentifier,
  ): JavacOptionsItem? =
    projectDetails.javacOptions.firstOrNull { it.target == targetId }

  private fun calculateScalacOptions(
    projectDetails: ProjectDetails,
    targetId: BuildTargetIdentifier,
  ): ScalacOptionsItem? =
    projectDetails.scalacOptions.firstOrNull { it.target == targetId }

  private fun calculatePythonOptions(
    projectDetails: ProjectDetails,
    targetId: BuildTargetIdentifier,
  ): PythonOptionsItem? =
    projectDetails.pythonOptions.firstOrNull { it.target == targetId }
}
