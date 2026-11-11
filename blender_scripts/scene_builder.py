"""Headless Blender (bpy) Scene Builder & Renderer for OfflineAnimStudio.

Can be run via:
  blender -b -P blender_scripts/scene_builder.py -- --scene scene_graph.json --output render_dir/ --engine BLENDER_EEVEE_NEXT
"""

import argparse
import json
import math
import os
import sys

# Try importing bpy (only available inside Blender headless)
try:
    import bpy
    import mathutils
    BPY_AVAILABLE = True
except ImportError:
    BPY_AVAILABLE = False


def reset_blend_scene():
    """Wipes existing meshes, lights, and cameras from Blender scene."""
    if not BPY_AVAILABLE:
        return
    bpy.ops.wm.read_factory_settings(use_empty=True)


def setup_world_environment(time_of_day: str, mood: str):
    """Configures world sky and ambient lighting."""
    if not BPY_AVAILABLE:
        return

    world = bpy.data.worlds.new("StudioWorld")
    bpy.context.scene.world = world
    world.use_nodes = True
    nodes = world.node_tree.nodes
    nodes.clear()

    node_background = nodes.new(type="ShaderNodeBackground")
    node_output = nodes.new(type="ShaderNodeOutputWorld")

    # Time-of-day sky color presets
    sky_colors = {
        "dawn": (0.8, 0.5, 0.4, 1.0),
        "noon": (0.5, 0.7, 1.0, 1.0),
        "golden_hour": (1.0, 0.65, 0.25, 1.0),
        "dusk": (0.4, 0.2, 0.5, 1.0),
        "night": (0.05, 0.05, 0.15, 1.0)
    }
    color = sky_colors.get(time_of_day.lower(), (0.6, 0.6, 0.8, 1.0))
    node_background.inputs["Color"].default_value = color
    node_background.inputs["Strength"].default_value = 0.8

    world.node_tree.links.new(node_background.outputs["Background"], node_output.inputs["Surface"])


def setup_camera(camera_data: dict, fps: int = 24, total_frames: int = 120):
    """Sets up camera with animated keyframes for dolly, pan, or orbit."""
    if not BPY_AVAILABLE:
        return

    cam_data = bpy.data.cameras.new("MainCamera")
    cam_data.lens = float(camera_data.get("focal_length_mm", 35.0))
    cam_data.clip_start = 0.1
    cam_data.clip_end = 500.0

    cam_obj = bpy.data.objects.new("MainCamera", cam_data)
    bpy.context.collection.objects.link(cam_obj)
    bpy.context.scene.camera = cam_obj

    start_t = camera_data.get("start_transform", {})
    end_t = camera_data.get("end_transform", {})

    start_pos = start_t.get("position", [0.0, -4.0, 1.8])
    end_pos = end_t.get("position", [0.0, -2.5, 1.2])

    start_rot = [math.radians(x) for x in start_t.get("rotation", [10.0, 0.0, 0.0])]
    end_rot = [math.radians(x) for x in end_t.get("rotation", [6.0, 0.0, 0.0])]

    # Frame 1 keyframe
    bpy.context.scene.frame_set(1)
    cam_obj.location = start_pos
    cam_obj.rotation_euler = start_rot
    cam_obj.keyframe_insert(data_path="location", frame=1)
    cam_obj.keyframe_insert(data_path="rotation_euler", frame=1)

    # Frame End keyframe
    bpy.context.scene.frame_set(total_frames)
    cam_obj.location = end_pos
    cam_obj.rotation_euler = end_rot
    cam_obj.keyframe_insert(data_path="location", frame=total_frames)
    cam_obj.keyframe_insert(data_path="rotation_euler", frame=total_frames)


def build_procedural_desert_terrain():
    """Builds low-poly stylized desert sand dunes."""
    if not BPY_AVAILABLE:
        return
    bpy.ops.mesh.primitive_grid_add(x_subdivisions=12, y_subdivisions=12, size=24.0, location=(0, 0, 0))
    grid = bpy.context.active_object
    grid.name = "DesertGround"

    # Add warm sand material
    mat = bpy.data.materials.new(name="SandMaterial")
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get("Principled BSDF")
    if bsdf:
        bsdf.inputs["Base Color"].default_value = (0.86, 0.65, 0.35, 1.0)
        bsdf.inputs["Roughness"].default_value = 0.9
    grid.data.materials.append(mat)


def build_procedural_robot(char_id: str, start_pos: list, end_pos: list, total_frames: int):
    """Builds a stylized low-poly scout robot with articulated animated head & chassis."""
    if not BPY_AVAILABLE:
        return

    # Robot body (beveled cube)
    bpy.ops.mesh.primitive_cube_add(size=0.6, location=(start_pos[0], start_pos[1], start_pos[2] + 0.5))
    chassis = bpy.context.active_object
    chassis.name = f"{char_id}_chassis"

    mat = bpy.data.materials.new(name="RobotMetal")
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get("Principled BSDF")
    if bsdf:
        bsdf.inputs["Base Color"].default_value = (0.2, 0.6, 0.8, 1.0)  # Cyan low-poly
        bsdf.inputs["Metallic"].default_value = 0.8
        bsdf.inputs["Roughness"].default_value = 0.3
    chassis.data.materials.append(mat)

    # Animated movement along path
    bpy.context.scene.frame_set(1)
    chassis.location = (start_pos[0], start_pos[1], start_pos[2] + 0.5)
    chassis.keyframe_insert(data_path="location", frame=1)

    bpy.context.scene.frame_set(total_frames)
    chassis.location = (end_pos[0], end_pos[1], end_pos[2] + 0.5)
    chassis.keyframe_insert(data_path="location", frame=total_frames)


def build_procedural_flower(pos: list):
    """Builds a glowing alien desert flower."""
    if not BPY_AVAILABLE:
        return
    # Stem
    bpy.ops.mesh.primitive_cylinder_add(radius=0.03, depth=0.4, location=(pos[0], pos[1], pos[2] + 0.2))
    stem = bpy.context.active_object
    stem.name = "FlowerStem"

    # Glowing Petals
    bpy.ops.mesh.primitive_ico_sphere_add(radius=0.12, subdivisions=1, location=(pos[0], pos[1], pos[2] + 0.42))
    petals = bpy.context.active_object
    petals.name = "FlowerPetals"

    mat = bpy.data.materials.new(name="FlowerGlow")
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes.get("Principled BSDF")
    if bsdf:
        bsdf.inputs["Base Color"].default_value = (1.0, 0.2, 0.6, 1.0)
        bsdf.inputs["Emission Color"].default_value = (1.0, 0.3, 0.7, 1.0)
        bsdf.inputs["Emission Strength"].default_value = 2.5
    petals.data.materials.append(mat)


def render_scene_to_pngs(output_dir: str, engine: str = "BLENDER_EEVEE_NEXT", resolution=(1920, 1080)):
    """Sets render settings and executes render animation."""
    if not BPY_AVAILABLE:
        print("[DryRun] Blender not installed; simulated 3D render complete.")
        return

    scene = bpy.context.scene
    scene.render.engine = engine
    scene.render.resolution_x = resolution[0]
    scene.render.resolution_y = resolution[1]
    scene.render.resolution_percentage = 100
    scene.render.image_settings.file_format = "PNG"
    scene.render.filepath = os.path.join(output_dir, "frame_#####")

    bpy.ops.render.render(animation=True)


def parse_and_build(scene_json_path: str, output_dir: str, engine: str = "BLENDER_EEVEE_NEXT"):
    """Entry point when invoked headlessly via command line."""
    with open(scene_json_path, "r", encoding="utf-8") as f:
        scene_data = json.load(f)

    fps = 24
    duration = float(scene_data.get("duration_sec", 5.0))
    total_frames = int(duration * fps)

    reset_blend_scene()
    setup_world_environment(
        scene_data.get("time_of_day", "golden_hour"),
        scene_data.get("lighting_mood", "warm")
    )
    setup_camera(scene_data.get("camera", {}), fps=fps, total_frames=total_frames)
    build_procedural_desert_terrain()

    # Characters
    for char in scene_data.get("characters", []):
        start_p = char.get("start_position", [0.0, 0.0, 0.0])
        end_p = char.get("end_position", [0.0, 1.0, 0.0])
        build_procedural_robot(char.get("character_id", "char"), start_p, end_p, total_frames)

    # Props
    for prop in scene_data.get("props", []):
        pos = prop.get("position", [0.3, 1.2, 0.0])
        build_procedural_flower(pos)

    os.makedirs(output_dir, exist_ok=True)
    render_scene_to_pngs(output_dir, engine=engine)


if __name__ == "__main__":
    # Filter args passed after '--'
    argv = sys.argv
    if "--" in argv:
        argv = argv[argv.index("--") + 1:]
    else:
        argv = []

    parser = argparse.ArgumentParser(description="Headless Blender Scene Builder")
    parser.add_argument("--scene", type=str, default="scene.json", help="Path to Scene Graph JSON")
    parser.add_argument("--output", type=str, default="renders/", help="Output directory for PNG sequence")
    parser.add_argument("--engine", type=str, default="BLENDER_EEVEE_NEXT", help="Render engine: BLENDER_EEVEE_NEXT or CYCLES")
    args = parser.parse_args(argv)

    if os.path.exists(args.scene):
        parse_and_build(args.scene, args.output, args.engine)
    else:
        print(f"Scene file not found: {args.scene}")
