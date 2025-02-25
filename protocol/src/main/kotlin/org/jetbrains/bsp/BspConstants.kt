package org.jetbrains.bsp

import ch.epfl.scala.bsp4j.BuildClientCapabilities

public const val BSP_CONNECTION_DIR: String = ".bsp"

public const val BSP_CLIENT_NAME: String = "IntelliJ-BSP"

public const val BSP_VERSION: String = "2.1.0"

public val CLIENT_CAPABILITIES: BuildClientCapabilities =
  BuildClientCapabilities(listOf("java", "kotlin", "scala", "python"))
