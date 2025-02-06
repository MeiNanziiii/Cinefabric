package ua.mei.cinefabric.cast;

import org.jetbrains.annotations.Nullable;
import ua.mei.cinefabric.scene.Cutscene;

public interface CinefabricPlayer {
    @Nullable Cutscene getScene();

    void playScene(Cutscene scene, Runnable callback);

    void stopScene();
}
