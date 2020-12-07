/// <reference types="react-scripts" />
declare namespace NodeJS {
    interface ProcessEnv {
        NODE_ENV: 'development' | 'production' | 'test'
        REACT_APP_DEV_MODE: string
        REACT_APP_API_ENDPOINT: string
    }
}
