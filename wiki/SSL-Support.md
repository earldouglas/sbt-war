To use SSL, add the following to your SBT file:

    ssl in container.Configuration := Some(ssl_port, "path_to_keystore", "keystore_password", "key_password")