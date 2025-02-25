package org.jetbrains.magicmetamodel.impl.workspacemodel.impl.updaters

import com.intellij.platform.workspace.jps.entities.LibraryEntity
import com.intellij.platform.workspace.jps.entities.LibraryId
import com.intellij.platform.workspace.jps.entities.LibraryTableId
import com.intellij.platform.workspace.jps.entities.ModuleCustomImlDataEntity
import com.intellij.platform.workspace.jps.entities.ModuleDependencyItem
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.ModuleId
import com.intellij.platform.workspace.jps.entities.modifyEntity
import com.intellij.platform.workspace.storage.MutableEntityStorage
import com.intellij.platform.workspace.storage.WorkspaceEntity
import org.jetbrains.magicmetamodel.impl.workspacemodel.GenericModuleInfo
import org.jetbrains.magicmetamodel.impl.workspacemodel.LibraryDependency
import org.jetbrains.magicmetamodel.impl.workspacemodel.ModuleDependency
import org.jetbrains.magicmetamodel.impl.workspacemodel.ModuleName
import org.jetbrains.workspacemodel.entities.BspProjectDirectoriesEntity
import org.jetbrains.workspacemodel.storage.BspEntitySource

internal class ModuleEntityUpdater(
  private val workspaceModelEntityUpdaterConfig: WorkspaceModelEntityUpdaterConfig,
  private val defaultDependencies: List<ModuleDependencyItem> = ArrayList(),
) : WorkspaceModelEntityWithoutParentModuleUpdater<GenericModuleInfo, ModuleEntity> {
  override fun addEntity(entityToAdd: GenericModuleInfo): ModuleEntity =
    addModuleEntity(workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder, entityToAdd)

  private fun addModuleEntity(
    builder: MutableEntityStorage,
    entityToAdd: GenericModuleInfo,
  ): ModuleEntity {
    val modulesDependencies = entityToAdd.modulesDependencies.map { toModuleDependencyItemModuleDependency(it) }
    val associatesDependencies = entityToAdd.associates.map { toModuleDependencyItemModuleDependency(it) }
    val librariesDependencies =
      entityToAdd.librariesDependencies.map { toModuleDependencyItemLibraryDependency(it, entityToAdd.name) }
    val moduleEntity = builder.addEntity(
      ModuleEntity(
        name = entityToAdd.name,
        dependencies = defaultDependencies + modulesDependencies + associatesDependencies + librariesDependencies,
        entitySource = BspEntitySource,
      ) {
        this.type = entityToAdd.type
      },
    )
    val imlData = builder.addEntity(
      ModuleCustomImlDataEntity(
        customModuleOptions = entityToAdd.capabilities.asMap() + entityToAdd.languageIdsAsSingleEntryMap,
        entitySource = BspEntitySource,
      ) {
        this.rootManagerTagCustomData = null
        this.module = moduleEntity
      },
    )
    builder.modifyEntity(moduleEntity) {
      this.customImlData = imlData
    }
    return moduleEntity
  }

  private fun toModuleDependencyItemModuleDependency(
    moduleDependency: ModuleDependency,
  ): ModuleDependencyItem.Exportable.ModuleDependency =
    ModuleDependencyItem.Exportable.ModuleDependency(
      module = ModuleId(moduleDependency.moduleName),
      exported = true,
      scope = ModuleDependencyItem.DependencyScope.COMPILE,
      productionOnTest = true,
    )
}

internal fun toModuleDependencyItemLibraryDependency(
  libraryDependency: LibraryDependency,
  moduleName: String,
): ModuleDependencyItem.Exportable.LibraryDependency {
  val libraryTableId = if (libraryDependency.isProjectLevelLibrary)
    LibraryTableId.ProjectLibraryTableId else LibraryTableId.ModuleLibraryTableId(ModuleId(moduleName))
  return ModuleDependencyItem.Exportable.LibraryDependency(
    library = LibraryId(
      name = libraryDependency.libraryName,
      tableId = libraryTableId,
    ),
    exported = true, // TODO https://youtrack.jetbrains.com/issue/BAZEL-632
    scope = ModuleDependencyItem.DependencyScope.COMPILE,
  )
}

internal class WorkspaceModuleRemover(
  private val workspaceModelEntityUpdaterConfig: WorkspaceModelEntityUpdaterConfig,
) : WorkspaceModuleEntityRemover<ModuleName> {
  override fun removeEntity(entityToRemove: ModuleName) {
    // TODO https://youtrack.jetbrains.com/issue/BAZEL-634
    val moduleToRemove =
      workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder.resolve(ModuleId(entityToRemove.name))!!

    workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder.removeEntity(moduleToRemove)
  }

  override fun clear() {
    removeEntities(ModuleEntity::class.java)
    removeEntities(LibraryEntity::class.java)
    removeEntities(BspProjectDirectoriesEntity::class.java)
  }

  private fun <E : WorkspaceEntity> removeEntities(entityClass: Class<E>) {
    val allEntities = workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder.entities(entityClass)
    allEntities.forEach { workspaceModelEntityUpdaterConfig.workspaceEntityStorageBuilder.removeEntity(it) }
  }
}
