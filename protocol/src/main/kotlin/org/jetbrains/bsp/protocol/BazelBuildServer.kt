package org.jetbrains.bsp.protocol

import ch.epfl.scala.bsp4j.RunResult
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest
import java.util.concurrent.CompletableFuture

public interface BazelBuildServer {
  @JsonRequest("workspace/libraries")
  public fun workspaceLibraries(): CompletableFuture<WorkspaceLibrariesResult>

  @JsonRequest("workspace/directories")
  public fun workspaceDirectories(): CompletableFuture<WorkspaceDirectoriesResult>

  @JsonRequest("workspace/invalidTargets")
  public fun workspaceInvalidTargets(): CompletableFuture<WorkspaceInvalidTargetsResult>

  @JsonRequest("buildTarget/runWithDebug")
  public fun buildTargetRunWithDebug(params: RunWithDebugParams): CompletableFuture<RunResult>

  @JsonRequest("buildTarget/mobileInstall")
  public fun buildTargetMobileInstall(params: MobileInstallParams): CompletableFuture<MobileInstallResult>

  @JsonRequest("buildTarget/jvmBinaryJars")
  public fun buildTargetJvmBinaryJars(params: JvmBinaryJarsParams): CompletableFuture<JvmBinaryJarsResult>
}
