package com.xxpermissions.lib;

final class ManifestRegisterException extends RuntimeException {

    ManifestRegisterException() {
        /* No permissions are registered in the manifest file */
        super("No permissions are registered in the manifest file");
    }

    ManifestRegisterException(String permission) {
        /* The requested dangerous permissions are not registered in the manifest file */
        super(permission + ": Permissions are not registered in the manifest file");
    }
}