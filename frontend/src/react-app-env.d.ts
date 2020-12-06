/// <reference types="react-scripts" />
declare namespace NodeJS {
    interface ProcessEnv {
        NODE_ENV: 'development' | 'production' | 'test'
        REACT_APP_DEV_MODE: boolean
        REACT_APP_API_ENDPOINT: string
    }
}

process.env.REACT_APP_DEV_MODE = true
