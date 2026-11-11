package com.example.pipeline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vec3) = Vec3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vec3) = Vec3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vec3(x * scalar, y * scalar, z * scalar)
}

data class Polygon3D(
    val vertices: List<Vec3>,
    val baseColor: Color,
    val isEmission: Boolean = false
) {
    fun getAverageDepth(yawRad: Float, pitchRad: Float): Float {
        var sumZ = 0f
        for (v in vertices) {
            val rotated = rotatePoint(v, yawRad, pitchRad)
            sumZ += rotated.y // Y is forward depth in standard isometric camera
        }
        return sumZ / vertices.size
    }
}

fun rotatePoint(p: Vec3, yawRad: Float, pitchRad: Float): Vec3 {
    // Rotate around Z axis (yaw)
    val cosY = cos(yawRad)
    val sinY = sin(yawRad)
    val x1 = p.x * cosY - p.y * sinY
    val y1 = p.x * sinY + p.y * cosY
    val z1 = p.z

    // Rotate around X axis (pitch)
    val cosP = cos(pitchRad)
    val sinP = sin(pitchRad)
    val y2 = y1 * cosP - z1 * sinP
    val z2 = y1 * sinP + z1 * cosP
    val x2 = x1

    return Vec3(x2, y2, z2)
}

fun projectToScreen(pRotated: Vec3, centerX: Float, centerY: Float, scale: Float): Offset {
    // Isometric / Weak perspective projection
    val screenX = centerX + pRotated.x * scale
    val screenY = centerY - pRotated.z * scale + (pRotated.y * scale * 0.35f)
    return Offset(screenX, screenY)
}

class LowPolyMeshBuilder {

    companion object {
        fun createTerrain(): List<Polygon3D> {
            val polys = mutableListOf<Polygon3D>()
            val gridSize = 4
            val step = 1.0f
            val baseColor = Color(0xFFD49B4F)

            for (i in -gridSize until gridSize) {
                for (j in -gridSize until gridSize) {
                    val x0 = i * step
                    val y0 = j * step
                    val x1 = (i + 1) * step
                    val y1 = (j + 1) * step

                    // Gentle dune elevation
                    val z00 = 0.15f * sin(x0 * 0.8f) + 0.1f * cos(y0 * 0.6f)
                    val z10 = 0.15f * sin(x1 * 0.8f) + 0.1f * cos(y0 * 0.6f)
                    val z11 = 0.15f * sin(x1 * 0.8f) + 0.1f * cos(y1 * 0.6f)
                    val z01 = 0.15f * sin(x0 * 0.8f) + 0.1f * cos(y1 * 0.6f)

                    val shadeOffset = ((i + j) % 2) * 0.05f
                    val polyColor = Color(
                        (baseColor.red - shadeOffset).coerceIn(0f, 1f),
                        (baseColor.green - shadeOffset).coerceIn(0f, 1f),
                        (baseColor.blue - shadeOffset).coerceIn(0f, 1f)
                    )

                    polys.add(
                        Polygon3D(
                            listOf(Vec3(x0, y0, z00), Vec3(x1, y0, z10), Vec3(x1, y1, z11), Vec3(x0, y1, z01)),
                            polyColor
                        )
                    )
                }
            }
            return polys
        }

        fun createRobot(origin: Vec3, progress: Float, action: String): List<Polygon3D> {
            val polys = mutableListOf<Polygon3D>()
            // Walking bounce
            val bounce = if (action == "walk") kotlin.math.abs(sin(progress * 12f)) * 0.1f else 0f
            val center = origin + Vec3(0f, 0f, 0.45f + bounce)
            val s = 0.3f // half size

            val robotBlue = Color(0xFF3B82F6)
            val robotMetal = Color(0xFF64748B)
            val visorCyan = Color(0xFF22D3EE)

            // Torso box
            polys.addAll(createBox(center, Vec3(s, s * 0.8f, s), robotBlue))

            // Optical visor (face)
            val visorCenter = center + Vec3(0f, s * 0.82f, 0.05f)
            polys.add(
                Polygon3D(
                    listOf(
                        visorCenter + Vec3(-s * 0.6f, 0f, -s * 0.3f),
                        visorCenter + Vec3(s * 0.6f, 0f, -s * 0.3f),
                        visorCenter + Vec3(s * 0.6f, 0f, s * 0.3f),
                        visorCenter + Vec3(-s * 0.6f, 0f, s * 0.3f)
                    ),
                    visorCyan,
                    isEmission = true
                )
            )

            // Antenna
            val antennaBase = center + Vec3(0f, 0f, s)
            polys.add(
                Polygon3D(
                    listOf(
                        antennaBase + Vec3(-0.02f, 0f, 0f),
                        antennaBase + Vec3(0.02f, 0f, 0f),
                        antennaBase + Vec3(0.02f, 0f, 0.25f),
                        antennaBase + Vec3(-0.02f, 0f, 0.25f)
                    ),
                    robotMetal
                )
            )

            // Reaching Arm
            val armExtension = if (action == "reach_out") (progress * 0.3f).coerceIn(0f, 0.4f) else 0.1f
            val armCenter = center + Vec3(s + 0.05f, armExtension, 0f)
            polys.addAll(createBox(armCenter, Vec3(0.08f, 0.2f + armExtension, 0.08f), robotMetal))

            return polys
        }

        fun createAlienFlower(origin: Vec3, pulse: Float): List<Polygon3D> {
            val polys = mutableListOf<Polygon3D>()
            val stemGreen = Color(0xFF2E7D32)
            val petalPink = Color(0xFFFF4081)

            // Stem
            val stemBase = origin
            val stemTop = origin + Vec3(0f, 0f, 0.5f)
            polys.add(
                Polygon3D(
                    listOf(
                        stemBase + Vec3(-0.04f, 0f, 0f),
                        stemBase + Vec3(0.04f, 0f, 0f),
                        stemTop + Vec3(0.04f, 0f, 0f),
                        stemTop + Vec3(-0.04f, 0f, 0f)
                    ),
                    stemGreen
                )
            )

            // Glowing petals
            val numPetals = 5
            val petalScale = 0.2f + pulse * 0.05f
            for (i in 0 until numPetals) {
                val angle = (i * 2 * Math.PI / numPetals).toFloat()
                val px = cos(angle) * petalScale
                val py = sin(angle) * petalScale
                polys.add(
                    Polygon3D(
                        listOf(
                            stemTop,
                            stemTop + Vec3(px * 0.4f, py * 0.4f, 0.15f),
                            stemTop + Vec3(px, py, 0.05f),
                            stemTop + Vec3(px * 0.5f, py * 0.5f, -0.05f)
                        ),
                        petalPink,
                        isEmission = true
                    )
                )
            }
            return polys
        }

        private fun createBox(center: Vec3, halfSize: Vec3, color: Color): List<Polygon3D> {
            val hx = halfSize.x
            val hy = halfSize.y
            val hz = halfSize.z

            val v0 = center + Vec3(-hx, -hy, -hz)
            val v1 = center + Vec3(hx, -hy, -hz)
            val v2 = center + Vec3(hx, hy, -hz)
            val v3 = center + Vec3(-hx, hy, -hz)
            val v4 = center + Vec3(-hx, -hy, hz)
            val v5 = center + Vec3(hx, -hy, hz)
            val v6 = center + Vec3(hx, hy, hz)
            val v7 = center + Vec3(-hx, hy, hz)

            val darkColor = Color(color.red * 0.7f, color.green * 0.7f, color.blue * 0.7f)
            val lightColor = Color(color.red * 1.15f.coerceAtMost(1f), color.green * 1.15f.coerceAtMost(1f), color.blue * 1.15f.coerceAtMost(1f))

            return listOf(
                Polygon3D(listOf(v0, v1, v2, v3), darkColor),      // Bottom
                Polygon3D(listOf(v4, v5, v6, v7), lightColor),     // Top
                Polygon3D(listOf(v0, v1, v5, v4), color),          // Front
                Polygon3D(listOf(v2, v3, v7, v6), darkColor),      // Back
                Polygon3D(listOf(v1, v2, v6, v5), lightColor),     // Right
                Polygon3D(listOf(v3, v0, v4, v7), color)           // Left
            )
        }
    }
}
