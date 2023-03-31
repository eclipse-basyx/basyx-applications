interface ImportMetaEnv {
    VITE_REGISTRY_PATH: string;
    VITE_AAS_SERVER_PATH: string;
    VITE_PRIMARY_COLOR: string;
}

interface ImportMeta {
    env: ImportMetaEnv;
}