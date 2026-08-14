package me.mrhikmen.colorlight.core.light.gpu;

import me.mrhikmen.colorlight.ColorLightClient;
import me.mrhikmen.colorlight.core.light.ColorLightUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.lwjgl.opengl.GL43.*;

public final class GlComputeLightBackend implements ILightComputeBackend {

    private static final Identifier SHADER_LOCATION = Identifier.fromNamespaceAndPath("colorlight", "shaders/light_propagate.comp");
    private static final Identifier SMOOTH_SHADER_LOCATION = Identifier.fromNamespaceAndPath("colorlight", "shaders/light_smooth.comp");

    private static final int LOCAL_SIZE = 4;

    private boolean triedInit = false;
    private boolean supported = false;
    private int program = -1;
    private int smoothProgram = -1;

    private int uSizeLoc, uDecayLoc;
    private int uSmoothSizeLoc;

    private int bufOpacity = -1, bufBase = -1, bufA = -1, bufB = -1, bufSmooth = -1;
    private int bufferCapacityVoxels = 0;

    @Override
    public boolean isSupported() {
        ensureInitialized();
        return supported;
    }

    private void ensureInitialized() {
        if (triedInit)
            return;
        triedInit = true;

        try {
            GLCapabilities caps = GL.getCapabilities();

            if (!caps.OpenGL43) {
                ColorLightClient.LOGGER.warn("[ColorLight] GPU context is not OpenGL 4.3 (current context: {}) " + "GPU lighting calculation is disabled, CPU is being used.", glGetString(GL_VERSION));
                return;
            }

            int prog = compileProgram(SHADER_LOCATION, "light_propagate.comp");
            if (prog == -1)
                return;

            int smoothProg = compileProgram(SMOOTH_SHADER_LOCATION, "light_smooth.comp");
            if (smoothProg == -1) {
                glDeleteProgram(prog);
                return;
            }

            this.program = prog;
            this.smoothProgram = smoothProg;
            this.uSizeLoc = glGetUniformLocation(program, "uSize");
            this.uDecayLoc = glGetUniformLocation(program, "uDecayPerOpacityUnit");
            this.uSmoothSizeLoc = glGetUniformLocation(smoothProgram, "uSize");

            if (!runSelfTest()) {
                ColorLightClient.LOGGER.warn("[ColorLight] GPU failed the verification test calculation " + "(compute shader compiled, but the result is incorrect or a GL error occurred) — " + "GPU lighting calculation is disabled, CPU is being used.");
                glDeleteProgram(program);
                glDeleteProgram(smoothProgram);
                program = -1;
                smoothProgram = -1;
                return;
            }

            this.supported = true;

            ColorLightClient.LOGGER.info("[ColorLight] GPU lighting calculation enabled " + "(OpenGL compute shader, SSBO backend, test passed).");

        } catch (Exception e) {
            ColorLightClient.LOGGER.error("[ColorLight] Failed to initialize GPU lighting calculation, " + "CPU fallback is used.", e);
        }
    }

    private int compileProgram(Identifier location, String debugName) throws IOException {
        String source = loadShaderSource(location);

        int shader = glCreateShader(GL_COMPUTE_SHADER);
        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            ColorLightClient.LOGGER.error("[ColorLight] Compilation error {}:\n{}", debugName, glGetShaderInfoLog(shader));
            glDeleteShader(shader);
            return -1;
        }

        int prog = glCreateProgram();
        glAttachShader(prog, shader);
        glLinkProgram(prog);
        glDeleteShader(shader);

        if (glGetProgrami(prog, GL_LINK_STATUS) == GL_FALSE) {
            ColorLightClient.LOGGER.error("[ColorLight] Linker error {}:\n{}", debugName, glGetProgramInfoLog(prog));
            glDeleteProgram(prog);
            return -1;
        }

        return prog;
    }

    private boolean runSelfTest() {
        glGetError();

        int sx = 2, sy = 2, sz = 2;
        byte[] opacity = new byte[8];
        int[] baseColor = new int[8];
        int expected = ColorLightUtil.pack(200, 100, 50);
        baseColor[0] = expected;

        try {
            LightComputeResult result = runPropagateAndSmooth(sx, sy, sz, opacity, baseColor, 1, 0f);

            if (glGetError() != GL_NO_ERROR)
                return false;

            if (result == null) return false;
            if (result.propagated() == null || result.propagated().length != 8 || result.propagated()[0] != expected)
                return false;

            return result.smoothed() != null && result.smoothed().length == 8 && result.smoothed()[0] == expected;

        } catch (Throwable t) {
            ColorLightClient.LOGGER.debug("[ColorLight] GPU self-test threw an exception", t);
            return false;
        }
    }

    private String loadShaderSource(Identifier location) throws IOException {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty())
            throw new IOException("Shader resource not found: " + location);

        try (InputStream in = resource.get().open()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Override
    public LightComputeResult propagateAndSmooth(int sx, int sy, int sz, byte[] opacity, int[] baseColor, int iterations, float decayPerOpacityUnit) {

        if (!isSupported())
            throw new IllegalStateException("GPU compute backend is unavailable");

        return runPropagateAndSmooth(sx, sy, sz, opacity, baseColor, iterations, decayPerOpacityUnit);
    }

    private LightComputeResult runPropagateAndSmooth(int sx, int sy, int sz, byte[] opacity, int[] baseColor, int iterations, float decayPerOpacityUnit) {

        int voxelCount = sx * sy * sz;
        if (opacity.length != voxelCount || baseColor.length != voxelCount)
            throw new IllegalArgumentException("The array size does not match sizeX*sizeY*sizeZ.");

        int[] prevBoundBuffer = new int[]{glGetInteger(GL_SHADER_STORAGE_BUFFER_BINDING)};

        try {
            ensureBufferCapacity(voxelCount);

            uploadOpacity(bufOpacity, opacity);
            uploadColor(bufBase, baseColor, voxelCount);
            uploadColor(bufA, baseColor, voxelCount);

            int groupsX = ceilDiv(sx, LOCAL_SIZE);
            int groupsY = ceilDiv(sy, LOCAL_SIZE);
            int groupsZ = ceilDiv(sz, LOCAL_SIZE);

            glUseProgram(program);
            glUniform3i(uSizeLoc, sx, sy, sz);
            glUniform1f(uDecayLoc, decayPerOpacityUnit / 255f);

            int prev = bufA;
            int next = bufB;

            int steps = Math.max(1, iterations);
            for (int i = 0; i < steps; i++) {
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, bufOpacity);
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, bufBase);
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, prev);
                glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 3, next);

                glDispatchCompute(groupsX, groupsY, groupsZ);
                glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

                int tmp = prev;
                prev = next;
                next = tmp;
            }

            glUseProgram(smoothProgram);
            glUniform3i(uSmoothSizeLoc, sx, sy, sz);

            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 0, bufOpacity);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 1, prev);
            glBindBufferBase(GL_SHADER_STORAGE_BUFFER, 2, bufSmooth);

            glDispatchCompute(groupsX, groupsY, groupsZ);
            glMemoryBarrier(GL_SHADER_STORAGE_BARRIER_BIT);

            int[] propagated = readBack(prev, voxelCount);
            int[] smoothed = readBack(bufSmooth, voxelCount);

            return new LightComputeResult(propagated, smoothed);

        } finally {
            glUseProgram(0);
            glBindBuffer(GL_SHADER_STORAGE_BUFFER, prevBoundBuffer[0]);
        }
    }

    private void ensureBufferCapacity(int voxelCount) {
        if (voxelCount <= bufferCapacityVoxels && bufOpacity != -1)
            return;

        deleteBuffersIfPresent();

        bufOpacity = glGenBuffers();
        bufBase = glGenBuffers();
        bufA = glGenBuffers();
        bufB = glGenBuffers();
        bufSmooth = glGenBuffers();

        long bytes = (long) voxelCount * Integer.BYTES;

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufOpacity);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufBase);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufA);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufB);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_DYNAMIC_COPY);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bufSmooth);
        glBufferData(GL_SHADER_STORAGE_BUFFER, bytes, GL_DYNAMIC_COPY);

        bufferCapacityVoxels = voxelCount;
    }

    private void deleteBuffersIfPresent() {
        if (bufOpacity != -1) glDeleteBuffers(bufOpacity);
        if (bufBase != -1) glDeleteBuffers(bufBase);
        if (bufA != -1) glDeleteBuffers(bufA);
        if (bufB != -1) glDeleteBuffers(bufB);
        if (bufSmooth != -1) glDeleteBuffers(bufSmooth);
        bufOpacity = bufBase = bufA = bufB = bufSmooth = -1;
        bufferCapacityVoxels = 0;
    }

    private static void uploadOpacity(int buffer, byte[] opacity) {
        IntBuffer data = IntBuffer.allocate(opacity.length);
        for (byte b : opacity) {
            data.put(b & 0xFF);
        }
        data.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
    }

    private static void uploadColor(int buffer, int[] packedColors, int voxelCount) {
        IntBuffer data = IntBuffer.allocate(voxelCount);
        for (int i = 0; i < voxelCount; i++) {
            data.put(packedColors[i]);
        }
        data.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);
        glBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, data);
    }

    private static int[] readBack(int buffer, int voxelCount) {
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, buffer);

        ByteBuffer raw = ByteBuffer.allocateDirect(voxelCount * Integer.BYTES);
        glGetBufferSubData(GL_SHADER_STORAGE_BUFFER, 0, raw);

        IntBuffer ints = raw.asIntBuffer();
        int[] result = new int[voxelCount];
        for (int i = 0; i < voxelCount; i++) {
            int packed = ints.get(i);
            int r = packed & 0xFF;
            int g = (packed >> 8) & 0xFF;
            int b = (packed >> 16) & 0xFF;
            result[i] = ColorLightUtil.pack(r, g, b);
        }
        return result;
    }

    private static int ceilDiv(int a, int b) {
        return (a + b - 1) / b;
    }

    @Override
    public void dispose() {
        if (program != -1) {
            glDeleteProgram(program);
            program = -1;
        }
        if (smoothProgram != -1) {
            glDeleteProgram(smoothProgram);
            smoothProgram = -1;
        }
        deleteBuffersIfPresent();
        supported = false;
        triedInit = false;
    }
}
