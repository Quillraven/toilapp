export function errorPromise<T>(error: any, message: string): Promise<T> {
    let errorDetails = ""
    if (error.response) {
        errorDetails += `data=${JSON.stringify(error.response.data)}`
        errorDetails += "\n"
        errorDetails += `headers=${JSON.stringify(error.response.headers)}`
    } else if (error.request) {
        errorDetails = `request=${JSON.stringify(error.request)}`
    } else {
        errorDetails = `message=${error.message}`
    }

    return Promise.reject(`${message}:\n ${errorDetails}'`)
}
