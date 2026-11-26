package com.ratons.features.misc.rat;

import java.util.UUID;

/** Implemented on the avatar render state by a mixin so the rat layer can tell whose head it is. */
public interface RatRiderHolder {
    UUID ratons$getRider();

    void ratons$setRider(UUID uuid);
}
