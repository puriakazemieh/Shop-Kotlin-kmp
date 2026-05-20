if (config.resolve) {
    config.resolve.fallback = {
        ...config.resolve.fallback,
        "os": false,
        "path": false
    };
}
