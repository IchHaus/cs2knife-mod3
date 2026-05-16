package com.csknife.client.animation;

/**
 * Controls Karambit animation state for CS2-style inspect and trick animations.
 * Animations are driven by tick progress and applied in the HeldItemRendererMixin.
 *
 * Animation States:
 *  IDLE      - normal held position (slight downward angle like CS2 default)
 *  INSPECT   - CS2 inspect: raise knife, rotate, lower
 *  TRICK     - Finger spin trick: full 360° roll with flip
 *  RETURNING - smooth return to idle after animation
 */
public class KarambithAnimationController {

    public enum AnimationState {
        IDLE, INSPECT, TRICK, RETURNING
    }

    private AnimationState state = AnimationState.IDLE;
    private int tick = 0;
    private int totalTicks = 0;

    // Smooth interpolated transform values (applied in mixin)
    public float offsetX = 0f;
    public float offsetY = 0f;
    public float offsetZ = 0f;
    public float rotationX = 0f;
    public float rotationY = 0f;
    public float rotationZ = 0f;
    public float scale = 1f;

    // Saved idle pose for smooth return
    private float returnFromX, returnFromY, returnFromRotX, returnFromRotY, returnFromRotZ;

    // Animation durations in ticks (20 ticks = 1 second)
    private static final int INSPECT_DURATION = 50;  // 2.5 seconds like CS2
    private static final int TRICK_DURATION = 35;    // 1.75 seconds
    private static final int RETURN_DURATION = 10;

    // ────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────

    public void startInspect() {
        if (state == AnimationState.IDLE || state == AnimationState.RETURNING) {
            state = AnimationState.INSPECT;
            tick = 0;
            totalTicks = INSPECT_DURATION;
        }
    }

    public void startTrick() {
        if (state == AnimationState.IDLE || state == AnimationState.RETURNING) {
            state = AnimationState.TRICK;
            tick = 0;
            totalTicks = TRICK_DURATION;
        }
    }

    public void reset() {
        state = AnimationState.IDLE;
        tick = 0;
        offsetX = offsetY = offsetZ = 0;
        rotationX = rotationY = rotationZ = 0;
        scale = 1f;
    }

    public AnimationState getState() {
        return state;
    }

    public boolean isAnimating() {
        return state != AnimationState.IDLE;
    }

    // ────────────────────────────────────────────────────────────
    // Tick update
    // ────────────────────────────────────────────────────────────

    public void tick() {
        switch (state) {
            case INSPECT  -> tickInspect();
            case TRICK    -> tickTrick();
            case RETURNING -> tickReturn();
            case IDLE     -> applyIdlePose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // IDLE pose — slight Karambit natural grip angle
    // ────────────────────────────────────────────────────────────

    private void applyIdlePose() {
        offsetX = 0.4f;
        offsetY = -0.4f;
        offsetZ = -0.3f;
        rotationX = -15f;
        rotationY = -30f;
        rotationZ = 10f;
        scale = 1f;
    }

    // ────────────────────────────────────────────────────────────
    // INSPECT animation — mimics CS2 inspect motion
    //
    //  Phase 1 (0–20%):  raise knife toward screen center
    //  Phase 2 (20–60%): rotate knife showing blade/handle (like CS2)
    //  Phase 3 (60–80%): tilt and spin the ring hole
    //  Phase 4 (80–100%): return to raised → begin lowering
    // ────────────────────────────────────────────────────────────

    private void tickInspect() {
        tick++;
        float t = (float) tick / totalTicks;  // 0.0 → 1.0

        if (t < 0.20f) {
            // Phase 1: Raise up and bring toward center
            float p = t / 0.20f;
            float ease = easeInOut(p);
            offsetX = lerp(0.4f, 0.0f, ease);
            offsetY = lerp(-0.4f, 0.1f, ease);
            offsetZ = lerp(-0.3f, -0.6f, ease);
            rotationX = lerp(-15f, -30f, ease);
            rotationY = lerp(-30f, 0f, ease);
            rotationZ = lerp(10f, 0f, ease);
            scale = lerp(1f, 1.15f, ease);

        } else if (t < 0.50f) {
            // Phase 2: Spin around Y axis — showing both sides of the blade
            float p = (t - 0.20f) / 0.30f;
            float ease = easeInOut(p);
            offsetX = 0.0f;
            offsetY = 0.1f;
            offsetZ = -0.6f;
            rotationX = lerp(-30f, -10f, ease);
            rotationY = lerp(0f, 360f, ease);   // Full rotation like CS2!
            rotationZ = 0f;
            scale = 1.15f;

        } else if (t < 0.75f) {
            // Phase 3: Tilt and show the ring/hook (Karambit signature)
            float p = (t - 0.50f) / 0.25f;
            float ease = easeInOut(p);
            offsetX = lerp(0.0f, 0.2f, ease);
            offsetY = lerp(0.1f, 0.05f, ease);
            offsetZ = lerp(-0.6f, -0.5f, ease);
            rotationX = lerp(-10f, -45f, ease);
            rotationY = lerp(0f, -20f, ease);
            rotationZ = lerp(0f, 30f, ease);
            scale = 1.1f;

        } else {
            // Phase 4: Return to idle position
            float p = (t - 0.75f) / 0.25f;
            float ease = easeInOut(p);
            offsetX = lerp(0.2f, 0.4f, ease);
            offsetY = lerp(0.05f, -0.4f, ease);
            offsetZ = lerp(-0.5f, -0.3f, ease);
            rotationX = lerp(-45f, -15f, ease);
            rotationY = lerp(-20f, -30f, ease);
            rotationZ = lerp(30f, 10f, ease);
            scale = lerp(1.1f, 1f, ease);
        }

        if (tick >= totalTicks) {
            state = AnimationState.IDLE;
            applyIdlePose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // TRICK animation — finger-spin trick
    //
    //  Phase 1 (0–30%):  toss knife upward
    //  Phase 2 (30–70%): full 360° roll in Z while flying up
    //  Phase 3 (70–100%): catch and return
    // ────────────────────────────────────────────────────────────

    private void tickTrick() {
        tick++;
        float t = (float) tick / totalTicks;

        if (t < 0.30f) {
            // Toss up
            float p = t / 0.30f;
            float ease = easeOut(p);
            offsetX = lerp(0.4f, 0.0f, ease);
            offsetY = lerp(-0.4f, 0.3f, ease);
            offsetZ = lerp(-0.3f, -0.5f, ease);
            rotationX = lerp(-15f, -60f, ease);
            rotationY = lerp(-30f, 0f, ease);
            rotationZ = lerp(10f, 0f, ease);
            scale = lerp(1f, 1.2f, ease);

        } else if (t < 0.70f) {
            // Spin! Full 360° + 180° flip
            float p = (t - 0.30f) / 0.40f;
            offsetX = 0.0f;
            offsetY = 0.3f;
            offsetZ = -0.5f;
            rotationX = lerp(-60f, 120f, p);      // flip
            rotationY = lerp(0f, 360f, p);         // full spin
            rotationZ = lerp(0f, 360f, easeInOut(p)); // roll
            scale = lerp(1.2f, 1.0f, p);

        } else {
            // Catch coming back down
            float p = (t - 0.70f) / 0.30f;
            float ease = easeIn(p);
            offsetX = lerp(0.0f, 0.4f, ease);
            offsetY = lerp(0.3f, -0.4f, ease);
            offsetZ = lerp(-0.5f, -0.3f, ease);
            rotationX = lerp(120f, -15f, ease);
            rotationY = lerp(0f, -30f, ease);
            rotationZ = lerp(360f, 10f, ease);
            scale = 1f;
        }

        if (tick >= totalTicks) {
            state = AnimationState.IDLE;
            applyIdlePose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // RETURNING — smooth return to idle
    // ────────────────────────────────────────────────────────────

    private void tickReturn() {
        tick++;
        float t = easeInOut((float) tick / RETURN_DURATION);
        offsetX = lerp(returnFromX, 0.4f, t);
        offsetY = lerp(returnFromY, -0.4f, t);
        rotationX = lerp(returnFromRotX, -15f, t);
        rotationY = lerp(returnFromRotY, -30f, t);
        rotationZ = lerp(returnFromRotZ, 10f, t);
        if (tick >= RETURN_DURATION) {
            state = AnimationState.IDLE;
            applyIdlePose();
        }
    }

    // ────────────────────────────────────────────────────────────
    // Math helpers
    // ────────────────────────────────────────────────────────────

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float easeInOut(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (-2 * t + 2) * (-2 * t + 2) / 2;
    }

    private static float easeOut(float t) {
        return 1 - (1 - t) * (1 - t);
    }

    private static float easeIn(float t) {
        return t * t;
    }
}
